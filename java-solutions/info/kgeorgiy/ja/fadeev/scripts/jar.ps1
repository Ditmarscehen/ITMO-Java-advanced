$scriptPath = Get-Location
$path = (get-item $scriptPath).Parent.Parent.Parent.Parent.Parent.FullName
cd $path
$pathClass = "info\kgeorgiy\ja\fadeev\implementor\Implementor.java"
javac $pathClass
$path = (get-item $scriptPath).Parent.Parent.Parent.Parent.Parent.Parent.Parent.FullName
cd $path
"Manifest-Version: 1.0 
Created-By: Dmitry Fadeev 
Main-Class: info.kgeorgiy.ja.fadeev.implementor.Implementor
Class-Path: java-advanced-2021\artifacts\info.kgeorgiy.java.advanced.implementor.jar 
"| Out-File -Encoding ascii manifest.mf
jar cmf manifest.mf implementor.jar -C java-solutions\ info\kgeorgiy\ja\fadeev\implementor\Implementor.class
cd $scriptPath