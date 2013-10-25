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

package com.zink.fly

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

import com.zink.fly.core.EntryRep
import com.zink.fly.core.RepFactory._
import com.zink.fly.core.RequestRouter._


object Flight {

  def write[T <: AnyRef : TypeTag : ClassTag ](entry : T, lease : FiniteDuration) : Future[FiniteDuration] = {
    val prm = Promise[FiniteDuration]
    route(WriteRequest(makeRep(entry), lease, prm))
    prm future 
  }
  
  def read[T <: AnyRef : TypeTag : ClassTag](template : T, lease : FiniteDuration) : Future[T] = {
    val prmsType = Promise[T]
    val prmsRep = Promise[EntryRep]
    route(ReadRequest(makeRep(template), lease, prmsRep ))
    chainReply( prmsRep.future, prmsType)
  }
  
  def take[T <: AnyRef : TypeTag : ClassTag](template : T, lease : FiniteDuration) : Future[T] = {
    val prmsType = Promise[T]
    val prmsRep = Promise[EntryRep]
    route(TakeRequest( makeRep(template), lease, prmsRep ))
    chainReply( prmsRep.future, prmsType) 
  }
    
  
  def writeMany[T <: AnyRef : TypeTag : ClassTag ](entries : Seq[T], lease : FiniteDuration) : Seq[Future[FiniteDuration]] = 
		  entries.map( e => write(e,lease))
		  
  def readMany[T <: AnyRef : TypeTag : ClassTag](templates : Seq[T], lease : FiniteDuration) : Seq[Future[T]] = 
	  		templates.map( t => read(t,lease))	
	  		
  def takeMany[T <: AnyRef : TypeTag : ClassTag](templates : Seq[T], lease : FiniteDuration) : Seq[Future[T]] = 
	  		templates.map( t => take(t,lease))	  		
  
	  		
  private def chainReply[T <: AnyRef : TypeTag : ClassTag] ( repFut : Future[EntryRep], typePrm : Promise[T] ) = {
    repFut onComplete { 
      case result =>
        result match {
          case Success(rep) => typePrm success makeT[T](rep)
          case f : Failure[_] => typePrm complete f.asInstanceOf[Failure[T]]
        }
    }
    typePrm.future  
  } 
  
}
