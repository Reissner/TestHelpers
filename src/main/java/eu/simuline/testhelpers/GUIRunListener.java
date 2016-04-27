package eu.simuline.testhelpers;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.Failure;
import org.junit.runner.Description;
import org.junit.runner.Result;

import java.util.Iterator;

//import javax.swing.SwingUtilities;// wrong place

/**
 * A {@link RunListener} which notifies the GUI {@link GUIRunner} 
 * of the events occuring while running a testsuite. 
 *
 * Created: Sat Jun  3 17:17:23 2006
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
public class GUIRunListener extends RunListener {


    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    protected TestCase testCase;

    protected Actions actions;

    protected GUIRunner runner;


    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    // does not work, is just for compilation of testcases. 
    public GUIRunListener() {
	this((Actions)null);
    }

    // does not work, is just for compilation of testcases. !!!!
    public GUIRunListener(Class<?> cls) { // NOPMD
	this((Actions)null);
    }

    public GUIRunListener(Actions actions) {
	this.actions = actions;
	this.runner = this.actions.getRunner();
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */


    // homemade extension of RunListener 
    public void testRunAborted() {
      // is empty. 
    }

    /**
     * Returns a string representation of <code>desc</code>. 
     */
    public static String desc2string(Description desc) {
	StringBuffer buf = new StringBuffer();
	if (desc.isSuite()) {
	    buf.append("<Suite name=\"");
	    buf.append(desc.getDisplayName());
	    buf.append("\">\n");
	    Description child;
	    Iterator<Description> iter = desc.getChildren().iterator();
	    assert iter.hasNext();
	    child = iter.next();
	    buf.append(desc2string(child));
	    while (iter.hasNext()) {
		child = iter.next();
		buf.append(", \n");
		buf.append(desc2string(child));
	    }
	    buf.append("\n</Suite>");
	} else {
	    assert desc.isTest();
	    buf.append(desc.getDisplayName());
	}
	return buf.toString();
    }


    /* -------------------------------------------------------------------- *
     * inner classes and enums.                                             *
     * -------------------------------------------------------------------- */

    /**
     * The listener if all test cases are run. 
     * **** maybe good extension: run a suite **** 
     */
    static class All extends GUIRunListener {

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	// -1 before testRunStarted
	private int testCaseCount;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	All(Actions actions) {
	    super(actions);
	    this.testCaseCount = -1;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Called before any tests have been run. 
	 * Essentially delegates to 
	 * {@link GUIRunner#testRunStarted(Description)}. 
	 *
	 * @param desc 
	 *    describes the tests to be run
	 */
	// api-docs inherited from class RunListener
	public void testRunStarted(final Description desc) {
	    System.out.println("testRunStarted("+desc2string(desc));
	    try {
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.runner.testRunStarted(desc);
		All.this.testCaseCount = 0;
	    // 	}
	    // }
	    // );

		System.out.println("...testRunStarted(");
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	/**
	 * Called when all tests have finished. 
	 * Prints a statistics to the standard output, 
	 * a summary to the status bar of the GUI 
	 *
	 * @param result 
	 *    the summary of the test run, including all the tests that failed
	 */
	// api-docs inherited from class RunListener
	public void testRunFinished(final Result result) {
	    try {
		System.out.println("testRunFinished(..."+result);
		System.out.println("Statistics: ");
		System.out.println("runs:         "+result.getRunCount());
		System.out.println("ignored:      "+result.getIgnoreCount());
		System.out.println("failures:     "+result.getFailureCount());
		System.out.println("time elapsed: "+result.getRunTime());
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.runner.setStatus("testRunFinished( required: " + 
					  result.getRunTime() + " ms. ");
		All.this.actions.setState(false);
	    // 	}
	    // }
	    // );

		System.out.println("...testRunFinished(");
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	/**
	 * Called when an atomic test is about to be started. 
	 * @param desc 
	 *    the description of the test that is about to be run 
	 *    (generally a class and method name)
	 */
	// api-docs inherited from class RunListener
	public void testStarted(final Description desc) 
	    throws Exception {// NOPMD
	    try {
		System.out.println("testStarted(..."+desc);
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.testCase = new TestCase(desc,
						 All.this.testCaseCount++);
		All.this.runner.noteTestStartedI(All.this.testCase);
	    // 	}
	    // }
	    // );

		System.out.println("...testStarted(");
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	/**
	 * Called when an atomic test has finished, 
	 * whether the test succeeds or fails.
	 * @param desc the description of the test that just ran
	 */
	public void testFinished(final Description desc) 
	    throws Exception {// NOPMD
	    System.out.println("testFinished("+desc);
	    assert All.this.testCase.getDesc() == desc;
	    try {
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.testCase.setFinished();
		All.this.runner.noteReportResult(All.this.testCase);
	    // 	}
	    // }
	    // );

		//this.currDesc = description;
		//assert this.currDesc == description;

		System.out.println("...testFinished(");	
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	/** 
	 * Called when an atomic test fails.
	 * @param failure 
	 *    describes the test that failed and the exception that was thrown
	 */
	public void testFailure(final Failure failure) 
	    throws Exception {// NOPMD
	    try {
System.out.println("testFailure("+failure);
System.out.println("msg is null("+failure.getException().getMessage() == null);
System.out.println("msg is 'null'("+
		   "null".equals(failure.getException().getMessage()));
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.runner.setStatus("testFailure: "
					  +failure.getException());
		All.this.testCase.setFailure(failure);
	    // 	}
	    // }
	    // );

		System.out.println("...testFailure(");
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	/**
	 * Called when a test will not be run, 
	 * generally because a test method is annotated 
	 * with <code>@Ignored</code>. 
	 * This implies 
	 * that neither {@link #testStarted} nor {@link #testFinished} 
	 * are invoked. 
	 *
	 * @param desc describes the test that will not be run
	 */
	public void testIgnored(final Description desc) 
	    throws Exception {// NOPMD
	    try {
		System.out.println("testIgnored("+desc);
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.testCase = new TestCase(desc,
						 All.this.testCaseCount++);
		All.this.testCase.setIgnored();
		All.this.runner.noteTestStartedI(All.this.testCase);
		All.this.runner.noteReportResult(All.this.testCase);
	    // 	}
	    // }
	    // );

		System.out.println("...testIgnored(");
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	// homemade extension 
	public void testRunAborted() {
	    System.out.println("testRunAborted(");
	    try {
// 	SwingUtilities.invokeLater(
// 	    new Runnable() {
// 		public void run() {

		All.this.runner.setStatus("testRunAborted(");
// 		}
// 	    }
// 	    );

		System.out.println("...testRunAborted(");
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

    } // class All 


    /**
     * The listener if a single test case is run. 
     * **** maybe good extension: run a suite **** 
     */
    static class Singular extends GUIRunListener {

	//private GUIRunner runner;
	//private Description fullDesc;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	Singular(Actions actions) {
	    super(actions);
	    //this.fullDesc = fullDesc;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Called before any tests have been run.
	 * @param desc describes the tests to be run
	 */
	public void testRunStarted(Description desc) {
	    System.out.println("S testRunStarted("+desc2string(desc));
//assert desc.isTest();//****
	    Singular.this.testCase = Singular.this.actions.getSingleTest();//NOPMD
	    //assert this.testCase.getDesc().equals(desc);

// 	this.runner.setStatus("single testRunStarted(");
// 	this.runner.startSingular(desc);

	}

	/**
	 * Called when all tests have finished. 
	 * Prints a statistics to the standard output, 
	 * a summary to the status bar of the GUI 
	 *
	 * @param result 
	 *    the summary of the test run, including all the tests that failed
	 */
	// api-docs inherited from class RunListener
	public void testRunFinished(final Result result) throws Exception {// NOPMD
	    System.out.println("S testRunFinished(..."+result);
	    System.out.println("Statistics: ");
	    System.out.println("runs:         "+result.getRunCount());
	    System.out.println("ignored:      "+result.getIgnoreCount());
	    System.out.println("failures:     "+result.getFailureCount());
	    System.out.println("time elapsed: "+result.getRunTime());
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {
	    Singular.this.runner.setStatus("testRunFinished( required: " + 
					   result.getRunTime() + " ms. ");
	    Singular.this.actions.setState(false);// not running 
	    // }
	    // }
	    // );
	}

	/**
	 * Called when an atomic test is about to be started. 
	 * @param desc 
	 *    the description of the test that is about to be run 
	 *    (generally a class and method name)
	 */
	// api-docs inherited from class RunListener
	public void testStarted(final Description desc) 
	    throws Exception {// NOPMD
	    System.out.println("S testStarted(..."+desc);
	    Singular.this.testCase.setRetried();
	    Singular.this.runner.updateSingularStarted();
//this.testCase = new TestCase(description);
	}


	/**
	 * Called when an atomic test has finished, 
	 * whether the test succeeds or fails. 
	 * @param desc the description of the test that just ran
	 */
	public void testFinished(final Description desc) 
	    throws Exception {// NOPMD
	    System.out.println("S testFinished("+desc);
// try {
	    Singular.this.testCase.setFinished();
	    Singular.this.runner
		.updateSingularFinished(Singular.this.testCase);
// } catch (Throwable e) {
//     e.printStackTrace();
// }

	    System.out.println("...S testFinished("+desc);
	}

	/** 
	 * Called when an atomic test fails.
	 * @param failure 
	 *    describes the test that failed and the exception that was thrown
	 */
	public void testFailure(final Failure failure) 
	    throws Exception {// NOPMD
	    System.out.println("S testFailure("+failure);
//this.runner.setStatus("testFailure: "+failure.getException());
	    Singular.this.testCase.setFailure2(failure);
	}

	/**
	 * Called when a test will not be run, 
	 * generally because a test method is annotated 
	 * with <code>@Ignored</code>. 
	 * This implies 
	 * that neither {@link #testStarted} nor {@link #testFinished} 
	 * are invoked. 
	 *
	 * @param desc describes the test that will not be run
	 */
	public void testIgnored(final Description desc) 
	    throws Exception {// NOPMD
	    System.out.println("S testIgnored("+desc);
	    Singular.this.testCase.setIgnored2();
//this.testCase.setFinished();
	    Singular.this.runner.updateSingularFinished(this.testCase);
	}

	// homemade extension 
	public void testRunAborted() {
	    System.out.println("S testRunAborted(");
	}
    } // class Singular 

}

