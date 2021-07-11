#!/bin/bash
scriptPath=$PWD
path="$(dirname "$scriptPath")"
path="$(dirname "$path")"
path="$(dirname "$path")"
path="$(dirname "$path")"
path="$(dirname "$path")"
cd $path
javadoc -link https://docs.oracle.com/en/java/javase/11/docs/api/ /info/kgeorgiy/ja/fadeev/implementor/Implementor.java -d javadoc
cd $scriptPath