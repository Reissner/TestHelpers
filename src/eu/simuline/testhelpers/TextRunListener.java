package eu.simuline.testhelpers;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.Failure;

import org.junit.runner.Description;
import org.junit.runner.Result;

/**
 * A simple RunListener which notifies of the events while running tests 
 * by text output. 
 *
 * Created: Wed Jun  7 16:41:14 2006
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
public class TextRunListener extends RunListener {

    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    /**
     * Creates a new <code>TextRunListener</code> instance.
     *
     */
    public TextRunListener() {
	// is empty. 
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Called before any tests have been run.
     * @param description describes the tests to be run
     */
    public void testRunStarted(Description description) 
	throws Exception {// NOPMD
System.out.println("T testRunStarted(...");
    }

    /**
     * Called when all tests have finished
     * @param result the summary of the test run, 
     * including all the tests that failed
     */
    public void testRunFinished(Result result) 
	throws Exception {// NOPMD
System.out.println("T testRunFinished(...");
    }

    /**
     * Called when an atomic test is about to be started.
     * @param description 
     *    the description of the test that is about to be run 
     *    (generally a class and method name)
     */
    public void testStarted(Description description) 
	throws Exception {// NOPMD
System.out.println("T testStarted(..."+description);
    }

    /**
     * Called when an atomic test has finished, 
     * whether the test succeeds or fails.
     * @param description the description of the test that just ran
     */
    public void testFinished(Description description) 
	throws Exception {// NOPMD
System.out.println("T testFinished("+description);	
    }

    /** 
     * Called when an atomic test fails.
     * @param failure 
     *    describes the test that failed and the exception that was thrown
     */
    public void testFailure(Failure failure) 
	throws Exception {// NOPMD
System.out.println("testFailure("+failure.getException());

    }

    /**
     * Called when a test will not be run, 
     * generally because a test method is annotated 
     * with <code>@Ignored</code>. 
     * This implies 
     * that neither {@link #testStarted} nor {@link #testFinished} 
     * are invoked. 
     *
     * @param description describes the test that will not be run
     */
    public void testIgnored(Description description) 
	throws Exception {// NOPMD
System.out.println("T testIgnored("+description);
    }
}
