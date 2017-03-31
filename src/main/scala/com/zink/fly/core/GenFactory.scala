package com.zink.fly.core

import shapeless.{HList, LabelledGeneric}
import shapeless.record._

/**
  * Created by nigel on 31/03/2017.
  */
object GenFactory {

  def makeGeneric[T](entry : T)(implicit e : LabelledGeneric[T]) : e.Repr = {
    e.to(entry)
  }


}
