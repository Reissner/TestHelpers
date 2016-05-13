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

    boolean isSingular;

    // -1 before testRunStarted and for Singular. 
    protected int testCaseCount;


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

    abstract int testCaseCount();

    boolean isSingular() {
	return this.isSingular;
    }

 

    /**
     * Called before any tests have been run.
     * Updates the enablement of the GUI-Actions 
     * and then delegates to 
     * {@link GUIRunner#testRunStarted(Description)}. 
     * Updates the enablement of the GUI-Actions 
     * and ***** OVERWRITTEN FOR ALL
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
		    GUIRunListener.this.actions.setEnableForRun(true);//running 
		    if (isSingular()) {
			GUIRunListener.this.testCase = 
			    GUIRunListener.this.actions.getSingleTest();//NOPMD
		    } else {
			GUIRunListener.this.guiRunner.testRunStarted(desc);
	     GUIRunListener.this.testCaseCount = 0;
		    }

		    //assert this.testCase.getDesc().equals(desc);
		    
		    // 	this.runner.setStatus("single testRunStarted(");
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
	// if (!isSingular()) {
	//     GUIRunListener.this.testCaseCount = 0;
	// }
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
		    GUIRunListener.this.testCase = 
			new TestCase(desc,Quality.Started, testCaseCount());
		    if (isSingular()) {
			GUIRunListener.this.guiRunner.updateSingularStarted();
		    } else {
			GUIRunListener.this.guiRunner
			    .noteTestStartedI(GUIRunListener.this.testCase);
		    }
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
	assert GUIRunListener.this.testCase.getDesc() == desc;

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.testCase.setFinished();
		    if (isSingular()) {
			GUIRunListener.this.guiRunner
			    .updateSingularFinished(GUIRunListener.this.testCase);

		    } else {
			GUIRunListener.this.guiRunner
			    .noteReportResult(GUIRunListener.this.testCase);
		    }
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

 

    /* -------------------------------------------------------------------- *
     * inner classes and enums.                                             *
     * -------------------------------------------------------------------- */

    /**
     * The listener if all test cases are run. 
     * **** maybe good extension: run a suite **** 
     */
    static class All extends GUIRunListener {

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	All(Actions actions) {
	    super(actions);
	    this.isSingular = false;
	    this.testCaseCount = -1;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */



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
	    this.isSingular = true;
	    //this.fullDesc = fullDesc;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	int testCaseCount() {
	    return -1;
	}

    } // class Singular 

}

