package eu.simuline.testhelpers;

import eu.simuline.util.images.GifResource;

import eu.simuline.junit.AssumptionFailure;

import junit.framework.AssertionFailedError;

import java.awt.Color;

import javax.swing.ImageIcon;

import org.junit.runner.notification.RunListener; // for javadoc only 
import org.junit.runner.notification.Failure; // for javadoc only 

import org.junit.AssumptionViolatedException;


/**
 * Represents the phases in the life-cycle of a {@link TestCase} 
 * from being {@link #Scheduled} to {@link #Ignored} 
 * or via {@link #Started} to finished 
 * which means either {@link #Invalidated}, {@link #Success}, 
 * {@link #Failure} or even {@link #Error}. 
 * <p>
 * The phase transitions are triggered by the events 
 * notified by a {@link RunListener} as depicted below. 

 * <pre>
 *                              |{@link RunListener#testRunStarted(Description)}
 *                              |
 *                   {@link #Scheduled}
 *                             /|
 *                            / |
 * {@link RunListener
 *#testIgnored(Description)}/   |{@link RunListener#testStarted(Description)}
 *                         /    |
 *                        /     |
 *          {@link #Ignored}    |
 *                              |
 *                    {@link #Started}
 *                             /|\
 * {@link RunListener
 *#testFinished(Description)}/  |{@link RunListener
 *                                        #testAssumptionFailure(Failure)}
 *                         /    |    \
 *                        /     |     \ 
 *        _______________/      |      \__________
 *       /                      |                 \ 
 *      / {@link RunListener#testFailure(Failure)} \ 
 *     /                        /\                  \
 *    /                        /  \                  \ 
 *   /                        /    \                  \
 * {@link #Success} {@link #Failure} {@link #Error} {@link #Invalidated}. 
 * </pre>
 *
 * Accordingly, 
 * there are methods <code>setXXX</code> for transition to the next state: 
 * {@link #setScheduled()}, {@link #setStarted()} prior to testing, 
 * {@link #setIgnored()} if not to be tested and 
 * {@link #setFinished()}, {@link #setAssumptionFailure()} and 
 * {@link #setFailure(Throwable)} 
 * after the test finished. 
 * All these methods prevent invalid phase transitions 
 * throwin an {@link IllegalStateException}. 
 * 
 * <p>
 * For each of these phases, there are methods to represent the quality 
 * or further aspects of a testcase graphically: Each Quality defines 
 * <ul>
 * <li>a separate icon given by {@link #getIcon()}, 
 * <li>a status message given by {@link #getMessage()} 
 * <li>a color {@link #getColor()} influencing the color of the progress bar 
 * <li>a method {@link #setTime(long)} to compute the time 
 * required for a test run.
 * </ul>
 *
 * Finally, there are further methods to ask for certain properties 
 * as {@link #isNeutral()} and {@link #hasFailure()}. 
 *
 * Created: Wed Jun 12 16:41:14 2006
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
enum Quality {

    /* -------------------------------------------------------------------- *
     * constant constructors.                                               *
     * -------------------------------------------------------------------- */


    /**
     * The testcase is scheduled for execution 
     * but execution has not yet been started 
     * nor has it been decided to ignore, i.e. not to execute the testcase. 
     *
     * @see #Started
     * @see #Ignored
     */
    Scheduled(0) {
	ImageIcon getIcon() {
	    return GifResource.getIcon(eu.simuline.sun.gtk.File.class);
	}
	Quality setStarted() {
	    return Started;
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase finished before started. ");
	}
	Quality setIgnored() {
	    return Ignored;
	}
	String getMessage() {
	    return "Scheduled";
	}
	long setTime(long time) {
	    return TestCase.TIME_SCHEDULED;
	}
    },

    /**
     * The execution of the testcase started but did not finish 
     * in any way. 
     * Thus it is neither clear whether the execution succeeds 
     * nor the outcoming of the test. 
     */
    Started(0) {
	ImageIcon getIcon() {
	    return GifResource.getIcon(eu.simuline.junit.New.class);
	}
	Quality setFinished() {
	    return Success;
	}
	Quality setIgnored() {
	    throw new IllegalStateException
		("Found testcase to be ignored while running. ");
	}
	Quality setScheduled() {
	    throw new IllegalStateException
		("Found testcase scheduled before started. ");
	}
	Quality setStarted() {
	    throw new IllegalStateException
		("Found testcase started while running. ");
	}
	String getMessage() {
	    return "started";
	}
	long setTime(long time) {
	    return System.currentTimeMillis();
	}
	Quality setAssumptionFailure() {
	    return Invalidated;
	}
	// **** should be based on AssertionFailedError only, 
	// but junit does not admit this. 
 	Quality setFailure(Throwable thrw) {
	    return thrw instanceof AssertionFailedError 
		|| thrw instanceof AssertionError ? Failure : Error;
	}
    },
    /**
     * The execution of the testcase finished and the test succeeded (passed): 
     * All assertions hold and no throwable has been thrown. 
     */
    Success(0) {
	ImageIcon getIcon() {
	    return GifResource.getIcon(eu.simuline.junit.Ok.class);
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase successful before finished. ");
	}
	String getMessage() {
	    return "succeeded";
	}
    },
    /**
     * The execution of the testcase ran onto an hurt assumption 
     * before maybe running into further exceptions or errors 
     * and is thus invalidated. 
     */
    Invalidated(1) {
	ImageIcon getIcon() {
	    return GifResource.getIcon(AssumptionFailure.class);
	}
	String getMessage() {
	    return "invalidated by failed assumption";
	}
    },
    /**
     * The testcase was ignored, i.e. was scheduled for execution 
     * but then decided not to start execution. 
     * In particular, nothing can be said about the course of the test run. 
     */
    Ignored(1) {
	ImageIcon getIcon() {
	    return GifResource.getIcon(eu.simuline.junit.Ignored.class);
	}
	long setTime(long time) {
	    return 0;
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase finished and ignored. ");
	}
	String getMessage() {
	    return "was ignored";
	}
    }, 
    /**
     * The execution of the testcase finished gracefully 
     * but did not succeed: 
     * At least one assertion is hurt, 
     * indicated by an {@link AssertionFailedError}. 
     * This excludes further throwables. 
     */
    Failure(2) {
	ImageIcon getIcon() {
	    return GifResource.getIcon(eu.simuline.junit.Failure.class);
	}
	String getMessage() {
	    return "failed";
	}
    }, 
    /**
     * The execution of the testcase failed 
     * indicated by finishing with exception or error 
     * other than {@link AssertionFailedError}. 
     * Thus there is no valid test result. 
     */
    Error(2) {
	ImageIcon getIcon() {
	    return GifResource.getIcon(eu.simuline.junit.Error.class);
	}
	String getMessage() {
	    return "had an error";
	}
    };


    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    /**
     * Encoding how good the result of the test is: 
     * <ul>
     * <li><code>2</code> if something went wrong: 
     *     {@link #Error} or {@link #Failure}.  
     * <li><code>1</code> if something was ignored 
     *     {@link #Ignored} or {@link #Invalidated}. 
     * <li><code>0</code> else: 
     *     {@link #Scheduled}, {@link #Started} or {@link #Success}. 
     * </ul>
     * This is used to define the color of the progress bar. 
     *
     * @see #max(Quality)
     */
    private int level;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    /**
     * Creates another Quality with the given level {@link #level}. 
     */
    Quality(int level) {
	this.level = level;
    }

 
    /* -------------------------------------------------------------------- *
     * class constants.                                                     *
     * -------------------------------------------------------------------- */

    /**
     * Represents a failed testcase. 
     */
    private static final Color COLOR_FAIL    = Color.red;

    /**
     * Represents a testcase which did neither failed. 
     */
    private static final Color COLOR_OK      = Color.green;

    /**
     * Represents an ignored testcase. 
     */
    private static final Color COLOR_IGNORED = Color.yellow;

    /* -------------------------------------------------------------------- *
     * methods for phase transitions.                                       *
     * -------------------------------------------------------------------- */

    /**
     * Returns the next phase 
     * when {@link RunListener#testRunStarted(Description)} is invoked. 
     * 
     * @return
     *    {@link #Scheduled} except this is {@link #Started}. 
     *    Even for {@link #Scheduled} itself. 
     * @throws IllegalStateException 
     *    for {@link #Started}. 
     *    **** it is a decision to disallow rescheduling a running testcase. 
     */
    Quality setScheduled() {
	return Scheduled;
    }

    /**
     * Returns the next phase 
     * when {@link RunListener#testStarted(Description)} is invoked. 
     * 
     * @return
     *    {@link #Started} if this is {@link #Scheduled}. 
     * @throws IllegalStateException 
     *    except if this is {@link #Scheduled}
     */
    Quality setStarted() {
	throw new IllegalStateException
	    ("Found testcase started but not scheduled. ");
    }

    /**
     * Returns the next phase 
     * when {@link RunListener#testIgnored(Description)} is invoked. 
     *
     * @return
     *    {@link #Ignored} if this is {@link #Scheduled}. 
     * @throws IllegalStateException 
     *    except for {@link #Scheduled}. 
     */
    Quality setIgnored() {
	throw new IllegalStateException
	    ("Found testcase ignored but not scheduled. ");
    }

    /**
     * Returns the next phase 
     * when {@link RunListener#testFinished(Description)} is invoked. 
     *
     * @return
     *    <ul>
     *    <li><code>this</code> 
     *       for {@link #Failure},  {@link #Invalidated} and {@link #Error}  
     *    <li>{@link #Success} for {@link #Started}. 
     *    </ul>
     * @throws IllegalStateException 
     *    for {@link #Scheduled}, {@link #Success} and {@link #Ignored}. 
     */
    Quality setFinished() {
	return this;
    }

    /**
     * Returns  the next phase 
     * when {@link RunListener#testAssumptionFailure(Failure)} is invoked. 
     *
     * @return
     *    {@link #Invalidated} for {@link #Started}. 
     * @throws IllegalStateException 
     *    for all but {@link #Started}. 
     */
    Quality setAssumptionFailure() {
	throw new IllegalStateException
	    ("Found testcase with assumtion failure which is not started. ");
    }

    /**
     * Returns  the next phase 
     * when {@link RunListener#testFailure(Failure)} is invoked. 
     *
     * @param thrw
     *    a throwable which determines 
     *    whether the returned phase indicates a failure or an error. 
     * @return
     *    {@link #Failure} or {@link #Error} for {@link #Started} 
     *    depending on whether <code>thrw</code> 
     *    is an {@link AssertionError} 
     *    (should be {@link AssertionFailedError}). 
     * @throws IllegalStateException 
     *    for all but {@link #Started}. 
     *    If this is {@link #Started} 
     *    but <code>failure</code> is an {@link AssumptionViolatedException}. 
     */
    Quality setFailure(Throwable thrw) {
	throw new IllegalStateException
	    ("Found testcase with failure which is not started. ");
    }

    /* -------------------------------------------------------------------- *
     * methods for phase representation.                                    *
     * -------------------------------------------------------------------- */

     /**
     * Returns an icon representing this phase on the GUI. 
     *
     * @return
     *    an icon representing this phase on the GUI. 
     */
    abstract ImageIcon getIcon();

    /**
     * Returns a status message which describes this phase. 
     *
     * @return
     *    a status message which describes this phase. 
     */
    abstract String getMessage();

    /**
     * Determines a Quality with the maximum level 
     * of <code>this</code> and <code>other</code>. 
     * In conjunction with {@link #getColor()}, 
     * this is used to determine the color of the progress bar in a JUnit GUI 
     *
     * @return
     *   the Quality with the maximum level 
     *   of <code>this</code> and <code>other</code>. 
     * @see GUIRunner.TestProgressBar#noteReportResult(TestCase)
     */
    Quality max(Quality other) {
	return (this.level > other.level) ? this : other;
    }

    /**
     * Returns the color associated with this phase: 
     * If this phase represents an irregular ending testcase, 
     * {@link #COLOR_FAIL} is returned. 
     * Else if this phase represents an ignored testcase, 
     * {@link #COLOR_IGNORED} is returned. 
     * Else {@link #COLOR_OK} is returned. 
     *
     * @return
     *    the color associated with this phase. 
     * @see #max(Quality)
     */
    Color getColor() {
	switch (this.level) {
	case 0:
	    return COLOR_OK;
	case 1:
	    return COLOR_IGNORED;
	case 2:
	    return COLOR_FAIL;
	default:
	    throw new IllegalStateException
		("Found undefined level " + this.level + ". ");
	}
    }

    /**
     * Returns the difference of the current time in milliseconds. 
     *
     * @param time
     *    some time in milliseconds. 
     *    The details depend on this: 
     *    <ul>
     *    <li>{@link #Scheduled}, {@link #Ignored} and {@link #Started} 
     *        ignore that parameter. 
     *    <li>{@link #Failure},  {@link #Invalidated}, {@link #Error} and 
     *        {@link #Success} interprete <code>time</code> 
     *        as the start time of the underlying testcase. 
     *    </ul>
     * @return
     *    <ul>
     *    <li><code>-1</code> for {@link #Scheduled}. 
     *    <li><code>-0</code> for {@link #Ignored}. 
     *    <li>The current time {@link System#currentTimeMillis()} 
     *        for {@link #Started}. 
     *    <li>the span of time the underlying testcase took 
     *        from start to finish 
     *    </ul>
     */
    long setTime(long time) {
	return System.currentTimeMillis() - time;
    }



    // /**
    //  * Whether the run of the underlying testcase stopped irregularly. 
    //  * This is the case only for {@link #Failure} and {@link #Error}. 
    //  */
    // boolean isIrregular() {
    // 	return this.level == 2;
    // }

    // /**
    //  * Returns whether the underlying testcase is ignored. 
    //  * This is <code>false</code> 
    //  * except for {@link #Ignored} and {@link #Invalidated}. 
    //  */
    // boolean isIgnored() {
    // 	return this.level == 1;
    // }

   /**
     * Returns whether the underlying testcase is 
     * neither finished unsuccessfully nor ignored. 
     * This is <code>false</code> 
     * except for {@link #Scheduled}, {@link #Started} and {@link #Success}. 
     * Neutral qualities do not occur in the statistics 
     * given by {@link GUIRunner.StatisticsTestState}. 
     *
     * @return
     *    whether the underlying testcase is 
     *    neither finished unsuccessfully nor ignored. 
     */
    boolean isNeutral() {
	return this.level == 0;
    }

    /**
     * Returns whether the given state is tied to a throwable. 
     *
     * @return
     *    <code>true</code> iff this is 
     *    {@link #Invalidated}, {@link #Failure} or {@link #Error}. 
     */
    boolean hasFailure() {
	switch (this.level) {
	case 0:
	    return false;
	case 1:
	    return this == Invalidated;
	case 2:
	    return true;
	default:
	    throw new IllegalStateException
		("Found undefined level " + this.level + ". ");
	}
    }

} // enum Quality 

