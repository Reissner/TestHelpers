package eu.simuline.testhelpers;

import eu.simuline.util.GifResource;

import eu.simuline.junit.Logo;
import eu.simuline.junit.Hierarchy;
import eu.simuline.junit.SmallLogo;

import eu.simuline.util.JavaPath;

import org.junit.runner.Description;

import java.awt.Container;
import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;

import java.util.List;

import java.io.IOException;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
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
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


import java.io.Serializable;

import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.Collection;

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
 * This is an instance of the class {@link GUIRunner.StatisticsTestState}. 
 * <li>
 * a treeview on the testsuite 
 * represented by the class {@link GUIRunner.HierarchyWrapper}. 
 * This needs support from the classes and 
 * {@link GUIRunner.TreePathIterator}, 
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
@SuppressWarnings("PMD.ExcessiveClassLength")
class GUIRunner {

    /* -------------------------------------------------------------------- *
     * constants.                                                           *
     * -------------------------------------------------------------------- */


    /**
     * The (big) JUnit-logo. 
     */
    private static final ImageIcon  LOGO_ICON = 
	GifResource.getIcon(Logo.class);

    /**
     * The icon representing the hierarchy of tests: 
     * used for the tabbed pane. 
     */
    private static final ImageIcon HIERARCHY_ICON = 
	GifResource.getIcon(Hierarchy.class);

    /**
     * The small JUnit-logo on top left of this frame. 
     * **** still this is not displayed properly ****. 
     */
    private static final ImageIcon SMALL_LOGO_ICON = 
	GifResource.getIcon(SmallLogo.class);

    /**
     * Represents the horizontal space used for the boundary. 
     */
    private static final Component HORIZ_BOUNDARY = 
	Box.createHorizontalStrut(10);

    /**
     * Represents double of the horizontal space used for the boundary. 
     */
    private static final Component HORIZ_BOUNDARY2 = 
	Box.createHorizontalStrut(20);

    /**
     * Represents the vertical space used for the boundary. 
     */
    private static final Component VERTI_BOUNDARY = 
	Box.createVerticalStrut(10);

    /**
     * The horizontal size of the frame. 
     */
    private static final int HORIZ_FRAME = 800;

    /**
     * The vertical size of the frame. 
     */
    private static final int VERTI_FRAME = 800;



    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    /**
     * The progress bar indicating how much of the testcases already passed. 
     * After the first testcase ending irregular, 
     * the bar takes {@link Quality#COLOR_FAIL}. 
     * Else after the first ignored testscase, 
     * the bar takes {@link Quality#COLOR_IGNORED}. 
     * Else, the bar takes {@link Quality#COLOR_OK}. 
     */
    static class TestProgressBar extends JProgressBar {

	/* ---------------------------------------------------------------- *
	 * constants.                                                       *
	 * ---------------------------------------------------------------- */

