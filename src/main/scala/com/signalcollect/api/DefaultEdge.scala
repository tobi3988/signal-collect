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

package com.signalcollect.api

import com.signalcollect.interfaces._

abstract class DefaultEdge[SourceIdTypeParameter, TargetIdTypeParameter](
  sourceId: SourceIdTypeParameter,
  targetId: TargetIdTypeParameter,
  description: String = getClass.getSimpleName) extends Edge {
  
  type SourceId = SourceIdTypeParameter
  type TargetId = TargetIdTypeParameter
  type Signal = Any

  def id = (sourceId, targetId, description)

  /** The hash code of the target vertex. */
  val cachedTargetIdHashCode = id._2.hashCode

  /**
   * This method will be called by {@link FrameworkVertex#executeSignalOperation}
   * of this {@Edge} source vertex. It calculates the signal and sends it over the message bus.
   * {@link OnlySignalOnChangeEdge}.
   *
   * @param mb the message bus to use for sending the signal
   */
  def executeSignalOperation(sourceVertex: Vertex, mb: MessageBus[Any]) {
    mb.sendToWorkerForVertexIdHash(SignalMessage(id._1, id._2, signal(sourceVertex.asInstanceOf[SourceVertex])), cachedTargetIdHashCode)
  }

}