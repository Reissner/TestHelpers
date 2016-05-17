package eu.simuline.testhelpers;

import org.junit.AssumptionViolatedException;
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
     * The index of this testcase 
     * or <code>-1</code> if not counted (singular test) 
     * or initialization with scheduled tests. 
     */
    private final int testCaseCount;

    /**
     * The phase of this testcase. 
     */
    private Quality qual;

    /**
     * The failure if any; otherwise <code>null</code>. 
     */
    private Failure failure;

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
	this.testCaseCount = testCaseCount;

	assert qual == Quality.Scheduled 
	    || qual == Quality.Started 
	    || qual == Quality.Ignored;
	this.qual = qual;
	//setRetried();
	this.failure = null;
	this.time = this.qual.time(this.time);
     }


    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    Description getDesc() {
    	return this.desc;
    }

    Quality getQuality() {
	return this.qual;
    }

    int getNum() {
	return this.testCaseCount;
    }

    void setFailure(Failure failure) {
	assert failure != null;
	assert this.desc == this.failure.getDescription();

	this.failure = failure;
	this.qual = this.qual.setFailure(failure);
   }

    void setAssumptionFailure(Failure failure) {
	assert failure != null;
	assert this.desc == this.failure.getDescription();
	assert failure.getException() instanceof AssumptionViolatedException;

	this.failure = failure;
	this.qual = this.qual.setAssumptionFailure();
    }

    void setFinished() {
	this.qual = this.qual.setFinished();// does not change anything 
	this.time = this.qual.time(this.time);
    }

    final void setRetried() {
	this.qual = Quality.Started;
	this.failure = null;
	this.time = this.qual.time(this.time);
    }

    boolean isSuccess() {
    	return this.qual == Quality.Success;
    }

    /**
     * Returns whether {@link #getException()} returns non-<code>null</code>. 
     * true also for {@link Quality#Invalidated}. 
     */
    boolean hasFailed() {
	return getException() != null;
    }

    /**
     * Returns <code>null</code> if no failure occurred. 
     * It returns non-<code>null</code> 
     * if {@link #qual} is 
     * {@link Quality#Error}, {@link Quality#Failure} or 
     * {@link Quality#Invalidated}. 
     */
    Throwable getException() {
	return this.failure == null ? null : this.failure.getException();
    }

    public String toString() {
	return this.qual + ": " + this.desc.toString();
    }

} // class TestCase 
