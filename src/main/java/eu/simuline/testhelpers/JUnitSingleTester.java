
package eu.simuline.testhelpers;

import eu.simuline.util.Finder;

import java.io.File;

/**
 * Runs a bunch of tests using {@link Actions#runTestClass(String)}. 
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public abstract class JUnitSingleTester {

    /**
     * Runs all testclasses in the directory given by the 0th argument 
     * in the package given by the 1st argument. 
     * The package separator is the file separator. 
     *
     * @param args
     *    two arguments together consituting the path to the classes: 
     *    The 0th one the path to the java classes, 
     *    the 1st one the package to be run 
     *    with file separator as package separator. 
     */
    @SuppressWarnings("checkstyle:nowhitespacebefore")
    public static void main(String[] args) { //throws ClassNotFoundException {
	if (args.length != 2) {
	    throw new IllegalArgumentException
		("Expected exactly two araguments but found " + 
		 args.length + ". ");
	}

	File baseDir = new File(args[0] + args[1]);
	String dirName = baseDir.getAbsolutePath();
	Finder finder = Finder.path(baseDir)
//	    .name(".*arithmetics.*")
	    .not(Finder.nameFilter(".*arithmetics.*"))
	    .not(Finder.nameFilter(".*graphDV.*"))
//	    .name(".*class")
	    .name(".*Test\\.class")
//	    .print(System.out)
	    ;

	File clsFile;
	String clsName;

	while (finder.hasNext()) {
	    clsFile = finder.next();
	    clsName = clsFile.getAbsolutePath();
System.out.println("clsName" + clsName);

	    assert clsName.startsWith(dirName);
	    clsName = clsName.substring(args[0].length());
	    clsName = clsName.substring(0,
					clsName.length() - ".class".length());
System.out.println("clsName" + clsName);

	    clsName = clsName.replace(System.getProperty("file.separator")
				      .charAt(0), '.');
	    System.out.println("clsName: " + clsName);
	    Actions.runTestClass(clsName);
	} // while 

    }
} // class JUnitSingleTester 

