#!/bin/sh

mkdir bin;
cd lib;
architecture=`uname -m`
if [ "$architecture" != "x86_64" ] && [ "$architecture" != "ia64" ]; then
    url="http://github.com/downloads/SyrusAkbary/RobotLand-Java/swt32.jar"
else
    url="http://github.com/downloads/SyrusAkbary/RobotLand-Java/swt64.jar"
fi
wget $url -O swt.jar;
cd ..;
echo "javac -Xlint -sourcepath src/ -classpath lib/swt.jar -d bin/ src/View.java 
java -classpath bin/:lib/swt.jar View" >> run.sh;
chmod +x run.sh;
