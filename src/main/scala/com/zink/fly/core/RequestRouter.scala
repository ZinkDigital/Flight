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

import scala.reflect.runtime.{universe => runUniv}
import scala.reflect.runtime.universe._
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.{future, promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.LinkedBlockingQueue

import scala.collection.concurrent.TrieMap


object RequestRouter {

	sealed abstract trait Request {
	   def key : String
	   def process(arm : Armand[EntryRep]) 
	}
	
	case class WriteRequest(entry : EntryRep, lease : FiniteDuration, 
								prms : Promise[FiniteDuration] ) extends Request {
	  def key = entry.fqcn 
	  def process(arm : Armand[EntryRep]) = prms completeWith arm.write(entry, lease) 
	}
	
	case class ReadRequest( template : EntryRep, lease : FiniteDuration, 
								prms : Promise[EntryRep] ) extends Request {
	  def key = template.fqcn
	  def process(arm : Armand[EntryRep]) = prms completeWith arm.read(template, lease)
	  
	}
	
	case class TakeRequest( template : EntryRep, lease : FiniteDuration, 
								prms : Promise[EntryRep] ) extends Request {
	  def key = template.fqcn
	  def process(arm : Armand[EntryRep]) = prms completeWith arm.take(template, lease)
	}
		
						
  import scala.collection.mutable.{ HashMap, SynchronizedMap }
  
  val rtr = new TrieMap[String, TypeChannel]

  def route( rq : Request ) : Unit = {
    val channel = rtr.getOrElseUpdate(rq.key, new TypeChannel) 
    channel.q.put(rq)
  } 
}
