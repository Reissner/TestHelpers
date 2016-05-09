package eu.simuline.testhelpers;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.Failure;
import org.junit.runner.Description;
import org.junit.runner.Result;

import java.util.Iterator;

//import javax.swing.SwingUtilities;// wrong place

/**
 * A {@link RunListener} which notifies the GUI {@link GUIRunner} 
 * of the events occuring while running a testsuite 
 * and which comprises a textual run listener. 
 *
 * Created: Sat Jun  3 17:17:23 2006
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
public class GUIRunListener extends TextRunListener {


    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    protected TestCase testCase;

    protected final Actions actions;

    protected final GUIRunner runner;


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
	 * Updates the enablement of the GUI-Actions 
	 * and then delegates to 
	 * {@link GUIRunner#testRunStarted(Description)}. 
	 *
	 * @param desc 
	 *    describes the tests to be run
	 */
	public void testRunStarted(final Description desc) {
	    // output text 
	    super.testRunStarted(desc);
	    All.this.actions.setEnableForRun(true);// running 

	    try {
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.runner.testRunStarted(desc);
		All.this.testCaseCount = 0;
	    // 	}
	    // }
	    // );
	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	/**
	 * Called when all tests have finished. 
	 * Prints a statistics on the result to the standard output, 
	 * and a summary to the status bar of the GUI 
	 * and updates the enablement of the GUI-Actions. 
	 *
	 * @param result 
	 *    the summary of the test run, including all the tests that failed
	 */
	public void testRunFinished(final Result result) {
	    // output text 
	    super.testRunFinished(result);

	    try {
		// SwingUtilities.invokeLater(
		//     new Runnable() {
		// 	public void run() {

		All.this.runner.setStatus("testRunFinished( required: " + 
					  result.getRunTime() + " ms. ");
		All.this.actions.setEnableForRun(false);// not running 
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
	 *
	 * @param desc 
	 *    the description of the test that is about to be run 
	 *    (generally a class and method name)
	 */
	public void testStarted(final Description desc) {
	    super.testStarted(desc);

	    try {
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
	 *
	 * @param desc
	 *    the description of the test that just ran
	 */
	public void testFinished(final Description desc) {
	    super.testFinished(desc);
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

	    } catch (Throwable e) {// NOPMD
		e.printStackTrace();
	    }
	}

	/** 
	 * Called when an atomic test fails. 
	 *
	 * @param failure 
	 *    describes the test that failed and the exception that was thrown
	 */
	public void testFailure(final Failure failure) {
	    super.testFailure(failure);

	    try {
	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {

		All.this.runner.setStatus("testFailure: "
					  +failure.getException());
		All.this.testCase.setFailure(failure);
	    // 	}
	    // }
	    // );

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
	 * @param desc 
	 *    describes the test that will not be run
	 */
	public void testIgnored(final Description desc) {
	    super.testIgnored(desc);

	    try {
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
	 * Updates the enablement of the GUI-Actions 
	 * and ***** 
	 *
	 * @param desc 
	 *    describes the tests to be run
	 */
	public void testRunStarted(final Description desc) {
	    // output text 
	    super.testRunStarted(desc);
	    Singular.this.actions.setEnableForRun(true);// running 

	    Singular.this.testCase = Singular.this.actions.getSingleTest();//NOPMD
	    //assert this.testCase.getDesc().equals(desc);

// 	this.runner.setStatus("single testRunStarted(");
// 	this.runner.startSingular(desc);

	}

	/**
	 * Called when all tests have finished. 
	 * Prints a statistics on the result to the standard output, 
	 * a summary to the status bar of the GUI 
	 * and updates the enablement of the GUI-Actions. 
	 *
	 * @param result 
	 *    the summary of the test run, including all the tests that failed
	 */
	// api-docs inherited from class RunListener
	public void testRunFinished(final Result result) {
	    // output text 
	    super.testRunFinished(result);

	// SwingUtilities.invokeLater(
	//     new Runnable() {
	// 	public void run() {
	    Singular.this.runner.setStatus("testRunFinished( required: " + 
					   result.getRunTime() + " ms. ");
	    Singular.this.actions.setEnableForRun(false);// not running 
	    // }
	    // }
	    // );
	}

	/**
	 * Called when an atomic test is about to be started. 
	 *
	 * @param desc 
	 *    the description of the test that is about to be run 
	 *    (generally a class and method name)
	 */
	// api-docs inherited from class RunListener
	public void testStarted(final Description desc) {
	    super.testStarted(desc);

	    Singular.this.testCase.setRetried();
	    Singular.this.runner.updateSingularStarted();
//this.testCase = new TestCase(description);
	}


	/**
	 * Called when an atomic test has finished, 
	 * whether the test succeeds or fails. 
	 *
	 * @param desc
	 *    the description of the test that just ran
	 */
	public void testFinished(final Description desc) {
	    super.testFinished(desc);


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
	public void testFailure(final Failure failure) {
	    super.testFailure(failure);

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
	 * @param desc 
	 *    describes the test that will not be run
	 */
	public void testIgnored(final Description desc) {
	    super.testIgnored(desc);

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

