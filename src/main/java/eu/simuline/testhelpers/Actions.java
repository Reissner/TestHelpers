package eu.simuline.testhelpers;

import eu.simuline.util.GifResource;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.Result;
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
 * The fundamental methods are {@link #runFromMain()} 
 * which runs the test class from its main method 
 * and {@link #runTstCls(String)} which runs a testclass with the given name. 
 *
 * @see GUIRunner
 * @see GUIRunListener
 * 
 * Created: Tue Jun 13 02:53:06 2006
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
public class Actions {

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
		return;
	    }
	    assert clsName != null;
	    Actions.this.coreRunner = new CoreRunner(clsName);

	    // clears list of failed testcases
	    Actions.this.guiRunner.resetTestCaseLister();
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

	public void actionPerformed(ActionEvent event) {
	    //setEnableForRun(false);// !isRunning
	    System.out.println("Break...");
	    Actions.this.coreRunner.pleaseStop();// avoids going on after stop 
	    // method stop is deprecated but there is no alternative 
	    Actions.this.coreRunner.stop(new StoppedByUserException());
	}
    } // class BreakAction 


    /**
     * The action of exiting the tester application. 
     */
    class ExitAction extends AbstractAction {

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

	public void actionPerformed(ActionEvent event) {
	    System.exit(0);
	}
    } // class ExitAction 

    /**
     * A thread in which a testclass is executed 
     * or at least a single testcase out of this testclass. 
     * Essentially, this is defined by the class {@link #testClass}. 
     * The major task of this class is, to reload {@link #testClass} 
     * using a classloader which allows reloading 
     * each test run without restarting the tester application. 
     * The core of the code is copied from {@link JUnitCore}. 
     */
    class CoreRunner extends Thread {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The notifier to run the tests as in JUnitCore 
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

	    Request request = Request.classes(newTestClass);
	    if (Actions.this.singTest != null) {
		request = request.filterWith(Actions.this.singTest.getDesc());
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
//System.out.println("runner: "+runner);is an instance of Suite 
	    
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

    private final GUIRunListener listener;

 
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
    private TestCase singTest;

    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    /**
     * Creates a new <code>Actions</code> instance.
     *
     */
   public Actions(String testClassName) {

	this. openAction = new  OpenAction();
	this.startAction = new StartAction();
	this. stopAction = new  StopAction();
	this.breakAction = new BreakAction();

	this.guiRunner  = new GUIRunner(this);
	this.listener   = new GUIRunListener(this);

	this.coreRunner = new CoreRunner(testClassName);
	this.isRunning  = false;
	this.singTest   = null;// leads to running all testcases 
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * The fundamental method to start tests with the underlying JUnit-GUI. 
     * The test class is supposed to define a method <code>main</code> 
     * with body <code>Actions.run(<testclass>.class);</code>. 
     *
     * @see JUnitSingleTester
     * @see #runFromMain()
     */
    static void runTstCls(final String testClassName) {
      Runnable guiCreator = new Runnable() {
		public void run() {
		    // parameter null is nowhere used. 
		    new Actions(testClassName).startAction.actionPerformed(null);
		}
        };
 
        // execute in Event-Dispatch-Thread 
        SwingUtilities.invokeLater(guiCreator);
    }

    /**
     * The fundamental method to start tests with the underlying JUnit-GUI. 
     * The test class is supposed to define a method <code>main</code> 
     * with body <code>Actions.runFromMain();</code>. 
     * Essentially invokes {@link #runTstCls(String)} 
     * with the proper test class name. 
     */
    public static void runFromMain() {
	runTstCls(new Throwable().getStackTrace()[1].getClassName());
    }

    GUIRunner getRunner() {
	return this.guiRunner;
    }

    /**
     * Sets {@link #singTest} according to <code>singTest</code>. 
     */
    void setSingleTest(TestCase singTest) {
	this.singTest = singTest;
    }

    TestCase getSingleTest() {
	return this.singTest;
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
    void setEnableForRun(boolean isRunning) {	
	assert this.isRunning ^ isRunning;
	this.isRunning = isRunning;
	Actions.this. openAction.setEnabled(!this.isRunning);
	Actions.this.startAction.setEnabled(!this.isRunning);
	Actions.this. stopAction.setEnabled( this.isRunning);
	Actions.this.breakAction.setEnabled( this.isRunning);//(true);
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

// well, i did not find any reasonable alternatie to stop. 
// maybe instrumentation is a way, but... 
// seems to be quite complicated. 
//
//
// warning: [options] bootstrap class path not set in conjunction with -source 1.6
// Actions.java:155: warning: [deprecation] stop() in Thread has been deprecated
// Actions.this.coreRunner.stop();//interrupt();
//                        ^
// 2 warnings

// Compilation finished at Tue May 10 01:51:04
