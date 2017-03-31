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
import scala.concurrent.ExecutionContext.Implicits.global


import org.scalatest._



//case class Car( val colour : String, val mileage : BigInt )
//case class CarOpt( val colour : Option[String], val mileage : Option[Int] )


class GenFactorySuite extends FunSuite with BeforeAndAfter with Matchers {


  case class Car( val colour : String, val mileage : BigInt )
  case class CarOpt( val colour : Option[String], val mileage : Option[Int] )


  test("Generic Record From Type") {
 
	   val entry = Car("Red", BigInt(20000) )
	   val rep = EntryRep("com.zink.fly.core.Car", List( "Red", BigInt(20000) ) )
	   val res = GenFactory.makeGeneric(entry)

     println
	    res should be (rep)
   }
  
//   test("EntryRep From Nulled") {
//
//	   val nullEntry = Car( null, null)
//
//	   val rep = EntryRep("com.zink.fly.core.Car", List( null, null ) )
//	   val res = RepFactory.makeRep(nullEntry)
//
//	   res should be (rep)
//   }
//
//   test("EntryRep From Options") {
//
//	   val entry = CarOpt( Some("Red"), Some(20000) )
//	   val rep = EntryRep("com.zink.fly.core.CarOpt", List(Some("Red"), Some(20000) ) )
//	   val res = RepFactory.makeRep(entry)
//
//	   res should be (rep)
//   }
//
//   test("T From Rep Values") {
//	   val rep = EntryRep("com.zink.fly.core.Car", List( "Red", BigInt(20000) ) )
//	   val res = RepFactory.makeT[Car](rep)
//	   res should be ( Car("Red", BigInt(20000) ) )
//   }
//
//   test("T From Rep Nulls") {
//       val rep = EntryRep("com.zink.fly.core.Car", List( null, null ) )
//	   val res = RepFactory.makeT[Car](rep)
//	   res should be ( Car( null, null) )
//   }
//
//   test("T From Options") {
//       val rep = EntryRep("com.zink.fly.core.CarOpt", List( Some("Red"), Some(20000) ) )
//	   val res = RepFactory.makeT[CarOpt](rep)
//	   res should be (CarOpt( Some("Red"), Some(20000) ) )
//   }
//
//    test("Perf makeRep") {
//      val rep = EntryRep("com.zink.fly.core.Car", List( "Red", BigInt(20000) ) )
//      val entry = RepFactory.makeT[Car](rep)
//      for (i <- 1 to 1000) {
//        RepFactory.makeRep(entry)
//      }
//    }
//
//    test("Perf makeT") {
//      val rep = EntryRep("com.zink.fly.core.Car", List( "Red", BigInt(20000) ) )
//      for (i <- 1 to 1000) {
//        RepFactory.makeT[Car](rep)
//      }
//    }
    
}
