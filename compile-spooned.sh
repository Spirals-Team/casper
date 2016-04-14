#!/bin/sh
mkdir -p spooned-classes
javac  -cp target/classes -d spooned spooned/*.java
