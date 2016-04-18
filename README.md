Casper
=====

A tool for finding the root cause of null pointer exceptions through causality traces

[![Build Status](https://travis-ci.org/Spirals-Team/casper.svg?branch=master)](https://travis-ci.org/Spirals-Team/casper)

Casper: Debugging Null Dereferences with Dynamic Causality Traces (Benoit Cornu, Earl T. Barr, Lionel Seinturier, Martin Monperrus), Technical report hal-01113988, Inria Lille, 2015.

```
@techreport{cornu:hal-01113988,
 title = {Casper: Debugging Null Dereferences with Dynamic Causality Traces},
 author = {Cornu, Benoit and Barr, Earl T. and Seinturier, Lionel and Monperrus, Martin},
 number = {hal-01113988},
 institution = {Inria Lille},
 year = {2015},
 url = {https://hal.archives-ouvertes.fr/hal-01113988/file/main.pdf},
}
```

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

```
public FooCasper bug1() {
    if (bcornu.nullmode.ComparizonOperator.isNotNull(new FooCasper().foo())) {
        throw new java.lang.Error();
    } 
    FooCasper g = bcornu.nullmode.AssignResolver.<FooCasper>setAssigned(new FooCasper().foo(), FooCasper.class, "g (FooCasper.java:14)");
    f = bcornu.nullmode.AssignResolver.<FooCasper>setAssigned(g, FooCasper.class, "f (FooCasper.java:15)");
    java.lang.System.out.println(f);
    f.bar();
    return bcornu.nullmode.ReturnResolver.<FooCasper>setReturn(null, FooCasper.class, "bug1 (FooCasper.java:19)");
}

```