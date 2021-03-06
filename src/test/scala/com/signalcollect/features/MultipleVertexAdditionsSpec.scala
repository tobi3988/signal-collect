/*
 *  @author Philip Stutz
 *
 *  Copyright 2012 University of Zurich
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

package com.signalcollect.features

import org.scalatest.FlatSpec
import org.scalatest.prop.Checkers
import com.signalcollect.util.IntSet
import com.signalcollect.util.Ints
import com.signalcollect._
import com.signalcollect.examples.PageRankVertex
import org.scalatest.Matchers

class DummyVertex(id: Int) extends PageRankVertex(id) {
  state = 1
}

class MultipleVertexAdditionsSpec extends FlatSpec with Matchers {

  "Adding the same vertex multiple times" should "be ignored" in {
    val g = GraphBuilder.build
    g.addVertex(new DummyVertex(133))
    g.addVertex(new DummyVertex(134))
    g.addVertex(new DummyVertex(133))
    val numberOfDummies = g.aggregate(SumOfStates[Double])
    g.shutdown
    numberOfDummies.get should equal(2.0)
  }

  it should "support merges via handler" in {
    val g = GraphBuilder.build
    g.setExistingVertexHandler((oldVertex, newVertex, ge) => {
      oldVertex.asInstanceOf[DummyVertex].state += 1.0
    })
    g.addVertex(new DummyVertex(133))
    g.addVertex(new DummyVertex(134))
    g.addVertex(new DummyVertex(133))
    val stateSum = g.aggregate(SumOfStates[Double])
    g.shutdown
    stateSum.get should equal(3.0)
  }

}