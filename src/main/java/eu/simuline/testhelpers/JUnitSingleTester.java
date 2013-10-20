
package eu.simuline.testhelpers;

import eu.simuline.util.Finder;

import java.io.File;

public class JUnitSingleTester {

    static class RunnerThread extends Thread {
	Class<?> tstCls;
	RunnerThread(Class<?> tstCls) {
	    this.tstCls = tstCls;
	}

	public void run() {
	    Actions.run(tstCls);
	}
    } // class RunnerThread 

    /**
     * Runs all testclasses in the directory given by the 0th argument 
     * in the package **** given by the second argument. 
     */
    public static void main(String[] args) throws ClassNotFoundException {
	File baseDir = new File(args[0]+args[1]);
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
	Class<?> tstCls;

	while (finder.hasNext()) {
	    clsFile = finder.next();
	    clsName = clsFile.getAbsolutePath();
System.out.println("clsName"+clsName);

	    assert clsName.startsWith(dirName);
	    clsName = clsName.substring(args[0].length());
	    clsName = clsName.substring(0, clsName.length()-".class".length());
System.out.println("clsName"+clsName);

	    clsName = clsName.replace(System.getProperty("file.separator")
				      .charAt(0),'.');
	    System.out.println("clsName: "+clsName);
	    tstCls = Class.forName(clsName);
	    //Actions.run(tstCls);
	    new RunnerThread(tstCls).start();
	} // while 

    }
} // class JUnitSingleTester 

