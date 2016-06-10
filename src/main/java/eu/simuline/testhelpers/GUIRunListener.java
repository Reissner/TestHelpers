package eu.simuline.testhelpers;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;// wrong place?

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.Failure;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.AssumptionViolatedException;

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

    private final Actions actions;

    private final GUIRunner guiRunner;

    private TestCase testCase;


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
    public void testRunStarted(final Description desc) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testRunStarted(desc);
	
	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.actions.setEnableForRun(true);//running 
		    GUIRunListener.this.guiRunner.testRunStarted(desc);
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
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
    public void testRunFinished(final Result result) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testRunFinished(result);

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.guiRunner
		    	.setStatus("testRunFinished(required: " + 
		    		   result.getRunTime() + "ms. ");
		    GUIRunListener.this.actions.setEnableForRun(false);//!runnin
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
    }

    /**
     * Called when a test suite is about to be started. 
     * If this method is called for a given {@link Description}, 
     * then {@link #testSuiteFinished(Description)} 
     * will also be called for the same {@link Description}. 
     * <p>
     * Note that not all runners will call this method, so runners should 
     * be prepared to handle {@link #testStarted(Description)} calls for tests 
     * where there was no cooresponding {@link #testSuiteStarted()} call 
     * for the parent {@link Description}. 
     *
     * @param desc
     *    the description of the test suite that is about to be run
     *    (generally a class name)
     * @since 4.13
     */
    // api-docs inherited from class RunListener
    public void testSuiteStarted(Description desc) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testSuiteStarted(desc);
	// **** at the moment no actions on the GUI 
    }

    /**
     * Called when a test suite has finished, 
     * whether the test suite succeeds or fails. 
     * This method will not be called for a given {@link Description} 
     * unless {@link #testSuiteStarted(Description)} was called 
     * for the same {@link Description}. 
     *
     * @param desc 
     *    the description of the test suite that just ran
     * @since 4.13
     */
    public void testSuiteFinished(Description desc) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testSuiteFinished(desc);
	// **** at the moment no actions on the GUI 
     }

    /**
     * Called when an atomic test is about to be started. 
     * An ignored test is never started. 
     *
     * @param desc 
     *    the description of the test that is about to be started 
     *    (generally a class and method name)
     * @see #testIgnored(Description)
     */
    // api-docs inherited from class RunListener
    public void testStarted(final Description desc) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testStarted(desc);
	

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.testCase = 
			GUIRunListener.this.guiRunner
			.noteTestStartedI(Quality.Started);
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
System.out.println("..testStarted");
    }

    /**
     * Called when an atomic test has finished, 
     * whether the test succeeds or fails. 
     * This method must be invoked after a test has been started 
     * which was indicated by {@link #testStarted(Description)} before. 
     * An ignored test is never finished. 
     *
     * @param desc
     *    the description of the test that just ran
     * @see #testIgnored(Description)
     */
    public void testFinished(final Description desc) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testFinished(desc);
	assert GUIRunListener.this.testCase.getDesc() == desc;

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.testCase.setFinished();
		    GUIRunListener.this.guiRunner
			.noteReportResult(GUIRunListener.this.testCase);
		}
	    };

	SwingUtilities.invokeAndWait(runnable);
System.out.println("..testFinished");
    }

    /**
     * Called when an atomic test fails to execute properly 
     * throwing a Throwable. 
     * This method is invoked after {@link #testStarted(Description)} 
     * and before {@link #testFinished(Description)}
     * with the according description. 
     * In case of a failed assumption, instead of this method 
     * {@link #testAssumptionFailure(Failure)} is invoked 
     * for the according test. 
     *
     * @param failure 
     *    describes the test that failed and the exception that was thrown
     */
    public void testFailure(final Failure failure) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testFailure(failure);
	// GUI ignores failures in the test mechanism. 
	// **** this may be inappropriate. 
	if (failure.getDescription().equals(Description.TEST_MECHANISM)) {
	    return;
	}
	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.testCase.setFailure(failure);
		    GUIRunListener.this.guiRunner.setStatus("testFailure: "
						 + failure.getException());
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
System.out.println("..testFailure");
    }


    /**
     * Called when an atomic test flags 
     * that it assumes a condition that is false. 
     * This is treated as ignored with the description of the failure. 
     * This method is invoked after {@link #testStarted(Description)} 
     * and before {@link #testFinished(Description)}
     * with the according description. 
     * A failed assertion does not count as a failure 
     * and so {@link #testFailure(Failure)} is not invoked 
     * for the according test. 
     * <p>
     * CAUTION: Although a failed assertion is like an ignored test, 
     * {@link #testRunFinished(Result)} does not count this as ignored test 
     * but rather than a passed test. 
     *
     * @param failure
     *    describes the test that failed 
     *    and the {@link AssumptionViolatedException} that was thrown. 
     * @see #testIgnored(Description)
     */
    public void testAssumptionFailure(final Failure failure) {
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testAssumptionFailure(failure);

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.testCase.setAssumptionFailure(failure);
		    GUIRunListener.this.guiRunner
			.setStatus("testAssumptionFailure: "
				   + failure.getException());
		}
	    };

	try {
	    SwingUtilities.invokeAndWait(runnable);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} catch (InvocationTargetException e) {
	    e.printStackTrace();
	}
System.out.println("..testAssumptionFailure");
    }

    /**
     * Called when a test will not be run, 
     * generally because a test method is annotated 
     * with <code>@Ignored</code>. 
     * This implies 
     * that neither {@link #testStarted(Description)} 
     * nor {@link #testFinished(Description)} are invoked 
     * for the according test. 
     * This in turn implies that neither {@link #testFailure(Failure)} 
     * nor {@link #testAssumptionFailure(Failure)} are invoked. 
      * are invoked. 
     *
     * @param desc 
     *    describes the test that will not be run
     */
    public void testIgnored(final Description desc) throws Exception {//NOPMD
	assert !SwingUtilities.isEventDispatchThread();
	// output text 
	super.testIgnored(desc);

	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.testCase = 
			GUIRunListener.this.guiRunner
			.noteTestStartedI(Quality.Ignored);
		    GUIRunListener.this.guiRunner
			.noteReportResult(GUIRunListener.this.testCase);
		}
	    };
	SwingUtilities.invokeAndWait(runnable);
System.out.println("...testIgnored");
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
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} catch (InvocationTargetException e) {
	    e.printStackTrace();
	}
    }

    // homemade extension 
    /**
     * Invoked if a test class is loaded defining a testsuite 
     * described by <code>desc</code>. 
     */
    public void testClassStructureLoaded(final Description desc) {
	System.out.println("S testClassStructureLoaded(" + desc);
	Runnable runnable = new Runnable() {
		public void run() {
		    GUIRunListener.this.guiRunner
			.testClassStructureLoaded(desc);
		    //GUIRunListener.this.guiRunner.setStatus("testlassLoaded(");
		    //GUIRunListener.this.actions.setEnableForRun(false);
		}
	    };
	try {
	    SwingUtilities.invokeAndWait(runnable);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} catch (InvocationTargetException e) {
	    e.printStackTrace();
	}
	System.out.println("..testClassStructureLoaded(");
    }
}

