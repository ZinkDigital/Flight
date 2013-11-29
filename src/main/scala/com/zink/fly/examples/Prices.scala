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

package com.zink.fly.examples


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import com.zink.fly.{ Flight => flt }
import scala.util.{ Success, Failure }


case class Price(symbol : Option[String],  value : Option[Int]) 

object Prices extends App {
  
  val offer = Price(Some("ARM"),Some(123))

  flt.write(offer, 100 seconds) onSuccess { 
	case lease => println(s"Offer written for $lease") 
  }
  
  val tmpl = Price(Some("ARM"), None)

  flt.read(tmpl, 10 seconds) onComplete {
     case Success(entry) => println("Read matched " + entry)
     case Failure(t) => println("Sorry: " + t.getMessage)
  }

  flt.take(tmpl, 10 seconds) onSuccess { 
	 case ent => println(s"Take matched $ent") 
  }

  val tmplArm = Price(Some("ARM"),Some(123))
  val tmplWlf = Price(Some("WLF"),Some(321))
  val lse = 10 seconds
  
  flt.write(tmplArm, lse)   // comment out to prevent deals
  flt.write(tmplWlf, lse)

  val futArm = flt.read(tmplArm, lse) 
  val futWlf = flt.read(tmplWlf, lse)

  (futArm zip futWlf) onComplete {
     case Success(e) => println("Deal")
     case Failure(t) => println("No Deal")
  }
  
  Thread.sleep( (11 seconds).toMillis )
  sys.exit()
}
