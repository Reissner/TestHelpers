package eu.simuline.testhelpers;

import eu.simuline.sun.gtk.File;

import eu.simuline.junit.Ok;
import eu.simuline.junit.New;
import eu.simuline.junit.Ignored;
import eu.simuline.junit.Failure;
import eu.simuline.junit.Error;

import eu.simuline.util.GifResource;

import javax.swing.ImageIcon;

/**
 * Represents the phases in the life-cycle of a {@link TestCase} 
 * from being {@link #Scheduled} to {@link #Ignored} 
 * or via {@link #Started} to finished (see {@link #isDecided}) 
 * which means either {@link #Success}, 
 * {@link #Failure} or even {@link #Error}. 
 * <p>
 * For each of these phases, 
 * there is a separate icon given by {@link #getIcon()}, 
 * a status message given by {@link #status()}. 
 * Also {@link #isDecided()} returns whether the outcoming of the test 
 * is decided in that phase. 
 * Methods {@link #setFinished()} and {@link #setIgnored()} 
 * specify part of the state transitions. 
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

    Scheduled {
	ImageIcon getIcon() {
	    return GifResource.getIcon(File.class);
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase finished before started. ");
	}
	Quality setIgnored() {
	    throw new IllegalStateException
		("Found testcase ignored before started. ");// shall be ok. 
	}
	String status() {
	    throw new UnsupportedOperationException();
	}
	boolean isDecided() {
	    return false;
	}
    },
    Started {
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
	boolean isDecided() {
	    return false;
	}
    },
    Success {
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
    Ignored {
	ImageIcon getIcon() {
	    return GifResource.getIcon(Ignored.class);
	}
	Quality setFinished() {
	    throw new IllegalStateException
		("Found testcase finished and ignored. ");
	}
	String status() {
	    return "was ignored";
	}
    }, 
    Failure {
	ImageIcon getIcon() {
	    return GifResource.getIcon(Failure.class);
	}
	String status() {
	    return "failed";
	}
    }, 
    Error {
	ImageIcon getIcon() {
	    return GifResource.getIcon(Error.class);
	}
	String status() {
	    return "had an error";
	}
    };

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Returns an icon representing this phase on the GUI. 
     */
    abstract ImageIcon getIcon();

    /**
     * Returns <code>this</code> for {@link #Failure} and {@link #Error} and 
     * returns {@link #Success} for {@link #Started}. 
     *
     * @throws IllegalStateException 
     *    for {@link #Scheduled}, {@link #Success} and {@link #Ignored}. 
     */
    Quality setFinished() {
	return this;
    }

    /**
     * Returns {@link #Ignored} for state {@link #Started}; 
     * otherwise throws an exception. 
     *
     * @throws IllegalStateException 
     *    except for {@link #Started}. 
     */
    Quality setIgnored() {
	throw new IllegalStateException
	    (this +" may not be ignored. ");
    }

    /**
     * Returns a status messag which describes this phase 
     * or throws an exception. 
     *
     * @throws UnsupportedOperationException
     *    if applied for {@link #Scheduled}. 
     */
    abstract String status();

    /**
     * Returns <code>true</code> for {@link #Success}, {@link #Failure} 
     * and {@link #Error}; otherwise <code>false</code>. 
     */
    boolean isDecided() {
	return true;
    }

} // enum Quality 

