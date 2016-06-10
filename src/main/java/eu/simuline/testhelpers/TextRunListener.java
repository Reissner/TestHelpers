package eu.simuline.testhelpers;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.Failure;

import org.junit.runner.Description;
import org.junit.runner.Result;

import java.util.Iterator;
import org.junit.AssumptionViolatedException;

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
     * Returns a string representation of <code>desc</code>: 
     * For atomic tests the display name, 
     * for suites an xml-style description. 
     */
    private static String desc2string(Description desc) {
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


    /**
     * Called before any tests 
     * of a suite described by <code>desc</code> have been run. 
     * This may be called on an arbitrary thread.
     *
     * @param desc
     *    describes the suite of tests to be run. 
     */
    // api-docs inherited from class RunListener
    public void testRunStarted(Description desc) throws Exception {//NOPMD
	System.out.println("T testRunStarted(..." + desc2string(desc));
    }

    /**
     * Called when all tests of the suite 
     * announced by {@link #testRunStarted(Description)} have finished. 
     * This may be called on an arbitrary thread. 
     * <p>
     * Prints a statistics on the result of the test suite 
     * to the standard output. 
     *
     * @param result 
     *    the summary of the outcoming of the suite of tests run, 
     *    including all the tests that failed
      */
    // api-docs inherited from class RunListener
    public void testRunFinished(Result result) throws Exception {//NOPMD
	System.out.println("T testRunFinished(..." + result);
	System.out.println("Statistics: ");
	System.out.println("runs:         " + result.getRunCount());
	System.out.println("ignored:      " + result.getIgnoreCount());
	System.out.println("failures:     " + result.getFailureCount());
	System.out.println("time elapsed: " + result.getRunTime() + "ms");
	// **** strange: 
	// runs seem without ignores 
	// failures seem all runs which did not succeed, 
	// i.e. test could not be executed or failed. 
	// **** only in singular tests with GUI: 
	// ignored seem to be also failures
    }

    /**
     * Called when a test suite is about to be started. 
     * If this method is called for a given {@link Description}, 
     * then {@link #testSuiteFinished(Description)} 
     * will also be called for the same {@code Description}. 
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
	System.out.println("S testSuiteStarted(...   " + desc);
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
	System.out.println("S testSuiteFinished(...  " + desc);
    }

    /**
     * Called when an atomic test is about to be started. 
     * An ignored test is never started. 
     *
     * @param desc
     *    the description of the test that is about to be run 
     *    (generally a class and method name)
     * @see #testIgnored(Description)
     */
    // api-docs inherited from class RunListener
    public void testStarted(Description desc) throws Exception {//NOPMD
	System.out.println("T testStarted(...       " + desc);
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
    // api-docs inherited from class RunListener
    public void testFinished(Description desc) throws Exception {//NOPMD
	System.out.println("T testFinished(         " + desc);
    }

    /** 
     * Called when an atomic test fails to execute properly 
     * throwing a Throwable, or when a listener throws an exception. 
     * <p>
     * In the case of a failure of an atomic test, 
     * this method is invoked after {@link #testStarted(Description)} 
     * and before {@link #testFinished(Description)}
     * with the according description of <code>failure</code> 
     * from the same thread that called {@link #testStarted(Description)}. 
     * In case of a failed assumption, instead of this method 
     * {@link #testAssumptionFailure(Failure)} is invoked 
     * for the according test. 
     * <p>
     * In the case of a listener throwing an exception, 
     * this method will be called with the description of <code>failure</code> 
     * given by {@link Description#TEST_MECHANISM}, 
     * and may be called on an arbitrary thread.
     *
     * @param failure 
     *    describes the test that failed and the exception that was thrown 
     *    or indicates that a listener has thrown an exception 
     *    and the according exception. 
     */
    // api-docs inherited from class RunListener
    public void testFailure(Failure failure) throws Exception {//NOPMD
	// description and exception 
	if (failure.getDescription().equals(Description.TEST_MECHANISM)) {
	    System.out.println("FRAMEWORK testFailure(          " + failure);
	}

	System.out.println("T testFailure(          " + failure);
    }

    /**
     * Called when an atomic test flags 
     * that it assumes a condition that is false. 
     * This is treated as ignored with the description of the failure. 
     * This method is invoked after {@link #testStarted(Description)} 
     * and before {@link #testFinished(Description)}
     * with the according description of <code>failure</code>. 
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
    public void testAssumptionFailure(Failure failure) {
	// description and exception 
	assert failure.getException() instanceof AssumptionViolatedException;
	System.out.println("T testAssumptionFailure(" + failure);
    }


    /**
     * Called when a test will not be run, 
     * generally because a test method is annotated 
     * with {@link org.junit.Ignore}. 
     * This implies 
     * that neither {@link #testStarted(Description)} 
     * nor {@link #testFinished(Description)} are invoked 
     * for the according test. 
     * This in turn implies that neither {@link #testFailure(Failure)} 
     * nor {@link #testAssumptionFailure(Failure)} are invoked. 
     *
     * @param desc 
     *    describes the test that will not be run
     */
    // api-docs inherited from class RunListener
    public void testIgnored(Description desc) throws Exception {//NOPMD
	System.out.println("T testIgnored(          " + desc);
    }
}
