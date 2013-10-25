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

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.locks.LockSupport


/** 
 * TimeTrigger is designed to fire very short running closures from a single thread.
 * 
 * A case example would be failing Promises at a given time in the future
 * 
 * This is not intended to scale up to scheduling large work elements 
*/

class TimeBlock(val expiry : Long, val block : () => Any) extends Comparable[TimeBlock]  {
  override def compareTo(that : TimeBlock) = if ( that.expiry < this.expiry) 1 else -1
}


final class TimeTrigger(val tick : FiniteDuration)  {
  
  val pbq = new PriorityBlockingQueue[TimeBlock]()
  
  // TODO use a low value thread pool here rather than the single 
  // thread this would work well for leases expiring at the same time
  // and constrain thread growth over promises for example
  
  val loop = new Runnable {
     override def run() : Unit = {
         while (!Thread.interrupted) {
        	val now = System.nanoTime
            while (pbq.size != 0 && pbq.peek.expiry < now) {
               try {  pbq.take.block.apply }
               catch {  case e: Exception => }      
            }
            // time may have gone by so park till the next tick
            val elapsed = System.nanoTime - now // then
            LockSupport.parkNanos(Math.max(1, tick.toNanos-elapsed))
         }  
     } 
  }
  
  val triggerThread = new Thread(loop).start   
  
  final def schedule(lease : FiniteDuration) (block : => Any) {
    val expiry  = lease.toNanos + System.nanoTime
    pbq.offer(new TimeBlock(expiry, () => block))   
  }
 
}
