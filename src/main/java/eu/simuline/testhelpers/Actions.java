package eu.simuline.testhelpers;

import eu.simuline.util.GifResource;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

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
 * Describe class Actions here.
 *
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
		Class cls = new TestCaseClassLoader().loadClass(clsName,true);
		Actions.this.coreRunner = new CoreRunner(cls);
	    } catch (ClassNotFoundException e) {
		System.out.println
		    ("Could not find testclass \"" + clsName + "\". ");
	    }
	    //Actions.this.runner.clear(clsName);
	}
    } // class OpenAction 


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
	    setState(false);// !isRunning
	    Actions.this.coreRunner.pleaseStop();
	}
    } // class StopAction 

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

    class CoreRunner extends Thread {
	private final JUnitCore core;
	private final Class testClass;
	TestCaseClassLoader classLd;

	CoreRunner(Class testClass)  {
	    this.core = new JUnitCore();
	    this.core.addListener(Actions.this.listener);
	    this.testClass = testClass;
	}

	CoreRunner(CoreRunner other)  {
	    this(other.testClass);
	}

	Class newTestClass() {
	    try {
		this.classLd = new TestCaseClassLoader();
		return classLd.loadClass(this.testClass.getName(), 
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
	    if (Actions.this.singTest == null) {
		runAll();
	    } else {
		runSingle();
	    }
	}

	public void runSingle() {
	    Request request = Request.classes("All",newTestClass());
	    // **** make sure that the shape of the testsuite 
	    // remains unchanged. 
	    request = request.filterWith(Actions.this.singTest.getDesc());
	    try {
		this.core.run(request);
	    } catch (StoppedByUserException e) {
		Actions.this.listener.testRunAborted();
	    }
	}

	public void runAll() {
System.out.println("Core run()"+this.core);
	    //System.out.println("Core run()"+this.testClass);
	    try {

		this.core.run(newTestClass());
	    } catch (StoppedByUserException e) {
		Actions.this.listener.testRunAborted();
	    }
System.out.println("...Core run()"+this.core);
	}

	public void pleaseBreak() {
	    this.classLd. pleaseBreak();
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




    private final  OpenAction  openAction;
    private final StartAction startAction;
    private final  StopAction  stopAction;
    private final BreakAction breakAction;

    private final GUIRunner runner;

    private boolean isRunning;

    // to run a single testcase **** may be null 
    // shall be replaced by compounds. 
    private TestCase singTest;

    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    /**
     * Creates a new <code>Actions</code> instance.
     *
     */
    public Actions(Class testClass) {
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

    public static void run(Class testClass) {
	// parameter null is nowhere used. 
	new Actions(testClass).startAction.actionPerformed(null);
    }

    GUIRunner getRunner() {
	return this.runner;
    }

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

    // **** at the moment boolean: true for: running. 
    void setState(boolean isRunning) {	
	assert this.isRunning ^ isRunning;
	this.isRunning = isRunning;
	if (this.isRunning) {
	    Actions.this. openAction.setEnabled(false);
	    Actions.this.startAction.setEnabled(false);
	    Actions.this. stopAction.setEnabled(true);
	    Actions.this.breakAction.setEnabled(true);//.setEnabled(true);
	} else {
	    Actions.this. openAction.setEnabled(true);
	    Actions.this.startAction.setEnabled(true);
	    Actions.this. stopAction.setEnabled(false);
	    Actions.this.breakAction.setEnabled(false);//.setEnabled(true);
	}
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
