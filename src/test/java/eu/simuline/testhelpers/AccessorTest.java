
package eu.simuline.testhelpers;

//import eu.simuline.testhelpers.Actions;
//import eu.simuline.testhelpers.Accessor;
//import static eu.simuline.testhelpers.Assert;

import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
// import static org.junit.Assert.assertNull;
// import static org.junit.Assert.fail;

import org.junit.Test;
//import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Testclass for class <code>Accessor</code>. 
 * This is rudimentary only. 
 *
 * @author <a href="mailto:e.reissner@rose.de">Ernst Reissner</a>
 * @version 1.0
 */
@RunWith(Suite.class)
@SuiteClasses({AccessorTest.TestAll.class})
public class AccessorTest {

    /* -------------------------------------------------------------------- *
     * framework.                                                           *
     * -------------------------------------------------------------------- */

    static AccessorTest TEST = new AccessorTest();

    public static class TestAll {
	@Test public void testGetField() throws Exception {
	    AccessorTest.TEST.testGetField();	    
	}
	@Test public void testSetField() throws Exception {
	    AccessorTest.TEST.testSetField();	    
	}
	@Test public void testCreate() throws Exception {
	    AccessorTest.TEST.testCreate();	    
	}
	@Test public void testInvoke() throws Exception {
	    AccessorTest.TEST.testInvoke();	    
	}
	@Test public void testGetInnerClass() {
	    AccessorTest.TEST.testGetInnerClass();	    
	}
    } // class TestAll 




    /* -------------------------------------------------------------------- *
     * methods for tests.                                                   *
     * -------------------------------------------------------------------- */

    Integer test = Integer.valueOf(0);

    class NonStatic {
	class NonStatic1 {
	}

    }

    static class Static {
	static class Static2 {
	}
    }

    static class ForTests {
	private int aPrimitiveField = 3;
	private static int aStaticPrimitiveField = 4;
	private final static int aFinalField = -1;

	// This is only to avoid a warning because of unused field aFinalField. 
	public static int getFinalFieldDummy() {
		return aFinalField;
	}
	static {
	    System.out.println("ForTests:static");
	}

	private int privateMethod(int i) {
		return aPrimitiveField*i;
  }
	// This is only to avoid a warning because of unused field aFinalField. 
  public int privateMethodP(int i) {
	  return privateMethod(i);
  }

	protected int protectedMethod(int i) {
	    return aPrimitiveField*i*10;
	}
	private static int privateStaticMethod(int i) {
	    return aStaticPrimitiveField*i;
	}

	// This is only to avoid a warning because of unused field aFinalField.
	public int privateStaticMethodP(int i) {
		return privateStaticMethod(i);
	}

    }

    static class ForTestsB extends ForTests {
	private int aPrimitiveField = 33;
	private static int aStaticPrimitiveField = 43;
	static {
	    System.out.println("ForTestsB:static");
	}

	private int privateMethod(int i) {
	    return aPrimitiveField*i;
	}
	// This is only to avoid a warning because of unused field aFinalField. 
  public int privateMethodP(int i) {
	  return privateMethod(i);
  }
	protected int protectedMethod(int i) {
	    return aPrimitiveField*i*10;
	}
	private static int privateStaticMethod(int i) {
	    return aStaticPrimitiveField*i;
	}
 	// This is only to avoid a warning because of unused field aFinalField.
	 public int privateStaticMethodP(int i) {
		return privateStaticMethod(i);
	}
   }

    /**
     * Initializes this class and all its inner classes
     */
    private void setUp() {
	try {
	    Class.forName("eu.simuline.testhelpers.AccessorTest");
	    Class.forName("eu.simuline.testhelpers.AccessorTest.Static");
	    Class.forName("eu.simuline.testhelpers.AccessorTest.Static.Static2");
	    Class.forName("eu.simuline.testhelpers.AccessorTest.ForTests");
	    Class.forName("eu.simuline.testhelpers.AccessorTest.ForTestsB");
	} catch (ClassNotFoundException e) {
	    throw new IllegalStateException("****");
	}
    }


