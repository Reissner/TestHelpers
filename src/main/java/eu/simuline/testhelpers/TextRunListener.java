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
     * Called before any tests have been run. 
     *
     * @param desc
     *    describes the tests to be run
     */
    // api-docs inherited from class RunListener
    public void testRunStarted(Description desc) throws Exception {//NOPMD
	System.out.println("T testRunStarted(..." + desc2string(desc));
    }

    /**
     * Called when all tests have finished. 
     * Prints a statistics on the result to the standard output. 
     *
     * @param result 
     *    the summary of the test run, including all the tests that failed
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
	System.out.println("T testStarted(..." + desc);
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
	System.out.println("T testFinished(" + desc);
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
    // api-docs inherited from class RunListener
    public void testFailure(Failure failure) throws Exception {//NOPMD
	// description and exception
	System.out.println("T testFailure(" + failure);
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
    public void testAssumptionFailure(Failure failure) {
	// description and exception 
	assert failure.getException() instanceof AssumptionViolatedException;
	System.out.println("T testAssumptionFailure(" + failure);
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
     *
     * @param desc 
     *    describes the test that will not be run
     */
    // api-docs inherited from class RunListener
    public void testIgnored(Description desc) throws Exception {//NOPMD
	System.out.println("T testIgnored("+desc);
    }
}
