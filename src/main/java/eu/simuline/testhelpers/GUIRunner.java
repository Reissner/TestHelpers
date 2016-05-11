package eu.simuline.testhelpers;

import eu.simuline.util.GifResource;

import eu.simuline.junit.Logo;
import eu.simuline.junit.Hierarchy;
import eu.simuline.junit.Smalllogo;

import eu.simuline.util.JavaPath;

import org.junit.runner.Description;

import java.awt.Container;
import java.awt.Component;
import java.awt.Color;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;

import javax.swing.filechooser.FileFilter;

import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.DefaultTreeCellRenderer;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * The GUI of a JUnit test-runner. 
 * Comprises 
 * <ul>
 * <li>
 * a menubar to select running, stopping or breaking a testcase 
 * or even exiting the application 
 * <li>
 * A label for the name of the testclass. 
 * <li>
 * A progress bar represented by the class {@link GUIRunner.TestProgressBar}. 
 * <li>
 * A component displaying the number of tests to run, 
 * already runned, ignored and failed. 
 * This is an instance of the class {@link GUIRunner.RunsErrorsFailures}. 
 * <li>
 * a treeview on the testsuite 
 * represented by the class {@link GUIRunner.HierarchyWrapper}. 
 * This needs support from the classes and 
 * {@link GUIRunner.TreePathIncrementor}, 
 * {@link GUIRunner.TestTreeCellRenderer}. 
 * <li>
 * a list of the testcases that failed in a sense 
 * given as a {@link GUIRunner.TestCaseLister}. 
 * <li>
 * Closely tied to the list of testcases failed so far, 
 * is the list-view on the stacktraces 
 * given by a {@link GUIRunner.StackTraceLister}. 
 * <li>
 * a statusbar. 
 * <li>
 * Note that the selection on the treeview of all testcases 
 * and on the listview of the testcases already failed 
 * must be synchronized. 
 * This is performed by the class {@link GUIRunner.TabChangeListener} 
 * and the interface {@link GUIRunner.Selector}. 
 * </ul>
 *
 * Partially the methods serve to make up the gui; 
 * the others are invoked by the {@link GUIRunListener}s 
 * to report the current state of the testsuitey. 
 *
 * Created: Sat Jun  3 18:29:52 2006
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
//@SuppressWarnings("")
public class GUIRunner {

    /* -------------------------------------------------------------------- *
     * constants.                                                           *
     * -------------------------------------------------------------------- */


    /**
     * The (big) JUnit-logo. 
     */
    private final static ImageIcon  logoIcon = 
    GifResource.getIcon(Logo.class);

    /**
     * The icon representing the hierarchy of tests: 
     * used for the tabbed pane. 
     */
    private final static ImageIcon hierarchyIcon = 
    GifResource.getIcon(Hierarchy.class);

    /**
     * The small JUnit-logo on top left of this frame. 
     * **** still this is not displayed properly ****. 
     */
    private final static ImageIcon smallLogoIcon = 
    GifResource.getIcon(Smalllogo.class);


    /**
     * Represents the horizontal space used for the boundary. 
     */
    private final static Component horizBoundary = 
    Box.createHorizontalStrut(10);


    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    /**
     * The progress bar indicating how much of the testcases already passed. 
     * As long as no error is found, this bar is green, 
     * after the first error, it is red. 
     */
    static class TestProgressBar extends JProgressBar {

	/* ---------------------------------------------------------------- *
	 * constants.                                                       *
	 * ---------------------------------------------------------------- */

	private static final long serialVersionUID = -2479143000061671589L;

	/**
	 * Represents the case that a testcase failed. 
	 */
	private static final Color COLOR_FAIL    = Color.red;

	/**
	 * Represents the case that so far no testcase failed. 
	 * Maybe: **** introduce yellow for ignored testcases. 
	 * see COLOR_IGNORED
	 */
	private static final Color COLOR_OK      = Color.green;

	/**
	 * Represents the case that so far no testcase failed 
	 * but some is ignored. **** not yet used. 
	 */
	private static final Color COLOR_IGNORED = Color.yellow;

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */


	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new <code>TestProgressBar</code> instance. 
	 */
	public TestProgressBar() {
	    super(new DefaultBoundedRangeModel());
	    this.model.setValueIsAdjusting(true);
	    //.setString("progress");//*** even better; fail or ok
	    //.setStringPainted(true);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	void start(Description desc) {
	    setMinimum(0);
	    setMaximum(desc.testCount());
	    //this.model.setExtent(0);/// **** how much is visible: 
	    //   minimum <= value <= value+extent <= maximum 
	    reset();
	}

	void reset() {
	    setValue(0);
	    setForeground(COLOR_OK);
	}

	void incNumRunsDone(TestCase testCase) {
	    setValue(getValue() + 1);
	    if (testCase.hasFailed()) {
		setForeground(COLOR_FAIL);
	    }
	}

    } // class TestProgressBar 


