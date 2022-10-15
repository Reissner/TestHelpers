package eu.simuline.testhelpers;

import eu.simuline.util.Benchmarker;

import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier; // ; for javadoc only

import junit.framework.AssertionFailedError; // NOPMD

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a test which may be a singular case or a suite 
 * during its lifetime 
 * from being scheduled to having runned successfully or not. 
 * In this respect, the testcase goes beyond a {@link Description}. 
 * It is a wrapper around a {@link Description} {@link #desc} 
 * but includes also the phase given by the quality {@link #qual}. 
 * If the test is a suite, the sub-tests are stored in {@link #children}. 
 * Each test has an index {@link #idxTest} which identifies the test. 
 * <p>
 * Singular tests may be either {@link Quality#Scheduled}, 
 * {@link Quality#Started} or finished in some sense. 
 * If finished, the test may be {@link Quality#Success} 
 * or it has been interrupted by an exception wrapped in a {@link #failure}. 
 * Also a timestamp {@link #timeMs} is provided 
 * which indicates the running time of a singular test 
 * if it has been started and finished. 
 * <p>
 * This class provides a single non-private constructor 
 * transforming a description of a test 
 * which may be singular or a suite into a hierarchy of {@link TestCase}s. 
 * <p>
 * The methods are either 
 * <ul>
 * <li>
 * getter methods and their convenience methods like 
 * {@link #getDesc()}, {@link #isTest()}, {@link #testCount()}, 
 * {@link #getChildren()}, {@link #getIdx()}, {@link #getQuality()}, 
 * {@link #hasFailed()} and {@link #getThrown()}. 
 * <li>
 * The special method {@link #fullSuccess()} 
 * indicating whether this testcase including all testcases succeeded. 
 * <li>
 * methods triggering phase transitions like 
 * {@link #setScheduledRec()}, {@link #setQualStartedIgnored(Quality)}, 
 * {@link #setFailure(Failure)}, {@link #setAssumptionFailure(Failure)} 
 * and {@link #setFinished()} tied to events given by {@link RunNotifier}. 
 * </ul>
 * 
 * @see Quality
 *
 * Created: Wed Jun 12 16:41:14 2006
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
class TestCase {

    /* -------------------------------------------------------------------- *
     * fields. *
     * -------------------------------------------------------------------- */

    /**
     * The description of this testcase. 
     * This may be singular or not. 
     */
    private final Description desc;

    /**
     * The list of children if this is a suite according to {@link #isTest()} 
     * or <code>null</code> if this is a (singular) test. 
     */
    private final List<TestCase> children;

    /**
     * The index of this testcase if this is a (singular) test; 
     * otherwise <code>-1</code>. 
     * Each testcase is part of a tree hierarchy 
     * in which the singular tests have indices <code>0...n-1</code> 
     * if there are <code>n</code> singular test. 
     */
    private final int idxTest;

    /**
     * The phase of this testcase. 
     * The phase is <code>null</code> iff this testcase is a suite. 
     * Otherwise, 
     * If this test is singular and newly created, 
     * the quality is {@link Quality#Scheduled}. 
     * State transitions are reigned by {@link Quality}. 
     */
    private Quality qual;

    /**
     * The failure if any; otherwise <code>null</code>. 
     * This is <code>null</code> iff 
     * <ul>
     * <li>
     * this is a test suite according to {@link #isTest()} 
     * and thus {@link #qual} is <code>null</code>. 
     * <li>
     * this is a singular test 
     * (thus the phase {@link #qual} is non-<code>null</code>) 
     * and a failure is associated with the phase 
     * according to {@link Quality#hasFailure()}. 
     * </ul>
     */
    private Failure failure;

    /**
     * If this testcase is singular, i.e. no suite and is is finished, 
     * this is the time elapsed in ms; 
     * else it is not significant. 
     */
    private double timeMs;

    /**
     * If this testcase is singular, i.e. no suite and is is finished, 
     * this is the memory elapsed in MB; 
     * else it is not significant. 
     * The memory allocated is the difference 
     * between memory when starting the test and when finishing. 
     * So this may well be negative. 
     * Memory is determined after the garbage collector is triggered. 
     * Since there is no way to force the VM to perform a complete garbage collection, 
     * this value is may not be very precise. 
     * In an extreme case, the garbage collector is not run at all 
     * and so the result is not very significant. 
     * It does not say anything about the maximum memory needed during the test run, 
     * but it is a kind of sticky memory required. 
     */
    private double memMB;

    /* -------------------------------------------------------------------- *
     * constructor. *
     * -------------------------------------------------------------------- */

    /**
     * Creates a new testcase based on the description <code>desc</code> 
     * with indices <code>0...n-1</code>, 
     * where <code>n</code> is the testcout of <code>desc</code>. 
     * This testcase is the root of a hierarchy of tests 
     * which consists of a single test if <code>desc</code> is singular. 
     * <p>
     * All singular tests have quality {@link Quality#Scheduled}. 
     *
     * @param desc
     *    a description of this testcase. 
     *    This may be singular or a test suite. 
     */
    TestCase(Description desc) {
        this(desc, 0);
    }

    /**
     * Creates a new testcase based on the description <code>desc</code> 
     * with indices <code>idxTest...idxTest+n-1</code>, 
     * where <code>n</code> is the testcout of <code>desc</code>. 
     * This testcase is the root of a hierarchy of tests 
     * which consists of a single test if <code>desc</code> is singular. 
     * <p>
     * All singular tests have quality {@link Quality#Scheduled}. 
     *
     * @param desc
     *    a description of this testcase. 
     *    This may be singular or a test suite. 
     * @param idxTest
     *    the minimal index of the (singular) tests to be created. 
     *    To create the root of 
     */
    private TestCase(Description desc, int idxTest) {
        this.desc = desc;

        if (desc.isTest()) {
            this.children = null;
            this.idxTest = idxTest;
            this.qual = Quality.Scheduled;
            this.failure = null;
            assert this.qual.hasFailure() == (this.failure != null);
        } else {
            this.children = new ArrayList<TestCase>();
            for (Description descChild : desc.getChildren()) {
                this.children.add(new TestCase(descChild, idxTest));
                idxTest += descChild.testCount();
            }
            this.idxTest = -1;
            this.qual = null;
            this.failure = null;
        }
    }


    /* -------------------------------------------------------------------- *
     * methods. *
     * -------------------------------------------------------------------- */


    /**
     * Returns the description of this testcase given by {@link #desc}. 
     * This may be singular or not. 
     *
     * @return
     *    {@link #desc}. 
     */
    Description getDesc() {
        return this.desc;
    }

    /**
     * Returns whether this testcase is a (singular) test. 
     * Otherwise it is a testsuite. 
     * This is a frequently used convenience method.
     *
     * @return
     *    this testcase is a (singular) test. 
     * @see Description#isTest()
     */
    boolean isTest() {
        return this.desc.isTest();
    }

    /**
     * Returns the number of singular test in this testcase. 
     * This is <code>1</code> 
     * iff this is a singular test according to {@link #isTest()}. 
     * This is a convenience method used once. 
     *
     * @return
     *    the number of singular test in this testcase. 
     * @see Description#testCount()
     */
    int testCount() {
        return this.desc.testCount();
    }

    /**
     * Returns the list of children given by {@link #children}. 
     *
     * @return
     *    <ul>
     *    <li><code>null</code>
     *    if this is a test according to {@link #isTest()}. 
     *    <li> the list of children of this suite otherwise. 
     *    </ul>
     */
    List<TestCase> getChildren() {
        return this.children;
    }

    /**
     * Returns the index of this testcase as described in {@link #idxTest}. 
     *
     * @return 
     *    {@link #idxTest}
     */
    int getIdx() {
        return this.idxTest;
    }

    /**
     * Returns the phase of this testcase as described in {@link #qual}. 
     *
     * @return 
     *    {@link #qual}
     */
    Quality getQuality() {
        return this.qual;
    }

    /**
     * Returns whether {@link #getThrown()} returns non-<code>null</code>. 
     * For singular tests, 
     * returns the same value as {@link Quality#hasFailure()} 
     * applied to {@link #qual}. 
     * This is a convenience method. 
     *
     * @return 
     *    whether {@link #getThrown()} returns non-<code>null</code>.
     * @see #getThrown()
     */
    boolean hasFailed() {
        assert isTest();
        assert this.qual.hasFailure() == (getThrown() != null);
        return getThrown() != null;
    }

    /**
     * Returns <code>null</code> if no failure occurred. 
     * It returns non-<code>null</code> iff {@link #qual} is 
     * {@link Quality#Error}, {@link Quality#Failure} or 
     * {@link Quality#Invalidated}. 
     * In particular, it returns <code>null</code> 
     * if {@link #qual} is <code>null</code>. 
     *
     * @return 
     *    the throwable defined by {@link #failure} 
     *    if this is non-<code>null</code>, otherwise the throwable 
     *    defined by {@link Failure#getException()}. 
     * @see Failure#getException()
     */
    Throwable getThrown() {
        assert this.qual.hasFailure() == (this.failure != null);
        return this.failure == null ? null : this.failure.getException();
    }

    // **** nowhere needed. 
    /*
     * If this testcase is a single testcase which has been finished,
     * this is the span of time required to run this test.
     * For single ignored tests, this is <code>0</code>
     * and in the other cases, the value is negative.
     * If this testcase is a suite, the result is <code>{@link
     * #TIME_SUITE}=-3</code>.
     *
     * @return
     * {@link #time}
     * 
     * @see Quality#setTime(long)
     */
    // long getTime() {
    // return this.time;
    // }

    /**
     * Returns whether this test including all sub-tests succeeded. 
     *
     * @return
     *    whether this test including all sub-tests succeeded. 
     */
    boolean fullSuccess() {
        if (isTest()) {
            return getQuality() == Quality.Success;
        }
        assert !isTest();
        for (TestCase child : this.children) {
            if (!child.fullSuccess()) {
                // Here, at least one child did not have full success 
                return false;
            }
        }
        // Here, all children had full success 
        return true;
    }

    /**
     * Recursively 
     * triggers a transition of the current phase to {@link Quality#Scheduled}: 
     * If this is a test, just set {@link #qual} to {@link Quality#Scheduled} 
     * if possible updating {@link #failure} and {@link #timeMs}. 
     * If this is a suite, go recursively into the {@link #children}. 
     *
     * @throws IllegalStateException 
     *    if it is not allowed to set the phase to {@link Quality#Scheduled} 
     *    according to {@link Quality#setScheduled()}. 
     */
    void setScheduledRec() {
        if (this.isTest()) {
            assert this.qual != null;
            this.qual = this.qual.setScheduled();
            if (Benchmarker.isStarted()) {
                Benchmarker.mtoc();
            }
            this.failure = null;
            assert this.qual.hasFailure() == (this.failure != null);
            return;
        }
        // Here, this is a suite 
        assert this.children != null;
        for (TestCase child : this.children) {
            child.setScheduledRec();
        }
    }

    /**
     * Triggers a transition of the current phase to <code>qual</code> 
     * if possible; otherwise throws an exception. 
     *
     * @param qual
     *    either {@link Quality#Started} or {@link Quality#Ignored}. 
     * @throws IllegalStateException
     *    <ul>
     *    <li>
     *    if <code>qual</code> is 
     *    neither {@link Quality#Started} nor {@link Quality#Ignored} 
     *    and in particular if it is <code>null</code> . 
     *    <li>
     *    if {@link #qual} does not allow the transition to <code>qual</code>. 
     *    </ul>
     * @throws NullPointerException
     *    if this is a test suite and thus {@link #qual} is <code>null</code>. 
     */
    void setQualStartedIgnored(Quality qual) {
        // The following assertions are not allowed 
        // because conditions are checked in the code 
        // assert this.qual == Quality.Scheduled;
        // assert qual == Quality.Started || qual == Quality.Ignored;

        // set this.qual or throw an exception. 
        switch (qual) {
            case Started:
                // throws IllegalStateException for this.qual == Started 
                this.qual = this.qual.setStarted();
                Benchmarker.mtic();
                assert Benchmarker.isStarted();
                break;
            case Ignored:
                // throws IllegalStateException for this.qual != Scheduled 
                this.qual = this.qual.setIgnored();
                assert !Benchmarker.isStarted();
                break;
            default:
                throw new IllegalStateException(
                        "Unexpected phase transition to " + qual + ". ");
        }
        // Here, setting qual succeeded 
        assert this.qual == qual;

        this.failure = null;
        assert this.qual.hasFailure() == (this.failure != null);
        //this.time = this.qual.setTime(this.time);
    }

    /**
     * Triggers a transition of the current phase 
     * if the non-assumption failure <code>failure</code> occurs. 
     * This presupposes that this is a singular test 
     * and that the current phase is {@link Quality#Started}. 
     * The new phase is either {@link Quality#Failure} or {@link Quality#Error} 
     * depending on whether the thrown throwable 
     * is an {@link AssertionError} 
     * (should be {@link AssertionFailedError}). 
     *
     * @param failure
     *   a failure representing either a proper failure or an error 
     *   but not an assumption failure. 
     * @throws IllegalStateException
     *    if <code>failure</code> 
     *    represents an {@link AssumptionViolatedException} 
     *    via {@link Failure#getException()}. 
     * @throws IllegalStateException
     *    if <code>failure</code> 
     *    does not represent an {@link AssumptionViolatedException}, 
     *    this is a singular test and 
     *    {@link #qual} does not allow the transition 
     *    given by a failure <code>failure</code>, 
     *    i.e. {@link #qual} is not {@link Quality#Started}. 
     * @throws NullPointerException
     *    if <code>failure</code> 
     *    does not represent an {@link AssumptionViolatedException} and 
     *    this is a test suite and thus {@link #qual} is <code>null</code>. 
     * @see #setAssumptionFailure(Failure)
     * @see Quality#setFailure(Throwable)
     */
    void setFailure(Failure failure) {
        assert failure != null;
        //assert this.desc == this.failure.getDescription();
        this.failure = failure;
        Throwable thrw = failure.getException();
        if (thrw instanceof AssumptionViolatedException) {
            throw new IllegalStateException(
                    "Found unexpected AssumptionViolatedException. ");
        }
        this.qual = this.qual.setFailure(thrw);

        assert this.qual.hasFailure();
        assert this.qual.hasFailure() == (this.failure != null);
        // deferred to setFinished() 
    }

    /**
     * Triggers a transition of the current phase 
     * if the assumption failure <code>failure</code> occurs. 
     * This presupposes that this is a singular test 
     * and that the current phase is {@link Quality#Started}. 
     * The new phase is {@link Quality#Invalidated}. 
     *
     * @param failure
     *   a failure representing a failed assumption. 
     * @throws IllegalStateException
     *    if <code>failure</code> 
     *    does not represent an {@link AssumptionViolatedException} 
     *    via {@link Failure#getException()}. 
     * @throws IllegalStateException
     *    if <code>failure</code> 
     *    represents an {@link AssumptionViolatedException}, 
     *    this is a singlar test and 
     *    {@link #qual} does not allow the transition 
     *    to {@link Quality#Invalidated}. 
     * @throws NullPointerException
     *    if <code>failure</code> 
     *    represents an {@link AssumptionViolatedException} and 
     *    this is a test suite and thus {@link #qual} is <code>null</code>. 
     * @see #setFailure(Failure)
     */
    void setAssumptionFailure(Failure failure) {
        assert failure != null;
        //assert this.desc == this.failure.getDescription();
        if (!(failure.getException() instanceof AssumptionViolatedException)) {
            throw new IllegalStateException(
                    "Expected AssumptionViolatedException but found "
                            + failure.getException() + ". ");
        }
        this.failure = failure;
        this.qual = this.qual.setAssumptionFailure();

        assert this.qual.hasFailure();
        assert this.qual.hasFailure() == (this.failure != null);
        // deferred to setFinished() 
    }

    /**
     * Triggers a transition of the current phase 
     * given by finishing the run of a singular testcase if possible 
     * as specified by {@link Quality#setFinished()}. 
     * The new phase 
     * <ul>
     * <li>remains unchanged for phases {@link Quality#Failure}, 
     * {@link Quality#Invalidated} and {@link Quality#Error}  
     * <li>is {@link Quality#Success} for current phase {@link Quality#Started}. 
     * </ul>
     * Note that this sets {@link #timeMs} 
     * to the time required to run this testcase 
     * and {@link #memMB} to the additional memory consumed. 
     *
     * @throws IllegalStateException
     *    if this is a singular test and 
     *    {@link #qual} does not allow the transition 
     *    by finishing a test case. 
     *    This is the case for states {@link Quality#Scheduled}, 
     *    {@link Quality#Success} and {@link Quality#Ignored}. 
     * @throws NullPointerException
     *    if this is a test suite and thus {@link #qual} is <code>null</code>. 
     * @see #setFailure(Failure)
     */
    void setFinished() {
        assert this.qual.hasFailure() == (this.failure != null);
        // does not change anything if there has been a failure. 
        this.qual = this.qual.setFinished();
        Benchmarker.mtoc();
        assert !Benchmarker.isStarted();
        this.timeMs = Benchmarker.getTimeMs();
        this.memMB = Benchmarker.getMemoryMB();
        assert this.qual.hasFailure() == (this.failure != null);
    }

    /**
     * Returns the string representation of {@link #desc} for suites 
     * and a representation 
     * including {@link #qual} and {@link #timeMs} for singular tests. 
     */
    public String toString() {
        // StringBuilder res = new StringBuilder();
        // if (isTest()) {
        //     res.append("<Test desc='");
        //     res.append(this.desc);
        //     res.append("' idxTest='");
        //     res.append(this.idxTest);
        //     res.append("' qual='");
        //     res.append(this.qual);
        //     res.append("'/>");
        //     // **** rework needed. 
        //     return res.toString();
        // }
        // // Here, this is a suite. 
        // res.append("<Suite>");
        // for (TestCase child : this.children) {
        //     res.append(child.toString());
        // }
        // res.append("</Suite>");


        assert isTest() == (this.qual != null);
        if (!isTest()) {
            return this.desc.toString();
        }
        System.out.println(" time: " + this.timeMs +" mem" + this.memMB);
        String timeStr = this.qual.lifePhase().timeMemString(this.timeMs, this.memMB);
        return this.qual + " " + timeStr + ": " + this.desc.toString();
    }

} // class TestCase 
