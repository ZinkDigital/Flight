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

import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class ArmandTestSuite extends FunSuite with BeforeAndAfter {
 
   test("EntryRep Matcher Match") {
 
	   val entry = EntryRep("com.zink.dennis", List(Some(1),Some(2)))
	     
	   import RepFactory._
	   
	   // full match
	   val template = EntryRep("com.zink.dennis", List(Some(1),Some(2)))
	   assert(matcher(template, entry) == true )
	   
	   // match with null
	   val tWithNull = EntryRep("com.zink.dennis", List(Some(1),None))
	   assert(matcher(tWithNull, entry) == true )
	   
	   // empty object 
	   val emptEntry = EntryRep("com.zink.dennis", List())
	   val emptTemplate = EntryRep("com.zink.dennis", List())
	   assert( matcher(emptTemplate, emptEntry) == true )
   }
 
  
   test("EntryRep Matcher NoMatch") {
 
	   val entry = EntryRep("com.zink.dennis", List(Some(1),Some(2)))
	   import RepFactory._
	   
	   // full no match
	   val template = EntryRep("com.zink.dennis", List(Some(2),Some(2)))
	   assert(matcher(template, entry) == false )
	   
	   // no match with null
	   val tWithNull = EntryRep("com.zink.dennis", List(Some(2),None))
	   assert(matcher(tWithNull, entry) == false )      
   }
    
    
  test("Write") {
	   val arm = new Armand[EntryRep](RepFactory.matcher)
	   val entry = EntryRep("com.zink.dennis", List(Some("Hello"), Some(2)))
	   val templ = EntryRep("com.zink.dennis", List(Some("Hello"), None))
	   arm.write(entry, 100 millis)

	   // check the stores
	   assert(arm.storedEntries.size === 1)
	   assert(arm.pendingReads.size === 0)	   
	   assert(arm.pendingTakes.size === 0)
	   
	   // if above asserts are true then ...
	   val res= arm.read(templ, 0 millis)
	   res onSuccess { case d => assert(d === entry) } 
	   res onFailure { case _ => assert(false, "Write failed") }	     
  }
  
  
  test("Read") { 
	   val arm = new Armand[EntryRep](RepFactory.matcher)
	   val entry = EntryRep("com.zink.dennis", List(Some("Hello"), Some(2)))
	   val templ = EntryRep("com.zink.dennis", List(Some("Hello"), None))
	     
	   // failed read without a lease
	   val res = arm.read(templ, 0 millis)
	   assert(arm.storedEntries.size === 0)
	   assert(arm.pendingReads.size === 0)	   
	   assert(arm.pendingTakes.size === 0)
	   res onSuccess { case d => assert(false, "Eeeee") } 
	   res onFailure { case _ => assert(true) }	 
	   
	   // Read with lease and subsequent no match expiring lease
	   val fpending = arm.read(templ, 1 millis)
	   assert(arm.storedEntries.size === 0)
	   assert(arm.pendingReads.size === 1)	   
	   assert(arm.pendingTakes.size === 0)
	   
	   fpending onSuccess { case d => assert(false, "Eeeee") }  
	   fpending onFailure { case _ => assert(true) }	 
	   assert(arm.storedEntries.size === 0)
	   // maybe no collection assert(arm.pendingReads.size === 1 || 0)	   
	   assert(arm.pendingTakes.size === 0)
	   
	   // let the dust settle
	   Thread.sleep(2)
	   
	   // a read with lease and subsequent match on write
	   val spending = arm.read(templ, 100 millis)
	   assert(arm.storedEntries.size === 0)
	   // maybe no collection assert(arm.pendingReads.size === 1 || 2)	   
	   assert(arm.pendingTakes.size === 0)
	   arm.write(entry, 10 millis)
	   spending onSuccess { case d => assert(d === entry) }  
	   spending onFailure { case _ => assert(false) }	 
	   assert(arm.storedEntries.size === 1)
	   assert(arm.pendingReads.size === 0)	   
	   assert(arm.pendingTakes.size === 0)   
    }
  
  
    test("Take") { 
	   val arm = new Armand[EntryRep](RepFactory.matcher)
	   val entry = EntryRep("com.zink.dennis", List(Some("Hello"), Some(2)))
	   val templ = EntryRep("com.zink.dennis", List(Some("Hello"), None))
	     
	   // failed take without a lease
	   val res = arm.take(templ, 0 millis)
	   assert(arm.storedEntries.size === 0)
	   assert(arm.pendingReads.size === 0)	   
	   assert(arm.pendingTakes.size === 0)
	   res onSuccess { case d => assert(false, "Eeeee") } 
	   res onFailure { case _ => assert(true) }	 
	   
	   // Take with lease and subsequent no match expiring lease
	   val fpending = arm.take(templ, 1 millis)
	   assert(arm.storedEntries.size === 0)
	   assert(arm.pendingReads.size === 0)	   
	   assert(arm.pendingTakes.size === 1)
	   
	   fpending onSuccess { case d => assert(false, "Eeeee") }  
	   fpending onFailure { case _ => assert(true) }	 
	   assert(arm.storedEntries.size === 0)
	   assert(arm.pendingReads.size === 0)	   
	   // maybe no collection assert(arm.pendingTakes.size === 1 || 0)
	   
	   // let the dust settle
	   Thread.sleep(2)
	   
	   // a take with lease and subsequent match on write
	   val spending = arm.take(templ, 100 millis)
	   assert(arm.storedEntries.size === 0)
	   assert(arm.pendingReads.size === 0)	   
	   // maybe no collection  assert(arm.pendingTakes.size === 1 || 2)
	   arm.write(entry, 10 millis)
	   spending onSuccess { case d => assert(d === entry) }  
	   spending onFailure { case _ => assert(false) }	 
	   assert(arm.storedEntries.size === 0)
	   assert(arm.pendingReads.size === 0)	   
	   assert(arm.pendingTakes.size === 0)   
  }
    
    
   test("Needle in Haystack") { 
     val arm = new Armand[EntryRep](RepFactory.matcher)
     val haySize = 100000
     for ( needle <- 1 to haySize) {
       val rep = EntryRep("nh", List(Some("Hello"), Some(needle)))
       arm.write(rep, 10 seconds)
     }
	 assert(arm.storedEntries.size === haySize)
	 assert(arm.pendingReads.size === 0)	   
	 assert(arm.pendingTakes.size === 0)
	 val templ = EntryRep("nh", List(Some("Hello"), Some(haySize)))
	 val readRes = arm.read(templ, 1 millis)
	 readRes onSuccess { case d => assert(templ === d) }  
	 readRes onFailure { case _ => assert(false) }	 
	 
	 val takeRes = arm.take(templ, 1 millis)
	 takeRes onSuccess { case d => assert(templ === d) }  
	 takeRes onFailure { case _ => assert(false) }	 
	 
	 assert(arm.storedEntries.size === haySize-1)
	 assert(arm.pendingReads.size === 0)	   
	 assert(arm.pendingTakes.size === 0)
   }
    
    
}
