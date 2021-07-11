$scriptPath = Get-Location
$path = (get-item $scriptPath).Parent.Parent.Parent.Parent.Parent.FullName
cd $path
javadoc -link https://docs.oracle.com/en/java/javase/11/docs/api/ -private info\kgeorgiy\ja\fadeev\implementor\Implementor.java info\kgeorgiy\java\advanced\implementor\JarImpler.java info\kgeorgiy\java\advanced\implementor\Impler.java info\kgeorgiy\java\advanced\implementor\ImplerException.java -d javadoc
cd $scriptPath