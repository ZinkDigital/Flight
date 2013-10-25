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

import scala.reflect.runtime.{ universe => runUniv }
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import sun.reflect.ReflectionFactory
import scala.collection.mutable.HashMap


case class EntryRep(val fqcn : String, val fields : List[Any]) 


object RepFactory {
  
    def matcher( temp : EntryRep, entry : EntryRep) : Boolean = {
      (temp.fields, entry.fields).zipped.forall {
        case (tplField, entField) => tplField == None || 
        						tplField == null ||
        						tplField == entField 
      } 
    }

   
   def makeRep[T : TypeTag : ClassTag](entry : T) : EntryRep = {
   
     val runMir = runUniv.runtimeMirror(entry.getClass.getClassLoader)
     val instMir = runMir.reflect(entry) 
     val tpe = typeTag[T].tpe
  
     val fieldVals : List[Any] = getFieldSymbols(tpe).map { sym => 
        instMir.reflectField(sym).get
      }
      EntryRep(tpe.toString, fieldVals)
    }
    
   
    // Missus will ya make T, put on the Telly to the BBC.
   def makeT[T : TypeTag : ClassTag](entryRep : EntryRep) : T = {
      
      val cls  = Class.forName(entryRep.fqcn)
      val runMir = runUniv.runtimeMirror(cls.getClassLoader)
      val clsSym = runMir.staticClass(entryRep.fqcn)
      val clsMir = runMir.reflectClass(clsSym)
      val ctorSym = runUniv.typeTag[T].tpe.member(runUniv.nme.CONSTRUCTOR).asMethod
      val ctorMir = clsMir.reflectConstructor(ctorSym)
      // ----------------------------- varargs sugar 
      val obj = ctorMir.apply(entryRep.fields: _*)
      obj.asInstanceOf[T] 
    }
    
  // speed the fields to get
  val fieldMap = HashMap[runUniv.Type,List[TermSymbol]]()
   
  private def getFieldSymbols(tpe : runUniv.Type) : List[TermSymbol] = {
    fieldMap.getOrElseUpdate(tpe , {
    	val allFields = tpe.members.collect{ case s: TermSymbol => s }
    	allFields.filter(s => s.isVal || s.isVar).toList.reverse 
    } )
  }
  
  
}
