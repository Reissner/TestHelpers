package eu.simuline.testhelpers;

import org.junit.runner.Description;

import org.junit.runner.notification.Failure;

import junit.framework.AssertionFailedError;

/**
 * Represents a test which may be a single case or a suite 
 * during its lifetime 
 * from being scheduled to having runned successfully or not. 
 * In this respect, the testcase goes beyond a {@link Description}. 
 * It is a wrapper around a {@link Description} {@link #desc} 
 * but includes also the phase given by the quality {@link #qual}. 
 * Depending on the phase, ****
 * 
 * @see Quality
 *
 * Created: Wed Jun 12 16:41:14 2006
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
class TestCase {

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    /**
     * The description of this testcase. 
     */
    private final Description desc;

    /**
     * The phase of this testcase. 
     */
    private Quality qual;
    /**
     * 
     */
    private Failure failure;
    /**
     * 
     */
    private final int testCaseCount;

    /**
     * The meaning depends on {@link #qual}: 
     * <ul>
     * <li><code>-1</code> for {@link Quality#Scheduled}, 
     * <li><code>-0</code> for {@link Quality#Ignored}, 
     * <li>the start time for {@link Quality#Started} and 
     * <li>the time the test required to finish (successfully or not) 
     * for all other phases. 
     * <ul>
     */
    private long time;

    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    /**
     * Creates a new testcase based on the description 
     * with the given Quality and a test count. 
     *
     * @param desc
     *    a description of this testcase
     * @param qual
     *    the initial phase. 
     * @param testCaseCount
     *    
     */
    TestCase(Description desc, Quality qual, int testCaseCount) {
	this.desc = desc;
	this.qual = qual;//Quality.Started;
	this.testCaseCount = testCaseCount;
	//setRetried();
	this.failure = null;
	switch (this.qual) {
	case Scheduled:
	    this.time = -1;
	    break;
	case Ignored:
	    this.time = 0;
	    break;
	case Started:
	    this.time = System.currentTimeMillis();
	    break;
	default:
	    this.time = System.currentTimeMillis() - this.time;
	    break;
	}
     }


    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    void setFailure(Failure failure) {
	assert failure != null;
	this.failure = failure;
	assert this.desc == this.failure.getDescription();
	this.qual = isFailure() ? Quality.Failure : Quality.Error;
    }

    void setFinished() {
	this.qual = this.qual.setFinished();
	this.time = System.currentTimeMillis() - this.time;
    }

    void setIgnored() {
	assert this.qual == Quality.Started;
	this.qual = Quality.Ignored;
	this.time = 0;
    }

    final void setRetried() {
	this.qual = Quality.Started;
	this.failure = null;
	this.time = System.currentTimeMillis();
    }

    boolean isSuccess() {
	return this.qual == Quality.Success;
    }

    boolean hasFailed() {
	return getException() != null;
    }

    private boolean isFailure() {
	Throwable thrw = this.failure.getException();
	return thrw instanceof AssertionFailedError
	    || thrw instanceof AssertionError;// **** does not seem reasonable 
    }

    Description getDesc() {
	return this.desc;
    }

    Throwable getException() {
	return this.failure == null ? null : this.failure.getException();
    }

    // **** ignore Ignored
    Quality getQuality() {
	return this.qual;
    }

    int getNum() {
	return this.testCaseCount;
    }

    public String toString() {
	return this.qual + ": " + getDesc().toString();
    }
} // class TestCase 