    public void testGetField() throws Exception {
	Object obj;

	// testcase 1
	//
	// non-static field 
	//
	obj = new ForTests();
	assertEquals(Integer.valueOf(3),
		     (Integer)Accessor.getField(obj,"aPrimitiveField"));


	// testcase 2
	//
	// non-static field without target 
	//
	try {
	     Accessor.getField((Object)null,"aPrimitiveField");
	} catch (IllegalArgumentException e) {
	    assertEquals("Specified null-target. ",e.getMessage());
	} // end of try-catch
	
		     
	// testcase 3
	//
	// static field 
	//
	assertEquals(Integer.valueOf(4),
		     (Integer)Accessor.getField(ForTests.class,
						"aStaticPrimitiveField"));


	// testcase 4
	//
	// static field without specifying a class 
	//
	try {
	     Accessor.getField((Class)null,"aStaticPrimitiveField");
	} catch (IllegalArgumentException e) {
	    assertEquals("Specified null-class. ",e.getMessage());
	} // end of try-catch


	// testcase 5
	//
	// static field 
	//
	try {
	     Accessor.getField(null,null,"aStaticPrimitiveField");
	} catch (IllegalArgumentException e) {
	    assertEquals("Specified null-class. ",e.getMessage());
	} // end of try-catch


	// testcase 6
	//
	// non-static overwritten field 
	//
	assertEquals(Integer.valueOf(3),
		     (Integer)Accessor.getField(ForTests.class,
						new ForTestsB(),
						"aPrimitiveField"));
	     
	// testcase 7
	//
	// non-static not overwritten field 
	//
	assertEquals(Integer.valueOf(33),
		     (Integer)Accessor.getField(ForTestsB.class,
						new ForTestsB(),
						"aPrimitiveField"));
	     
	// testcase 8
	//
	// static field 
	//
	assertEquals(Integer.valueOf(4),
		     (Integer)Accessor.getField(ForTests.class,
						null,
						"aStaticPrimitiveField"));
	     
	// testcase 9
	//
	// static field 
	//
	assertEquals(Integer.valueOf(43),
		     (Integer)Accessor.getField(ForTestsB.class,
						null,
						"aStaticPrimitiveField"));
	     
// java.lang.AssertionError: expected:<43> but was:<430>
//         at org.junit.Assert.fail(Assert.java:91)
//         at org.junit.Assert.failNotEquals(Assert.java:645)
//         at org.junit.Assert.assertEquals(Assert.java:126)
//         at org.junit.Assert.assertEquals(Assert.java:145)
//         at eu.simuline.testhelpers.AccessorTest.testGetField(AccessorTest.java:195)
//         at eu.simuline.testhelpers.AccessorTest$TestAll.testGetField(AccessorTest.java:41)

	// testcase 10
	//
	// non-static field 
	//
	try {
	     Accessor.getField(Integer.class,null,"aNonExistingField");
	} catch (NoSuchFieldException e) {
	    assertEquals("aNonExistingField",e.getMessage());
	} // end of try-catch


	// testcase 11
	//
	// non-static field 
	//
	try {
	     Accessor.getField(ForTests.class,
			       new ForTests(),
			       "aStaticPrimitiveField");
	} catch (IllegalArgumentException e) {
	    assertEquals("The specified field 'aStaticPrimitiveField' should " + 
			 "not be static. ",
			 e.getMessage());
	} // end of try-catch


	// testcase 12
	//
	// non-static field 
	//
	try {
	     Accessor.getField(ForTests.class,
			       null,
			       "aPrimitiveField");
	} catch (IllegalArgumentException e) {
	    assertEquals("The specified field 'aPrimitiveField' should " + 
			 "be static. ",
			 e.getMessage());
	} // end of try-catch

    } // testGetField 


