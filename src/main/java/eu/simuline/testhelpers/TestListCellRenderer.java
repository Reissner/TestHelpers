/*
 * @(#)DefaultListCellRenderer.java	1.25 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package eu.simuline.testhelpers;

import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.Box;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;

import java.io.Serializable;


/**
 * A special renderer object 
 * consisting of a label and a location within java code 
 * for an item in a failure list. 
 */
class TestListCellRenderer 
    implements ListCellRenderer<TestCase>, Serializable {

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
}