	private static final long serialVersionUID = -2479143000061671589L;

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	/**
	 * The maximal quality found in testcases so far. 
	 * This determines the foreground color of this progress bar. 
	 * This is <code>null</code> initially 
	 * and initialized in {@link #startTestRun(TestCase)}. 
	 */
	private Quality qual;


	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new <code>TestProgressBar</code> instance. 
	 */
	TestProgressBar() {
	    super(new DefaultBoundedRangeModel());
	    this.model.setValueIsAdjusting(true);
	    this.qual = null;
	    //.setString("progress");//*** even better; fail or ok
	    //.setStringPainted(true);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Notifies that the structure of the test class may have been updated. 
	 * <p>
	 * Sets maximum, minimum an current value of the progress bar. 
	 * Sets {@link #qual} to {@link Quality#Scheduled} 
	 * indicating that no failure occurred yet 
	 * and the color is the ok-color. 
	 *
	 * @param desc
	 *    a description of the test structure defined in the test class 
	 *    which is a hierarchy of suites and singular tests. 
	 */
	void initClassStructure(Description desc) {
	    setMinimum(0);
	    setMaximum(desc.testCount());
	    setValue(0);
	    this.qual = Quality.Scheduled; // color OK 
	    //this.model.setExtent(0);/// **** how much is visible: 
	    //   minimum <= value <= value+extent <= maximum 
	}

	// for TestProgressBar 
	/**
	 * Notifies that a test run given by <code>testCase</code> 
	 * is going to be run next. 
	 * <p>
	 * Only the tests which are {@link Quality#Scheduled} are really run. 
	 * The others just go into the length and color of the progress bar 
	 * when starting the run: 
	 * The initial length depends on the number of non-scheduled testcases 
	 * and the color is the worst-case color of all these testcases. 
	 *
	 * @param testCase
	 *    the hierarchy of all tests defined by the test class 
	 *    and in particular of those to be run: 
	 *    those which are {@link Quality#Scheduled}. 
	 */
	void startTestRun(TestCase testCase) {
	    this.qual = Quality.Scheduled; // color OK 
	    setValue(0);
	    detValQualRec(testCase);
	    setForeground(this.qual.getColor());
	}

	/**
	 * Determines recursively the length of the progress bar 
	 * and {@link #qual} based on the non-scheduled test cases. 
	 *
	 * @param testCase
	 *    the hierarchy of all tests defined by the test class 
	 *    and in particular of those to be run: 
	 *    those which are {@link Quality#Scheduled}. 
	 */
	private void detValQualRec(TestCase testCase) {
	    if (testCase.isTest()) {
		Quality qual = testCase.getQuality();
		switch (qual) {
		case Started:
		    assert false;
		    break;
		case Scheduled:
		    break;
		default:
		    // all but Scheduled and Started: 
		    setValue(getValue() + 1);
		    this.qual = this.qual.max(qual);
		    break;
		}

		return;
	    }
	    for (TestCase child : testCase.getChildren()) {
		detValQualRec(child);
	    }
	}

	/**
	 * Notifies that the singular test <code>testCase</code> is finished. 
	 * <p>
	 * Pushes the progress bar one further 
	 * and upates the color of the progress bar 
	 * as described in {@link GUIRunner.TestProgressBar}. 
	 *
	 * @param testCase
	 *    The testcase comprising the result of the singular test finished. 
	 */
	void noteReportResult(TestCase testCase) {
	    setValue(getValue() + 1);
	    this.qual = this.qual.max(testCase.getQuality());
	    setForeground(this.qual.getColor());
	}

    } // class TestProgressBar 


    /**
     * To render a cell of the hierarchy tree. 
     * The icon represents the state of the {@link TestCase}. 
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
	TestTreeCellRenderer() {
	    // is empty. 
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Renders the <code>value</code> interpreting it as Node 
	 * the user object of which is a {@link TestCase}. 
	 * Rendering is by setting the icon 
	 * associated with the state of the @link TestCase}. 
	 */
	public Component getTreeCellRendererComponent(JTree tree, 
						      Object value,
						      boolean sel, 
						      boolean expanded, 
						      boolean leaf, 
						      int row, 
						      boolean hasFocus) {

	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	    TestCase userObj = (TestCase) node.getUserObject();
	    assert userObj != null;
	    if (userObj.getQuality() != null) {
		// node is a leaf 
		setLeafIcon(userObj.getQuality().getIcon());
	    }

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
    interface Selector {

	/**
	 * Sets selection of <code>index</code>th item 
	 * and clears other selections. 
	 *
	 * @param index 
	 *    a non-negative <code>int</code> value 
	 *    representing the index of a testcase. 
	 * **** CAUTION: This presupposes that in a tree 
	 *    only the leaves can be selected. 
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

    /**
     * Represents a path {@link #currPath} 
     * in the tree of testsuites represented by {@link #treeModel}. 
     * This is initialized by {@link #setFirstPath()} 
     * to the uppermost complete path, 
     * can be incremented via {@link #incPath()} 
     * and be returned by {@link #getPath()}. 
     */
    static class TreePathIterator {


	/* ---------------------------------------------------------------- *
	 * inner class.                                                     *
	 * ---------------------------------------------------------------- */

	/**
	 * Expands the tree along the current path, 
	 * for {@link #Generic} after incrementing the current path. 
	 */
	enum TreePathUpdater {

	    First() {
		void updatePath(TreePathIterator treePathIter) {
		    treePathIter.treePathUpdater = Generic;
		    treePathIter.setFirstPath();
		}
	    },
	    Generic() {
		void updatePath(TreePathIterator treePathIter) {
		    treePathIter.incPath();
		}
	    };
	    abstract void updatePath(TreePathIterator treePathIter);
	} // enum TreePathUpdater 

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	/**
	 * A model of the tree of testsuites and tests. 
	 */
	private final TreeModel treeModel;

	// is null after this is created. 
	private       TreePath currPath;

	/**
	 * Decides how to update the current path: 
	 * For the first path, just inkoke ***** . 
	 */
	private TreePathUpdater treePathUpdater;



	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	// for index==0, treePathUpdater is First, 
	// setFirstPath not invoked and so like null-pointer 
	// and so after next update iterator points to 0th entry 
	// for index>0, treePathUpdater is Generic, 
	// iterator points to one before correct position 
	// and so aftter next update iterator points to index-th entry 
	TreePathIterator(JTree tree, int index) {
	    this.treeModel = tree.getModel();
	    this.currPath = null; // formally only 
	    this.treePathUpdater = TreePathUpdater.First;
	    for (int i = 0; i < index; i++) {
		updatePathI();
	    }
	}


	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	void updatePathI() {
	    this.treePathUpdater.updatePath(this);
	}

	/**
	 * Initializes {@link #currPath} 
	 * with the first path in {@link #treeModel}. 
	 * **** Shall be invoked by TreePathUpdater only **** 
	 */
	void setFirstPath() {
	    TreeNode lastNode = (TreeNode) this.treeModel.getRoot();
	    this.currPath = prolonguePath(new TreePath(lastNode));
	}

	/**
	 * Prolongues path as long as possible in each step with 0th child. 
	 */
	static TreePath prolonguePath(TreePath path) {
	    TreeNode lastNode = (TreeNode) path.getLastPathComponent();
	    while (!lastNode.isLeaf()) {
		// one has to add the 0th child of lastNode. 
		lastNode = lastNode.getChildAt(0);
		path = path.pathByAddingChild(lastNode);
	    }
	    assert lastNode.isLeaf();
	    return path;
	}

	/**
	 * Replaces {@link #currPath} removing the last node 
	 * as long as the last node in the path 
	 * is the last child of the last but one node 
	 * and after having done this 
	 * returns the index of the last node 
	 * as a child of the last but one node. 
	 * <p>
	 * CAUTION: This method shall be invoked only 
	 * if {@link #currPath} can be incremented. 
	 *
	 * @throws NullPointerException
	 *    if {@link #currPath} cannot be incremented. 
	 */
	private int shortenPath() {
	    TreeNode lastNode = (TreeNode)
		this.currPath.getLastPathComponent();
	    TreePath prefix = this.currPath.getParentPath();
	    TreeNode lastButOneNode = (TreeNode) prefix.getLastPathComponent();
	    int index = lastButOneNode.getIndex(lastNode);

	    while (index == lastButOneNode.getChildCount() - 1) {
		this.currPath = prefix;
		lastNode = lastButOneNode;
		// if currPath is the root, prefix is null 
		prefix = this.currPath.getParentPath();
		assert prefix != null : "tried to ";
		lastButOneNode = (TreeNode) prefix.getLastPathComponent();
		index = lastButOneNode.getIndex(lastNode);
	    }
	    return index;
	}

	// **** Shall be invoked by TreePathUpdater only **** 
	/**
	 * Increments {@link #currPath} and returns the result. 
	 */
	TreePath incPath() {
	    int index = shortenPath();
	    TreePath prefix = this.currPath.getParentPath();
	    TreeNode lastButOneNode = (TreeNode) prefix.getLastPathComponent();
	    TreeNode lastNode = lastButOneNode.getChildAt(index + 1);
	    this.currPath = prefix.pathByAddingChild(lastNode);
	    this.currPath = prolonguePath(this.currPath);
	    return this.currPath;
	}

	TreePath getPath() {
	    return this.currPath;
	}
    } // class TreePathIterator 

    /**
     * Represents the hierarchy of testsuites and testcases 
     * as a tree {@link #hierarchyTree} possibly with a single selected node 
     * given by {@link #singleSelectedNode}. 
     */
    static class HierarchyWrapper 
	implements Selector, TreeSelectionListener {


	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */


	/**
	 * The hierarchy of testsuites and testcases as a tree. 
	 * After creation this is a default tree, 
	 * after invocation of {@link #initClassStructure(TestCase)} 
	 * the hierarchy reflects the given testcase. 
	 * The selection model is given by {@link #treeSelection} 
	 * and this is the {@link TreeSelectionListener}. 
	 */
	private final JTree hierarchyTree;

	/**
	 * The selection model for {@link #hierarchyTree}. 
	 * At most one entry is selected. 
	 * If the entry represents a failure, the according element in 
	 * {@link GUIRunner.TestCaseLister#failureSelection} is selected also. 
	 *
	 * @see #valueChanged(TreeSelectionEvent)
	 */
	private final TreeSelectionModel treeSelection;

	/**
	 * This is used only by {@link #valueChanged(TreeSelectionEvent)} 
	 * to set the filter via 
	 * {@link Actions#setFilter(Description)}. 
	 */
	private final Actions actions;

	/**
	 * The the {@link TestCaseLister} 
	 * listing the failed test cases. 
	 * This is used in {@link #noteReportResult(TestCase)} only 
	 * and is to notify that a certain tescase failed 
	 * and is selected. 
	 */
	private final TestCaseLister testCaseLister;

	/**
	 * Represents the selected node in {@link #hierarchyTree}. 
	 * This is <code>null</code> if nothing selected 
	 * which is also the initial value. 
	 * This is set by {@link #valueChanged(TreeSelectionEvent)} 
	 * and updated also by {@link #initClassStructure(TestCase)}. 
	 */
	private DefaultMutableTreeNode singleSelectedNode;

	/**
	 * Represents the path to the testcase currently run. 
	 * This is <code>null</code> after this is created 
	 * and is initialized by {@link #startTestRun(Description)}. 
	 */
	private TreePathIterator currPathIter;

	/**
	 * Selector to be influenced: 
	 * If this is in the selected tab, {@link #selector} 
	 * is the tab with the {@link GUIRunner.TestCaseLister TestCaseLister}; 
	 * otherwise it is {@link GUIRunner.TabChangeListener#EMPTY_SELECTOR}. 
	 * Set by {@link #registerSelector(GUIRunner.Selector)}. 
	 */
	private Selector selector;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new HierarchyWrapper 
	 * with the given <code>actions</code> and <code>testCaseLister</code>
	 * which are used 
	 * to initialize {@link #actions} and {@link #testCaseLister}. 
	 *
	 * @param actions
	 *    the Actions to be written into {@link #actions}. 
	 * @param testCaseLister
	 *    TestCaseLister to be written into {@link #testCaseLister}. 
	 */
	HierarchyWrapper(Actions actions,
			 TestCaseLister testCaseLister) {
	    assert SwingUtilities.isEventDispatchThread();

	    this.hierarchyTree = new JTree();
	    // generate selection model 
	    this.treeSelection = new DefaultTreeSelectionModel();
	    this.treeSelection.setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    this.hierarchyTree.setSelectionModel(this.treeSelection);
	    this.hierarchyTree.addTreeSelectionListener(this);
	    this.hierarchyTree.setRootVisible(true);

	    TreeNode root = (TreeNode) this.hierarchyTree.getModel().getRoot();
	    this.hierarchyTree.setModel(new DefaultTreeModel(root));

	    assert actions != null;
	    this.actions = actions;
	    assert testCaseLister != null;
	    this.testCaseLister = testCaseLister;

	    // purely formally 
	    this.singleSelectedNode = null;
	    this.currPathIter       = null;
	    this.selector           = null;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Converts the (tree of) testcases given by <code>testCase</code>. 
	 * **** 
	 */
	private static 
	    DefaultMutableTreeNode testCase2treeNode(TestCase testCase) {
	    if (testCase.isTest()) {
		return new DefaultMutableTreeNode(testCase);
	    }
	    DefaultMutableTreeNode ret = new DefaultMutableTreeNode(testCase);
	    List<TestCase> childrenIn = testCase.getChildren();
	    DefaultMutableTreeNode childOut;
	    for (TestCase childIn : childrenIn) {
		childOut = testCase2treeNode(childIn);
		ret.add(childOut);
	    }
	    return ret;
	}

	TestCase getRoot() {
	    DefaultMutableTreeNode root = (DefaultMutableTreeNode)
		this.hierarchyTree.getModel().getRoot();
	    return (TestCase) root.getUserObject();
	}

	/**
	 * Returns the action. Invoked by the enclosing GUIRunner. 
	 *
	 * @see GUIRunner#testRunStarted(Description)
	 * @see GUIRunner#testRunFinished(long)
	 * @see GUIRunner#testRunAborted()
	 */
	Actions getActions() {
	    return this.actions;
	}

	/* ---------------------------------------------------------------- *
	 * further methods.                                                 *
	 * ---------------------------------------------------------------- */


	/**
	 * Notifies that the structure of the test class may have been updated. 
	 * <p>
	 * Converts the overall test case hierarchy <code>testCase</code> 
	 * into a a tree node hierarchy 
	 * invoking {@link #testCase2treeNode(TestCase)}. 
	 * This defines the tree model of the hierarchy {@link #hierarchyTree}. 
	 * The selected node {@link #singleSelectedNode} is set to the root. 
	 * <p>
	 * Since {@link #currPathIter} points to the first testcase run, 
	 * it is set to <code>null</code>. 
	 * Finally, {@link #actions} is notified about the test to be run next 
	 * which is specified by the testcase given by the selected node. 
	 * by invoking {@link #setFilter()}. 
	 *
	 * @param testCase
	 *    a description of the test structure defined in the test class 
	 *    which is a hierarchy of suites and singular tests. 
	 */
	void initClassStructure(TestCase testCase) {
	    this.singleSelectedNode = testCase2treeNode(testCase);
	    TreeModel newModel = new DefaultTreeModel(this.singleSelectedNode);
	    this.hierarchyTree.setModel(newModel);
	    // cell renderer after model. 
	    this.hierarchyTree.setCellRenderer(new TestTreeCellRenderer());

	    this.currPathIter = null;
	    setFilter();
	}

	// invoked by {@link #initClassStructure(TestCase)} 
	// and by {@link #valueChanged(TreeSelectionEvent)} 
	/**
	 * Sets the filter (given by a description) 
	 * invoking {@link Actions#setFilter(Description)}: 
	 * The description is taken from {@link #singleSelectedNode}. 
	 */
	private void setFilter() {
	    TestCase testCase = (TestCase) this.singleSelectedNode
		.getUserObject();
	    this.actions.setFilter(testCase.getDesc());
	}

	// for HierarchyWrapper 
	/**
	 * Notifies that a test run with structure given by <code>desc</code> 
	 * is going to be run next. 
	 * <p>
	 * Sets the quality of the testcases 
	 * given by {@link #singleSelectedNode} 
	 * recursively to {@link Quality#Scheduled} 
	 * and sets {@link #currPathIter}. 
	 *
	 * @param desc 
	 *    describes the (hierarchy of) tests to be run. 
	 *    This is a sub-hierarchy of the one given by the test class. 
	 *    This parameter is purely formally 
	 *    because it is given by {@link #singleSelectedNode}. 
	 */
	void startTestRun(Description desc) {
	    TestCase testCase = (TestCase)
		this.singleSelectedNode.getUserObject();
	    // no longer true: **** 
//assert testCase.getDesc().equals(desc);
//assert desc == testCase.getDesc();
	    testCase.setScheduledRec();

	    testCase = (TestCase) this.singleSelectedNode.getFirstLeaf()
		.getUserObject();
	    this.currPathIter = new TreePathIterator
		(this.hierarchyTree, testCase.getIdx());
	}

	/**
	 * Expands the path to the leaf {@link #currPathIter} points to. 
	 * This is invoked by {@link #noteTestStartedI(Quality)} 
	 * when a testcase is started or ignored. 
	 *
	 * @see #collapseAlongPath()
	 */
	void expandAlongPath() {
	    this.hierarchyTree.expandPath(this.currPathIter.getPath()
					  // that the last node is no leaf 
					  .getParentPath());
	}

	/**
	 * Collapses the path to the leaf {@link #currPathIter} points to 
	 * as much as possible in order not to hide {@link #singleSelectedNode} 
	 * and the leafs corresponding with singular tests 
	 * which failed already (assumption failure, failure and error). 
	 * This is invoked by {@link #noteReportResult(TestCase)} 
	 * when a testcase is finished or after being ignored. 
	 *
	 * @see #expandAlongPath()
	 */
	void collapseAlongPath() {
	    Collection<?> selPath = 
		Arrays.asList(this.singleSelectedNode.getPath());

	    TreePath treePath = this.currPathIter.getPath().getParentPath();
	    Object[] pathArr = treePath.getPath();
	    TestCase testCase;
	    DefaultMutableTreeNode node;
	    for (int idx = pathArr.length - 1; idx >= 0; idx--) {
		node = (DefaultMutableTreeNode) pathArr[idx];
		if (selPath.contains(node)) {
		    break;
		}

		testCase = (TestCase) node.getUserObject();

		if (testCase.fullSuccess()) {
		    Object[] pathArrNew = new Object[idx + 1]; // 0,...,idx 
		    System.arraycopy(pathArr, 0,
				     pathArrNew, 0,
				     pathArrNew.length);
		    treePath = new TreePath(pathArrNew);
		    assert treePath.getLastPathComponent() == pathArr[idx];
		    this.hierarchyTree.collapsePath(treePath);
		}
	    } // for 
	}

	JTree getTree() {
	    return this.hierarchyTree;
	}

	// **** for tests only 
	TreePath getPath() {
	    return this.currPathIter.getPath();
	}

	// invoked by noteTestStartedI(Quality)
	/**
	 * Notifies that an atomic test is started or ignored. 
	 * <p>
	 * To Updates {@link #currPathIter} pointing to the current testcase 
	 * and expands along the path given by the current testcase. 
	 * Then sets the quality of the according testcase 
	 * to <code>qual</code> and updates the according tree node. 
	 *
	 * @param qual
	 *    the quality of the testcase: 
	 *    This is {@link Quality#Started} or {@link Quality#Ignored}. 
	 * @return
	 *    the current testcase. 
	 */
	TestCase noteTestStartedI(Quality qual) {
	    this.currPathIter.updatePathI();
	    expandAlongPath();

	    DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode)
		this.currPathIter.getPath().getLastPathComponent();
	    TestCase result = (TestCase) lastNode.getUserObject();
	    result.setQualStartedIgnored(qual);
	    ((DefaultTreeModel) this.hierarchyTree.getModel())
	    	.nodeChanged(lastNode);
	    return result;
	}

	// invoked by noteReportResult(TestCase) 
	/**
	 * Notifies that the singular test <code>testCase</code> is finished. 
	 * <p>
	 * Collapses the current path invoking {@link #collapseAlongPath()}, 
	 * updates the tree node {@link #currPathIter} points to 
	 * and notifies {@link #testCaseLister} 
	 * if <code>testCase</code> failed and is selected invoking 
	 * {@link TestCaseLister#addSelectedTestCaseByNeed(TestCase)}. 
	 *
	 * @param testCase
	 *    The testcase comprising the result of the singular test finished. 
	 */
	void noteReportResult(TestCase testCase) {
	    collapseAlongPath();

	    MutableTreeNode lastNode = (MutableTreeNode)
		this.currPathIter.getPath().getLastPathComponent();
	    ((DefaultTreeModel) this.hierarchyTree.getModel())
	    	.nodeChanged(lastNode);

	    // If testCase is selected and testCase.hasFailed(), 
	    // it shall be added to testCaseLister if not yet listed. 
	    if (testCase.hasFailed() && isSelected(testCase)) {
		this.testCaseLister.addSelectedTestCaseByNeed(testCase);
	    }
	}

	/**
	 * Returns whether the given testcase <code>testCase</code> 
	 * is selected in this HierarchyTree. 
	 *
	 * @return
	 *    whether the given testcase <code>testCase</code> 
	 *    is selected in this HierarchyTree. 
	 */
	private boolean isSelected(TestCase testCase) {
	    int numSel = this.treeSelection.getSelectionCount();
	    if (numSel == 0) {
		return false;
	    }
	    assert numSel == 1;
	    DefaultMutableTreeNode selNode = (DefaultMutableTreeNode)
		this.treeSelection.getSelectionPath().getLastPathComponent();
	    TestCase selTestCase = (TestCase) selNode.getUserObject();

	    return selTestCase == testCase; // NOPMD
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing Selector.                                   *
	 * ---------------------------------------------------------------- */

	// api-docs inherited from Selector 
	public void setSelection(int index) {
	    // **** note: for index==0 one update is required and so index+1
	    // abuse of TreePathIterator to obtain the right selection path 
	    TreePathIterator currPathIter = 
		new TreePathIterator(this.hierarchyTree, index + 1);
	    this.treeSelection.addSelectionPath(currPathIter.getPath());
	    //**** still a problem with update 
	    // what follows does not solve the problem. 
	    //this.frame.update(this.frame.getGraphics());
	}

	// api-docs inherited from Selector 
	public void clearSelection() {
	    this.treeSelection.clearSelection();
	}

	// api-docs inherited from Selector 
	public void registerSelector(Selector selector) {
	    assert selector instanceof TestCaseLister
		|| selector == TabChangeListener.EMPTY_SELECTOR;
	    this.selector = selector;
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing TreeSelectionListener.                      *
	 * ---------------------------------------------------------------- */

	/**
	 * Called whenever the value of the selection changes. 
	 * Note that deselection is treated as selection of the root, 
	 * so w.l.o.g, we have to consider selections only. 
	 * <p>
	 * If an entry is selected, 
	 * {@link #singleSelectedNode} holds the selected node.
	 * Next {@link #selector} is notified of the selection: 
	 * <ul>
	 * <li>
	 * if a failure is selected (which is a leaf) 
	 * triggers according selection in the failure list, 
	 * otherwise triggers a deselection. 
	 * </ul>
	 * Both have further effects invoking 
	 * {@link GUIRunner.TestCaseLister#valueChanged(ListSelectionEvent)}.
	 * <p>
	 * Finally, invokes {@link #setFilter()} 
	 * setting the filter for the next test run. 
	 */
	public void valueChanged(TreeSelectionEvent selEvent) {
	    // paths is a list of paths with changed selection 
	    TreePath[] paths = selEvent.getPaths();
	    // two paths if one selected and the other deselected. 
	    // one path: either selected or deselected 
	    assert paths.length == 1 || paths.length == 2;

	    for (int i = 0; i < paths.length; i++) {

		// set singleSelectedNode 
		if (selEvent.isAddedPath(paths[i])) {
		    // Here, paths[i] has been selected 
		    this.singleSelectedNode = (DefaultMutableTreeNode)
			paths[i].getLastPathComponent();
		} else {
		    // Here, paths[i] has been deselected 
		    if (paths.length == 2) {
			// Here, the other path is selected 
			// which handles all we need 
			continue;
		    }

		    // Here, the deselection is the only change 
		    assert paths.length == 1;
		    // treat as if root was selected 
		    // **** this is merely a copy! 
		    this.singleSelectedNode = (DefaultMutableTreeNode)
			paths[i].getPathComponent(0);
		}

		setFilter();

		if (this.singleSelectedNode.isLeaf()) {
		    TestCase testCase = (TestCase)
			this.singleSelectedNode.getUserObject();
		    this.selector.setSelection(testCase.getIdx());
		} else {
		    this.selector.clearSelection();
		}
	    } // for
	}

    } // class HierarchyWrapper 

    /**
     * Represents the table displaying the number of runs, 
     * both, passed and to be performed altogether, 
     * the tests already ignored and those a failure or an error was found. 
     * <p>
     * The numbers {@link #numRunsDone}, {@link #numRuns} and {@link #qual2num} 
     * represent the number of runs done, 
     * the overall number of runs, done or not, and 
     * the number of finished tests of the according quality. 
     */
    static class StatisticsTestState extends JComponent {

	private static final long serialVersionUID = -2479143000061671589L;

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	/**
	 * Maps the quality with level <code>> 0</code> 
	 * to the according labels. 
	 */
	private final Map<Quality, JLabel>  qual2label;

	/**
	 * Maps the quality with any level
	 * to the number of testcases already finished or ignored 
	 * with according state. 
	 */
	private final Map<Quality, Integer> qual2num;

	/**
	 * Label for the number of runs done and to be executed. 
	 *
	 * @see #numRunsDone
	 * @see #numRuns
	 */
	private final JLabel runs;

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


	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	StatisticsTestState() {
	    super();

	    this.runs    = new JLabel();
	    //this.numRuns = 0;// formally. This is set by #start()

	    this.qual2label = new EnumMap<Quality, JLabel>(Quality.class);
	    for (Quality qual : Quality.values()) {
		if (qual.isNeutral()) {
		    continue;
		}
		this.qual2label.put(qual, new JLabel());
	    }
	    this.qual2num = new EnumMap<Quality, Integer>(Quality.class);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Returns a horizontal box with the labels in {@link #qual2label}
	 * intermangled with according icons. 
	 */
	Box getBox() {
	    Box res = Box.createHorizontalBox();
	    res.add(this.runs);
	    for (Quality qual : Quality.values()) {
		if (qual.isNeutral()) {
		    continue;
		}
		res.add(new JLabel(qual.getIcon()));
		res.add(this.qual2label.get(qual));
	    }

	    res.add(Box.createGlue());
	    return res;
	}

	/**
	 * Notifies that the structure of the test class may have been updated. 
	 * <p>
	 * Initiates 
	 * <ul>
	 * <li>
	 * {@link #numRuns} with the testcount given by <code>desc</code>, 
	 * <li>
	 * {@link #numRunsDone} and all numbers in {@link #qual2num} 
	 * with <code>0</code>, 
	 * </ul>
	 * and updates the labels {@link #runs} 
	 * and those in {@link #qual2label}. 
	 *
	 * @param desc
	 *    a description of the test structure defined in the test class 
	 *    which is a hierarchy of suites and singular tests. 
	 */
	void initClassStructure(Description desc) {
	    this.numRuns = desc.testCount();
	    this.numRunsDone = 0;

	    for (Quality qual : Quality.values()) {
		this.qual2num.put(qual, 0);
	    }

	    updateLabels();
	}

	// for StatisticsTestState 
	/**
	 * Notifies that a test with structure given by <code>desc</code> 
	 * is going to be run next. 
	 * <p>
	 * 
	 * Initiates 
	 * <ul>
	 * <li>
	 * {@link #numRunsDone} to <code>0</code>, 
	 * <li>
	 * {@link #qual2num} with the testcount given by <code>testCase</code> 
	 * invoking {@link #detQual2NumRec(TestCase)}, 
	 * </ul>
	 * and updates the labels {@link #runs} 
	 * and those in {@link #qual2label}. 
	 *
	 * @param testCase 
	 *    describes the (hierarchy of) tests defined by the test class. 
	 *    Those which are in phase {@link Quality#Scheduled} 
	 *    are to be run. 
	 */
	void startTestRun(TestCase testCase) {
	    this.numRunsDone = 0;
	    detQual2NumRec(testCase);
	    updateLabels();
	}

	/**
	 * Initializes teh statistics in {@link #qual2num} 
	 * according to <code>testCase</code>. 
	 *
	 * @param testCase 
	 *    describes the (hierarchy of) tests defined by the test class. 
	 *    Those which are in phase {@link Quality#Scheduled} 
	 *    are to be run. 
	 */
	private void detQual2NumRec(TestCase testCase) {
	    if (testCase.isTest()) {
		Quality qual = testCase.getQuality();
		switch (qual) {
		case Started:
		    assert false;
		    break;
		case Scheduled:
		    break;
		default:
		    this.numRunsDone++;
		    break;
		}

		int num = this.qual2num.get(qual);
		this.qual2num.put(qual, ++num);
		return;
	    }
	    assert !testCase.isTest();
	    for (TestCase child : testCase.getChildren()) {
		detQual2NumRec(child);
	    }
	}

	/**
	 * Notifies that the singular test <code>testCase</code> is finished. 
	 * <p>
	 * Updates all counters and labels 
	 * according to <code>testCase</code>'s quality: 
	 * It may succeed, be ignored, had a failure or 
	 * could not be executed due to an exception or an error. 
	 *
	 * @param testCase
	 *    The testcase comprising the result of the singular test finished. 
	 * @see TestCase#getQuality()
	 */
	void noteReportResult(TestCase testCase) {
	    this.numRunsDone++;

	    int num = this.qual2num.get(testCase.getQuality());
	    /*      */this.qual2num.put(testCase.getQuality(), ++num);

	    updateLabels();
	}

	/**
	 * Updates all labels if a counter has changed. 
	 * This is invoked by {@link #initClassStructure(Description)} 
	 * and by {@link #startTestRun(TestCase)}. 
	 */
	private void updateLabels() {
	    this.runs.setText("Runs: " + this.numRunsDone + 
			      "/"      + this.numRuns     + "    ");

	    for (Quality qual : Quality.values()) {
		if (qual.isNeutral()) {
		    continue;
		}
		/*         */this.qual2label.get(qual)
		    .setText(qual.toString()           + "s: " + 
			     this.qual2num  .get(qual) + "    ");
	    }
	}
    } // class StatisticsTestState 


    /**
     * A special renderer object 
     * consisting of a label and a location within java code 
     * for an item in a failure list. 
     */
    static class TestListCellRenderer 
	implements ListCellRenderer<TestCase>, Serializable {

	/**
	 * A special kind of label which fires only change in the text 
	 * and which allows to set details: selection and focus. 
	 */
	static class XLabel extends JLabel {

	    private static final long serialVersionUID = -2479143000061671589L;

	    XLabel(Icon icon) {
		super(icon);
	    }

	    XLabel(String text) {
		super(text);
	    }

	    void setDetails(JList<?> list,
			    boolean isSelected,
			    boolean cellHasFocus) {
		if (isSelected) {
		    setBackground(list.getSelectionBackground());
		    setForeground(list.getSelectionForeground());
		} else {
		    setBackground(list.getBackground());
		    setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setBorder(cellHasFocus
			  ? UIManager.getBorder(BORDER_DESC) 
			  : NO_FOCUS_BORDER);
		setOpaque(true);
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a> 
	     * for more information.
	     *
	     * @since 1.5
	     * @return <code>true</code> if the background is completely opaque
	     *         and differs from the JList's background;
	     *         <code>false</code> otherwise
	     */
	    public boolean isOpaque() { 
		Color back = getBackground();
		Component comp = getParent(); 
		if (comp != null) { 
		    comp = comp.getParent(); 
		}
		// comp should now be the JList. 
		boolean colorMatch = 
		    back != null && 
		    comp != null && 
		    back.equals(comp.getBackground()) && 
		    comp.isOpaque();
		return !colorMatch && super.isOpaque(); 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void validate() {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     *
	     * @since 1.5
	     */
	    public void invalidate() {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     *
	     * @since 1.5
	     */
	    public void repaint() {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void revalidate() {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void repaint(long timm, 
				int xCoord, int yCoord, 
				int width, int height) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void repaint(Rectangle rect) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    protected void firePropertyChange(String propertyName, 
					      Object oldValue, 
					      Object newValue) {
		// Strings get interned...
		if (propertyName == "text") { // NOPMD
		    super.firePropertyChange(propertyName, oldValue, newValue);
		}
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName, 
					   byte oldValue, 
					   byte newValue) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName, 
					   char oldValue, 
					   char newValue) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName, 
					   short oldValue, 
					   short newValue) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName, 
					   int oldValue, 
					   int newValue) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName, 
					   long oldValue, 
					   long newValue) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName, 
					   float oldValue, 
					   float newValue) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName, 
					   double oldValue, 
					   double newValue) {
		// is empty. 
	    }

	    /**
	     * Overridden for performance reasons.
	     * See the <a href="#override">Implementation Note</a>
	     * for more information.
	     */
	    public void firePropertyChange(String propertyName,
					   boolean oldValue, 
					   boolean newValue) {
		// is empty. 
	    }
	} // class XLabel 

	private static final long serialVersionUID = -2479143000061671589L;

	private static final String BORDER_DESC = 
	    "List.focusCellHighlightBorder";

	protected static final Border NO_FOCUS_BORDER = 
	    new EmptyBorder(1, 1, 1, 1);

	/* ------------------------------------------------------------------ *
	 * constructor.                                                       *
	 * ------------------------------------------------------------------ */

	/**
	 * Constructs a special renderer object 
	 * consisting of a label and a location within java code 
	 * for an item in a failure list. 
	 */
	TestListCellRenderer() {
	    super();
	}

	/* ------------------------------------------------------------------ *
	 * methods.                                                           *
	 * ------------------------------------------------------------------ */

	// **** hack ****
	private String thrwToString(Throwable thrw) {
	    // if ("null".equals(thrw.getMessage())) {
	    //     return thrw.getClass().toString();
	    // } else {
	    //     return thrw           .toString();
	    // }


	    return "null".equals(thrw.getMessage())	    
		? thrw.getClass().toString()
		: thrw           .toString();
	}

	public Component getListCellRendererComponent
	    (JList<? extends TestCase> list,
	     TestCase testCase,
	     int index,
	     boolean isSelected,
	     boolean cellHasFocus) {

	    Box failureEntry = Box.createHorizontalBox();

	    XLabel iconLabel = new XLabel(testCase.getQuality().getIcon());
	
	    // for rerun testcases which eventually do no longer fail. 
	    XLabel textLabel = new XLabel(testCase.hasFailed() 
					? thrwToString(testCase.getThrown()) 
					: testCase.getQuality().getMessage());
	    iconLabel.setDetails(list, isSelected, cellHasFocus);
	    iconLabel.setDetails(list, isSelected, cellHasFocus);
	    failureEntry.add(iconLabel);
	    failureEntry.add(textLabel);


	    return failureEntry;
	}

	/**
	 * A subclass of DefaultListCellRenderer that implements UIResource.
	 * DefaultListCellRenderer doesn't implement UIResource
	 * directly so that applications can safely override the
	 * cellRenderer property with DefaultListCellRenderer subclasses.
	 * <p>
	 * <strong>Warning:</strong>
	 * Serialized objects of this class will not be compatible with
	 * future Swing releases. The current serialization support is
	 * appropriate for short term storage or RMI 
	 * between applications running the same version of Swing.  
	 * As of 1.4, support for long term storage
	 * of all JavaBeans<sup><font size="-2">TM</font></sup>
	 * has been added to the <code>java.beans</code> package.
	 * Please see <code>java.beans.XMLEncoder</code>.
	 */
	/*
	  public static class UIResource extends MyListCellRenderer
	  implements javax.swing.plaf.UIResource {
	  private static final long serialVersionUID = -2479143000061671589L;
	  }
	*/

    } // class TestListCellRenderer 



    /**
     * Represents the list of testcases already failed 
     * shown in one of the tabs 
     * and a {@link #stackTraceLister} 
     * which represents the stack trace box below the tabbed pane. 
     */
    static class TestCaseLister implements ListSelectionListener, Selector {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The current selection of {@link #failureListMod}. 
	 * At most one entry is selected. 
	 * If so, the according element in 
	 * {@link GUIRunner.HierarchyWrapper#treeSelection} is selected also. 
	 *
	 * @see #valueChanged(ListSelectionEvent)
	 */
	private final ListSelectionModel failureSelection;

	/**
	 * The list of testcases which are either ignored, 
	 * failed in some sense or with hurt assumption. 
	 */
	private final DefaultListModel<TestCase> failureListMod;

	/**
	 * Contains the stack trace 
	 * if a failure in {@link #failureListMod} is selected 
	 * which caused an exception. 
	 * This is true if an assertion was wrong, an assumption was wron 
	 * or if the execution of a testcase could not be completed 
	 * due to an exception. 
	 */
	private final StackTraceLister stackTraceLister;

	// selector to be influenced. 
	/**
	 * Selector to be influenced: 
	 * If this is in the selected tab, {@link #selector} 
	 * is the tab with the {@link GUIRunner.HierarchyWrapper}; 
	 * otherwise it is {@link GUIRunner.TabChangeListener#EMPTY_SELECTOR}. 
	 * Set by {@link #registerSelector(GUIRunner.Selector)}. 
	 */
	private Selector selector;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	TestCaseLister() {
	    // init failureSelection 
	    this.failureSelection = new DefaultListSelectionModel();
	    this.failureSelection
		.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    this.failureSelection.setValueIsAdjusting(true);
	    this.failureSelection.addListSelectionListener(this);
	    // init failureListMod 
	    this.failureListMod = new DefaultListModel<TestCase>();
	    // init stacktrace 
	    this.stackTraceLister = new StackTraceLister();
	}


	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	// invoked by {@link GUIRunner#setCenter(Actions)} 
	JList<TestCase> getFailList() {
	    JList<TestCase> jFailureList = 
		new JList<TestCase>(this.failureListMod);
	    jFailureList.setCellRenderer(new TestListCellRenderer());
	    jFailureList.setSelectionModel(this.failureSelection);
	    return jFailureList;
	}

	// invoked by {@link GUIRunner#setCenter(Actions)} 
	Component getStackTraceBox() {
	    return this.stackTraceLister.getStackTraceBox();
	}

	/* ---------------------------------------------------------------- *
	 * further methods.                                                 *
	 * ---------------------------------------------------------------- */

	/**
	 * Clears the failure list, its selection and the failure stack. 
	 */
	void initClassStructure() {
	    this.failureSelection.clearSelection();
	    this.failureListMod  .clear();
	    this.stackTraceLister.clearStack();
	}

	// for TestCaseLister

	// maybe remove from failureListMod only 
	// what is in desc 
	void startTestRun() {
	}

	/**
	 * Notifies that the singular test <code>testCase</code> is finished. 
	 * <p>
	 * If the test failed, 
	 * it should be in the failure list {@link #failureListMod}. 
	 * If so the element is added if not yet present. 
	 * <p>
	 * If <code>testCase</code> is selected 
	 * then if <code>testCase</code> failed, 
	 * the stack trace {@link #stackTraceLister} 
	 * is set to the stacktrace of the failure; 
	 * otherwise the stacktrace is cleared. 
	 *
	 * @param testCase
	 *    The testcase comprising the result of the singular test finished. 
	 */
	void noteReportResult(TestCase testCase) {
	    // add testCase to failureListMod by need 
	    if (testCase.hasFailed()
		&& !this.failureListMod.contains  (testCase)) {
		/**/this.failureListMod.addElement(testCase);
		// Here, the new element is not selected 
		// and so stackTraceLister needs no update. 
		return;
	    }
	    assert !testCase.hasFailed() 
		|| this.failureListMod.contains(testCase);

	    // update stackTraceLister by need 
	    int selIndex = this.failureSelection.getMinSelectionIndex();
	    if (selIndex == -1 
		|| testCase != this.failureListMod.getElementAt(selIndex)) {
		// Here, no testcase is selected 
		// or some testcase is selected but not testCase. 
		// In any case, stackTraceLister is empty and needs no update. 
		return;
	    }

	    // Here, testCase is selected 
	    if (testCase.hasFailed()) {
		this.stackTraceLister.setStack(testCase.getThrown());
	    } else {
		this.stackTraceLister.clearStack();
	    }
	}

	/**
	 * Adds <code>testCase</code> to the failure list if not yet listed. 
	 * It is assumed that <code>testCase</code> failed 
	 * and that it is selected in the {@link HierarchyWrapper}. 
	 *
	 * @param testCase
	 *    a testcase which failed. 
	 */
	void addSelectedTestCaseByNeed(TestCase testCase) {
	    assert testCase.hasFailed();
	    int selIndex;
	    if (this.failureListMod.contains(testCase)) {
		// nothing to be added; check only 
		selIndex = this.failureSelection.getMinSelectionIndex();
		assert this.failureListMod.get(selIndex) == testCase;
	    } else {
		// add testCase and select it 
		this.failureListMod.addElement(testCase);

		selIndex = this.failureSelection.getMinSelectionIndex();
		// because testCase is selected in the HierarchyTree, 
		// and selections are synchronized, 
		// nothing is selected in the list
		assert selIndex == -1;

		selIndex = this.failureListMod.size() - 1;
		assert this.failureListMod.get(selIndex) == testCase;
		this.failureSelection.setSelectionInterval(selIndex, selIndex);
		// Here, testCase is in the list and is selected. 
	    }
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing Selector.                                   *
	 * ---------------------------------------------------------------- */

	// api-docs inherited from Selector 
	// index starts with 0 
	public void setSelection(int index) {
	    for (int idx = 0; idx < this.failureListMod.getSize(); idx++) {
		// **** this.failureListMod.get(idx) == testCase should do 
		if (this.failureListMod.get(idx).getIdx() == index) {
		    this.failureSelection.setSelectionInterval(idx, idx);
		    return;
		}
	    }
	    clearSelection();
	}

	// api-docs inherited from Selector 
	public void clearSelection() {
	    this.failureSelection.clearSelection();
	    this.stackTraceLister.clearStack();
	}

	// api-docs inherited from Selector 
	public void registerSelector(Selector selector) {
	    assert selector instanceof HierarchyWrapper
		|| selector == TabChangeListener.EMPTY_SELECTOR;
	    this.selector = selector;
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing ListSelectionListener.                      *
	 * ---------------------------------------------------------------- */

	/**
	 * Called whenever the value of the selection changes. 
	 * <p>
	 * CAUTION: From the documentation of {@link ListSelectionEvent}: 
	 * queries from {@link ListSelectionModel}, 
	 * rather from {@link ListSelectionEvent}. 
	 * <p>
	 * If an entry is selected, 
	 * {@link #stackTraceLister} is notified of the stacktrace 
	 * of the throwable of the according testcase 
	 * and {@link #selector} is notified of the selection 
	 * to trigger the according selection 
	 * which in turn has further effects invoking 
	 * {@link GUIRunner.HierarchyWrapper#valueChanged(TreeSelectionEvent)}.
	 * Deselection causes {@link #stackTraceLister} to clear the stacktrace 
	 * and also affects {@link #selector} like selection of the root. 
	 */
	public void valueChanged(ListSelectionEvent lse) {
	    // the index of the (single) selected entry or -1 if none selected 
	    int selIndex = this.failureSelection.getMinSelectionIndex();
	    if (selIndex == -1) {
		// Here, the selection is empty. 
		this.stackTraceLister.clearStack();
		this.selector.clearSelection();
		return;
	    }
	    // Here, the selection consists of a single entry. 
	    TestCase testCase = this.failureListMod.getElementAt(selIndex);
	    this.stackTraceLister.setStack(testCase.getThrown());
	    this.selector.setSelection(testCase.getIdx());

	    //GUIRunner.this.splitPane.resetToPreferredSizes();
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
		.addListSelectionListener(this);
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
		// and from testcase selected without exception, 
		// as ignored testcases. 
		// This is like clearing the stack trace 
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
	    System.out.println("location: " + location);
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

    /**
     * A listener to the switching of a tab in the foreground; 
     * the other in the background. 
     */
    static class TabChangeListener implements ChangeListener {

	/* ---------------------------------------------------------------- *
	 * constants.                                                       *
	 * ---------------------------------------------------------------- */

	/**
	 * The index of the tab selected initially. 
	 * This may be 0 or 1. 
	 * Note that the failure list is the 0th tab for some reason 
	 * as seen from {@link GUIRunner#setCenter(Actions)}. 
	 * It must be the one initially in the foreground. 
	 */
	private static final int SEL_IND1 = 0;

	/**
	 * Registered with the unselected Selector. 
	 */
	private static final Selector EMPTY_SELECTOR = 
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
		testHierarchy,
		testCaseLister,
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
	 * for {@link GUIRunner.Selector#registerSelector(GUIRunner.Selector)}, 
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
	    Selector    sel = this.selectors[    index];
	    Selector notSel = this.selectors[1 - index];
	    sel   .registerSelector(notSel);
	    notSel.registerSelector(EMPTY_SELECTOR);
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing ChangeListener.                             *
	 * ---------------------------------------------------------------- */

	// Invoked when the target of the listener has changed its state.
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
	    return this.clsPath.absFile2cls(clsFile,
					    JavaPath.ClsSrc.Class);
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
    private final StatisticsTestState statisticsTestState;

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
     * {@link GUIRunner.TestCaseLister#getStackTraceBox()}. 
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
     * Creates a new <code>GUIRunner</code> instance 
     * which defines components with unloaded test class. 
     * Loading is done 
     * by invoking {@link #testClassStructureLoaded(Description)}. 
     */
    GUIRunner(Actions actions) {
	assert SwingUtilities.isEventDispatchThread();
	this.frame = new JFrame("JUnit GUI");
	this.frame.setIconImage(SMALL_LOGO_ICON.getImage());
	this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	this.classChooser = new ClassChooser();

	setMenuBar(actions);

	this.className = new JLabel("class name");
	this.statusBar = new JLabel("status bar");

	Container content = this.frame.getContentPane();
	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
	//content.setLayout(new BorderLayout());


	this.statisticsTestState = new StatisticsTestState();

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
	progLogo.add(HORIZ_BOUNDARY2);
	progLogo.add(new JLabel(LOGO_ICON));
	progLogo.add(HORIZ_BOUNDARY);
	this.frame.add(progLogo);

	// add separator and line with runs statistics 
 	this.frame.add(this.statisticsTestState.getBox());
 	this.frame.add(new JSeparator());
	this.frame.add(VERTI_BOUNDARY);

	this.frame.add(Box.createVerticalGlue());

	// add results-line 
	Box results = Box.createHorizontalBox();
	results.add(HORIZ_BOUNDARY);
	results.add(new JLabel("Results:"));
	results.add(Box.createGlue());
	this.frame.add(results);
 	//this.frame.add(new JLabel("Results:"));

	//this.frame.add(Box.createGlue());

	// add split pane and Run2-button 
	Box resRun2 = Box.createHorizontalBox();
	resRun2.add(HORIZ_BOUNDARY);
	resRun2.add(this.splitPane);
	resRun2.add(Box.createGlue());
	resRun2.add(new JButton("Run2"));
	resRun2.add(HORIZ_BOUNDARY);
	this.frame.add(resRun2);
//this.frame.add(this.splitPane);

	// add status bar and Exit-button 
	Box statExit = Box.createHorizontalBox();
	statExit.add(HORIZ_BOUNDARY);
	statExit.add(this.statusBar);
	statExit.add(Box.createGlue());
	statExit.add(new JButton("Exit"));
	statExit.add(HORIZ_BOUNDARY);

 	this.frame.add(statExit);

	this.frame.setSize(HORIZ_FRAME, VERTI_FRAME);
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
	// add Tree as second because otherwise a problem becomes visible 
	this.testHierarchy = new HierarchyWrapper(actions,
						  this.testCaseLister);


	tabbedPane.addTab("Test Hierarchy",
			  HIERARCHY_ICON,
			  new JScrollPane(this.testHierarchy.getTree()));
	tabbedPane.addTab("Failures",
			  Quality.Error.getIcon(),
			  new JScrollPane(this.testCaseLister.getFailList()));
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
		  testCase.getQuality().getMessage() + ": " + 
		  testCase.getDesc());
    }

    /**
     * Notifies that an atomic test is started or ignored. 
     *
     * @param qual
     *   the quality of the testcase: 
     *   This is either {@link Quality#Started} or {@link Quality#Ignored}. 
     */
    TestCase noteTestStartedI(Quality qual) {
	
//	assert SwingUtilities.isEventDispatchThread();
	assert qual == Quality.Started
	    || qual == Quality.Ignored;
	TestCase testCase = this.testHierarchy.noteTestStartedI(qual); 
	setStatus(testCase);
	return testCase;
    }

    /**
     * Notifies that the singular test <code>testCase</code> is finished, 
     * whether successful or not or that a test is ignored 
     * after invoking {@link #noteTestStartedI(Quality)} 
     * with parameter {@link Quality#Ignored}. 
     *
     * @param testCase
     *    The testcase comprising the result of the singular test finished. 
     */
    void noteReportResult(TestCase testCase) {
	//	assert SwingUtilities.isEventDispatchThread();
	
	assert testCase.getQuality() == Quality.Ignored
	    || testCase.getQuality() == Quality.Invalidated
	    || testCase.getQuality() == Quality.Success
	    || testCase.getQuality() == Quality.Failure
	    || testCase.getQuality() == Quality.Error;

	this.testHierarchy      .noteReportResult(testCase);
	this.progress           .noteReportResult(testCase);
	this.statisticsTestState.noteReportResult(testCase);
	this.testCaseLister     .noteReportResult(testCase);
	setStatus                                (testCase);

	//this.splitPane          .resetToPreferredSizes();
    }

    /**
     * Notifies that the structure of the test class may have been updated. 
     *
     * @param desc
     *    a description of the test structure defined in the test class 
     *    which is a hierarchy of suites and singular tests. 
     * @see #testRunStarted(Description)
     */
    void testClassStructureLoaded(Description desc) {
//	assert SwingUtilities.isEventDispatchThread();
	setStatus("testClassLoaded(");

	// **** strange way to obtain the classname ***** 
	this.className.setText(desc.getChildren().get(0).toString());
	TestCase testCaseAll = new TestCase(desc);

	this.testHierarchy      .initClassStructure(testCaseAll);
	this.testCaseLister     .initClassStructure();
	this.progress           .initClassStructure(desc);
	this.statisticsTestState.initClassStructure(desc);
    }

    /**
     * Notifies that a test with structure given by <code>desc</code> 
     * is going to be run next. 
     * Called before any tests have been run. 
     * At least once before this one, 
     * {@link #testClassStructureLoaded(Description)} has been invoked. 
     *
     * @param desc 
     *    describes the (hierarchy of) tests to be run. 
     *    This is a sub-hierarchy of the one given by the test class. 
     */
    void testRunStarted(Description desc) {
 //	assert SwingUtilities.isEventDispatchThread();
	// **** strange way to obtain the classname ***** 
	setStatus("testRunStarted(");
	this.testHierarchy.getActions().setEnableForRun(true); //running 

	TestCase root = this.testHierarchy.getRoot();

	this.testHierarchy      .startTestRun(desc);
	this.testCaseLister     .startTestRun(); // purely formally 
	this.progress           .startTestRun(root);
	this.statisticsTestState.startTestRun(root);
    }

    /**
     * Notifies that a test has been finished sufficiently or not. 
     *
     * @param runTime
     *    the time execution of the test took in milliseconds. 
     */
    void testRunFinished(long runTime) {
	setStatus("testRunFinished(required: " + 
		 runTime + "ms. ");
	this.testHierarchy.getActions().setEnableForRun(false); //!running
    }

    /**
     * Notifies that a test has been aborted by the user. 
     */
    void testRunAborted() {
	setStatus("testRunAborted(");
	this.testHierarchy.getActions().setEnableForRun(false); //!running
    }

} // NOPMD coupling is not that high!