    public void testSetField() throws Exception {

	Object obj;

	// testcase 1
	//
	// non-static field 
	//
	obj = new ForTests();
	Accessor.setField(obj,"aPrimitiveField",Integer.valueOf(30));

	assertEquals(Integer.valueOf(30),
		     (Integer)Accessor.getField(obj,"aPrimitiveField"));



	// testcase 2
	//
	// non-static field without target 
	//
	try {
	     Accessor.setField((Object)null,"aPrimitiveField",null);
	} catch (IllegalArgumentException e) {
	    assertEquals("Specified null-target. ",e.getMessage());
	} // end of try-catch


	// testcase 3
	//
	// static field 
	//
	Accessor.setField(ForTests.class,
			  "aStaticPrimitiveField",
			  Integer.valueOf(40));
	assertEquals(Integer.valueOf(40),
		     (Integer)Accessor.getField(ForTests.class,
						"aStaticPrimitiveField"));


	// testcase 4
	//
	// static field without specifying a class 
	//
	try {
	     Accessor.setField((Class)null,
			       "aStaticPrimitiveField",
			       Integer.valueOf(-1));
	} catch (IllegalArgumentException e) {
	    assertEquals("Specified null-class. ",e.getMessage());
	} // end of try-catch

	// testcase 5
	//
	// static field 
	//
	try {
	     Accessor.setField(null,
			       null,
			       "aStaticPrimitiveField",
			       Integer.valueOf(-1));
	} catch (IllegalArgumentException e) {
	    assertEquals("Specified null-class. ",e.getMessage());
	} // end of try-catch


	// testcase 6
	//
	// non-static overwritten field 
	//
	obj = new ForTestsB();
	Accessor.setField(ForTests.class,
			  obj,
			  "aPrimitiveField",
			  Integer.valueOf(30));
	assertEquals(Integer.valueOf(30),
		     (Integer)Accessor.getField(ForTests.class,
						obj,
						"aPrimitiveField"));


	// testcase 7
	//
	// non-static not overwritten field 
	//
	obj = new ForTestsB();
	Accessor.setField(ForTestsB.class,
			  obj,
			  "aPrimitiveField",
			  Integer.valueOf(330));
	assertEquals(Integer.valueOf(330),
		     (Integer)Accessor.getField(ForTestsB.class,
						obj,
						"aPrimitiveField"));

	// testcase 8
	//
	// static field 
	//
	Accessor.setField(ForTests.class,
			  null,
			  "aStaticPrimitiveField",
			  Integer.valueOf(40));
	assertEquals(Integer.valueOf(40),
		     (Integer)Accessor.getField(ForTests.class,
						null,
						"aStaticPrimitiveField"));


	// testcase 9
	//
	// static field 
	//
	Accessor.setField(ForTestsB.class,
			  null,
			  "aStaticPrimitiveField",
			  Integer.valueOf(430));
	assertEquals(Integer.valueOf(430),
		     (Integer)Accessor.getField(ForTestsB.class,
						null,
						"aStaticPrimitiveField"));

	// testcase 10
	//
	// non-static field 
	//
	try {
	     Accessor.setField(Integer.class,
			       null,
			       "aNonExistingField",
			       Integer.valueOf(-1));
	} catch (NoSuchFieldException e) {
	    assertEquals("aNonExistingField",e.getMessage());
	} // end of try-catch


	// testcase 11
	//
	// non-static field 
	//
	try {
	     Accessor.setField(ForTests.class,
			       new ForTests(),
			       "aStaticPrimitiveField",
			       Integer.valueOf(-1));
	} catch (IllegalArgumentException e) {
	    assertEquals("The specified field 'aStaticPrimitiveField' should " + 
			 "not be static. ",
			 e.getMessage());
	} // end of try-catch


	// testcase 12
	//
	// non-static field 
	//
	try {
	     Accessor.setField(ForTests.class,
			       null,
			       "aPrimitiveField",
			       Integer.valueOf(-1));
	} catch (IllegalArgumentException e) {
	    assertEquals("The specified field 'aPrimitiveField' should " + 
			 "be static. ",
			 e.getMessage());
	} // end of try-catch


	// testcase 12
	//
	// final field 
	//
	try {
	     Accessor.setField(ForTests.class,
			       "aFinalField",
			       Integer.valueOf(-1));
	} catch (IllegalArgumentException e) {
	    assertEquals("Field 'aFinalField' in class '" + 
			 ForTests.class.getName() + 
			 "' is declared final and is hence not accessible. ",
			 e.getMessage());
	} // end of try-catch


	// testcase 13
	//
	// primitive field tried to assign a null value. 
	//
	try {
	    Accessor.setField(ForTests.class,
			      "aStaticPrimitiveField",
			      null);
	} catch (IllegalArgumentException e) {
	    assertEquals("Tried to assign null-value " + 
			 "to field 'aStaticPrimitiveField'" +
			 " in class '" + ForTests.class.getName() + 
			 "' although its type '" + Integer.TYPE + 
			 "' is primitive. ",
			 e.getMessage());
	} // end of try-catch

    } // testSetField 

