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

import scala.reflect.runtime.universe._
import scala.concurrent.duration._

import java.util.concurrent.LinkedBlockingQueue

import RequestRouter._

class TypeChannel {
  
  val maxQueueSize= 1024	// backflow after a backlog of 1024
  val q = new LinkedBlockingQueue[Request](maxQueueSize)
  
  val arm = new Armand[EntryRep](RepFactory.matcher)

  // single dispatch loop for each request on this channel
  val dispatchLoop = new Runnable {
     override def run() : Unit = {
         while (!Thread.interrupted) {
        	q.take.process(arm)
         }  
     } 
  }
  val singleDispatchThread = new Thread(dispatchLoop).start   

}

