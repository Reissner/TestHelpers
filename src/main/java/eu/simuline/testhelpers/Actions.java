package eu.simuline.testhelpers;

import eu.simuline.util.GifResource;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import org.javalobby.icons20x20.Open;
import org.javalobby.icons20x20.ExecuteProject;
import org.javalobby.icons20x20.Stop;
import org.javalobby.icons20x20.Hammer;
import org.javalobby.icons20x20.Delete;


/**
 * Represents the actions of the test GUI inspired by old junit GUI. 
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
     * The action of opening a <code>java</code>-file defining a test class. 
     * **** missing: update of the GUI. 
     */
    class OpenAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	OpenAction() {
	    super("Open",GifResource.getIcon(Open.class));
	    putValue(SHORT_DESCRIPTION, "Opens a class and executes run. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_O,
					    ActionEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent event) {
	    String clsName = Actions.this.runner.openClassChooser();
	    if (clsName == null) {
		// open action was cancelled 
		return;
	    }
	    assert clsName != null;

	    try {
		Class<?> cls = new TestCaseClassLoader()
		    .loadClass(clsName,true);
		Actions.this.coreRunner = new CoreRunner(cls);
	    } catch (ClassNotFoundException e) {
		System.out.println
		    ("Could not find testclass \"" + clsName + "\". ");
	    }
	    //Actions.this.runner.clear(clsName);
	}
    } // class OpenAction 


    /**
     * The action of starting the testcases in the loaded testclass. 
     */
    class StartAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	StartAction() {
	    super("Run",GifResource.getIcon(ExecuteProject.class));
	    putValue(SHORT_DESCRIPTION, "Runs the testcases. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_R,
					    ActionEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent event) {
	    setState(true);// isRunning
 	    Actions.this.coreRunner = 
 		new CoreRunner(Actions.this.coreRunner);
	    Actions.this.coreRunner.start();
	}
    } // class StartAction 

    /**
     * The action of breaking the sequence of testcases currently running. 
     */
    class BreakAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	BreakAction() {
	    super("Break",GifResource.getIcon(Hammer.class));
	    putValue(SHORT_DESCRIPTION, 
		     "Tries to break execution of current testcase. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_B,
					    ActionEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent event) {
	    setState(false);// !isRunning
System.out.println("Break...");
Actions.this.coreRunner.pleaseBreak();
	    Actions.this.coreRunner.interrupt();////stop();
	    setState(!Actions.this.coreRunner.interrupted());
System.out.println("...Break"+Actions.this.coreRunner.interrupted());   
	}
    } // class BreakAction 

    /**
     * The action of stopping the test run. 
     * This is effected not before having finished 
     * the currently running testcase. 
     */
    class StopAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	StopAction() {
	    super("Stop",GifResource.getIcon(Stop.class));
	    putValue(SHORT_DESCRIPTION, 
		     "Stops after having executed the current testcase. ");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ACCELERATOR_KEY,
		     KeyStroke.getKeyStroke(KeyEvent.VK_S,
					    ActionEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent event) {
	    //setState(false);// !isRunning **** wrong: does not stop imm. 
	    Actions.this.coreRunner.pleaseStop();
	}
    } // class StopAction 

    /**
     * The action of exiting the tester application. 
     */
    class ExitAction extends AbstractAction {

	private static final long serialVersionUID = -589L;

	ExitAction() {
	    super("Exit",GifResource.getIcon(Delete.class));
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
     * Essentially, this is defined by the class {@link #testClass} 
     * to be tested and the execution is delegated to {@link #core}. 
     * The major task of this class is, to reload {@link #testClass} 
     * using a classloader {@link #classLd} which allows reloading 
     * each test run without restarting the tester application. 
     */
    class CoreRunner extends Thread {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The JUnitCore effectively executing the testcases. 
	 */
  	private final JUnitCore core;

	/**
	 * The class to be tested. 
	 */
	private final Class<?> testClass;

	/**
	 * The classloader used. 
	 * The special about this is, that it can unload and reload classes 
	 * when modified without leaving the tester application. 
	 * <p>
	 * Note that this field is not initialized by the constructor, 
	 * but as a side effect invoking {@link #newTestClass()}. 
	 */
	TestCaseClassLoader classLd;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a runner running all testcases in the given test class. 
	 */
	CoreRunner(Class<?> testClass)  {
	    this.core = new JUnitCore();
	    this.core.addListener(Actions.this.listener);
	    this.testClass = testClass;
	}

	/**
	 * Copy constructor. 
	 */
	CoreRunner(CoreRunner other)  {
	    this(other.testClass);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new {@link TestCaseClassLoader} 
	 * and assigns it to {@link #classLd}. 
	 * With this classloader reloads the test class 
	 * given by {@link #testClass} and returns this" copy". 
	 *
	 * @throws IllegalStateException
	 *    in case the given class is not found. 
	 */
 	private Class<?> newTestClass() {
	    this.classLd = new TestCaseClassLoader();
	    try {
		return this.classLd.loadClass(this.testClass.getName(), 
					 true);
	    } catch (ClassNotFoundException e) {
		throw new IllegalStateException// NOPMD
		    ("Testclass \"" + this.testClass + "\" disappeared. ");
		// **** This makes the problem 
		// that in the GUI the run button is shaded 
		// (and the stop button is not). 
	    }
	}

	public void run() {
System.out.println("Core run()"+this.core);
	    Request request = Request.classes(newTestClass());
	    if (Actions.this.singTest != null) {
		request = request.filterWith(Actions.this.singTest.getDesc());
	    }
	    try {
		this.core.run(request);
	    } catch (StoppedByUserException e) {
		Actions.this.listener.testRunAborted();
	    }
System.out.println("...Core run()"+this.core);
	}

	// **** does not work properly 
	public void pleaseBreak() {
	    this.classLd.pleaseBreak();
	}

	public void pleaseStop() {
	    // **** hack: should be provided by JUnitCore. 
	    try {
		RunNotifier notifier = 
		    (RunNotifier)Accessor.getField(this.core,"fNotifier");
		notifier.pleaseStop();
	    } catch (NoSuchFieldException nsfe) {
		throw new IllegalStateException// NOPMD
		    ("Access to internal JUnit classes failed. " + 
		     "Implementation changed? ");
	    }
	}
    } // class CoreRunner 

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */


    private CoreRunner coreRunner;

    private       GUIRunListener          listener;
    private final GUIRunListener.All      listenerAll;
    private final GUIRunListener.Singular listenerSingle;



    /**
     *
     */
    private final  OpenAction  openAction;
    /**
     *
     */
    private final StartAction startAction;
    /**
     *
     */
    private final  StopAction  stopAction;
    /**
     *
     */
    private final BreakAction breakAction;

    private final GUIRunner runner;

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
    public Actions(Class<?> testClass) {
	this.coreRunner = new CoreRunner(testClass);
	this.singTest = null;// leads to running all testcases 
	this. openAction = new  OpenAction();
	this.startAction = new StartAction();
	this. stopAction = new  StopAction();
	this.breakAction = new BreakAction();

	this.runner = new GUIRunner(this);


	this.listenerSingle = new GUIRunListener.Singular(this);
	this.listenerAll    = new GUIRunListener.All     (this);
	this.listener = this.listenerAll;

	this.isRunning = false;
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public static void run(final Class<?> testClass) {
      Runnable guiCreator = new Runnable() {
		public void run() {
		    // parameter null is nowhere used. 
		    new Actions(testClass).startAction.actionPerformed(null);
		}
        };
 
        // execute in Event-Dispatch-Thread 
        SwingUtilities.invokeLater(guiCreator);
    }

    GUIRunner getRunner() {
	return this.runner;
    }

    /**
     * Sets {@link #singTest} according to <code>singTest</code> 
     * and also {@link #listener} accordingly: 
     * <ul>
     * <li>
     * If <code>singTest</code> is not <code>null</code>, 
     * i.e. signifies a single testcase, 
     * then {@link #listener} is set to {@link #listenerSingle}. 
     * <li>
     * If <code>singTest</code> is <code>null</code>, 
     * then {@link #listener} is set to {@link #listenerAll}. 
     * </ul>
     * 
     */
    void setSingleTest(TestCase singTest) {
	this.singTest = singTest;
	if (this.singTest == null) {
	    this.listener = this.listenerAll;
	} else {
	    this.listener = this.listenerSingle;
	}
    }

    TestCase getSingleTest() {
	return this.singTest;
    }

    /**
     * Updates the actions according to whether a test is running or not. 
     *
     *
     * @param isRunning
     *    whether a test is running. 
     */
    void setState(boolean isRunning) {	
	assert this.isRunning ^ isRunning;
	this.isRunning = isRunning;
	Actions.this. openAction.setEnabled(!this.isRunning);
	Actions.this.startAction.setEnabled(!this.isRunning);
	Actions.this. stopAction.setEnabled( this.isRunning);
	Actions.this.breakAction.setEnabled( this.isRunning);//.setEnabled(true);
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

}


// cd /home/ernst/Software/src/main/java/eu/simuline/testhelpers/
// /usr/lib64/jvm/javaLatest/bin/javac -classpath /home/ernst/Software/target/test-classes:/home/ernst/Software/target/classes:/home/ernst/Software/jars/junitLatest.jar:/home/ernst/Software/jars/javaoctaveLatest.jar:/home/ernst/Software/jars/commons-loggingLatest.jar:/home/ernst/Software/jars/antlr-4.5-complete.jar:/home/ernst/Software/jars/jnaLatest.jar:/home/ernst/Software/jars/jnaPlatformLatest.jar:/usr/share/ant/lib/ant.jar -sourcepath /home/ernst/Software/src/main/java -encoding UTF-8 -d /home/ernst/Software/target/classes -deprecation -target 1.6 -source 1.6 -Xlint Actions.java

// warning: [options] bootstrap class path not set in conjunction with -source 1.6
// Actions.java:125: warning: [static] static method should be qualified by type name, Thread, instead of by an expression
// 	    setState(!Actions.this.coreRunner.interrupted());
// 	                                     ^
// Actions.java:126: warning: [static] static method should be qualified by type name, Thread, instead of by an expression
// System.out.println("...Break"+Actions.this.coreRunner.interrupted());   
//                                                      ^
// 3 warnings

// Compilation finished at Wed Apr 27 16:39:52
