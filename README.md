Flight
======

Flight is an asynchronous version of the Fly Object Spaces that runs ( for the moment ) in a local Java Virtual Machine.

In the Spaces model of interaction there are three basic operations 

* Write an Immutable Object to the Space
* Read a copy of an Object from the Space
* Take (remove) an Object from the Space 

And equally as important is that each interaction is performed under a time constraint. Addtionally in Flight these 
operations are asychronous, hence return Future of the resulting type.

To bring this to life here is some code ...
