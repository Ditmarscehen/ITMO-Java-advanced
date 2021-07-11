$scriptPath = Get-Location
$path = (get-item $scriptPath).Parent.Parent.Parent.Parent.Parent.Parent.Parent.FullName
cd $path
javac "test\TestInterface.java"
java -jar implementor.jar -jar java.util.Set '"'
cd $scriptPath