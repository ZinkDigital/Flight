Flight
======

Flight is an asynchronous version of the Fly Object Spaces that runs ( for the moment ) in a local Java Virtual Machine.

In the Spaces model of interaction there are three basic operations 

* Write an Immutable Object to the Space
* Read a copy of an Object from the Space
* Take (remove) an Object from the Space 

And equally as important is that each interaction is performed under a time constraint called a 'lease'. Addtionally in Flight these operations are asychronous, hence return Future of the resulting type.

If you are using Flight in your project you can download the binaries from sonatype, for example in an SBT powered project you need to include this line :

```scala
libraryDependencies += "com.flyobjectspace" %% "flight" % "0.0.1-SNAPSHOT"
```

To bring this to life here is some code, starting with the imports. You can find this code in the examples package of
the project and run it from sbt by typing '>sbt run' and selecting the 'Prices' app. 


```scala
import com.zink.fly.{ Flight => flt }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
```

That brings in the elements of the concurrent libraries that we need mostly in the form of implicits and aliases the 
interface to Flight to flt.

Here is the definition of a case class that will represent stock prices.

```scala
case class Price(symbol : Option[String],  value : Option[Int]) 
```

For objects that we are going to use as 'entries' or 'templates' in the Space, the convention is to use Options of the 
field values of case classes. Given this definition we can set up target price for our stock and wait for the price 
to be the one we want.

```scala
  val bid = Price(Some("IBM"),Some(123))
  
  flt.read(bid, 100 seconds) onSuccess { case e => println(s"Found $e") }
```

The value bid is acting as a 'template' in the call to the read method and we have set up some code that will be
executed if and when the future read finds the price given in the template.

Now write a set of values to trigger the successful completion of the read.

```scala
 for (v <- 120 to 123) flt.write(Price(Some("IBM"),Some(v)) , 1 second)
```

Which writes a range of values into the Space the last of which matches the template bid price triggering the output

```scala
Found Price(Some(IBM),Some(123))
```

Thats the first example complete. Check the code in the examples package for more details and complex examples.



