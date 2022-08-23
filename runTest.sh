#!/bin/bash
# this script can be invoked from the base only. 
HERE=$(pwd)
echo "here: $HERE"
TCLS="$HERE/target/test-classes"
echo "tstcls: $TCLS"
clspath=$(cat target/classpath.txt):$HERE/target/classes:$TCLS

echo "$clspath"
#java -Dsourcepath=src/main/java:src/test/java -DchooseClasspath=target/test-classes -Denvironment=. -DsrcDir=src/main/java -classpath $clspath eu.simuline.arithmetics.left2right.XFPNumberTest
java -ea -esa -Dsourcepath=./src/main/java:./src/test/java -DchooseClasspath=$TCLS -classpath $clspath $@
