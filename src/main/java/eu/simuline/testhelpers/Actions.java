package eu.simuline.testhelpers;

import eu.simuline.util.images.GifResource;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.Result;
import org.junit.runner.Description;

import org.junit.runner.manipulation.Filter;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import org.javalobby.icons20x20.Open;
import org.javalobby.icons20x20.ExecuteProject;
import org.javalobby.icons20x20.Stop;
import org.javalobby.icons20x20.Hammer;
import org.javalobby.icons20x20.Delete;


/**
 * Represents the actions of the test GUI inspired by old junit GUI. 
 * **** Moreover provides a wrapper to access junit 
 * and the access point to run the gui started from the class to be tested. 
 * ****
 * The fundamental methods are {@link #runFromMain()} 
 * which runs the test class from its main method and 
 * {@link #runTestClass(String)} which runs a testclass with the given name. 
 *
 * @see GUIRunner
 * @see GUIRunListener
 * 
 * Created: Tue Jun 13 02:53:06 2006
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class Actions {

    /* -------------------------------------------------------------------- *
     * inner classes and enums.                                             *
     * -------------------------------------------------------------------- */

    /**
     * The action of opening a <code>java</code>-file defining a test class 
     * and starting the tests defined by it. 
     */
    class OpenAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	OpenAction() {
	    super("Open", GifResource.getIcon(Open.class));
	    putValue(SHORT_DESCRIPTION, "Opens a class and executes run. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_O,
					    ActionEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent event) {
	    String clsName = Actions.this.guiRunner.openClassChooser();

	    if (clsName == null) {
		// open action was not successful
		System.out.println("Warning: no class choosen");
		return;
	    }
	    assert clsName != null;
	    System.out.println("Info: class `" + clsName + "' choosen");
	    Actions.this.coreRunner = new CoreRunner(clsName);

	    // clears list of failed testcases
	    Actions.this.filter = null;
	    Actions.this.getStartAction().actionPerformed(null);
	}
    } // class OpenAction 


    /**
     * The action of starting the testcases in the loaded testclass. 
     */
    class StartAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	StartAction() {
	    super("Run", GifResource.getIcon(ExecuteProject.class));
	    putValue(SHORT_DESCRIPTION, "Runs the testcases. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_R,
					    ActionEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent event) {
 	    Actions.this.coreRunner = new CoreRunner(Actions.this.coreRunner);
	    Actions.this.coreRunner.start();
	}
    } // class StartAction 

    /**
     * The action of stopping the test run//  after having finished 
     * the currently running testcase. 
     */
    class StopAction extends AbstractAction {

    	private static final long serialVersionUID = -589L;

    	StopAction() {
    	    super("Stop", GifResource.getIcon(Stop.class));
    	    putValue(SHORT_DESCRIPTION, 
    		     "Stops after having executed the current testcase. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ACCELERATOR_KEY,
    		     KeyStroke.getKeyStroke(KeyEvent.VK_S,
    					    ActionEvent.CTRL_MASK));
    	}

     	public void actionPerformed(ActionEvent event) {
	    Actions.this.coreRunner.pleaseStop();
	}
    } // class StopAction 

    /**
     * The action of breaking the sequence of testcases currently running. 
     * Tries to stop the currently running testcase 
     * but guarantees only that the break is effected 
     * after the current testcase has finished. 
     */
    class BreakAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	BreakAction() {
	    super("Break", GifResource.getIcon(Hammer.class));
	    putValue(SHORT_DESCRIPTION, 
		     "Tries to break execution of current testcases. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_B,
					    ActionEvent.CTRL_MASK));
	}

	@SuppressWarnings("deprecation")// because no alternative to stop. 
	public void actionPerformed(ActionEvent event) {
	    //setEnableForRun(false); // !isRunning
	    System.out.println("Break...");
	    Actions.this.coreRunner.pleaseStop(); // avoids going on after stop 
	    // method stop is deprecated but there is no alternative
	    // TBD: rework: shall throw exception. 
			// original argument: deprecated?
	    Actions.this.coreRunner.stop(); //new StoppedByUserException()
	}
    } // class BreakAction 


    /**
     * The action of exiting the tester application. 
     */
    static class ExitAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	ExitAction() {
	    super("Exit", GifResource.getIcon(Delete.class));
	    putValue(SHORT_DESCRIPTION, 
		     "Quits this application immediately. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_E,
					    ActionEvent.CTRL_MASK));
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings
	(value = "DM_EXIT", 
	 justification = "To ensure safe exit " + 
	 "not reached by throwing exception. " + 
	 "Also Actions is not invoked by other code. ")
	public void actionPerformed(ActionEvent event) {
	    //throw new RuntimeException("Exit by user action. ");
	    System.exit(0);
	}
    } // class ExitAction 

    /**
     * A thread in which a testclass is executed 
     * or at least a single testcase out of this testclass. 
     * Essentially, this is defined by the class {@link #testClassName}. 
     * The major task of this class is, to reload {@link #testClassName} 
     * using a classloader which allows reloading 
     * each test run without restarting the tester application. 
     * The core of the code is copied from {@link JUnitCore}. 
     */
    class CoreRunner extends Thread {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The notifier to run the tests as in JUnitCore. 
	 */
	private final RunNotifier notifier;

	/**
	 * The name of the class to be tested. 
	 */
	private final String testClassName;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a runner running all testcases in the given test class. 
	 */
	CoreRunner(String testClassName)  {
	    this.notifier = new RunNotifier();
	    this.notifier.addListener(Actions.this.listener);

	    this.testClassName = testClassName;
	}

	/**
	 * Copy constructor. 
	 */
	CoreRunner(CoreRunner other)  {
	    this(other.testClassName);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */


	// api-docs inherited from Runnable 
	// overwrites void implementation provided by class Thread 
	/**
	 * Loads the class with name {@link #testClassName} 
	 * with a {@link TestCaseClassLoader} to allow reloading. 
	 * Then creates a {@link Request} filtering it with {@link #filter} 
	 * defining the tests to be run and runs those tests 
	 * invoking {@link #run(Request)}. 
	 */
	public void run() {
	    Class<?> newTestClass = null;
	    try {
		newTestClass = new TestCaseClassLoader()
		    .loadClass(this.testClassName, true);
	    } catch (ClassNotFoundException e) {
		throw new IllegalStateException// NOPMD
		    ("Testclass '" + this.testClassName + "' disappeared. ");
		// **** This makes the problem 
		// that in the GUI the run button is shaded 
		// (and the stop button is not). 
	    }
	    assert newTestClass != null;

	    Request request = Request.aClass(newTestClass);
	    //Request request = Request.classes(newTestClass);
	    if (Actions.this.filter != null && Actions.this.filter.isTest()) {
		request = request.filterWith(Actions.this.filter);
	    } else {
		Actions.this.listener.testClassStructureLoaded
		    (request.getRunner().getDescription());
	    }

	    try {
		run(request);
	    } catch (StoppedByUserException ee) {
		// either Break or Stop 
  		Actions.this.listener.testRunAborted();
  	    }

	    //System.out.println("...Core run()"+this.core);
	}

	// almost copy from JUnitCore
	public void run(Request request) {
	    Runner runner = request.getRunner();
	    Result result = new Result();
	    RunListener listener = result.createListener();
	    this.notifier.addFirstListener(listener);
	    try {
		this.notifier.fireTestRunStarted(runner.getDescription());
		runner.run(this.notifier);
		this.notifier.fireTestRunFinished(result);
	    } finally {
		this.notifier.removeListener(listener);
	    }
	}

	public void pleaseStop() {
	    this.notifier.pleaseStop();
	}

    } // class CoreRunner 

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */


    /**
     * The action to open a new testclass. 
     */
    private final  OpenAction  openAction;

    /**
     * The action to run a testcase. 
     */
    private final StartAction startAction;

    /**
     * The action to stop after having finished the currently running testcase. 
     */
    private final  StopAction  stopAction;

    /**
     * The action to break execution of testcases. 
     * Tries to break the currently running testcase 
     * and does not go on with further testcases. 
     */
    private final BreakAction breakAction;

    private final GUIRunner guiRunner;

    private CoreRunner coreRunner;

    private final ExtRunListener listener;

 
    /**
     * Defines whether a test is running. 
     * This is used to activate/deactivate the actions 
     * {@link #openAction}, {@link #startAction}, {@link #stopAction} 
     * and {@link #breakAction}. 
     */
    private boolean isRunning;

    // to run a single testcase **** may be null 
    // which signifies that all tests shall be executed. 
    // shall be replaced by compounds. 
    /**
     * Defines the filter for tests to be run. 
     * **** may be null 
     *
     * @see #setFilter(Description)
     * @see Actions.CoreRunner#run()
     */
    private Description filter;

    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    /**
     * Creates a new <code>Actions</code> instance.
     *
     * @param testClassName
     *    the name of the test class. 
     */
    @SuppressWarnings("checkstyle:nowhitespaceafter")
    public Actions(String testClassName) {

	this. openAction = new  OpenAction();
	this.startAction = new StartAction();
	this. stopAction = new  StopAction();
	this.breakAction = new BreakAction();

	this.guiRunner  = new GUIRunner(this);
	this.listener   = new SeqRunListener(this.guiRunner);

	this.coreRunner = new CoreRunner(testClassName);
	this.isRunning  = false;
	this.filter     = null; // **** to be reworked 
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */


    private static boolean descShouldRun(Description desc, 
					 Description desiredDesc) {
	assert desc.isTest();
	if (desiredDesc.isTest()) {
	    return desiredDesc.equals(desc);
	}
	for (Description each : desiredDesc.getChildren()) {
	    if (descShouldRun(desc, each)) {
		return true;
	    }
	}
	return false;
    }

    // required only because of a bug in junit. 
    /**
     * Returns a {@code Filter} that only runs methods in 
     * desiredDesc. 
     *
     * @param desiredDesc
     *    a description of the tests to be run. 
     * @return
     *    a {@code Filter} that only runs methods in <code>desiredDesc</code>. 
     */
    public static Filter desc2filter(final Description desiredDesc) {
        return new Filter() {
            @Override
            public boolean shouldRun(Description desc) {
		return descShouldRun(desc, desiredDesc);
               }

            @Override
            public String describe() {
                return String.format("Methods %s", 
				     desiredDesc.getDisplayName());
            }
        };
    }


    /**
     * The fundamental method to start tests with the underlying JUnit-GUI. 
     * The test class is supposed to define a method <code>main</code> 
     * with body <code>Actions.run(&lt;testclass&gt;.class);</code>. 
     *
     * @param testClassName
     *    the name of the test class to represent and run. 
     * @see JUnitSingleTester
     * @see #runFromMain()
     */
    static void runTestClass(final String testClassName) {
	Runnable guiCreator = new Runnable() {
		public void run() {
		    // parameter null is nowhere used. 
		    new Actions(testClassName)
			.startAction.actionPerformed(null);
		}
        };
 
        // execute in Event-Dispatch-Thread 
        SwingUtilities.invokeLater(guiCreator);
    }

    /**
     * The fundamental method to start tests with the underlying JUnit-GUI. 
     * The test class is supposed to define a method <code>main</code> 
     * with body <code>Actions.runFromMain();</code>. 
     * Essentially invokes {@link #runTestClass(String)} 
     * with the proper test class name. 
     */
    public static void runFromMain() {
	runTestClass(new Throwable().getStackTrace()[1].getClassName());
	// TBD: use instead
	// Thread.currentThread().getStackTrace()[1].getClassName()
	// avoiding creating a throwable which is nowhere used. 
    }

    GUIRunner getRunner() {
	return this.guiRunner;
    }

    /**
     * Sets {@link #filter} according to <code>filter</code>. 
     *
     * @param filter
     *    the filter for the tests to be run 
     */
    void setFilter(Description filter) {
	assert filter != null;
	this.filter = filter;
    }

    /**
     * Updates the action-enablements 
     * depending on whether a test is running or not: 
     * Open and Start action are enabled iff no test is running, 
     * whereas stop and break actions are enabled iff some test is running. 
     *
     * @param isRunning
     *    whether a test is running. 
     */
    @SuppressWarnings("checkstyle:nowhitespaceafter")
    void setEnableForRun(boolean isRunning) {	
	assert this.isRunning ^ isRunning;
	this.isRunning = isRunning;
	this. openAction.setEnabled(!this.isRunning);
	this.startAction.setEnabled(!this.isRunning);
	this. stopAction.setEnabled( this.isRunning);
	this.breakAction.setEnabled( this.isRunning); //(true);
    }

    AbstractAction getOpenAction() {
	return this.openAction;
    }

    AbstractAction getStartAction() {
	return this.startAction;
    }

    AbstractAction getStopAction() {
	return this.stopAction;
    }

    AbstractAction getBreakAction() {
	return this.breakAction;
    }

    AbstractAction getExitAction() {
	return new ExitAction();
    }

    public static void main(String[] args) {
	runFromMain();
    }
}