    /**
     * To render a cell of the hierarchy tree. 
     */
    static class TestTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -2479143000061671589L;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new <code>TestTreeCellRenderer</code> instance.
	 * The user objects of the tree nodes are always testcases. 
	 * We display essentially the icon 
	 * attached to {@link TestCase#getQuality}. 
	 */
	public TestTreeCellRenderer() {
	    // is empty. 
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	public Component getTreeCellRendererComponent(JTree tree, 
						      Object value,
						      boolean sel, 
						      boolean expanded, 
						      boolean leaf, 
						      int row, 
						      boolean hasFocus) {

	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	    TestCase userObj = (TestCase)node.getUserObject();
	    setLeafIcon(userObj.getQuality().getIcon());

	    return super.getTreeCellRendererComponent(tree, 
						      value, 
						      sel, 
						      expanded, 
						      leaf, 
						      row, 
						      hasFocus);
	}
    } // class TestTreeCellRenderer 

    /**
     * Minimal interface for notifying about singular selection events. 
     * Implemented by {@link GUIRunner.HierarchyWrapper}, 
     * by {@link GUIRunner.TestCaseLister} and 
     * by {@link GUIRunner.TabChangeListener#EMPTY_SELECTOR}. 
     *
     * @see GUIRunner.TabChangeListener
     */
    static interface Selector {

	/**
	 * Sets selection of <code>index</code>th item 
	 * and clears other selections. 
	 *
	 * @param index 
	 *    a non-negative <code>int</code> value representing an index. 
	 */
	void setSelection(int index);

	/**
	 * Clears the selection.  
	 */
	void clearSelection();

	/**
	 * Acquaints this selector with another one 
	 * which is notified of the selection events 
	 * of this <code>Selector</code>. 
	 * The one in the foreground is notified directly by a mouse event, 
	 * whereas the one in the background is selected via registration. 
	 * The one in the background in turn 
	 * notifies the empty selector 
	 * {@link GUIRunner.TabChangeListener#EMPTY_SELECTOR} 
	 * which takes no actions. 
	 *
	 * @param sel 
	 *    another <code>Selector</code>. 
	 * @throws IllegalStateException
	 *    only for {@link GUIRunner.TabChangeListener#EMPTY_SELECTOR}. 
	 * @see GUIRunner.TabChangeListener#setSelUnSel(int)
	 */
	void registerSelector(Selector sel);

    } // interface Selector 

    static class TreePathIncrementor {

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	// is null after this is created. 
	private       TreePath currPath;
	private final TreeModel treeModel;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	TreePathIncrementor(JTree tree) {
	    this.currPath = null;// formally only 
	    this.treeModel = tree.getModel();
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	void setFirstPath() {
	    TreeNode lastNode = (TreeNode) this.treeModel.getRoot();
	    this.currPath = prolonguePath(new TreePath(lastNode));
	}

	// prolongues path as long as possible 
	// in each step with minimal child 
	private static TreePath prolonguePath(TreePath path) {
	    TreeNode lastNode = (TreeNode)path.getLastPathComponent();
	    while (!lastNode.isLeaf()) {
		// one has to add the 0th child of lastNode. 
		lastNode = lastNode.getChildAt(0);
		path = path.pathByAddingChild(lastNode);
	    }
	    assert lastNode.isLeaf();
	    return path;
	}

	private int shortenPath() {
	    TreeNode lastNode = (TreeNode)
		this.currPath.getLastPathComponent();
	    TreePath prefix = this.currPath.getParentPath();
	    TreeNode lastButOneNode = (TreeNode)
		prefix.getLastPathComponent();
	    int index = lastButOneNode.getIndex(lastNode);

	    while (index == lastButOneNode.getChildCount()-1) {
		this.currPath = prefix;
		lastNode = lastButOneNode;
		prefix = this.currPath.getParentPath();
		lastButOneNode = (TreeNode)prefix.getLastPathComponent();
		index = lastButOneNode.getIndex(lastNode);
	    }
	    return index;
	}

	TreePath incPath() {
	    int index = shortenPath();
	    TreePath prefix = this.currPath.getParentPath();
	    TreeNode lastButOneNode = 
		(TreeNode)prefix.getLastPathComponent();
	    TreeNode lastNode = lastButOneNode.getChildAt(index+1);
	    this.currPath = prefix.pathByAddingChild(lastNode);
	    this.currPath = prolonguePath(this.currPath);
	    return this.currPath;
	}

	TreePath getPath() {
	    return this.currPath;
	}
    } // class TreePathIncrementor 

    static class HierarchyWrapper 
	implements Selector, TreeSelectionListener {

	/* ---------------------------------------------------------------- *
	 * inner class.                                                     *
	 * ---------------------------------------------------------------- */

	enum TreeUpdater {

	    First() {
		void updatePath(HierarchyWrapper testHiererarchy) {
		    testHiererarchy.treeUpdater = Generic;
		    testHiererarchy.expandAlongPath();
		}
	    },
	    Generic() {
		void updatePath(HierarchyWrapper testHiererarchy) {
		    testHiererarchy.currPathInc.incPath();
		    testHiererarchy.expandAlongPath();
		}
	    };
	    abstract void updatePath(HierarchyWrapper testHiererarchy);
	} // enum TreeUpdater 

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */


	private final JTree hierarchyTree;

	// is null after this is created. 
	private TreePathIncrementor currPathInc;

	private Description desc;

	private TreeUpdater treeUpdater; // NOPMD 

	private final TreeSelectionModel treeSelection;

	// selector to be influenced. 
	private Selector selector;
	private final Actions actions;
	// the selected node. Is null if nothing selected. 
	private DefaultMutableTreeNode singleSelectedNode;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	HierarchyWrapper(Actions actions) {
	    assert SwingUtilities.isEventDispatchThread();

	    this.hierarchyTree = new JTree();
	    // generate selection model 
	    this.treeSelection = new DefaultTreeSelectionModel();
	    this.treeSelection.setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    this.hierarchyTree.setSelectionModel(this.treeSelection);
	    this.hierarchyTree.addTreeSelectionListener(this);
	    this.hierarchyTree.setRootVisible(false);
	    this.currPathInc = null;

	    TreeNode root = (TreeNode)this.hierarchyTree.getModel().getRoot();
	    this.hierarchyTree.setModel(new DefaultTreeModel(root));
	    this.actions = actions;
	    this.singleSelectedNode = null;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	// **** Descriptions instead of testcases 
	// bad: design and problem with selecting testcases not yet run. 
	// 
	private static 
	MutableTreeNode desc2treeNode(Description desc) {
//System.out.println("desc2treeNode(");
	    DefaultMutableTreeNode ret;
	    if (desc.isTest()) {
//System.out.println("ret1: "+ret);
		ret = new DefaultMutableTreeNode(new TestCase(desc));
		return ret;
	    } else {
		ret = new DefaultMutableTreeNode(new TestCase(desc));
	    }

	    List<Description> childrenIn = desc.getChildren();
//	    List<TreeNode> childrenOut = new ArrayList<TreeNode>();
	    MutableTreeNode childOut;
	    for (Description childIn : childrenIn) {
		childOut = desc2treeNode(childIn);
		//childOut.setParent(ret);
		ret.add(childOut);
	    }
	
	    return ret;
	}

	void updateSingular() {
	    ((DefaultTreeModel)this.hierarchyTree.getModel())
	    	.nodeChanged(this.singleSelectedNode);
	}

	/* ---------------------------------------------------------------- *
	 * further methods.                                                 *
	 * ---------------------------------------------------------------- */

	void start(Description desc) {
	    this.desc = desc;
	    reset();
	}

	void reset() {
	    MutableTreeNode treeNode = desc2treeNode(this.desc);
	    TreeModel newModel = new DefaultTreeModel(treeNode);
	    this.hierarchyTree.setModel(newModel);
	    this.hierarchyTree.setCellRenderer(new TestTreeCellRenderer());
	    //this.testHierarcy.update(this.testHierarcy.getGraphics());

	    this.currPathInc = 
		new TreePathIncrementor(this.hierarchyTree);
	    this.currPathInc.setFirstPath();
	    expandAlongPath();
	    this.treeUpdater = TreeUpdater.First;

//Tree  TreePath 	getLeadSelectionPath() 
//Tree   TreePath 	getSelectionPath() 
	}

	void expandAlongPath() {
	    this.hierarchyTree.expandPath
		(this.currPathInc.getPath().getParentPath());
	}

	void collapseAlongPath() {
	    this.hierarchyTree.collapsePath
		(this.currPathInc.getPath().getParentPath());
	}

	JTree getTree() {
	    return this.hierarchyTree;
	}

	// **** for tests only 
	TreePath getPath() {
	    return this.currPathInc.getPath();
	}

	void setResult(TestCase result) {
	    MutableTreeNode lastNode = (MutableTreeNode)
		this.currPathInc.getPath().getLastPathComponent();
	    lastNode.setUserObject(result);
	    ((DefaultTreeModel)this.hierarchyTree.getModel())
	    	.nodeChanged(lastNode);
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing Selector.                                   *
	 * ---------------------------------------------------------------- */

	public void setSelection(int index) {
	    TreePathIncrementor inc = 
		new TreePathIncrementor(this.hierarchyTree);
	    inc.setFirstPath();
	    for (int i = 0; i < index; i++) {
		inc.incPath();
	    }
	    this.treeSelection.addSelectionPath(inc.getPath());
	    //**** still a problem with update 
	    // what follows does not solve the problem. 
	    //this.frame.update(this.frame.getGraphics());
	}

	public void clearSelection() {
	    this.treeSelection.clearSelection();
	}

	public void registerSelector(Selector selector) {
	    this.selector = selector;
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing TreeSelectionListener.                      *
	 * ---------------------------------------------------------------- */

	public void valueChanged(TreeSelectionEvent selEvent) {
//public boolean isAddedPath(TreePath path)
//public boolean isAddedPath(int index)
// indicate whether 
// true:  formerly not selected but now selected, or 
// false: the other way round. 

//public TreePath[] getPaths()


	    // paths is a list of paths with changed selection 
	    TreePath[] paths = selEvent.getPaths();

	    DefaultMutableTreeNode lastNode;
	    int index;
	    TestCase testCase;
	    for (int i = 0; i < paths.length; i++) {
		lastNode = (DefaultMutableTreeNode)
		    paths[i].getLastPathComponent();


		if (selEvent.isAddedPath(paths[i])) {
		    // Here, paths[i] has been selected 
		    

		    // Here one has to notify 
		    // the demanded description. 

		    // ****

		    if (lastNode.isLeaf()) {
			testCase = (TestCase)lastNode.getUserObject();
			if (!testCase.getQuality().isDecided()) {
			    treeSelection.clearSelection();
			    this.selector.clearSelection();
			    continue;
			}

			// ***** for the moment better here. 
			this.actions.setSingleTest(testCase);
			this.singleSelectedNode = lastNode;
			index = testCase.getNum();
			this.selector.setSelection(index);
		    } else {
			// **** for the moment: other selections: rebuild all 
			this.actions.setSingleTest(null);
			this.singleSelectedNode = null;
			this.selector.clearSelection();
		    }
		    continue;
		}
		// Here, paths[i] has been deselected (continue)
	    } // for

	    //selEvent.isAddedPath(index);
	}

    } // HierarchyWrapper 

    /**
     * Represents the table displaying the number of runs, 
     * both, passed and to be performed altogether, 
     * the tests already ignored and those a failure or an error was found. 
     * <p>
     * The numbers {@link #numRunsDone}, {@link #numRuns}, {@link #numIgn},
     * {@link #numFails} and {@link #numExc} 
     * represent the numbe of runs done, the number of runs done, 
     * the overall number of runs, done or not, 
     * the number of ignored tests of failed tests and of tests in error, 
     * i.e. which caused an error or an exception. 
     */
    static class RunsErrorsFailures extends JComponent {

	private static final long serialVersionUID = -2479143000061671589L;

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */


	private final JLabel runs;
	private final JLabel ignored;
	private final JLabel failures;
	private final JLabel errors;

	/**
	 * The number of runs already finished. 
	 */
	private int numRunsDone;

	/**
	 * The overall number of runs, to be done, 
	 * in execution or not yet started. 
	 * The constructor initializes this with <code>0</code>. 
	 */
	private int numRuns;

	/**
	 * The number of runs already identified as ignored. 
	 */
	private int numIgn;

	/**
	 * The number of runs already failed. 
	 * This does not include the runs with exception or error. 
	 *
	 * @see #numExc
	 */
	private int numFails;

	/**
	 * The number of runs ended with exception or error. 
	 * These tests did not fail, but the tests could not be executed. 
	 *
	 * @see #numFails
	 */
	private int numExc;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	RunsErrorsFailures() {
	    super();

	    this.runs     = new JLabel();
	    this.ignored  = new JLabel();
	    this.failures = new JLabel();
	    this.errors   = new JLabel();

	    this.numRuns  = 0;// formally. This is set by #start()
	    reset();
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Returns a horizontal box with the labels 
	 * {@link #runs}, {@link #ignored}, 
	 * {@link #failures} and {@link #errors} 
	 * intermangled with according icons. 
	 */
	Box getBox() {
	    Box res = Box.createHorizontalBox();
	    res.add(this.runs);
	    res.add(new JLabel(Quality.Ignored.getIcon()));
	    res.add(this.ignored);
	    res.add(new JLabel(Quality.Failure.getIcon()));
	    res.add(this.failures);
	    res.add(new JLabel(Quality.Error  .getIcon()));
	    res.add(this.errors);
	    res.add(Box.createGlue());
	    return res;
	}

	/**
	 * Initiates {@link #numRuns} 
	 * with the testcount from <code>desc</code> 
	 * and resets this component invoking {@link #reset()}. 
	 */
	void start(Description desc) {
	    this.numRuns = desc.testCount();
	    reset();
	}

	/**
	 * Resets all counters to <code>0</code> except {@link #numRuns} 
	 * and updates all labels invoking {@link #updateLabels()}. 
	 */
	void reset() {
	    this.numRunsDone = 0;
	    this.numIgn      = 0;
	    this.numFails    = 0;
	    this.numExc      = 0;
	    updateLabels();
	}

	/**
	 * Updates all counters and labels after a test has been run 
	 * according to its <code>result</code>: 
	 * It may succeed, be ignored, had a failure or 
	 * could not be executed due to an exception or an error. 
	 *
	 * @see TestCase#getQuality()
	 */
	void incNumRunsDone(TestCase result) {
	    this.numRunsDone++;
	    switch (result.getQuality()) {
		case Success:
		    // nothing to Do
		    break;
		case Ignored:
		    this.numIgn++;
		    break;
		case Failure:
		    this.numFails++;
		    break;
		case Error:
		    this.numExc++;
		    break;
		default:
		    throw new IllegalStateException();

	    }
	    updateLabels();
	}

	/**
	 * Updates all labels if a counter has changed. 
	 * This is invoked by {@link #reset()} 
	 * and by {@link #incNumRunsDone(TestCase)}. 
	 */
	private void updateLabels() {
	    this.runs    .setText("Runs: "     + this.numRunsDone + 
				  "/"          + this.numRuns + "    ");
	    this.ignored .setText("Ignored: "  + this.numIgn + "    ");
	    this.failures.setText("Failures: " + this.numFails + "    ");
	    this.errors  .setText("Errors: "   + this.numExc);

/*
	    setText("Runs: " + this.numRunsDone + "/" + this.numRuns + 
		    "    Ignored: " + this.numIgn +
		    "    Failures: " + this.numFails +
		    "    Errors: " + this.numExc);
*/
	}
    } // class RunsErrorsFailures 

    /**
     * Represents the list of testcases already failed 
     * shown in one of the tabs 
     * and a {@link #stackTraceLister} 
     * which represents the stack trace box below the tabbed pane. 
     */
    class TestCaseLister implements ListSelectionListener, Selector {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	// now not only failures but ALL testcases 
	private final List<TestCase> testsDoneList;

	private final ListSelectionModel failureSelection;

	private final DefaultListModel<TestCase> failureListMod;

	private final StackTraceLister stackTraceLister;

	// selector to be influenced. 
	private Selector selector;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	TestCaseLister() {
	    // init testsDoneList 
	    this.testsDoneList = new ArrayList<TestCase>();
	    // init failureSelection 
	    this.failureSelection = new DefaultListSelectionModel();
	    this.failureSelection
		.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    this.failureSelection.setValueIsAdjusting(true);
	    this.failureSelection
		.addListSelectionListener(TestCaseLister.this);
	    // init failureListMod 
	    this.failureListMod = new DefaultListModel<TestCase>();
	    // init stacktrace 
	    this.stackTraceLister = new StackTraceLister();
	}


	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	JList<TestCase> getFailList() {
	    JList<TestCase> jFailureList = 
		new JList<TestCase>(this.failureListMod);
	    jFailureList.setCellRenderer(new TestListCellRenderer());
	    jFailureList.setSelectionModel(this.failureSelection);
	    return jFailureList;
	}

	Component getStackTraceBox() {
	    return this.stackTraceLister.getStackTraceBox();
	}

	/* ---------------------------------------------------------------- *
	 * further methods.                                                 *
	 * ---------------------------------------------------------------- */


	void start(Description desc) {
	    reset();
	}

	void reset() {
	    this.testsDoneList   .clear();
	    this.failureSelection.clearSelection();
	    this.failureListMod  .clear();
	    this.stackTraceLister.clearStack();
	}

	void updateSingular(TestCase testCase) {
	    if (testCase.hasFailed()) {
		this.stackTraceLister.setStack(testCase.getException());
	    } else {
		this.stackTraceLister.clearStack();
	    }
	    updateG();
	}

	boolean toBeAdded(TestCase result) {
	    return result.getException() != null;
	}

	void recordTestDone(TestCase result) {
	    // the ordering is important! 
	    this.testsDoneList.add(result);
	    if (!toBeAdded(result)) {
		return;
	    }

	    this.failureListMod.addElement(result);
	    if (this.failureListMod.getSize() == 1) {
		this.failureSelection.setSelectionInterval(0,0);
	    }
	    // the following is superfluous: triggered by selection event
	    //this.stackTraceLister.setStack(thrw);
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing Selector.                                   *
	 * ---------------------------------------------------------------- */

	// index starts with 0 
	public void setSelection(int index) {
	    TestCase testCase = this.testsDoneList.get(index);
	    if (!toBeAdded(testCase)) {
		clearSelection();
		return;
	    }

	    int indAdded = 0;
	    for (int i = 0; i < index; i++) {
		if (toBeAdded(this.testsDoneList.get(i))) {
		    indAdded++;
		}
	    }

	    this.failureSelection.setSelectionInterval(indAdded,indAdded);
	}

	public void clearSelection() {
	    this.failureSelection.clearSelection();
	    this.stackTraceLister.clearStack();
	}

	public void registerSelector(Selector selector) {
	    this.selector = selector;
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing ListSelectionListener.                      *
	 * ---------------------------------------------------------------- */


	public void valueChanged(ListSelectionEvent lse) {
	    int selIndex = this.failureSelection.getMinSelectionIndex();
	    if (selIndex == -1) {
		// Here, the selection is empty. 
		this.stackTraceLister.clearStack();
		this.selector.clearSelection();
		return;
	    }
	    // Here, the selection consists of a single entry. 
	    TestCase testCase = this.failureListMod.getElementAt(selIndex);
	    this.selector.setSelection(testCase.getNum());
	    this.stackTraceLister.setStack(testCase.getException());
	    GUIRunner.this.splitPane.resetToPreferredSizes();
	}

    } // class TestCaseLister 

    /**
     * Represents the stack trace of the throwable, {@link #thrw} 
     * currently selected in the error list. 
     * The representation consists in 
     * the string representation {@link #thrwMessager} 
     * and in the stack trace given by {@link #stacktrace}. 
     * <p>
     * This class is also a {@link ListSelectionListener} 
     * which opens <code>emacsclient</code> 
     * with the source file and the line number 
     * given by the selected stack trace element. 
     */
    static class StackTraceLister implements ListSelectionListener {

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	/**
	 * Represents a throwable or is <code>null</code> 
	 * if no throwable is represented. 
	 * This is also the initial value. 
	 */
	private Throwable thrw;

	/**
	 * Is empty iff {@link #thrw} is <code>null</code> 
	 * and contains the string representation 
	 * of the represented throwable {@link #thrw}. 
	 */
	private final JLabel thrwMessager;

	/**
	 * Is either empty or contains the stacktrace
	 * of the represented throwable {@link #thrw}. 
	 */
	private final DefaultListModel<String> stacktrace;

	/**
	 * The selection of this stack trace: 
	 * This is either empty 
	 * (which is mandatory for empty {@link #thrwMessager}) 
	 * or selects a single stack element. 
	 * If so, by selection <code>emacsclient</code> is started 
	 * at the place the stack element points to. 
	 */
	private final ListSelectionModel stackElemSelection;


	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new StackTraceLister with empty throwable. 
	 */
	StackTraceLister() {
	    // init thrw and its string representation. 
	    this.thrw = null;
	    this.thrwMessager = new JLabel("", SwingConstants.LEADING);

	    // init stacktrace 
	    this.stacktrace = new DefaultListModel<String>();
	    // init stackElemSelection 
	    this.stackElemSelection = new DefaultListSelectionModel();
	    this.stackElemSelection
		.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    this.stackElemSelection.setValueIsAdjusting(true);
	    this.stackElemSelection
		.addListSelectionListener(StackTraceLister.this);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Returns a graphical representation of this StackTraceLister. 
	 */
	Component getStackTraceBox() {
	    JList<String> stacktraceList = new JList<String>(this.stacktrace);
	    stacktraceList.setSelectionModel(this.stackElemSelection);

	    Box stackTraceBox = Box.createVerticalBox();
	    stackTraceBox.add(this.thrwMessager);
	    stackTraceBox.add(new JScrollPane(stacktraceList));
	    return stackTraceBox;
	}

	/**
	 * Clears the represented stack including {@link #thrw} and text. 
	 */
	void clearStack() {
	    this.thrw = null;
	    this.thrwMessager.setText("");
	    //this.thrwMessager.setHorizontalAlignment(SwingConstants.LEADING);
	    this.stacktrace.clear();
	    this.stackElemSelection.clearSelection();
	}

	/**
	 * Represents the throwable <code>thrw</code> 
	 * if this is not <code>null</code>; 
	 * otherwise just clears this stack trace lister 
	 * as is done by {@link #clearStack()}. 
	 * The selection is cleared anyway. 
	 */
	void setStack(Throwable thrw) {
	    clearStack();
	    if (thrw == null) {
		// **** can only occur for rerun testcases 
		// which eventually succeed. 
		// is like clearing the stack trace 
		return;
	    }

	    this.thrw = thrw;
	    this.thrwMessager.setText(this.thrw.toString());
	    StackTraceElement[] stack = this.thrw.getStackTrace();
	    for (int i = 0; i < stack.length; i++) {
		this.stacktrace.addElement(stack[i].toString());
	    }
	}

	/**
	 * If an entry is selected, move with emacs to the according place. 
	 */
	// api-docs inherited from ListSelectionListener 
	public void valueChanged(ListSelectionEvent lse) {
	    // ***Here, the stacktrace cannot be empty. *** not right. 
	    int selIndex = this.stackElemSelection.getMinSelectionIndex();
	    if (selIndex == -1) {
		// this can come only from invoking clear. 
		// *** not ok because event is fired although nothing happened 
		//assert this.stacktrace.getSize() == 0;
		return;
	    }

	    StackTraceElement location = this.thrw.getStackTrace()[selIndex];
	    System.out.println("location: "+location);
	    JavaPath jPath = new JavaPath(System.getProperty("sourcepath"));
	    File toBeLoaded = jPath.getFile(location.getClassName(),
					    JavaPath.ClsSrc.Source);
	    if (toBeLoaded == null) {
		return;
	    }

	    // move with emacs to the selected position 
	    try {
		Runtime.getRuntime().exec(new String[] {
			"emacsclient", 
			"--no-wait", 
			"+" + location.getLineNumber(), 
			toBeLoaded.getPath()
		    },
		    // environment variables and working directory 
		    // inherited from the current process 
		    null, null);
	    } catch (IOException ioe) {
		System.err.println("Failed to invoke emacs. ");
		ioe.printStackTrace();
	    }
	}
    } // class StackTraceLister 

    static class TabChangeListener implements ChangeListener {

	/* ---------------------------------------------------------------- *
	 * constants.                                                       *
	 * ---------------------------------------------------------------- */

	/**
	 * The index of the tab selected initially. 
	 * This may be 0 or 1. 
	 * Note that the failure list is the 0th tab for some reason 
	 * It must be the one initially in the foreground. 
	 */
	private final static int SEL_IND1 = 1;

	private final static Selector EMPTY_SELECTOR = 
	    new Selector() {
		public void setSelection(int index) {
		    // is empty. 
		}
		public void clearSelection() {
		    // is empty. 
		}
		public void registerSelector(Selector sel) { 
		    throw new IllegalStateException();
		}
	    }; // EMPTY_SELECTOR

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	private final JTabbedPane tabbedPane;
	private final Selector[] selectors;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	TabChangeListener(JTabbedPane tabbedPane,
			  TestCaseLister testCaseLister,
			  HierarchyWrapper testHierarchy) {
	    this.tabbedPane = tabbedPane;
	    this.selectors = new Selector[] {
		testCaseLister,
		testHierarchy,
	    };
	    setSelUnSel(SEL_IND1);
	}

	/**
	 * Makes the tab with the given index in the foreground 
	 * and the other one in the background. 
	 * This must be invoked 
	 * with the index of the tab initially in the foreground 
	 * and if the tab is changed, this methd must be invoked accordingly. 
	 * <p>
	 * As described 
	 * for {@link GUIRunner.Selector#registerSelector(Selector)}, 
	 * the tab in the foreground receives its selection events directly, 
	 * whereas the one in the background 
	 * must be registered at the one in the foreground 
	 * to receive the according selections. 
	 * For sake of unification, the one in the background also sends 
	 * selection events to the one which is registered, 
	 * but this is just {@link #EMPTY_SELECTOR}. 
	 *
	 * @param index
	 *   the index of the tab/Selector in the foreground. 
	 *   This may be either 0 or 1. 
	 */
	private void setSelUnSel(int index) {
	    assert index == 0 || index == 1;
	    Selector    sel = selectors[  index];
	    Selector notSel = selectors[1-index];
	    sel   .registerSelector(notSel);
	    notSel.registerSelector(EMPTY_SELECTOR);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	public void stateChanged(ChangeEvent che) {
	    assert this.tabbedPane == che.getSource();
	    setSelUnSel(this.tabbedPane.getSelectedIndex());
	}
    } // class TabChangeListener 

    /**
     * Provides a method to choose a test class. 
     * This is triggered by clicking on the 'open' icon 
     * in the enclosing GUIRunner. 
     * Class ClassChooser is based 
     * on the {@link JFileChooser} {@link #clsFileChooser}
     * It is not static because of its access to {@link GUIRunner#frame}. 
     */
    class ClassChooser {

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	/**
	 * File chooser for class files representing test classes. 
	 */
	private final JFileChooser clsFileChooser;

	/**
	 * The class path for test classes. 
	 */
	private final JavaPath clsPath;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	ClassChooser() {
	    String classpath = System.getProperty("chooseClasspath");
	    this.clsPath = new JavaPath(classpath);
	    this.clsFileChooser = new JFileChooser(classpath);
	    this.clsFileChooser.setMultiSelectionEnabled(false);
	    this.clsFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    this.clsFileChooser.setFileHidingEnabled(true);
	    // File filter accepting directories 
	    // and files ending with class but without inner classes 
	    this.clsFileChooser.setFileFilter(new FileFilter() {
		    public boolean accept(File file) {
			if (!file.isFile()) {
			    // directories accepted 
			    return true;
			}
			String name = file.getName();
			// file accepted if class file but no inner class 
			return name.endsWith(".class") && !name.contains("$");
		    }
		    public String getDescription() {
			return "Java class files ending with .class ";
		    }
		});
	    this.clsFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    this.clsFileChooser.setDialogTitle("Testclasses");
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */


	/**
	 * Opens the class chooser dialog 
	 * and returns the choosen class or <code>null</code> 
	 * if either no file was selected, 
	 * a file is selected which does not exist 
	 * or does not represent a java class file. 
	 */
	String getChosenClass() {
	    if (this.clsFileChooser.showOpenDialog(GUIRunner.this.frame) != 
		JFileChooser.APPROVE_OPTION) {
		return null;
	    }
	    File clsFile = this.clsFileChooser.getSelectedFile();
	    if (!clsFile.exists()) {
		return null;
	    }
	    String clsName = this.clsPath.absFile2cls(clsFile,
						      JavaPath.ClsSrc.Class);
	    return clsName;
	}
    } // class ClassChooser 



    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    /**
     * The frame in which the Testrunne GUI is displayed. 
     * Since this is the outermost component, 
     * this is used in {@link GUIRunner.ClassChooser} 
     * and in {@link #updateG()}. 
     */
    private final JFrame frame;

    /**
     * A chooser for testclasses. 
     */
    private final ClassChooser classChooser;

    /**
     * Contains the fully qualified name of the testclasse 
     * currently under consideration. 
     */
    private final JLabel className;

    /**
     * The progress bar indicating how much of the testcases already passed. 
     */
    private final TestProgressBar progress;

    /**
     * Represents the table displaying the number of runs, 
     * both passed and to be performed altogether, 
     * the tests already ignored and those in which an error was found. 
     */
    private final RunsErrorsFailures runsErrorsFailures;

    /**
     * Represents the list of testcases already failed. 
     * This is shown in one of the tabs. 
     */
    private TestCaseLister testCaseLister;

    /**
     * Represents the hierarchy of testcases. 
     * This is shown in one of the tabs. 
     */
    private HierarchyWrapper testHierarchy;

    /**
     * Represents the split pane with a tabbed pane as top component 
     * and the stack trace box as bottom component. 
     * The tabbed pane contains a tab for the {@link #testCaseLister} 
     * and another one for the {@link #testHierarchy}. 
     * The stack trace box is given by 
     * {@link GuiRunner.TestCaseLister#getStackTraceBox()}. 
     * <p>
     * This field is used to reset to preferred size. 
     */
    private JSplitPane splitPane;

    /**
     * Contains a status message. 
     */
    private final JLabel statusBar;


    /* -------------------------------------------------------------------- *
     * constructor with its methods.                                        *
     * -------------------------------------------------------------------- */

    /**
     * Opens the class chooser dialog 
     * and returns the choosen class or <code>null</code> 
     * if either no file was selected, 
     * a file is selected which does not exist 
     * or does not represent a java class file. 
     * <p>
     * Essentially delegates 
     * to {@link GUIRunner.ClassChooser#getChosenClass()}. 
     */
    String openClassChooser() {
	return this.classChooser.getChosenClass();
    }

    /**
     * Creates a new <code>GUIRunner</code> instance.
     */
    public GUIRunner(Actions actions) {
	assert SwingUtilities.isEventDispatchThread();
	this.frame = new JFrame("JUnit GUI");
System.out.println("smallLogoIcon.getImage()"+smallLogoIcon.getImage());
	this.frame.setIconImage(smallLogoIcon.getImage());
	this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	this.classChooser = new ClassChooser();

	setMenuBar(actions);


	this.className = new JLabel("class name");


	this.statusBar = new JLabel("status bar");

	Container content = this.frame.getContentPane();
	content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
	//content.setLayout(new BorderLayout());


	this.runsErrorsFailures = new RunsErrorsFailures();


	setCenter(actions);

	Box testClassName = Box.createHorizontalBox();
	// add classname 
	testClassName.add(new JLabel("Test class name:    "));
	testClassName.add(this.className);
	testClassName.add(Box.createHorizontalGlue());
 	this.frame.add(testClassName);
 	//this.frame.add(new JLabel("Test class name:    "));
 	//this.frame.add(this.className);

	// add progress bar 
	Box progLogo = Box.createHorizontalBox();
	this.progress = new TestProgressBar();
	progLogo.add(this.progress);
	progLogo.add(Box.createHorizontalStrut(20));
	progLogo.add(new JLabel(logoIcon));
	progLogo.add(horizBoundary);
	this.frame.add(progLogo);

	// add separator and line with runs ignored, failures and errors
 	this.frame.add(this.runsErrorsFailures.getBox());
 	this.frame.add(new JSeparator());
	this.frame.add(Box.createVerticalStrut(10));

	this.frame.add(Box.createVerticalGlue());

	// add results-line 
	Box results = Box.createHorizontalBox();
	results.add(horizBoundary);
	results.add(new JLabel("Results:"));
	results.add(Box.createGlue());
	this.frame.add(results);
 	//this.frame.add(new JLabel("Results:"));

	//this.frame.add(Box.createGlue());

	// add split pane and Run2-button 
	Box resRun2 = Box.createHorizontalBox();
	resRun2.add(horizBoundary);
	resRun2.add(this.splitPane);
	resRun2.add(Box.createGlue());
	resRun2.add(new JButton("Run2"));
	resRun2.add(horizBoundary);
	this.frame.add(resRun2);
//this.frame.add(this.splitPane);

	// add status bar and Exit-button 
	Box statExit = Box.createHorizontalBox();
	statExit.add(horizBoundary);
	statExit.add(this.statusBar);
	statExit.add(Box.createGlue());
	statExit.add(new JButton("Exit"));
	statExit.add(horizBoundary);

 	this.frame.add(statExit);

	this.frame.setSize(800,800);
	//this.frame.pack();
	this.frame.setVisible(true);
    }

    final void setMenuBar(Actions actions) {
	JMenuBar menubar = new JMenuBar();
	//JMenu file = new JMenu("File");
	//menubar.add(file);

	menubar.add(new JMenuItem(actions.getOpenAction()));
	menubar.add(new JMenuItem(actions.getStartAction()));
	menubar.add(new JMenuItem(actions.getStopAction()));
	menubar.add(new JMenuItem(actions.getBreakAction()));
	menubar.add(new JMenuItem(actions.getExitAction()));
	// **** hack to have buttons aligned left 
	menubar.add(new JLabel("                                           "));

// 	menubar.add(new JMenuItem(exit));
// 	menubar.add(new JMenu("help"));


	//JMenu help = new JMenu("Help");
	//menubar.setHelpMenu(help);

	this.frame.setJMenuBar(menubar);
    }

    final void setCenter(Actions actions) {
	final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
	// add FailureList 
	this.testCaseLister = new TestCaseLister();
	tabbedPane.addTab("Failures",
			  Quality.Error.getIcon(),
			  new JScrollPane(this.testCaseLister.getFailList()));
	// add Tree as second because otherwise a problem becomes visible 
	this.testHierarchy = new HierarchyWrapper(actions);
	tabbedPane.addTab("Test Hierarchy",
			  hierarchyIcon,
			  new JScrollPane(this.testHierarchy.getTree()));


	tabbedPane
	    .addChangeListener(new TabChangeListener(tabbedPane,
						     this.testCaseLister,
						     this.testHierarchy));

	// make up SplitPane 
	this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	this.splitPane.setTopComponent(tabbedPane);
	this.splitPane.setBottomComponent
	    (this.testCaseLister.getStackTraceBox());
    }

    /* -------------------------------------------------------------------- *
     * methods not tied to constructor.                                     *
     * -------------------------------------------------------------------- */

    private void updateG() {
	this.frame.update(this.frame.getGraphics());
    }

    // invoked if a test is started or ignored. 
    void noteTestStartedI(TestCase testCase) {
//	assert SwingUtilities.isEventDispatchThread();
	setStatus(testCase);
	this.testHierarchy.treeUpdater.updatePath(this.testHierarchy);
	this.testHierarchy.setResult(testCase);// sounds strange. 
    }

    /**
     * Sets the message <code>msg</code> to the status bar. 
     */
    void setStatus(String msg) {
//	assert SwingUtilities.isEventDispatchThread();
	this.statusBar.setText(msg);
	updateG();
    }

    /**
     * Sets a status message describing <code>testCase</code> 
     * to the status bar using {@link #setStatus(String)}. 
     */
    private void setStatus(TestCase testCase) {
	setStatus("Test " + 
		  testCase.getQuality().status() + ": " + 
		  testCase.getDesc());
	//this.frame.update();
    }

    // invoked if a test is finished, whether successful or not 
    // or after noteTestStartedI(TestCase) if it is ignored 
    void noteReportResult(TestCase testCase) {
//	assert SwingUtilities.isEventDispatchThread();
	if (testCase.isSuccess()) {
	    this.testHierarchy.collapseAlongPath();
	}
	setStatus                             (testCase);
	this.testHierarchy     .setResult     (testCase);
	this.progress          .incNumRunsDone(testCase);
	this.runsErrorsFailures.incNumRunsDone(testCase);
	this.testCaseLister    .recordTestDone(testCase);

	this.splitPane         .resetToPreferredSizes();
    }

    void resetTestCaseLister() {
	this.testCaseLister.reset();
    }

    /**
     * Called before any tests have been run. 
     * Essentially distributes <code>desc</code> to various components. 
     *
     * @param desc 
     *    describes the tests to be run
     */
    void testRunStarted(final Description desc) {
//	assert SwingUtilities.isEventDispatchThread();
	// **** strange way to obtain the classname ***** 
	setStatus("testRunStarted(");
	this.className.setText(desc.getChildren().get(0).toString());

	this.progress          .start(desc);
	this.runsErrorsFailures.start(desc);
	this.testHierarchy     .start(desc);
	this.testCaseLister    .start(desc);// does not depend on desc: no fail
    }

    void updateSingularStarted() {
//	assert SwingUtilities.isEventDispatchThread();
	this.testHierarchy.updateSingular();
    }
    void updateSingularFinished(TestCase testCase) {
//	assert SwingUtilities.isEventDispatchThread();
	this.testCaseLister.updateSingular(testCase);
    }

} // NOPMD coupling is not that high!

