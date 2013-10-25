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

/**
 * LeasedChain
 * 
 * Time managed chain with add/remove possible at head or tail
 * Each link has an associated expiry time which invalidates the 
 * entry beyond that time.
 * Links may stay on the chain past expiry time but cannot be access 
 * but will be deleted when they are next visited. Lazy.
 * 
 * This is not thread safe - the consumer is responsible for ensuring use from a 
 * single thread for any given method call at any given time. 
 * Often a single thread dispatch queue is used to enforce single calls
 */

class LeasedChain[T] {
  
  // Time handling is relatively weak here see 
  // see SI-7546 for example
  // the expiry fields needs to handle overflow properly
  trait Link
  class LeasedLink(val entry : T, val lease : FiniteDuration) extends Link {
    val expiry : Long = lease.toNanos + System.nanoTime
    def expired : Boolean = expiry < System.nanoTime  
    var next : Link = EmptyLink
    var prev : Link = EmptyLink
  }
  object EmptyLink extends Link
   
  
  /*
   * The following private methods and vars below
   * are side effecting and order dependent.
   * Hence not for public consumption - "Well ask yourself do I feel lucky ?"
   */
  var head : Link = EmptyLink
  var tail : Link = EmptyLink
  var size : Long = 0
  
  
  private def addToHead(link : LeasedLink) : Unit = {
	  if ( head == EmptyLink) {
	    	head = link
	    	tail = link
	   } else {
	    	head.asInstanceOf[LeasedLink].prev = link
	    	link.next = head
	    	head = link    
	    }
	  size += 1
  }
  
  private def addToTail(link : LeasedLink) : Unit = {
	  if ( head == EmptyLink) {
	    	head = link
	    	tail = link
	   } else {
	    	tail.asInstanceOf[LeasedLink].next = link
	    	link.prev = tail
	    	tail = link    
	    }
	  size += 1
  }
  
  
  /*
   * Assumes that the link parameter exists in the list.
   * Attempt to remove a link not in the list is undefined
   */
  private def remove( toRemove : LeasedLink ) : Unit = {
    if ( toRemove eq head ) {
       toRemove.prev = EmptyLink
       head = toRemove.next
    }
    if ( toRemove eq tail ) {
       toRemove.next = EmptyLink
       tail = toRemove.prev
    }
    if ( toRemove.prev != EmptyLink && toRemove.next != EmptyLink ) {
      toRemove.prev.asInstanceOf[LeasedLink].next = toRemove.next
      toRemove.next.asInstanceOf[LeasedLink].prev = toRemove.prev
    }
    size -= 1
  }
      
  
  /**
   * Write the Entry on the chain now
   */
  def writeImmediate( entry : T, lease : FiniteDuration) : Unit = {
     addToTail( new LeasedLink(entry, lease) )
  }
  
  /**
   * Search the queue from the head to find the first matching non
   * expired entry leaving it in the list
   */
  def readImmediate(template : T, matcher : (T, T) => Boolean) : Option[T] = {
    var current = head
    var matched : Option[T] = None
    while (current != EmptyLink && matched == None) {
      val link : LeasedLink = current.asInstanceOf[LeasedLink]
      if (!link.expired) { 
        if ( matcher( template,link.entry) ) matched = Some(link.entry) 
      } else {
        remove(link)
      }
      current = link.next
    } 
    matched
  }
  
 /**
   * Search the queue from the head to find the first matching entry non
   * expired entry and remove it from the list
   */
  def takeImmediate(template : T, matcher : (T, T) => Boolean) : Option[T] = {
    var current = head
    var matched : Option[T] = None
    while (current != EmptyLink && matched == None) {
      val link : LeasedLink = current.asInstanceOf[LeasedLink]
      if (!link.expired) { 
        if ( matcher( template,link.entry) ) {
          matched = Some(link.entry) 
          remove(link)
        }
      } else {
        remove(link)
      }
      current = link.next
    } 
    matched
  }
 
  
  /** 
   *  Remove and return all of the matching entries from the chain 
   *  TODO : When doing takeMany re-implement in terms of that.
   */
  def takeAll( template : T, matcher : (T, T) => Boolean) : List[Option[T]] = {
      takeImmediate(template, matcher) match {
        case None => Nil
        case Some(s) => Some(s) :: takeAll( template, matcher )
      }
    
  }
}
