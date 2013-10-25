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
import scala.concurrent.duration._
import scala.concurrent.promise


class TimeTriggerTestSuite extends FunSuite with BeforeAndAfter {

   test("Single Complete") {
 
       val tick = 5 nanos
	   val tt = new TimeTrigger( tick )
       val lease = 200 nanos
       var toggle = false
	   tt.schedule(lease) { toggle = true }
	   
	   assert(toggle === false)
	   Thread.sleep(0, 300)
	   assert(toggle === true)  
  }
      
  
  test("Fire in Sequence") {
    
	  val l1 = 10 millis
	  val l2 = 20 millis
	  val l3 = 30 millis
	  val tick = 100 nanos
	  val tt = new TimeTrigger( tick )
	  var t1 = false
	  var t2 = false
	  var t3 = false 
	  
	  // schedule backwards on the queue
	  tt.schedule( l3 ) { t3 = true }
	  tt.schedule( l2 ) { t2 = true }
      tt.schedule( l1 ) { t1 = true }
	  
      val itr = tt.pbq.iterator
      // (1 to 3).foreach( x => println( itr.next.expiry ) )
      
	  // check nothing fired 
      if (t1) assert(t1 === false)
	  if (t2) assert(t2 === false)
	  if (t3) assert(t3 === false)
 
	  // wait for the lease + 1 
	  Thread.sleep( l1.toMillis + 1  )
	  if (!t1) assert(t1 === true)
	  if (t2) assert(t2 === false)
	  if (t3) assert(t3 === false)
	  
	  Thread.sleep( l1.toMillis + 1  )
	  if (!t1) assert(t1 === true)
	  if (!t2) assert(t2 === true)
	  if (t3) assert(t3 === false)
	  
	  Thread.sleep( l1.toMillis + 1  )
	  if (!t1) assert(t1 === true)
	  if (!t2) assert(t2 === true)
	  if (!t3) assert(t3 === true)
   }
  
  
   test("Flood Promises Same Lease") {
     
     val pCount = 100000
     val tick = 10 nanos
     val lease = 100 millis
     val tt = new TimeTrigger( tick )
     val ps = ( 1 to pCount ).map( x => promise[Int] )
     
     assert ( ps.forall( x => x.isCompleted == false) )
     
     // schedule everything for completion
     ps.foreach( x => tt.schedule(lease) { x success 1 } )
     // wait 
     Thread.sleep(1 * 1000)
     assert ( ps.forall( x => x.isCompleted == true) )
    }   
        
}

 



class TimeTriggerTest {

}