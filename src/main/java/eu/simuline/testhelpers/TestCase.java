package eu.simuline.testhelpers;

import org.junit.runner.Description;

import org.junit.runner.notification.Failure;

import junit.framework.AssertionFailedError;

/**
 * Represents a test which may be a single case or a suite 
 * during its lifetime 
 * from being scheduled to having runned successfully or not. 
 * It is a wrapper around a Description {@link #desc} 
 * but includes also the phase given by th quality {@link #qual}. 
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

    private final Description desc;
    private Failure failure;
    private Quality qual;
    private final int testCaseCount;

    // either starttime or time required to finished; 
    //  0 if Ignored. 
    // -1 if Scheduled
    private long time;

    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    // Create Started TestCase 
    TestCase(Description desc, int testCaseCount) {
	this.desc = desc;
	this.testCaseCount = testCaseCount;
	setRetried();
    }

    // Create Scheduled TestCase 
    TestCase(Description desc) {
	this.desc = desc;
	this.qual = Quality.Scheduled;
	this.failure = null;
	this.testCaseCount = -1;
	this.time = -1;
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    void setFailure(Failure failure) {
	this.failure = failure;
	assert this.desc == this.failure.getDescription();
	this.qual = isFailure()? Quality.Failure : Quality.Error;
    }

    void setFailure2(Failure failure) {
	this.failure = failure;
	//assert this.desc == this.failure.getDescription();
	this.qual = isFailure()? Quality.Failure : Quality.Error;
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

    void setIgnored2() {
	//assert this.qual == Quality.Scheduled;
	this.qual = Quality.Ignored;
	this.time = 0;

	this.failure = null;
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
	return 
	    thrw instanceof AssertionFailedError ||
	    thrw instanceof AssertionError;
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
