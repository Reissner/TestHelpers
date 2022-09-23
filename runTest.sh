#!/bin/bash
# this script can be invoked from the base only. 
HERE=$(pwd)
echo "here: $HERE"
TCLS="$HERE/target/test-classes"
echo "tstcls: $TCLS"
clspath=$(cat target/classpath.txt):$HERE/target/classes:$TCLS

# shall comprise what is given in .vscode/settings.json 
# for this project, in that file there is too much. 
# in addition required are DchooseClasspath, -DchooseClasspath and of course -classpath

echo "$clspath"
java -ea -esa -Xverify:all -Xmx1023m \
  -Dsourcepath=./src/main/java:./src/test/java \
  -DchooseClasspath=$TCLS \
  -classpath $clspath $@
