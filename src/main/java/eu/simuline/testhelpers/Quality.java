package eu.simuline.testhelpers;

import eu.simuline.sun.gtk.File;

import eu.simuline.junit.Ok;
import eu.simuline.junit.New;
import eu.simuline.junit.Ignored;
import eu.simuline.junit.AssumptionFailure;
import eu.simuline.junit.Failure;
import eu.simuline.junit.Error;

import eu.simuline.util.GifResource;

import junit.framework.AssertionFailedError;

import java.awt.Color;

import javax.swing.ImageIcon;

import org.junit.runner.notification.RunListener;// for javadoc only 


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
 *                                     |testRunStarted
 *                                     |
 *                          {@link #Scheduled}
 *                                    /|
 *                                   / |
 * {@link RunListener
 *#testIgnored(Description)}          /  |{@link RunListener#testStarted(Description)}
 *                                 /   |
 *                                /    |
 *                 {@link #Ignored}    |
 *                                     |
 *                            {@link #Started}
 *                                    /|\
 * {@link RunListener#testFinished(Description)}/ | \{@link RunListener#testAssumptionFailure(Failure)}
 *                                  /  |  \
 *                                 /   |   \ 
 *        ________________________/    |    \_________
 *       /                             |              \ 
 *      / {@link RunListener#testFailure(Failure)}|               \ 
 *     /                              /\                \
 *    /                              /  \                \ 
 *   /                              /    \                \
 * {@link #Success}   {@link #Failure} {@link #Error} {@link #Invalidated}. 
 * </pre>
 *
 * Accordingly, 
 * there are methods <code>setXXX</code> for transition to the next state: 
 * {@link #setFinished()}, {@link #setAssumptionFailure()} and 
 * {@link #setFailure(org.junit.runner.notification.Failure)}. 
 * 
 * <p>
 * For each of these phases, there are methods to represent the quality 
 * or further aspects of a testcase graphically: Each Quality defines 
 * <ul>
 * <li>a separate icon given by {@link #getIcon()}, 
 * <li>a status message given by {@link #status()} 
 * <li>a color {@link #getColor()} influencing the color of the progress bar 
 * <li>a method {@link #time(long)} to compute the time required for a test run.
 * </ul>
 *
 * Finally, there are further methods to ask for certain properties 
 * as {@link #isIrregular()}, {@link #isIgnored()} and {@link #allowsRerun()}. 
 *
 * Created: Wed Jun 12 16:41:14 2006
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
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
	    return GifResource.getIcon(File.class);
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase finished before started. ");
	}
	// **** seems strange and contradicts introduction **** 
	Quality setIgnored() {
	    throw new IllegalStateException
		("Found testcase ignored before started. ");// shall be ok. 
	}
	String status() {
	    throw new UnsupportedOperationException();
	}
	long time(long time) {
	    return -1;
	}
	boolean allowsRerun() {
	    return false;
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
	    return GifResource.getIcon(New.class);
	}
	Quality setFinished() {
	    return Success;
	}
	Quality setIgnored() {
	    return Ignored;
	}
	String status() {
	    return "started";
	}
	long time(long time) {
	    return System.currentTimeMillis();
	}
	boolean allowsRerun() {
	    return false;
	}
	Quality setAssumptionFailure() {
	    return Invalidated;
	}
	// **** should be based on AssertionFailedError only, 
	// but junit does not admit this. 
 	Quality setFailure(org.junit.runner.notification.Failure failure) {
	    Throwable thrw = failure.getException();
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
	    return GifResource.getIcon(Ok.class);
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase successful before finished. ");
	}
	String status() {
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
	Quality setFinished() {
	    return this;
	}
	String status() {
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
	    return GifResource.getIcon(Ignored.class);
	}
	long time(long time) {
	    return 0;
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase finished and ignored. ");
	}
	String status() {
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
	    return GifResource.getIcon(Failure.class);
	}
	String status() {
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
	    return GifResource.getIcon(Error.class);
	}
	String status() {
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
    int level;

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
     * methods.                                                             *
     * -------------------------------------------------------------------- */

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
     * @return
     *    {@link #Failure} or {@link #Error} for {@link #Started} 
     *    depending on whether the thrown throwable 
     *    is an {@link AssertionFailedError}. 
     * @throws IllegalStateException 
     *    for all but {@link #Started}. 
     */
    Quality setFailure(org.junit.runner.notification.Failure failure) {
	throw new IllegalStateException
	    ("Found testcase with failure which is not started. ");
    }


    /**
     * Returns an icon representing this phase on the GUI. 
     */
    abstract ImageIcon getIcon();

    /**
     * Returns a status message which describes this phase 
     * or throws an exception. 
     *
     * @throws UnsupportedOperationException
     *    if applied for {@link #Scheduled}. 
     */
    abstract String status();

    /**
     * Determines a Quality with the maximum level 
     * of <code>this</code> and <code>other</code>. 
     * In conjunction with {@link #getColor()}, 
     * this is used to determine the color of the progress bar in a JUnit GUI 
     *
     * @see GUIRunner.TestProgressBar#incNumRunsDone(TestCase)
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
     * Returns the difference of the current time in miliseconds 
     *
     * @return
     *    <ul>
     *    <li><code>-1</code> for {@link #Scheduled}. 
     *    <li><code>-0</code> for {@link #Ignored}. 
     *    <li>The current time {@link System#currentTimeMillis()} 
     *        for {@link #Started}. 
     *    <li>the span of time the underlying testcase took 
     *        from start to finish. 
     *    </ul>
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
     */
    long time(long time) {
	return System.currentTimeMillis() - time;
    }



    /**
     * Whether the run of the underlying testcase stopped irregularly. 
     * This is the case only for {@link #Failure} and {@link #Error}. 
     */
    boolean isIrregular() {
	return this.level == 2;
    }

    /**
     * Returns whether the underlying testcase is ignored. 
     * This is <code>false</code> 
     * except for {@link #Ignored} and {@link #Invalidated}. 
     */
    boolean isIgnored() {
	return this.level == 1;
    }

   /**
     * Returns whether the underlying testcase is 
     * neither finished unsuccessfully nor ignored. 
     * This is <code>false</code> 
     * except for {@link #Scheduled}, {@link #Started} and {@link #Success}. 
     */
    boolean isNeutral() {
	return this.level == 0;
    }

    /**
     * Returns whether the underlying tescase allows to be rerun. 
     * This is <code>false</code> 
     * for {@link #Scheduled} and {@link #Started} only. 
     * **** for Scheduled, this is not ideal. 
     */
    boolean allowsRerun() {
	return true;
    }

} // enum Quality 

