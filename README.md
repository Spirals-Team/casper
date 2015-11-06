Casper
=====

A tool for finding the root cause of null pointer exceptions through causality traces

Casper: Debugging Null Dereferences with Ghosts and Causality Traces (Benoit Cornu, Earl T. Barr, Lionel Seinturier, Martin Monperrus), Technical report hal-01113988, Inria Lille, 2015.

```
@techreport{cornu:hal-01113988,
 title = {Casper: Debugging Null Dereferences with Ghosts and Causality Traces},
 author = {Cornu, Benoit and Barr, Earl T. and Seinturier, Lionel and Monperrus, Martin},
 number = {hal-01113988},
 institution = {Inria Lille},
 year = {2015},
 url = {https://hal.archives-ouvertes.fr/hal-01113988/file/main.pdf},
}
```

''mvn compile''

You have to use a custom classloader
-Djava.system.class.loader=sacha.reflect.classloading.PermissiveClassLoader