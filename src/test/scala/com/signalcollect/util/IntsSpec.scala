package com.signalcollect.util

import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary._
import org.scalatest.FlatSpec
import org.scalatest.ShouldMatchers
import org.scalatest.prop.Checkers
import java.io.DataOutputStream
import java.io.ByteArrayOutputStream
import org.scalacheck.Arbitrary

class IntsSpec extends FlatSpec with ShouldMatchers with Checkers {

  implicit lazy val arbInt = Arbitrary(Gen.chooseNum(Int.MinValue, Int.MaxValue))

  "bytesForVarint" should "correctly compute the number of bytes required for a varint" in {
    check(
      (i: Int) => {
        val baos = new ByteArrayOutputStream
        val dos = new DataOutputStream(baos)
        Ints.writeUnsignedVarInt(i, dos)
        dos.flush
        baos.flush
        val bytes = baos.toByteArray.length
        val computed = Ints.bytesForVarint(i)
        dos.close
        baos.close
        bytes == computed
      },
      minSuccessful(100))
  }

  "writeUnsignedVarInt" should "correctly write to arrays of the proper size" in {
    check(
      (i: Int) => {
        val a = new Array[Byte](Ints.bytesForVarint(i))
        Ints.writeUnsignedVarInt(i, a, 0)
        true
      },
      minSuccessful(100))
  }

  "IntSet" should "store sets of Ints" in {
    check(
      (ints: Array[Int]) => {
        val intSet = ints.toSet
        val compact = Ints.createCompactSet(ints)
        new IntSet(compact).toSet == intSet
      },
      minSuccessful(1000))
  }

  it should "support the 'contains' operation" in {
    check(
      (ints: Array[Int], item: Int) => {
        val intSet = ints.toSet
        val compact = Ints.createCompactSet(ints)
        new IntSet(compact).contains(item) == intSet.contains(item)
      },
      minSuccessful(1000))
  }

  it should "support the 'insert' operation" in {
    check(
      (ints: Set[Int], item: Int) => {
        try {
          val intSet = ints + item
          val compact = Ints.createCompactSet(ints.toArray)
          val afterInsert = new IntSet(compact).insert(item)
          intSet == new IntSet(afterInsert).toSet
        } catch {
          case t: Throwable =>
            t.printStackTrace
            throw t
        }
      },
      minSuccessful(10000))
  }

  "SearchableIntSet" should "store sets of Ints" in {
    check(
      (ints: Array[Int]) => {
        val intSet = ints.toSet
        val searchable = Ints.createSearchableSet(ints)
        new SearchableIntSet(searchable).toSet == intSet
      },
      minSuccessful(1000))
  }

  it should "support the 'contains' operation" in {
    check(
      (ints: Array[Int], item: Int) => {
        val intSet = ints.toSet
        val searchable = Ints.createSearchableSet(ints)
        new SearchableIntSet(searchable).contains(item) == intSet.contains(item)
      },
      minSuccessful(1000))
  }

  it should "support the 'insertIndex' operation" in {
    check(
      (ints: Array[Int], item: Int) => {
        val sortedInts = ints.distinct.toArray.sorted
        val referenceInsertIndex = {
          var index = -1
          val length = sortedInts.length
          for (i <- 0 until length) {
            if (index == -1) {
              if (sortedInts(i) >= item) {
                index = i
              } else if (i == length - 1) {
                index = length
              }
            }
          }
          math.max(index, 0)
        }
        val searchable = Ints.createSearchableSet(ints)
        val index = new SearchableIntSet(searchable).insertIndex(item)
        index == referenceInsertIndex
      },
      minSuccessful(1000))
  }

  it should "support the 'insert' operation" in {
    check(
      (ints: Array[Int], item: Int) => {
        val intList = (ints.toSet + item).toList.sorted
        val searchable = Ints.createSearchableSet(ints)
        val actual = new SearchableIntSet(new SearchableIntSet(searchable).insert(item)).toList
        actual == intList
      },
      minSuccessful(1000))
  }

}