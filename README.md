Casper
=====

A tool for finding the root cause of null pointer exceptions through causality traces

[![Build Status](https://travis-ci.org/Spirals-Team/casper.svg?branch=master)](https://travis-ci.org/Spirals-Team/casper)

Casper: Automatic Tracking of Null Dereferences to Inception with Causality Traces (Benoit Cornu, Earl T. Barr, Martin Monperrus and Lionel Seinturier), In Journal of Systems and Software, Elsevier, 2016.

```
@article{cornu:hal-01354090,
 title = {{Casper: Automatic Tracking of Null Dereferences to Inception with Causality Traces}},
 author = {Cornu, Benoit and Barr, Earl T. and Monperrus, Martin and Seinturier, Lionel},
 url = {https://hal.archives-ouvertes.fr/hal-01354090/document},
 doi = {10.1016/j.jss.2016.08.062},
 journal = {{Journal of Systems and Software}},
 publisher = {{Elsevier}},
 year = {2016},
}
```

Casper: Debugging Null Dereferences with Dynamic Causality Traces (Benoit Cornu, Earl T. Barr, Lionel Seinturier, Martin Monperrus), Technical report hal-01113988, Inria Lille, 2015.

The evaluation dataset is at <https://github.com/Spirals-Team/npe-dataset/>

To run casper on a simple example:

`mvn test`

it creates `spooned/FooCasper.java` and `spooned/FooCasperNullified.java` (the ghost class).

It triggers a deluxe NPE of the form (printed on the console):

```
throws NPE calling bar at FooCasper.bug1(FooCasper.java:15)
parameter o is null in foo5 at (FooCasper.java:38)
returned null in method foo5 (FooCasper.java:39)
field/array access on null at (FooCasper.java:43)
throws NPE at FooCasper.bug2(FooCasper.java:40)
```

Casper has two modes for creating ghost classes: source transformation and binary transformation at loading time.
To use the former, you have to use a custom classloader as system classloader.
-Djava.system.class.loader=sacha.reflect.classloading.PermissiveClassLoader

Example of modified Java code
-----------------------------
(excerpt from `spooned/FooCasper.java`)

Before:
```java
public FooCasper bug1() {
		if (new FooCasper(1).foo() != null) {
			throw new Error();
		}
		FooCasper g = new FooCasper(1).foo();
		f=g;
		System.out.println(f);
		f.bar(); 		// the NPE
		return null;
}
```

After:

```java
public FooCasper bug1() {
    if (isNotNull(new FooCasper().foo())) {
        throw new java.lang.Error();
    } 
    FooCasper g = setAssigned(new FooCasper().foo(), FooCasper.class, "g (FooCasper.java:14)");
    f = setAssigned(g, FooCasper.class, "f (FooCasper.java:15)");
    System.out.println(f);
    f.bar();
    return setReturn(null, FooCasper.class, "bug1 (FooCasper.java:19)");
}

```
