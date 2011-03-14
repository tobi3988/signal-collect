/*
 *  @author Philip Stutz
 *  
 *  Copyright 2010 University of Zurich
 *      
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *         http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package ch.uzh.ifi.ddis.signalcollect.implementations.worker

import java.util.Arrays
import java.util.Collections
import ch.uzh.ifi.ddis.signalcollect.api.Queues._
import ch.uzh.ifi.ddis.signalcollect.interfaces._
import java.util.concurrent.TimeUnit
import java.util.concurrent.BlockingQueue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.LinkedHashSet


/*
 * Incredibly inefficient implementation of a worker that executes operations with high scores
 * first.
 */
class AsynchronousPriorityWorker(
  mb: MessageBus[Any, Any],
  messageInboxFactory: QueueFactory) extends AbstractWorker(mb, messageInboxFactory) {

  def handlePauseAndContinue {
    if (shouldStart) {
      shouldStart = false
      isPaused = false
    }
    if (shouldPause) {
      shouldPause = false
      isPaused = true
      messageBus.sendToCoordinator(StatusWorkerHasPaused)
    }
  }

  def handleIdling {
    handlePauseAndContinue
    if (isConverged || isPaused) {
      processInboxOrIdle(idleTimeoutNanoseconds)
    } else {
      processInbox
    }
  }

  def run {
    while (!shutDown) {
      handleIdling
      // While the computation is in progress, alternately check the inbox and collect/signal
      if (!isPaused) {
        if (!toSignal.isEmpty) {
          val i = toSignal.iterator
          while (i.hasNext) {
            val vertex = i.next
            signal(vertex)
          }
          toSignal.clear
        }
        while (!toCollect.isEmpty) {
          processInbox
          val toCollectSnapshot = toCollect.toArray
          Arrays.sort(toCollectSnapshot) // horribly, horribly inefficient. i only needed this to test something.
          val vertex = toCollectSnapshot(0).asInstanceOf[Vertex[_, _]]
          if (collect(vertex)) {
            signal(vertex)
          }
          toCollect.remove(vertex)
        }
      }
    }
  }
}