    public void testCreate() throws Exception {
	ForTests ft;

	// testcase 1
	//
	// default constructor 
	//
	ft = Accessor.create(ForTests.class);

	// testcase 2
	//
	// default constructor 
	//
	ft = Accessor.create(ForTests.class,
			     new Class[] {},
			     new Object[] {});
    } // testCreate 

    public void testInvoke() throws Exception {
	int result;


	// testcase 1
	//
	//
	//
	result = ((Integer)Accessor.invoke(new ForTestsB(),
					   "privateMethod",
					   Integer.valueOf(2))
		      ).intValue();
	assertEquals(66,result);


	// testcase 2
	//
	//
	//
	result = ((Integer)Accessor.invoke(new ForTests(),
					   "privateMethod",
					   Integer.valueOf(2))
		      ).intValue();
	assertEquals(6,result);


	// testcase 3
	//
	//
	//
	result = ((Integer)Accessor.invoke(new ForTestsB(),
					   "protectedMethod",
					   Integer.valueOf(2))
		      ).intValue();
	assertEquals(660,result);


	// testcase 4
	//
	//
	//
	result = ((Integer)Accessor.invoke(new ForTests(),
					   "protectedMethod",
					   Integer.valueOf(2))
		      ).intValue();
	assertEquals(60,result);


	// testcase 5
	//
	//
	//
	Accessor.setField(ForTests.class,
			  "aStaticPrimitiveField",
			  Integer.valueOf(4));
	result = ((Integer)Accessor.invokeStatic(ForTests.class,
						 "privateStaticMethod",
						 Integer.valueOf(2))
		      ).intValue();
	assertEquals(8,result);

    } // testInvoke 

    public void testGetInnerClass() {

	 // testcase 1
	 //
	 // get inner static class 
	 //
	 try {
	     Accessor.getInnerClass(null,"ForTests");
	 } catch (IllegalArgumentException e) {
	     assertEquals("Specified null-class. ",e.getMessage());
	 } // end of try-catch
		      

	 // testcase 2
	 //
	 // get inner static class 
	 //
	 try {
	     Accessor.getInnerClass(AccessorTest.class,(String)null);
	 } catch (IllegalArgumentException e) {
	     assertEquals("Specified null-class-name. ",e.getMessage());
	 } // end of try-catch
		      

	 // testcase 3
	 //
	 // get inner static class 
	 //
	 try {
	     Accessor.getInnerClass(AccessorTest.class,"noInnerClass");
	 } catch (IllegalArgumentException e) {
	     assertEquals("Class '" + AccessorTest.class.getName() + 
			  "' has no inner class named '" + "noInnerClass" + "'. ",
			  e.getMessage());
	 } // end of try-catch


	 // testcase 4
	 //
	 // get inner static class 
	 //
	 assertEquals(ForTests.class,
		      Accessor.getInnerClass(AccessorTest.class,
					     "ForTests"));
    } // testGetInnerClass 

   /* -------------------------------------------------------------------- *
     * framework.                                                           *
     * -------------------------------------------------------------------- */


    /**
     * Runs the test case.
     *
     * Uncomment either the textual UI, Swing UI, or AWT UI.
     */
    public static void main(String args[]) {
	Actions.runFromMain();
    }

}
