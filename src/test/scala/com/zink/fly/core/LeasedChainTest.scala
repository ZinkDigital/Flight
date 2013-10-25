/*
 *       >          
 *     <----       
 *   -------->                     
 *     <----      Flight 
 *       >        Copyright (c)2013 Zink Digital Ltd.    
 *      
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.  
 */

package com.zink.fly.core

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers

import scala.concurrent.duration._


class LeasedChainTestSuite extends FunSuite with BeforeAndAfter with ShouldMatchers {

   test("Single Promise") {
 
	   val lec = new LeasedChain[Int]
	   lec.size should equal (0)
	   lec.head should equal (lec.EmptyLink)
	   lec.tail should equal (lec.EmptyLink)
	   
	   val matcher = ( x : Int, y : Int) => x == y  
	   val lease = 100000 millis
	   val entry = 7
 
	   lec.writeImmediate(entry , lease)
	   lec.size should equal (1)
	   lec.head should not be lec.EmptyLink
	   lec.tail should not be lec.EmptyLink 
	   
	   // read matcher fail 
	   val r1 = lec.readImmediate(8 , matcher)
	   assert(r1 === None)
	   assert(lec.size === 1)
	   lec.head should not be lec.EmptyLink
	   lec.tail should not be lec.EmptyLink
	   
	   // read matcher some 
	   val r2 = lec.readImmediate(entry , matcher)
	   assert(r2 === Some(entry))
	   assert(lec.size === 1)
	   lec.head should not be lec.EmptyLink
	   lec.tail should not be lec.EmptyLink
	   
	   // take matcher none 
	   val r3 = lec.takeImmediate(6 , matcher)
	   assert(r3 === None)
	   assert(lec.size === 1)
	   lec.head should not be lec.EmptyLink
	   lec.tail should not be lec.EmptyLink
	   
	   // take matcher some
	   val r4 = lec.takeImmediate(entry, matcher)
	   assert(r4 === Some(entry))
	   assert(lec.size === 0)
	   assert(lec.head === lec.EmptyLink)
	   assert(lec.tail === lec.EmptyLink)
  }
      
  
  test("Expiry") {
 
	   val lec = new LeasedChain[Int]
	   val matcher = ( x : Int, y : Int) => x == y 
	   val shortLifeEntry = 7
	   val longLifeEntry = 4
	   val slRet = Some(shortLifeEntry)
	   val llRet = Some(longLifeEntry)
	   val shortLease = 10 millis
	   val longLease = 50 millis
	    
	   // load up
	   lec.writeImmediate(shortLifeEntry , shortLease)
	   lec.writeImmediate(longLifeEntry , longLease)
	     
	   // check both in 
	   assert ( lec.readImmediate(shortLifeEntry , matcher) === slRet )
	   assert ( lec.readImmediate(longLifeEntry , matcher) === llRet )
	   assert (lec.size === 2)
	   
	   // expire short
	   Thread.sleep(shortLease.toMillis)
   
	   // check short gone
	   assert (lec.readImmediate(shortLifeEntry , matcher) === None  )
	   assert (lec.readImmediate(longLifeEntry , matcher) === llRet )
	   assert (lec.size === 1)
	   
	   // check long gone
	   Thread.sleep(longLease.toMillis - shortLease.toMillis)
	   assert (lec.readImmediate(shortLifeEntry , matcher) === None  )
	   assert (lec.readImmediate(longLifeEntry , matcher) === None )
	   assert (lec.size === 0)  
   }
  
  
   test("Many Entries") {

       val lec = new LeasedChain[Int]
	   val matcher = ( x : Int, y : Int) => x == y	
	   val longLease = 10000 millis
	   val rangeSize = 1000

       (1 to rangeSize).map ( x => lec.writeImmediate(x,longLease))
       assert(lec.size === rangeSize)
       
       val readSize = 20
       val reads = (1 to readSize).flatMap( x=> lec.readImmediate(x,matcher))
       assert(lec.size === rangeSize)
       assert(reads.size === readSize)
       assert(reads.sum === (1 to readSize).sum)
       
       val takeSize = 100
       val takes = (1 to takeSize).flatMap( x=> lec.takeImmediate(x,matcher))
       assert(lec.size === rangeSize-takeSize)
       assert(takes.size === takeSize)
       assert(takes.sum === (1 to takeSize).sum) 
    }   
        
        
  
}