package com.zink.fly.examples

import com.zink.fly.{ Flight => flt }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Success, Failure}



// Biff == BId oFFer
case class Biff(val prc : BigInt, val vol :BigInt )


object Matching extends App {
  
   
   val bid = Biff(123, 1000) 
    
   flt.write(bid, 100 seconds)
   
   val offer = Biff(123, null)
   
   flt.take(offer,  0 millis) onComplete {
       case Success(entry) => println(entry)
       case Failure(t) => println("Sorry: " + t.getMessage)
   }
  
   Thread.sleep( (10 millis).toMillis )
   sys.exit()
}