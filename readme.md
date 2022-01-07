# Testhelpers

This package offers elementary classes which proved useful 
for tests of software made at simuline (www.simuline.eu).
For further pieces of information see the homepage http://www.simuline.eu/TestHelpers/index.html. 

The functionality is
- an accessor class for whitebox tests
- a static class providing specialized assertions
- a gui to run tests and testsuites selectively and interactively.

Although most of the classes are outdated if using new junit5
in combination with modern IDEs,
the testhelpers are still used at (www.simuline.eu).

Originally, the GUI filled the gap for the IDE given by the emacs plugin jdee.
The assertions class provided completed the assertions offered by junit4.
Solely the accessor seems really up to date at the time of this writing.

Anyway, if you want, feel free to use this piece of software.

The GUI is intended to be used in a maven project.
To that end, just place [runTest.sh](./runTest.sh) in the root directory of the project
(where your `pom.xml` resides)
and make sure that in the pom something like the following can be found
```
<project...>
  ...
  <build>
    ...
    <plugins>
      ...
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-dependency-plugin</artifactId>
	<version>3.0.2</version>
	<executions>
	  <execution>
	    <goals>
	      <goal>build-classpath</goal>
	    </goals>
	    <configuration>
	      <outputFile>${project.build.directory}/classpath.txt</outputFile>
	    </configuration>
	  </execution>
          ...
	</executions>
      </plugin>
      ...
   <plugins>
    ...
  <build>
</project>
 ```

Then run maven pass lifecycle phase `generate-sources`,
most naturally by
```
mvn generate-sources
```
As a result, in the target folder the file `classpath.txt` appears
containing the classpath required by `runTest.sh`.
Then you can run that script on a testclass, provided you use a unixoid operating system.
You can also run a test on a testclass of the testhelpers by
```
./runTest.sh eu.simuline.testhelpers.AccessorTest&
```

