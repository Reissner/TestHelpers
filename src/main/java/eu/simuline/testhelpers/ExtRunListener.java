package eu.simuline.testhelpers;

import org.junit.runner.notification.RunListener;

import org.junit.runner.Description;

/**
 * Describe class <code>ExtRunListener</code> here.
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public class ExtRunListener extends RunListener {

    // api-docs inherited from class RunListener 
    // starting with junit 4.13 but this one uses 4.12 
    /**
     * Called when a test suite is about to be started. 
     * If this method is called for a given {@link Description}, 
     * then {@link #testSuiteFinished(Description)} 
     * will also be called for the same {@code Description}. 
     * <p>
     * Note that not all runners will call this method, so runners should 
     * be prepared to handle {@link #testStarted(Description)} calls for tests 
     * where there was no corresponding {@link #testSuiteStarted(Description)} 
     * call for the parent {@link Description}.
     *
     * @param desc
     *    the description of the test suite that is about to be run
     *    (generally a class name)
     * @since 4.13
     */
     public void testSuiteStarted(Description desc) throws Exception { //NOPMD
     }

    // api-docs inherited from class RunListener 
    // starting with junit 4.13 but this one uses 4.12 
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
    public void testSuiteFinished(Description desc) throws Exception { //NOPMD
    }


    // homemade extension 
    /**
     * Invoked for stop and for break originated by the user. 
     */
    // not clear which test has been aborted. 
    public void testRunAborted() {
    }

    // homemade extension 
    /**
     * Invoked if a test class is loaded defining a testsuite 
     * described by <code>desc</code>. 
     */
    public void testClassStructureLoaded(final Description desc) {
    }
}
