#!/bin/sh
mkdir -p spooned-classes
javac --add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED -cp target/classes -d spooned spooned/*.java
