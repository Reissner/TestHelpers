#!/bin/bash
# this script can be invoked from the base only. 
HERE=$(pwd)
echo "here: $HERE"
TCLS="$HERE/target/test-classes"
echo "tstcls: $TCLS"
clspath=$(cat target/classpath.txt):$HERE/target/classes:$TCLS

echo "$clspath"
java -ea -esa -Dsourcepath=./src/main/java:./src/test/java -DchooseClasspath=$TCLS -classpath $clspath $@
