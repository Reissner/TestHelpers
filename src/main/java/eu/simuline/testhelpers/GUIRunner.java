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
import java.awt.Rectangle;

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
//@SuppressWarnings("")
class GUIRunner {

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
	 * and initialized in {@link #start(Description)}. 
	 */
	private Quality qual;


	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new <code>TestProgressBar</code> instance. 
	 */
	public TestProgressBar() {
	    super(new DefaultBoundedRangeModel());
	    this.model.setValueIsAdjusting(true);
	    this.qual = null;
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
	    resetA();
	}

	void resetA() {
	    setValue(0);
	    this.qual = Quality.Scheduled;// color OK 
	}

	/**
	 * Pushes the progress bar one further 
	 * and upates the color of the progress bar 
	 * as described in {@link GUIRunner.TestProgressBar}. 
	 */
	void incNumRunsDone(TestCase testCase) {
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
	public TestTreeCellRenderer() {
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
		void updatePath(HierarchyWrapper testHiererarchy) {
		    testHiererarchy.currPathIter.treePathUpdater = Generic;
		    testHiererarchy.currPathIter.setFirstPath();
		}
	    },
	    Generic() {
		void updatePath(HierarchyWrapper testHiererarchy) {
		    testHiererarchy.currPathIter.incPath();
		}
	    };
	    abstract void updatePath(HierarchyWrapper testHiererarchy);
	} // enum TreePathUpdater 

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	// is null after this is created. 
	private       TreePath currPath;

	/**
	 * A model of the tree of testsuites and tests. 
	 */
	private final TreeModel treeModel;

	private TreePathUpdater treePathUpdater;



	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	TreePathIterator(JTree tree) {
	    this.currPath = null;// formally only 
	    this.treeModel = tree.getModel();
	    this.treePathUpdater = TreePathUpdater.First;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	void updatePath(HierarchyWrapper testHiererarchy) {
	    assert testHiererarchy.currPathIter ==  this;
	    this.treePathUpdater.updatePath(testHiererarchy);
	    testHiererarchy.expandAlongPath();
	}

	/**
	 * Initializes {@link #currPath} 
	 * with the first path in {@link #treeModel}. 
	 */
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

	/**
	 * Replaces {@link #currPath} removing the last node 
	 * as long as the last node is the last child 
	 * and returns the 
	 */

	private int shortenPath() {
	    TreeNode lastNode = (TreeNode)
		this.currPath.getLastPathComponent();
	    TreePath prefix = this.currPath.getParentPath();
	    TreeNode lastButOneNode = (TreeNode)prefix.getLastPathComponent();
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

	/**
	 * Increments {@link #currPath} and returns the result. 
	 */
	TreePath incPath() {
	    int index = shortenPath();
	    TreePath prefix = this.currPath.getParentPath();
	    TreeNode lastButOneNode = (TreeNode)prefix.getLastPathComponent();
	    TreeNode lastNode = lastButOneNode.getChildAt(index+1);
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
     * given by {@link #singleSelectedNode} 
     */
    static class HierarchyWrapper 
	implements Selector, TreeSelectionListener {


	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */


	/**
	 * The hierarchy of testsuites and testcases as a tree. 
	 * After creation this is a default tree, 
	 * after invocation of {@link #start(Description)} 
	 * the hierarchy reflects {@link #desc}. 
	 * The selection model is given by {@link #treeSelection} 
	 * and this is the {@link TreeSelectionListener}. 
	 */
	private final JTree hierarchyTree;

	/**
	 * Set by {@link #start(Description)} initially <code>null</code>: 
	 * Reflects the hierarchy of testsuites and tests 
	 * to be represented by {@link #hierarchyTree}. 
	 */
	private Description desc;

	/**
	 * The selection model for {@link #hierarchyTree}. 
	 */
	private final TreeSelectionModel treeSelection;

	/**
	 * This is used only by {@link #valueChanged(TreeSelectionEvent)} 
	 * to set the testcase via {@link Actions#setSingleTest(TestCase)}. 
	 */
	private final Actions actions;

	/**
	 * Represents the selected node in {@link #hierarchyTree}. 
	 * This is <code>null</code> if nothing selected 
	 * which is also the initial value. 
	 * This is set by {@link #valueChanged(TreeSelectionEvent)}. 
	 */
	private DefaultMutableTreeNode singleSelectedNode;

	/**
	 * Is <code>null</code> after this is created 
	 * and is initialized by {@link #start(Description)}. 
	 * Represents the path to the testcase currently run. 
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
	 * Creates a new HierarchyWrapper with the given <code>actions</code> 
	 * which is used to initialize {@link #actions}. 
	 */
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

	    TreeNode root = (TreeNode)this.hierarchyTree.getModel().getRoot();
	    this.hierarchyTree.setModel(new DefaultTreeModel(root));
	    this.actions = actions;

	    // purely formally 
	    this.desc               = null;
	    this.singleSelectedNode = null;
	    this.currPathIter       = null;
	    this.selector           = null;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	// **** Descriptions instead of testcases 
	// bad: design and problem with selecting testcases not yet run. 
	// 
	private static MutableTreeNode desc2treeNode(Description desc) {
//System.out.println("desc2treeNode(");
	    DefaultMutableTreeNode ret = new DefaultMutableTreeNode
		(new TestCase(desc, Quality.Scheduled, -1));
	    if (desc.isTest()) {
//System.out.println("ret1: "+ret);
		return ret;
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


	void updatePath() {
	    this.currPathIter.updatePath(this);
	}

	void updateSingular() {
	    ((DefaultTreeModel)this.hierarchyTree.getModel())
	    	.nodeChanged(this.singleSelectedNode);
	}

	/* ---------------------------------------------------------------- *
	 * further methods.                                                 *
	 * ---------------------------------------------------------------- */

	/**
	 * Sets the description {@link #desc}, 
	 * sets the according tree model of {@link #hierarchyTree} 
	 * using {@link #desc2treeNode(Description)}, 
	 * after that the cell renderer and the path incrementor 
	 * {@link #currPathIter} in conjunction with the ***** bad design. 
	 */
	void start(Description desc) {
	    this.desc = desc;
	    MutableTreeNode treeNode = desc2treeNode(this.desc);
	    TreeModel newModel = new DefaultTreeModel(treeNode);
	    this.hierarchyTree.setModel(newModel);
	    // cell renderer after model. 
	    this.hierarchyTree.setCellRenderer(new TestTreeCellRenderer());
	    //this.testHierarcy.update(this.testHierarcy.getGraphics());

	    this.currPathIter = new TreePathIterator(this.hierarchyTree);

//Tree  TreePath 	getLeadSelectionPath() 
//Tree   TreePath 	getSelectionPath() 
	}

	void expandAlongPath() {
	    this.hierarchyTree.expandPath
		(this.currPathIter.getPath().getParentPath());
	}

	void collapseAlongPath() {
	    this.hierarchyTree.collapsePath
		(this.currPathIter.getPath().getParentPath());
	}

	JTree getTree() {
	    return this.hierarchyTree;
	}

	// **** for tests only 
	TreePath getPath() {
	    return this.currPathIter.getPath();
	}

	void setResult(TestCase result) {
	    MutableTreeNode lastNode = (MutableTreeNode)
		this.currPathIter.getPath().getLastPathComponent();
	    lastNode.setUserObject(result);
	    ((DefaultTreeModel)this.hierarchyTree.getModel())
	    	.nodeChanged(lastNode);
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing Selector.                                   *
	 * ---------------------------------------------------------------- */

	public void setSelection(int index) {
	    TreePathIterator iter = new TreePathIterator(this.hierarchyTree);
	    iter.setFirstPath();
	    for (int i = 0; i < index; i++) {
		iter.incPath();
	    }
	    this.treeSelection.addSelectionPath(iter.getPath());
	    //**** still a problem with update 
	    // what follows does not solve the problem. 
	    //this.frame.update(this.frame.getGraphics());
	}

	public void clearSelection() {
	    this.treeSelection.clearSelection();
	}

	public void registerSelector(Selector selector) {
	    assert selector instanceof TestCaseLister
		|| selector == TabChangeListener.EMPTY_SELECTOR;
	    this.selector = selector;
	}

	/* ---------------------------------------------------------------- *
	 * methods implementing TreeSelectionListener.                      *
	 * ---------------------------------------------------------------- */

	// as a side effect affects {@link #treeSelection}, 
	// {@link #singleSelectedNode} and {@link #selector}
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
			if (!testCase.getQuality().allowsRerun()) {
			    // no rerun, no selection possible 
			    this.treeSelection.clearSelection();
			    this.     selector.clearSelection();
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

	/**
	 * Maps the quality with level <code>> 0</code> 
	 * to the according labels. 
	 */
	private final Map<Quality, JLabel>  qual2label;

	/**
	 * Maps the quality with any level
	 * to the number of testcases with according state. 
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

	RunsErrorsFailures() {
	    super();

	    this.runs    = new JLabel();
	    this.numRuns = 0;// formally. This is set by #start()

	    this.qual2label = new EnumMap<Quality, JLabel>(Quality.class);
	    for (Quality qual : Quality.values()) {
		if (qual.isNeutral()) {
		    continue;
		}
		this.qual2label.put(qual, new JLabel());
	    }
	    this.qual2num = new EnumMap<Quality, Integer>(Quality.class);

	    resetB();
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
	 * Initiates {@link #numRuns} 
	 * with the testcount from <code>desc</code> 
	 * and resets this component invoking {@link #resetB()}. 
	 */
	void start(Description desc) {
	    this.numRuns = desc.testCount();
	    resetB();
	}

	/**
	 * Resets all counters to <code>0</code> except {@link #numRuns} 
	 * and updates all labels invoking {@link #updateLabels()}. 
	 */
	void resetB() {
	    this.numRunsDone = 0;

	    for (Quality qual : Quality.values()) {
		this.qual2num.put(qual, 0);
	    }

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

	    int num = this.qual2num.get(result.getQuality());
	    /*      */this.qual2num.put(result.getQuality(), ++num);

	    updateLabels();
	}

	/**
	 * Updates all labels if a counter has changed. 
	 * This is invoked by {@link #resetB()} 
	 * and by {@link #incNumRunsDone(TestCase)}. 
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
    } // class RunsErrorsFailures 


    /**
     * A special renderer object 
     * consisting of a label and a location within java code 
     * for an item in a failure list. 
     */
    static class TestListCellRenderer 
	implements ListCellRenderer<TestCase>, Serializable {

	static class Label extends JLabel {

	    private static final long serialVersionUID = -2479143000061671589L;

	    Label(Icon icon) {
		super(icon);
	    }

	    Label(String text) {
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
		if (propertyName=="text") {
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
	} // class Label 

	private static final long serialVersionUID = -2479143000061671589L;

	private final static String BORDER_DESC = "List.focusCellHighlightBorder";

	protected final static Border NO_FOCUS_BORDER = new EmptyBorder(1,1,1,1);

	/* -------------------------------------------------------------------- *
	 * constructor.                                                         *
	 * -------------------------------------------------------------------- */

	/**
	 * Constructs a special renderer object 
	 * consisting of a label and a location within java code 
	 * for an item in a failure list. 
	 */
	TestListCellRenderer() {
	    super();
	}

	/* -------------------------------------------------------------------- *
	 * methods.                                                             *
	 * -------------------------------------------------------------------- */

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

	public Component getListCellRendererComponent(JList<? extends TestCase> list,
						      TestCase testCase,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus) {

	    Box failureEntry = Box.createHorizontalBox();

	    Label iconLabel = new Label(testCase.getQuality().getIcon());
	
	    // for rerun testcases which eventually do no longer fail. 
	    Label textLabel = new Label(testCase.hasFailed() 
					? thrwToString(testCase.getException()) 
					: testCase.getQuality().status());
	    iconLabel.setDetails(list,isSelected,cellHasFocus);
	    iconLabel.setDetails(list,isSelected,cellHasFocus);
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
	 * appropriate for short term storage or RMI between applications running
	 * the same version of Swing.  As of 1.4, support for long term storage
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

	public static void main(String[] args) {
	    System.out.println(": "+(new AssertionError((AssertionError)null).getMessage()==null));

	
	}
    } // class TestListCellRenderer 



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

	// does not depend on desc 
	void start(Description desc) {
	    resetTCL();
	}

	void resetTCL() {
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
	    return result.hasFailed();
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
	    assert selector instanceof HierarchyWrapper
		|| selector == TabChangeListener.EMPTY_SELECTOR;
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

	/**
	 * Registered with the unselected Selector. 
	 */
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
	this.testHierarchy.updatePath();
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
	this.testCaseLister.resetTCL();
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

