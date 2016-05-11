package eu.simuline.testhelpers;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.Failure;
import org.junit.runner.Description;
import org.junit.runner.Result;

import java.util.Iterator;

import javax.swing.SwingUtilities;// wrong place?

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
public abstract class GUIRunListener extends TextRunListener {


    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    protected TestCase testCase;

    protected final Actions actions;

    protected final GUIRunner guiRunner;


    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    public GUIRunListener(Actions actions) {
	this.actions = actions;
	this.guiRunner = this.actions.getRunner();
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */


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
    public void testRunFinished(final Result result) throws Exception {
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testRunFinished(result);

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.guiRunner
		    	.setStatus("testRunFinished(required: " + 
		    		   result.getRunTime() + "ms. ");
		    GUIRunListener.this.actions
			.setEnableForRun(false);// not running 
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
    }

  

    /**
     * Called when an atomic test fails. 
     *
     * @param failure 
     *    describes the test that failed and the exception that was thrown
     */
    public void testFailure(final Failure failure) throws Exception {
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testFailure(failure);

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.guiRunner.setStatus("testFailure: "
						 + failure.getException());
		    GUIRunListener.this.testCase.setFailure(failure);
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
    }


    // homemade extension 
    // invoked for stop and for break 
    // not clear which test has been aborted. 
    public void testRunAborted() {
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	System.out.println("S testRunAborted(");

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.guiRunner.setStatus("testRunAborted(");
		    GUIRunListener.this.actions.setEnableForRun(false);
		}
	    };
	try {
	    SwingUtilities.invokeAndWait(runnable);
	} catch (Exception e) {
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
    public void testIgnored(final Description desc) throws Exception {
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testIgnored(desc);

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.testCase = new TestCase(desc,
						     Quality.Ignored,
						     testCaseCount());
		    GUIRunListener.this.guiRunner
			.noteTestStartedI(GUIRunListener.this.testCase);
		    GUIRunListener.this.guiRunner
			.noteReportResult(GUIRunListener.this.testCase);
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
    }



    abstract int testCaseCount();

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
	public void testRunStarted(final Description desc) throws Exception {
	    assert !SwingUtilities.isEventDispatchThread();
	    // output text 
	    super.testRunStarted(desc);

	    Runnable runnable = new Runnable() {
		    public void run() {
			All.this.actions.setEnableForRun(true);// running 
			All.this.guiRunner.testRunStarted(desc);
		    }
		};
	    SwingUtilities.invokeAndWait(runnable);
	    All.this.testCaseCount = 0;
	}

	/**
	 * Called when an atomic test is about to be started.  
	 *
	 * @param desc 
	 *    the description of the test that is about to be run 
	 *    (generally a class and method name)
	 */
	public void testStarted(final Description desc) throws Exception {
	    assert !SwingUtilities.isEventDispatchThread();
	    // output text 
	    super.testStarted(desc);

	    Runnable runnable = new Runnable() {
		    public void run() {
			All.this.testCase = new TestCase(desc,
							 Quality.Started,
							 testCaseCount());
			All.this.guiRunner.noteTestStartedI(All.this.testCase);
		    }
		};
	    SwingUtilities.invokeAndWait(runnable);
	}

	/**
	 * Called when an atomic test has finished, 
	 * whether the test succeeds or fails. 
	 *
	 * @param desc
	 *    the description of the test that just ran
	 */
	public void testFinished(final Description desc) throws Exception {
	    assert !SwingUtilities.isEventDispatchThread();
	    // output text 
	    super.testFinished(desc);
	    assert All.this.testCase.getDesc() == desc;

	    Runnable runnable = new Runnable() {
		    public void run() {
			All.this.testCase.setFinished();
			All.this.guiRunner.noteReportResult(All.this.testCase);
		    }
		};

	    SwingUtilities.invokeAndWait(runnable);
	}

	// /** 
	//  * Called when an atomic test fails. 
	//  *
	//  * @param failure 
	//  *    describes the test that failed and the exception that was thrown
	//  */
	// public void testFailure(final Failure failure) throws Exception {
	//     assert !SwingUtilities.isEventDispatchThread();
	//     // output text 
	//     super.testFailure(failure);

	//     Runnable runnable = new Runnable() {
	// 	    public void run() {
	// 		All.this.guiRunner.setStatus("testFailure: "
	// 					   + failure.getException());
	// 		All.this.testCase.setFailure(failure);
	// 	    }
	// 	};
	//     SwingUtilities.invokeAndWait(runnable);
	// }

	int testCaseCount() {
	    return All.this.testCaseCount++;
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
	public void testRunStarted(final Description desc) throws Exception {
	    assert !SwingUtilities.isEventDispatchThread();
	    // output text 
	    super.testRunStarted(desc);

	    Runnable runnable = new Runnable() {
		    public void run() {
			Singular.this.actions.setEnableForRun(true);// running 

			Singular.this.testCase = 
			    Singular.this.actions.getSingleTest();//NOPMD
			//assert this.testCase.getDesc().equals(desc);

			// 	this.runner.setStatus("single testRunStarted(");
			// 	this.runner.startSingular(desc);
		    }
		};
	    SwingUtilities.invokeAndWait(runnable);
	}

	/**
	 * Called when an atomic test is about to be started. 
	 *
	 * @param desc 
	 *    the description of the test that is about to be run 
	 *    (generally a class and method name)
	 */
	// api-docs inherited from class RunListener
	public void testStarted(final Description desc) throws Exception {
	    assert !SwingUtilities.isEventDispatchThread();
	    // output text 
	    super.testStarted(desc);


	    Runnable runnable = new Runnable() {
		    public void run() {
			Singular.this.testCase = new TestCase(desc,
							 Quality.Started,
							 testCaseCount());
			// Singular.this.testCase.setRetried();


			//Singular.this.guiRunner.noteTestStartedI(Singular.this.testCase);

			Singular.this.guiRunner.updateSingularStarted();

			//this.testCase = new TestCase(desc);
		    }
		};
	    SwingUtilities.invokeAndWait(runnable);
	}


	/**
	 * Called when an atomic test has finished, 
	 * whether the test succeeds or fails. 
	 *
	 * @param desc
	 *    the description of the test that just ran
	 */
	public void testFinished(final Description desc) throws Exception {
	    assert !SwingUtilities.isEventDispatchThread();
	    // output text 
	    super.testFinished(desc);
	    //assert Singular.this.testCase.getDesc() == desc;

	    Runnable runnable = new Runnable() {
		    public void run() {
			Singular.this.testCase.setFinished();
			Singular.this.guiRunner
			    .updateSingularFinished(Singular.this.testCase);
		    }
		};
	    SwingUtilities.invokeAndWait(runnable);
	}

	// /** 
	//  * Called when an atomic test fails. 
	//  *
	//  * @param failure 
	//  *    describes the test that failed and the exception that was thrown
	//  */
	// public void testFailure(final Failure failure) throws Exception {
	//     assert !SwingUtilities.isEventDispatchThread();
	//     // output text 
	//     super.testFailure(failure);

	//     Runnable runnable = new Runnable() {
	// 	    public void run() {
	// 		Singular.this.guiRunner.setStatus("testFailure: "
	// 					   + failure.getException());
	// 		Singular.this.testCase.setFailure(failure);
	// 	    }
	// 	};
	//     SwingUtilities.invokeAndWait(runnable);
	// }

	int testCaseCount() {
	    return -1;
	}

    } // class Singular 

}

