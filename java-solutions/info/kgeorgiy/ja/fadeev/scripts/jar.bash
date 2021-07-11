#!/bin/bash
scriptPath=$PWD
path="$(dirname "$scriptPath")"
path="$(dirname "$path")"
path="$(dirname "$path")"
path="$(dirname "$path")"
path="$(dirname "$path")"
cd $path
pathClass = "/info/kgeorgiy/ja/fadeev/implementor/Implementor.java"
javac $pathClass
path="$(dirname "$path")"
cd $path
echo "Manifest-Version: 1.0
Created-By: Dmitry Fadeev
Main-Class: info.kgeorgiy.ja.fadeev.implementor.Implementor
Class-Path: java-advanced-2021\artifacts\info.kgeorgiy.java.advanced.implementor.jar
" > manifest.fm
jar cmf manifest.mf implementor.jar -C /java-solutions/ /info/kgeorgiy/ja/fadeev/implementor/Implementor.class
cd $scriptPath