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
 * Represents the phases in the livecycle of a {@link TestCase} 
 * from being {@link #Scheduled} to {@link #Ignored} 
 * or via {@link #Started} to finished (see {@link #isDecided}) 
 * which means either {@link #Success}, 
 * {@link #Failure} or even {@link #Error}. 
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

    abstract ImageIcon getIcon();

    // appropriate for Failur and Error 
    Quality setFinished() {
	return this;
    }

    Quality setIgnored() {
	throw new IllegalStateException
	    (this +" may not be ignored. ");
    }

    abstract String status();

    // appropriate for Success, Failure and Error 
    boolean isDecided() {
	return true;
    }

} // enum Quality 

