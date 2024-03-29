package eu.simuline.testhelpers;

import eu.simuline.util.Benchmarker;
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
 * To be more precise {@link Quality} is a combination of {@link Quality.Phase} 
 * and {@link Quality.Deficiency}. 
 * On the other hand Quality is more than those aspects 
 * which becomes apparent becaue {@link Quality#Error} and {@link Quality#Failure} 
 * have the same combination of phase and deficiency 
 * and so  have {@link Quality#Invalidated} and {@link Quality#Ignored}. 
 * <p>
 * The quality-phase transitions are triggered by the events 
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
 * thrown in an {@link IllegalStateException}. 
 * 
 * <p>
 * For each of these phases, there are methods to represent the quality 
 * or further aspects of a testcase graphically: Each Quality defines 
 * <ul>
 * <li>a separate icon given by {@link #getIcon()}, 
 * <li>a status message given by {@link #getMessage()} 
 * <li>a color {@link #getColor()} influencing the color of the progress bar 
 * This refers to the maximum encountered {@link Quality.Deficiency}. 
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
     * constant constructors. *
     * -------------------------------------------------------------------- */


    /**
     * The testcase is scheduled for execution 
     * but execution has not yet been started 
     * nor has it been decided to ignore, i.e. not to execute the testcase. 
     *
     * @see #Started
     * @see #Ignored
     */
    Scheduled(Deficiency.SoFarOk, Phase.Waiting) {
        ImageIcon getIcon() {
            return GifResource.getIcon(eu.simuline.sun.gtk.File.class);
        }

        Quality setStarted() {
            return Started;
        }

        Quality setFinished() {
            throw new IllegalStateException(
                    "Found testcase finished before started. ");
        }

        Quality setIgnored() {
            return Ignored;
        }

        String getMessage() {
            return "Scheduled";
        }
    },

    /**
     * The execution of the testcase started but did not finish 
     * in any way. 
     * Thus it is neither clear whether the execution succeeds 
     * nor the outcoming of the test. 
     */
    Started(Deficiency.SoFarOk, Phase.Running) {
        ImageIcon getIcon() {
            return GifResource.getIcon(eu.simuline.junit.New.class);
        }

        Quality setFinished() {
            //Benchmarker.mtoc();
            return Success;
        }

        Quality setIgnored() {
            throw new IllegalStateException(
                    "Found testcase to be ignored while running. ");
        }

        Quality setScheduled() {
            throw new IllegalStateException(
                    "Found testcase scheduled before started. ");
        }

        Quality setStarted() {
            throw new IllegalStateException(
                    "Found testcase started while running. ");
        }

        String getMessage() {
            return "started";
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
    Success(Deficiency.SoFarOk, Phase.Completed) {
        ImageIcon getIcon() {
            return GifResource.getIcon(eu.simuline.junit.Ok.class);
        }

        Quality setFinished() {
            throw new IllegalStateException(
                    "Found testcase successful before finished. ");
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
    Invalidated(Deficiency.Indifferent, Phase.Incomplete) {
        ImageIcon getIcon() {
            return GifResource.getIcon(AssumptionFailure.class);
        }

        String getMessage() {
            return "invalidated by failed assumption";
        }

        boolean hasFailure() {
            return true;
        }
    },
    /**
     * The testcase was ignored, i.e. was scheduled for execution 
     * but then decided not to start execution. 
     * In particular, nothing can be said about the course of the test run. 
     */
    Ignored(Deficiency.Indifferent, Phase.Incomplete) {
        ImageIcon getIcon() {
            return GifResource.getIcon(eu.simuline.junit.Ignored.class);
        }

        Quality setFinished() {
            throw new IllegalStateException(
                    "Found testcase finished and ignored. ");
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
    Failure(Deficiency.Failed, Phase.Completed) {
        ImageIcon getIcon() {
            return GifResource.getIcon(eu.simuline.junit.Failure.class);
        }

        String getMessage() {
            return "failed";
        }

        boolean hasFailure() {
            return true;
        }
    },
    /**
     * The execution of the testcase failed 
     * indicated by finishing with exception or error 
     * other than {@link AssertionFailedError}. 
     * Thus there is no valid test result. 
     */
    Error(Deficiency.Failed, Phase.Completed) {
        ImageIcon getIcon() {
            return GifResource.getIcon(eu.simuline.junit.Error.class);
        }

        String getMessage() {
            return "had an error";
        }

        boolean hasFailure() {
            return true;
        }
    };

    /* -------------------------------------------------------------------- *
     * inner enums. *
     * -------------------------------------------------------------------- */

    enum Phase {
        /**
         * The testcase is waiting to run. 
         * This is the phase for {@link Quality#Scheduled}. 
         */
        Waiting {
           /**
             * Returns a time/memory string 
             * indicating that the testcase is waiting for the run: 
             * an according sandclock. 
             * Checks that <code>snap</code> is null. 
             * 
             * @return
             *   <code>&#x231b</code>. 
             */
            String timeMemString(Benchmarker.Snapshot snap) {
                assert snap == null;
                return "\u231b";
            }
        },
        /**
         * The testcase is running. 
         * This is the phase for {@link Quality#Started}. 
         */
        Running {
            /**
             * Returns a time/memory string 
             * indicating that the testcase is running: 
             * an according sandclock. 
             * Checks that <code>snap</code> is null. 
             * 
             * @return
             *   <code>&#x23f3</code>. 
             */
            String timeMemString(Benchmarker.Snapshot snap) {
                assert snap == null;
                return "\u23f3";
            }
        },
        /**
         * The testcase is completed. 
         * This applies whether completion was successful or not. 
         * This applies to {@link Quality#Failure}, 
         * {@link Quality#Error} and {@link Quality#Success}. 
         */
        Completed {
            String timeMemString(Benchmarker.Snapshot snap) {
                // getTimeMs and getMemoryMB both ensure that snap is stopped. 
                return String.format("%.0fms/%.3fMB", snap.getTimeMs(), snap.getMemoryMB());
            }

            boolean isCompleted() {
                return true;
            }
        },
        /**
         * The testcase in neither of the other phases. 
         * This applies to {@link Quality#Ignored} and {@link Quality#Invalidated}. 
         */
        Incomplete {
            /**
             * Returns the string indicating that the time is unknown. 
             * 
             * @return
             *   <code>??ms</code>. 
             */
            String timeMemString(Benchmarker.Snapshot snap) {
                // here snap may be null or not. 
                return "??ms/??MB";
            }

            boolean isCompleted() {
                return true;
            }
        };

        /**
         * Returns a string representing the time <code>timeMs</code> 
         * and the memory <code>memMB</code> 
         * if the test is {@link Completed}; 
         * else a symbol indicating the phase. 
         * 
         * @param snap
         *    a stopped snapshot or <code>null</code>
         * @return
         *    a string representation 
         *    either indicating time and memory elapsed 
         *    or indicating the current phase 
         *    in which the latter would not not make sense. 
         */
        abstract String timeMemString(Benchmarker.Snapshot snap);

        boolean isCompleted() {
            return false;
        }
    } // enum Phases 

    // CAUTION: ordering is vital!
    enum Deficiency {
        /**
         * The testcase neither failed 
         * nor will not run nor is interrupted 
         * without failing. 
         * The options are {@link Quality#Scheduled}, 
         * {@link Quality#Started} and {@link Quality#Success}. 
         */
        SoFarOk {
            Color getColor() {
                return Color.green;
            }
        },
        /**
         * The testcase will never pass or fail. 
         * This is the case for {@link Quality#Ignored} 
         * and for {@link Quality#Invalidated}. 
         */
        Indifferent {
            Color getColor() {
                return Color.yellow;
            }
        },
        /**
         * The testcase failed in some sense, 
         * i.e. the options are {@link Quality#Failure} and
         * {@link Quality#Error}. 
         */
        Failed {
            Color getColor() {
                return Color.red;
            }
        };

        abstract Color getColor();
    } // enum Deficiency 

    /* -------------------------------------------------------------------- *
     * fields. *
     * -------------------------------------------------------------------- */

    /**
     * Encoding how good the result of the test is: 
     * <ul>
     * <li><code>2</code> if something went wrong: 
     *     {@link #Error} or {@link #Failure}.  
     * <li><code>1</code> if something was ignored or invalidated 
     *     (not clear whether the test really would have had a real error)
     *     {@link #Ignored} or {@link #Invalidated}. 
     * <li><code>0</code> else: 
     *     {@link #Scheduled}, {@link #Started} or {@link #Success}. 
     *     The outcoming is open or successful so no worries yet. 
     * </ul>
     * This is used to define the color of the progress bar. 
     *
     * @see #max(Quality)
     */
    private Deficiency deficiency;// TBD: replace by enum. Also with max. 

    /**
     * Encoding how far the testcase is proceeded; 
     * <ul>
     * <li> <code>0</code> not even started 
     *      ({@link #Scheduled} or {@link #Ignored})
     * <li> <code>1</code> started (but not finished in any way), 
     *      ({@link #Started})
     * <li> <code>2</code> finished, either successfully, or not 
     *      ({@link #Error}, {@link #Failure}, {@link #Invalidated} 
     *    or {@link #Success}). 
     * </ul>
     */
    private Phase lifePhase;

    /* -------------------------------------------------------------------- *
     * constructors. *
     * -------------------------------------------------------------------- */

    /**
     * Creates another Quality with the given 
     * {@link #deficiency} and {@link #lifePhase}. 
     */
    Quality(Deficiency deficiency, Phase lifePhase) {
        this.deficiency = deficiency;
        this.lifePhase = lifePhase;
    }


    /* -------------------------------------------------------------------- *
     * methods for phase transitions. *
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
        throw new IllegalStateException(
                "Found testcase started but not scheduled. ");
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
        throw new IllegalStateException(
                "Found testcase ignored but not scheduled. ");
    }

    // Overwritten for Started to return Success 
    // Else overwritten to throw an exception 
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
        throw new IllegalStateException(
                "Found testcase with assumption failure which is not started. ");
    }

    /**
     * Returns the next phase 
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
        throw new IllegalStateException(
                "Found testcase with failure which is not started. ");
    }

    /* -------------------------------------------------------------------- *
     * methods for phase representation. *
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
        return this.deficiency.ordinal() > other.deficiency.ordinal() 
            ? this : other;// NOPMD
    }

    /**
     * Returns the life phase representing completeness: 
     * not yet started, running, finished in any form. 
     * 
     * @return
     *   the life phase according to {@link #lifePhase}. 
     */
    Phase lifePhase() {
        return this.lifePhase;
    }

    /**
     * Returns the color associated with this phase 
     * this is uniform with the color of the icon of that quality. 
     *
     * @return
     *    the color associated with this phase. 
     * @see #max(Quality)
     */
    Color getColor() {
        return this.deficiency.getColor();
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
        return this.deficiency == Deficiency.SoFarOk;
    }

    // seems currently unused 
    /**
     * Whether this testcase completed in any way. 
     * 
     * @return
     *   completeness according to {@link #lifePhase}. 
     */
    boolean isCompleted() {
        return this.lifePhase.isCompleted();
    }

    // Currently used for assertions only. 
    /**
     * Returns whether the given state is tied to a throwable. 
     *
     * @return
     *    <code>true</code> iff this is 
     *    {@link #Invalidated}, {@link #Failure} or {@link #Error}. 
     */
    boolean hasFailure() {
        return false;
    }

} // enum Quality 

