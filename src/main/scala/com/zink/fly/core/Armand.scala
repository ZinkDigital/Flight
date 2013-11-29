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

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.promise
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

import java.util.concurrent.TimeoutException



object LeaseTimeoutException extends TimeoutException("Lease Timed Out")

/**
 * There is one Armand instance per stored type that is being 
 * stored in the Space that keeps a record of all of the 
 * entries of that type and the pending reads and takes of 
 * that type 
 */
class Armand[T]( val matcher : (T,T) => Boolean ) {
  
  val storedEntries = new LeasedChain[T]()
  
  val pendingReads = new LeasedChain[PendingPromise]()
  
  val pendingTakes = new LeasedChain[PendingPromise]()

  case class PendingPromise(val template : T, val promise : Promise[T] = null) 
  
  // Apply the matcher to the pending promise of T 
  // (sense inverted i.e. matching the template to the entry)
  def pendingMatcher( pp1 : PendingPromise, pp2 : PendingPromise) :  Boolean = {
       matcher(pp2.template, pp1.template)	 
  }
  
  val trigger = new TimeTrigger(100 nanos)
  
  
  /**
   * Write the entry checking if any read or takes are pending
   */
  def write(entry : T, lease : FiniteDuration) : Future[FiniteDuration] = {
    
	  val templ = PendingPromise(entry) 
      
      // try to find all the pending reads and dispatch them
	  // takeAll could be optimised to make a single pass.
      val reads = pendingReads.takeAll( templ, pendingMatcher )
      // TODO : these promises may expire due to the lease timeout
      // whilst we are here need to check for failure to complete
      reads.foreach( pr => pr.get.promise.success(entry))
         
      // try to find a pending take
      val take = pendingTakes.takeImmediate( templ, pendingMatcher)
      take match {
	    // TODO : this promises may expire due to the lease timeout
        // whilst we are here need to check for failure to complete
	    case Some(t) => t.promise.success(entry)
	    // check resources here and reduce lease if required
	    case None => storedEntries.writeImmediate(entry, lease)
	  }
      Future(lease)
  }
  
  
  def read(template : T, lease : FiniteDuration) : Future[T] = {
    val prms = promise[T]
    storedEntries.readImmediate(template, matcher) match {
      case Some(entry) => prms success entry
      case None => {
        if (lease == (0 nanos)) {
          prms failure LeaseTimeoutException
        } else {
        	val pp = PendingPromise(template,prms)
        	pendingReads.writeImmediate(pp,lease)
    	    trigger.schedule(lease) { prms failure LeaseTimeoutException }
        }
      }
    }
    prms future  
  }
  
  
  def take(template : T, lease : FiniteDuration) : Future[T] = {
    val prms = promise[T] 
    storedEntries.takeImmediate(template, matcher) match {
      case Some(entry) => prms success entry
      case None => { 
        if (lease == (0 nanos)) {
          prms failure LeaseTimeoutException
        } else {
    	  val pp = PendingPromise(template,prms)
    	  pendingTakes.writeImmediate(pp,lease)
    	  trigger.schedule(lease) { prms failure LeaseTimeoutException }
        }
      }
    } 
    prms future
  } 
 
  
}

