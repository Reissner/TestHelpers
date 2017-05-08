
package eu.simuline.testhelpers;

import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Comparator;
import java.util.Collection;

import junit.framework.AssertionFailedError;

/**
 * Extends the framework of assertions 
 * provided by <code>junit.framework.Assert</code>. 
 * <ul>
 * <li>
 * The method {@link #assertNormAbsEquals(Object,Object,String,double)} 
 * generalizes the method 
 * <code>assertEquals(double expected, double actual, double delta)</code> 
 * to objects with a metric defined by a method (name). 
 * There is also a variant for relative precision, 
 * {@link #assertNormRelEquals(Object,Object,String,double)}. 
 * <li>
 * The methods {@link #assertRelEquals(double,double,double)} 
 * provides relative tolerances suited to numerical applications. 
 * For small number applications (e.g. failure probabilities) 
 * a combination of relative tolerance and absolute one 
 * may be prescribed by 
 * {@link #assertAbsRelEquals(double,double,double,double,double)}. 
 * <li>
 * Sometimes equality with respect to the equals-method 
 * differs from equality 
 * with respect to <code>Comparator</code>s and the <code>compareTo</code> 
 * method (think e.g. of the package <code>java.math</code>). 
 * To fill this gap {@link #assertIs(Assert.CmpObj,Comparable,Object)} 
 * is useful. 
 * This method can also check for "greater than" or that like. 
 * A variant of this using <code>Comparator</code>s 
 * is {@link #assertIs(Assert.CmpObj,Object,Object,Comparator)}. 
 * <li>
 * The method {@link #assertArraysEquals(Object,Object)} 
 * tests equality of arrays recursively. 
 * <li>
 * The method {@link #assertArraysEquals(Object,Object,double)} 
 * is a combination ot the two. 
 * <li>
 * The method 
 * {@link #assertRelEquals(double expected, double actual, double reldev)} 
 * is for testing relative deviations. 
 * <li>
 * The method 
 * {@link #assertStringEquals(String expected, String actual)} 
 * is designed for testing long strings. 
 * It provides error messages suitable to find deviations quickly. 
 * </ul>
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 */
public abstract class Assert<E> extends org.junit.Assert {

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    /**
     * Represents an ordering relation. 
     *
     * To be used as a parameter for the methods 
     * {@link #assertIs(Assert.CmpObj,Comparable,Object)}, 
     * {@link #assertIs(Assert.CmpObj,Object,Object,Comparator)} 
     * and variants thereof. 
     *
     * @see #LESS_EQ
     * @see #LESS
     * @see #GREATER
     * @see #GREATER_EQ
     * @see #EQUAL
     * @see #NOT_EQUAL
     */
    public enum CmpObj {

	LESS_EQ(" less or equal") {
	    boolean isValid(int flag3) {
		return flag3 <= 0;
	    }
	},

	LESS(" less") {
	    boolean isValid(int flag3) {
		return flag3 < 0;
	    }
	},

	GREATER(" greater") {
	    boolean isValid(int flag3) {
		return flag3 > 0;
	    }
	},

	GREATER_EQ(" greater or equal") {
	    boolean isValid(int flag3) {
		return flag3 >= 0;
	    }
	},

	EQUAL(" equal") {
	    boolean isValid(int flag3) {
		return flag3 == 0;
	    }
	},

	NOT_EQUAL(" not equal") {
	    boolean isValid(int flag3) {
		return flag3 != 0;
	    }
	};

	/**
	 * Core part of the message when the relation fails. 
	 */
	private String message;

	/**
	 * Creates a new <code>CmpObj</code> 
	 * with the given fractin of message; 
	 * also the implementation of method {@link #isValid} 
	 * has to be provided hereafter. 
	 *
	 * @param message a <code>String</code> value
	 */
	private CmpObj(String message) {
	    this.message = message;
	}

	/**
	 * Returns whether <code>isValid</code> method here.
	 *
	 * @param flag3 
	 *    an <code>int</code> which is the output 
	 *    of a method {@link java.lang.Comparable#compareTo} 
	 *    or {@link java.util.Comparator#compare}. 
	 * @return 
	 *    a <code>boolean</code> value 
	 *    signifying whether <code>flag3</code> is allowed. 
	 */
	abstract boolean isValid(int flag3);

	/**
	 * Returns <code>expected.compareTo(actual)</code> if possible. 
	 *
	 * @param expected 
	 *    an instance of an <code>Object</code>. 
	 * @param actual 
	 *    an <code>Object</code> or <code>null</code>. 
	 * @return 
	 *    the <code>int</code> value which results in invoking 
	 *    <code>expected.compare(actual)</code>. 
	 * @throws IllegalArgumentException
	 *    <ul>
	 *    <li>
	 *    if <code>expected</code> is not an instance 
	 *    of <code>Comparable</code> 
	 *    and in particular for <code>expected == null</code>, 
	 *    <li>
	 *    if invoking <code>expected.compareTo(actual)</code> 
	 *    raises an exception. 
	 *    </ul>
	 * @throws IllegalStateException
	 *    if <code>expected.compare(actual)</code> is evaluated 
	 *    for <code>actual == null</code> without throwing an exception 
	 *    as specified for {@link java.lang.Comparable#compareTo}. 
	 */
	private boolean invokeCompareTo(Comparable<?> expected,
					Object actual) {

	    // Check "expected". 
	    if (expected == null) {
		throw new IllegalArgumentException
		    ("Found expected value " + expected + 
		     " -- Use method assertNull instead. ");
	    }
	    // Here, expected instanceof Comparable 
	    // and in particular expected != null. 

	    Method compareTo;
	    try {
		compareTo = expected.getClass().getMethod("compareTo",
							  Object.class);
	    } catch(NoSuchMethodException e) {
		throw new IllegalStateException// NOPMD
		    (STR_OBJECT + expected + STR_DN_PROV + 
		     "public int compareTo(Object)\" - impossible " + 
		     "because expected is declared as Comparable. ");
	    }
	    // this is necessary 
	    // despite compareTo(Object) is supposed to be public, 
	    // because the including class 
	    // may not be accessible from within this Accessor. 
	    compareTo.setAccessible(true);

	    try {
		Integer result = (Integer)compareTo.invoke(expected, actual);
		if (actual != null) {
		    return isValid(result);
		}
		// by symmetry, expected.compareTo(actual) 
		// should have thrown a NullPointerException. 
		
		throw new IllegalStateException
		    ("Tried to compare <" + expected + 
		     "> to: <" + actual + 
		     "> which should raise a NullPointerException. ");
		
	    } catch(IllegalAccessException iace) {
		thrwAccessible(compareTo);
	    } catch(IllegalArgumentException iage) {
		thrwWrongArgs(compareTo);
	    } catch(InvocationTargetException ite) {
		throw new IllegalArgumentException// NOPMD
		    ("Could not test ordering because method " + compareTo + 
		     STR_RAISED + ite.getTargetException() + ". ");
	    }
	    throw new IllegalStateException();
	}

    } // enum CmpObj 

    /* -------------------------------------------------------------------- *
     * constants.                                                           *
     * -------------------------------------------------------------------- */

//    private final static String STR_METHOD = "Method ";
    private final static String STR_OBJECT = "Object ";
    private final static String STR_DN_PROV = " does not provide a method \"";
    private final static String STR_RAISED = " raised ";
//    private final static String STR_EXPECTED = "expected: <";
    private final static String STR_BUTWAS = "> but was: <";
    private final static String STR_ASTOP = ">. ";
    private final static String STR_IN_ABS_VAL = " in absolute value. ";

    /* -------------------------------------------------------------------- *
     * thrower methods.                                                     *
     * -------------------------------------------------------------------- */

    private final static void thrwAccessible(Method method) {
	throw new IllegalStateException
	    ("Method " + method + " is not accessible although it should. ");
    }

    private final static void thrwWrongArgs(Method method) {
	throw new IllegalStateException
	    ("Method " + method + 
	     " is provided with illegal arguments " + 
	     "although this should not be possible. ");
    }

    /* -------------------------------------------------------------------- *
     * methods: assertEquals.                                               *
     * -------------------------------------------------------------------- */


    // in fact, norm is more a metric. 
    /**
     * Returns the distance 
     * of the two objects <code>expected</code> and <code>actual</code> 
     * defined by the metric defined by the method named <code>norm</code>; 
     * typically something like the norm of a kind of difference. 
     * <p>
     * CAUTION: This method shall be designed to compute values close to zero, 
     * i.e. is applied to <code>actual</code> cloase to <code>expected</code>. 
     * This must be reflected in the definition 
     * of the method given by <code>norm</code>. 
     *
     * @param norm
     *    the non-null name of a metric method, 
     *    i.e. of a member method of the form 
     *    <code>public double norm(other)</code>  
     *    with the properties of a metric: 
     *    <ul>
     *    <li><code>x.norm(x)</code> yields <code>0</code>
     *    <li><code>x.norm(y)</code> and <code>y.norm(x)</code> 
     *        yield the same result, 
     *    <li><code>x.norm(y)+y.norm(z)\(\ge\) x.norm(z)</code>. 
     *    </ul>
     *    CAUTION: This method must be designed to be applied 
     *    to <code>actual</code> close to <code>expected</code> 
     *    i.e. with a lot of extinction. 
     *    For certain arithmetic types, this requires care 
     *    to obtain reasonable performance. 
     * @throws IllegalArgumentException
     *    if the test cannot be performed, i.e. 
     *    <ul>
     *    <li>
     *    <code>norm</code> does not represent a metric method, 
     *    i.e. a member method of the form 
     *    <code>public double norm(other)</code>. 
     *    Of course, the properties of a metric cannot be proved. 
     *    <li>
     *    invoking <code>expected.norm(actual)</code> raises an exception 
     *    </ul>
     * @see #assertNormAbsEquals(String,Object,Object,String,double)
     * @see #assertNormRelEquals(String,Object,Object,String,double)
     * @see #computeNorm1(String,Object)
     *///<code></code>
    private static double computeNorm2(String norm, 
				       Object expected, 
				       Object actual
				       //Object... actuals
				       ) {

	// get the norm2 method or null 
	Method mNorm2 = Accessor.getToBeInvoked(expected.getClass(),
						norm,
						expected.getClass());


	// ensure that a method with given signature exists 
	if (mNorm2 == null) {
	    throw new IllegalArgumentException
		(STR_OBJECT + expected + STR_DN_PROV + 
		 "public ... " + norm + "(" + expected.getClass() + 
		 ") as expected. ");
	}

	// ensure the right return type 
	if (!Double.TYPE.equals(mNorm2.getReturnType())) {
	    throw new IllegalArgumentException
		(STR_OBJECT + expected + STR_DN_PROV + 
		 "public double " + norm + "(" + expected.getClass() + 
		 ") as expected (have a look at the return type). ");
	}

	// ensure that the method is a member method 
	if (Modifier.isStatic(mNorm2.getModifiers())) {
	    throw new IllegalArgumentException
		(STR_OBJECT + expected + " provides a static method " + 
		 "\"public static double " + norm + "(" + expected.getClass() + 
		 ") not a member method as expected. ");
	}

	// Here, mNorm contains the correct method representing a norm 
	// resp. a metric 
	// (we expect this is unique). ***** 

	try {
	    // invoke norm method 
	    return (Double)mNorm2.invoke(expected, actual);
	} catch(IllegalAccessException iace) {
	    // throws IllegalStateException but shall never occur 
	    thrwAccessible(mNorm2);
	} catch(InvocationTargetException ite) {
ite.getTargetException().printStackTrace();
	    throw new IllegalArgumentException // NOPMD
		("Could not test deviation, because method " + mNorm2 + 
		 STR_RAISED + ite.getTargetException() + ". ");
	}
	throw new IllegalStateException("Reached unreachable statement. ");
    } // computeNorm2(...) 


    /**
     * Fails if <code>actual</code> is <code>null</code> 
     * or <code>actual</code> deviates from <code>expected</code> 
     * by at least <code>delta</code>, 
     * provided the test can be executed at all. 
     * The deviation is computed with respect to the metric 
     * given by <code>expected.norm(actual)</code> 
     * which is assumed to have signature <code>double norm(Cls actual)</code> 
     * with <code>Cls</code> the class of <code>expected</code>. 
     *
     * @param message
     *    the error message displayed if the test fails regularly, 
     *    i.e. if <code>computeNorm2(norm, expected, actual)</code> 
     *    can be evaluated without throwing an exception. 
     * @param expected
     *    The expected object (see the actual object). 
     * @param actual
     *    The actual object 
     *    which is expected to deviate from <code>expected</code> 
     *    by at most <code>delta</code> in norm 
     * @param norm
     *    the name of a metric method, 
     *    i.e. of a member method of the form 
     *    <code>public double norm(other)</code>  
     *    with the properties of a metric: 
     *    <ul>
     *    <li><code>x.norm(x)</code> yields <code>0</code>
     *    <li><code>x.norm(y)</code> and <code>y.norm(x)</code> 
     *        yield the same result, 
     *    <li><code>x.norm(y)+y.norm(z)\(\ge\) x.norm(z)</code>. 
     *    </ul>
     * @param delta
     *    the allowed deviation as a <code>double</code> value. 
     * @throws IllegalArgumentException
     *    if the test cannot be performed, i.e. 
     *    <ul>
     *    <li>
     *    <code>norm</code> is <code>null</code> 
     *    or does not represent a metric method, 
     *    i.e. a member method of the form 
     *    <code>public double norm(other)</code>. 
     *    Of course, the properties of a metric cannot be proved. 
     *    <li>
     *    invoking <code>expected.norm(actual)</code> raises an exception 
     *    </ul>
     * @throws IllegalArgumentException 
     *    if <code>expected==null</code>. 
     * @throws AssertionFailedError
     *    if the test can be performed, 
     *    i.e. no IllegalArgumentException is thrown but 
     *    <ul>
     *    <li>
     *    <code>actual==null</code> or 
     *    <li>the distance returned by the metric exceeds <code>delta</code>. 
     *    </ul>
      *///<code></code>
    public static void assertNormAbsEquals(String message,
					   Object expected, 
					   Object actual, 
					   String norm,
					   double delta) {
       
	checkNullsB(norm, expected, actual);
	double diff = computeNorm2(norm, expected, actual);

	if (diff > delta) {
	    fail(message);
	}

    } // assertNormAbsEquals(...) 

    public static void assertNormAbsEquals(Object expected, 
					   Object actual, 
					   String norm,
					   double delta) {
       
	assertNormAbsEquals(expectedActual(expected,actual) + ":  deviation " + 
			    computeNorm2(norm, expected, actual) + 
			    " exceeds " + delta + STR_IN_ABS_VAL,
			    expected, 
			    actual, 
			    norm,
			    delta);

    } // assertNormAbsEquals(...) 


    /**
     * Returns the norm of <code>expected</code>
     * defined by the method named <code>norm</code>. 
     *
     * @param norm
     *    the non-null name of a norm method, 
     *    i.e. of a member method of the form 
     *    <code>public double norm()</code>  
     *    with the properties of a norm: 
     *    <ul>
     *    <li>
     *    <code>x.norm()</code> yields <code>0</code> 
     *    iff <code>x</code> represents the zero vector. 
     *    <li>
     *    <code>(sx).norm()=s(x.norm())</code> 
     *    where <code>sx</code> and <code>s(...)</code> 
     *    represent scalar multiplication. 
     *    <li>
     *    <code>x.norm()+y.norm()\(\ge (x+y)\).norm(z)</code>, 
     *    where \(x+y\) represents the sum of vectors. 
     *    </ul>
     * @param expected
     *    The object from which the norm is to be computed. 
     * @return
     *    the norm of <code>expected</code>
     *    defined by the method named <code>norm</code>. 
     * @throws IllegalArgumentException
     *    if the test cannot be performed, i.e. 
     *    <ul>
     *    <li>
     *    <code>norm</code> does not represent a norm method, 
     *    i.e. a member method of the form 
     *    <code>public double norm(other)</code>. 
     *    Of course, the properties of a norm cannot be proved. 
     *    <li>
     *    invoking <code>expected.norm()</code> raises an exception 
     *    </ul>
     *///<code></code>
    private static double computeNorm1(String norm, 
				       Object expected) {
	Object[] actuals = new Object[0];
	//checkNullsB(norm, expected); checked by computeNorm2 already
	//assert expected != null && norm != null;

	// get the norm1 method or null 
	Method mNorm1 = Accessor.getToBeInvoked(expected.getClass(),
						norm);

	// ensure that a method with given signature exists 
	if (mNorm1 == null) {
	    throw new IllegalArgumentException
		(STR_OBJECT + expected + STR_DN_PROV + 
		 "public ... " + norm + "(" + expected.getClass() + 
		 ") as expected. ");
	}

	// ensure the right return type 
	if (!Double.TYPE.equals(mNorm1.getReturnType())) {
	    throw new IllegalArgumentException
		(STR_OBJECT + expected + STR_DN_PROV + 
		 "public double " + norm + 
		 "() as expected (have a look at the return type). ");
	}

	// ensure that the method is a member method 
	if (Modifier.isStatic(mNorm1.getModifiers())) {
	    throw new IllegalArgumentException
		(STR_OBJECT + expected + " provides a static method " + 
		 "\"public static double " + norm + 
		 "() not a member method as expected. ");
	}

	// Here, mNorm1 contains the correct method representing a norm 
	// (we expect this is unique). ***** 

	try {
	    // invoke norm method 
	    return (Double)mNorm1.invoke(expected);
	} catch(IllegalAccessException iace) {
	    // throws IllegalStateException but shall never occur 
	    thrwAccessible(mNorm1);
	} catch(InvocationTargetException ite) {
	    throw new IllegalArgumentException // NOPMD
		("Could not test deviation, because method " + mNorm1 + 
		 STR_RAISED + ite.getTargetException() + ". ");
	}
	throw new IllegalStateException("Reached unreachable statement. ");
    } // computeNorm1(...) 


    /**
     * Fails if <code>actual</code> is <code>null</code> 
     * or <code>actual</code> deviates from <code>expected</code> 
     * by at least <code>delta</code>, 
     * provided the test can be executed at all. 
     */
    public static void assertNormRelEquals(Object expected, 
					   Object actual, 
					   String norm,
					   double reldev) {
	assertNormRelEquals(expectedActual(expected,actual) + 
			    ":  relative deviation " + 
			    computeNorm2(norm, expected, actual)/
			    computeNorm1(norm, expected) + 
			    " exceeds " + reldev + STR_IN_ABS_VAL,
			    expected, 
			    actual, 
			    norm,
			    reldev);
    } // assertNormEquals(...) 

    public static void assertNormRelEquals(String message,
					   Object expected, 
					   Object actual, 
					   String norm,
					   double reldev) {

	checkNullsB(norm, expected, actual);
	double diff = computeNorm2(norm, expected, actual);
	// get the norm method 
	Method mNorm1 = Accessor.getToBeInvoked(expected.getClass(), norm);
	double dNorm = computeNorm1(norm, expected);

	if (diff/dNorm > reldev) {
	    fail(message);
	}
    } // assertNormEquals(...) 

    private static String expectedActual(Object expected, 
					 Object actual) {
	return "expected: <" + expected + STR_BUTWAS + actual + ">";
    }

    /**
     * Fails if the test can be executed, i.e. 
     * neither <code>norm</code> nor <code>expected</code> 
     * is <code>null</code>, and <code>actual</code> is <code>null</code>. 
     *
     * @param norm 
     *    name of a member method in class <code>Cls</code>
     *    with signature <code>double norm(Cls actual)</code>. 
     * @param expected 
     *    an <code>Object</code> with type <code>Cls</code>; 
     *    in particular not <code>null</code>. 
     * @param actual 
     *    another <code>Object</code>; in particular not <code>null</code>. 
     * @throws IllegalArgumentException 
     *    if <code>norm==null</code> or <code>expected==null</code>. 
     * @throws AssertionFailedError
     *    if <code>norm,expected!=null</code> but <code>actual==null</code>. 
     * @see #assertNormAbsEquals(String,Object,Object,String,double)
     * @see #assertNormRelEquals(String,Object,Object,String,double)
     * @see #checkNulls(String,Object,Object)
     */
    private static void checkNullsB(String norm, 
				    Object expected, 
				    Object actual) {

	if (norm == null) {
	    throw new IllegalArgumentException
	    ("Norm is " + norm + 
	     "; could not prove whether deviation exceeds some threshold. ");
	}
 
	if (expected == null) {
	    throw new IllegalArgumentException
		(expectedActual(expected,actual) + "; " +
		 "could not prove whether deviation exceeds some threshold. ");
	}

	if (actual == null) {
	    fail(expectedActual(expected,actual) + "; " +
		 "could not prove whether deviation exceeds some threshold. ");
	}
   }

    /**
     * Fails for <code>!expected.equals(actual)</code> 
     * and raises an exception if this expression cannot be evaluated. 
     *
     * @param expected 
     *    an instance of an <code>Object</code>. 
     * @param actual 
     *    an <code>Object</code> or <code>null</code>. 
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    for <code>expected == null</code>, 
     *    <li>
     *    if invoking <code>expected.equals(actual)</code> 
     *    raises an exception. 
     *    </ul>
     * @throws AssertionFailedError
     *    for <code>!expected.equals(actual)</code>. 
     */
    public static void assertEquals(Object expected,
				    Object actual) {

	// Exclude expected == null. 
	if (expected == null) {
	    throw new IllegalArgumentException
		("Found expected value " + expected + 
		 " -- Use method assertNull instead. ");
	}
	// Here, expected != null. 

	Method equals;
	try {
	    equals = expected.getClass().getMethod("equals", Object.class);
	} catch (NoSuchMethodException e) {
	    throw new IllegalStateException// NOPMD
		(STR_OBJECT + expected + STR_DN_PROV + 
		 "public boolean equals(Object)\" - " + 
		 "impossible because this is inherited from class Object. ");
	}
	// Here, equals contains the equals method of object expected. 

	// this is necessary 
	// despite equals(Object,double) is supposed to be public, 
	// because the including class 
	// may not be accessible from within this Accessor. 
	equals.setAccessible(true);

	try {
	    Boolean result = (Boolean)equals.invoke(expected, actual);
	    // for actual == null, result is Boolean.FALSE. 

	    if (!Boolean.TRUE.equals(result)) {
		fail(expectedActual(expected,actual) + ". ");
	    }
	} catch (IllegalAccessException iace) {
	    thrwAccessible(equals);
	} catch (IllegalArgumentException iage) {
	    thrwWrongArgs(equals);
	} catch(InvocationTargetException ite) {
	    throw new IllegalArgumentException// NOPMD
		("Could not test equality because method " + equals + 
		 STR_RAISED + ite.getTargetException() + ". ");
	}
    }

    public static void assertEquals(String message,
				    Object expected,
				    Object actual) {

	try {
	    assertEquals(expected,actual);
	} catch(AssertionFailedError e) {
	    fail(message);
	}
    }

    /* -------------------------------------------------------------------- *
     * methods concerning subset and containment relations.                 *
     * -------------------------------------------------------------------- */

    /**
     * Fails if <code>actualElement</code> is not an element 
     * of <code>expectedContainer</code>. 
     *
     * @param expectedContainer 
     *    a <code>Collection</code> the object <code>actualElement</code> 
     *    is expected to be contained in. 
     * @param actualElement 
     *    an instance of an <code>Object</code>. 
     * @throws AssertionFailedError
     *    if <code>actualElement</code> 
     *    is not in <code>expectedContainer</code>
     *    and in particular if it is <code>null</code>. 
     */
    public static <E> void assertIsIn(Collection<E> expectedContainer, 
				      Object actualElement) {
	if (actualElement == null ||
	    !expectedContainer.contains(actualElement)) {
	    fail("Expected an element of <" + expectedContainer + 
		 "> but found <" + actualElement + STR_ASTOP);
	}
    }

    /**
     * Fails if <code>actual</code> is not a subcollection 
     * of <code>expected</code>. 
     *
     * @param expected 
     *    a <code>Collection</code> 
     * @param actual 
     *    another <code>Collection</code> 
     * @throws AssertionFailedError
     *    if <code>actual</code> is not a subcollection 
     *    of <code>expectedContainer</code>. 
     *    This includes the cases where a parameter is <code>null</code>. 
     */
    public static <E> void assertIsContainedAll(Collection<E> expected, 
						Collection<E> actual) {
	if (expected == null) {
	    fail("Found expected set value null; use assertNull instead. ");
	}
	
	if (actual == null) {
	    fail("Tried to perform containment check on null-collection. ");
	}
	
	assertTrue("Expected <" + actual + 
		   "> to be a subset of <"+expected + STR_ASTOP,
		   expected.containsAll(actual));
    }

    /* -------------------------------------------------------------------- *
     * methods: assertions based on comparisons.                            *
     * -------------------------------------------------------------------- */

    /**
     * Fails if <code>expected.compareTo(actual)</code> 
     * is not as expected 
     * and raises an exception if this expression cannot be evaluated. 
     *
     * @param message
     *    the error message displayed if the test fails regularly, 
     *    i.e. if <code>expected.compareTo(actual)</code> can be evaluated 
     *    without throwing an exception. 
     * @param cmpObj 
     *    The action that decides whether the relation is satisfied. 
     * @param expected 
     *    an instance of a <code>Comparable</code>. 
     * @param actual 
     *    an <code>Object</code> or <code>null</code>. 
     *    Actually this should be a <code>Comparable</code> as well. 
     * @throws IllegalArgumentException
     *    if invoking <code>expected.compareTo(actual)</code> 
     *    raises an exception. 
     *    In particular for <code>expected == null</code>
     * @throws IllegalStateException
     *    if <code>expected.compare(actual)</code> is evaluated 
     *    for <code>actual == null</code> without throwing an exception 
     *    as specified for {@link java.lang.Comparable#compareTo}. 
     * @throws AssertionFailedError
     *    if the value of <code>expected.compareTo(actual)</code> 
     *    is not as specified by <code>cmpObj</code>. 
     * @see #assertIs(CmpObj,Comparable,Object)
     */
    public static <E> void assertIs(CmpObj cmpObj,
				    String message,
				    Comparable<E> expected,
				    E actual) {
	if (!(cmpObj.invokeCompareTo(expected,actual))) {
	    fail(message);
	}
    }

    /**
     * Fails if <code>cmp.compare(expected,actual)</code> 
     * is not as expected 
     * and raises an exception if this expression cannot be evaluated. 
     *
     * @param message
     *    the error message displayed if the test fails regularly, 
     *    i.e. if <code>cmp.compare(expected,actual)</code> can be evaluated 
     *    without throwing an exception. 
     * @param cmpObj 
     *    The action that decides whether the relation is satisfied. 
     * @param expected 
     *    an <code>Object</code>. 
     * @param actual 
     *    another <code>Object</code>. 
     * @throws IllegalArgumentException
     *    if invoking <code>cmp.compare(expected,actual)</code> 
     *    raises an exception. 
     * @throws AssertionFailedError
     *    if the value of <code>cmp.compare(expected,actual)</code> 
     *    is not as specified by <code>cmpObj</code>. 
     * @see #assertIs(CmpObj,Object,Object,Comparator)
     */
   public static <E> void assertIs(CmpObj cmpObj,
				   String message,
				   E expected,
				   E actual,
				   Comparator<E> cmp) {
	if (!(cmpObj.isValid(invokeCompare(expected,actual,cmp)))) {
	    fail(message);
	}
    }

    /**
     * Fails if <code>expected.compareTo(actual)</code> 
     * is not as expected 
     * and raises an exception if this expression cannot be evaluated. 
     *
     * @param cmpObj 
     *    The action that decides whether the relation is satisfied. 
     * @param expected 
     *    an instance of a <code>Comparable</code>. 
     * @param actual 
     *    an <code>Object</code> or <code>null</code>. 
     *    Actually this should be a <code>Comparable</code> as well. 
     * @throws IllegalArgumentException
     *    if invoking <code>expected.compareTo(actual)</code> 
     *    raises an exception. 
     *    In particular for <code>expected == null</code>
     * @throws IllegalStateException
     *    if <code>expected.compare(actual)</code> is evaluated 
     *    for <code>actual == null</code> without throwing an exception 
     *    as specified for {@link java.lang.Comparable#compareTo}. 
     * @throws AssertionFailedError
     *    if the value of <code>expected.compareTo(actual)</code> 
     *    is not as specified by <code>cmpObj</code>. 
     * @see #assertIs(CmpObj,String,Comparable,Object)
     */
    public static <E> void assertIs(CmpObj cmpObj,
				    Comparable<E> expected,
				    E actual) {

	assertIs(cmpObj,
		 "expected: <" + expected + 
		 "> to be" + cmpObj.message + 
		 ": <" + actual + STR_ASTOP,
		  expected,actual);
    }

    /**
     * Fails if <code>cmp.compare(expected,actual)</code> 
     * is not as expected 
     * and raises an exception if this expression cannot be evaluated. 
     *
     * @param cmpObj 
     *    The action that decides whether the relation is satisfied. 
     * @param expected 
     *    an <code>Object</code>. 
     * @param actual 
     *    another <code>Object</code>. 
     * @throws IllegalArgumentException
     *    if invoking <code>cmp.compare(expected,actual)</code> 
     *    raises an exception. 
     * @throws AssertionFailedError
     *    if the value of <code>cmp.compare(expected,actual)</code> 
     *    is not as specified by <code>cmpObj</code>. 
     * @see #assertIs(CmpObj,String,Object,Object,Comparator)
     */
    public static <E> void assertIs(CmpObj cmpObj,
				    E expected,
				    E actual,
				    Comparator<E> cmp) {

	assertIs(cmpObj,
		 "expected: <" + expected + 
		 "> to be" + cmpObj.message + 
		 ": <" + actual + 
		 "> with respect to the comparator <" + cmp + STR_ASTOP,
		  expected,actual,cmp);
    }


    /**
     * Returns <code>cmp.compare(expected,actual)</code> if possible. 
     *
     * @param obj1 
     *    an <code>Object</code>. 
     * @param obj2 
     *    another <code>Object</code>. 
     * @param cmp 
     *    a comparator which is capable of comparing 
     *   <code>obj1</code> with <code>obj2</code>. 
     * @return 
     *    the <code>int</code> value which results in invoking 
     *    <code>cmp.compare(expected,actual)</code>. 
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    for <code>cmp == null</code>, 
     *    <li>
     *    if invoking <code>cmp.compare(expected,actual)</code> 
     *    raises an exception. 
     *    </ul>
     */
    private static <E> int invokeCompare(E obj1,
					 E obj2,
					 Comparator<E> cmp) {

	// Check comparator. 
	if (cmp == null) {
	    throw new IllegalArgumentException
		("Found null-comparator . ");
	}
	// Here, the comparator is not null. 

	Method compare;
	try {
	    compare = cmp.getClass().getMethod("compare",
					       Object.class,
					       Object.class);
	} catch(NoSuchMethodException e) {
	    throw new IllegalStateException // NOPMD
		("Comparator " + cmp + STR_DN_PROV + 
		 "public int compare(Object, Object)\" - impossible. ");
	}
	// this is necessary 
	// despite compare(Object,Object) is supposed to be public, 
	// because the including Comparator 
	// may not be accessible from within this Accessor. 
	compare.setAccessible(true);


	try {
	    return (Integer)compare.invoke(cmp, obj1, obj2);
	} catch(IllegalAccessException iace) {
	    thrwAccessible(compare);
	} catch(IllegalArgumentException iage) {
	    thrwWrongArgs(compare);
	} catch(InvocationTargetException ite) {
	    throw new IllegalArgumentException// NOPMD
		("Could not test ordering because method " + compare + 
		 STR_RAISED + ite.getTargetException() + ". ");
	}
	throw new IllegalStateException();
    }

    /**
     * Throws an error if exactly one of the parameters are <code>null</code> 
     * and otherwise returns whether both are <code>null</code>. 
     *
     * @param message 
     *    the error message used in case the assertion fails. 
     * @param expected 
     *    an <code>Object</code>. 
     * @param actual 
     *    another <code>Object</code>. 
     * @return 
     *    whether both are <code>null</code>, 
     *    if either both, <code>expected</code> and <code>actual</code> 
     *    are <code>null</code>. 
     * @throws AssertionFailedError 
     *    if exactly one of the parameters are <code>null</code>. 
     */
    private static boolean checkNulls(String message,
				      Object expected, 
				      Object actual) {

	if (expected == null ^ actual == null) {
	    fail(message);
	}
	return expected == null;
    }

    /* -------------------------------------------------------------------- */
    /* methods for tests of arrays                                          */
    /* -------------------------------------------------------------------- */



    /**
     * Is a deep version of method 
     * <code>junit.framework.Assert.assertEquals(Object,Object)</code> 
     * for arrays: checks 
     * <ul>
     * <li>
     * whether the two arguments are arrays and whether they are arrays, 
     * <li>
     * recursively whether the lengths coincide 
     * and if the entries do so. 
     * </ul>
     *
     * @param message 
     *    the error message used in case the assertion fails. 
     * @param expected 
     *    an array. 
     * @param actual 
     *    an array. 
     * @exception IllegalArgumentException 
     *    if <code>expected</code> is not an array. 
     * @exception AssertionFailedError
     *    if the types of the two arguments do not coincide 
     *    (e.g. because the second one is not an array). 
     *    See also {@link #assertRecArraysEquals}. 
     * @throws AssertionFailedError
     *    if the the two arrays do not coincide in their lenght 
     *    or in some entry. 
     */
    public static void assertArraysEquals(String message,
					  Object expected, 
					  Object actual) {


	try {
	    assertArraysEquals(expected,actual);
	} catch(AssertionFailedError e) {
	    fail(message);
	}
    }

    public static void assertArraysEquals(Object expected, 
					  Object actual) {

	// Exclude the case that either "expected" or "actual" is null. 
	if (checkNulls(expectedActual(expected,actual) + ". ",
		       expected,
		       actual)) {
	    return;
	}
	// Here, neither "expected" nor "actual" is null. 

	checkArraysSameClass(expected,actual);
	// Here, both are arrays or neither of them. 
	assertRecArraysEquals(expected,actual,new int[0]);
    }

    /**
     * Checks whether <code>expected</code> is an array 
     * and whether its type coincides with the type of <code>actual</code>. 
     * If not an exception is thrown. 
     * Invokes {@link #fail} if the types of <code>expected</code> 
     * and of <code>actual</code> do not coincide. 
     *
     * @param expected 
     *    the expected array. 
     * @param actual 
     *    the actual array. 
     * @throws IllegalArgumentException 
     *    if <code>expected</code> is not an array. 
     */
    private static void checkArraysSameClass(Object expected, 
					     Object actual) {
	if (!expected.getClass().isArray()) {
	    throw new IllegalArgumentException
		("Array expected; found type " + expected.getClass() + ". ");
	}

	if (!expected.getClass().equals(actual.getClass())) {
	    fail("expected class: <" + expected.getClass() + 
		 STR_BUTWAS + actual.getClass() + STR_ASTOP);
	}
    }

    /**
     * Is a deep version of method 
     * <code>junit.framework.Assert.assertEquals(Object,Object)</code> 
     * for arrays: 
     * checks recursively whether the lengths coincide 
     * and if the entries do so. 
     * Contract: the classes of the two arguments coincide 
     * and both are arrays. 
     *
     * @param expected 
     *    the expected array. 
     * @param actual 
     *    the actual array. 
     * @param indices 
     *    contains the list of indices of the arrays 
     *    <code>expected</code> and <code>actual</code> 
     *    within the arrays comprising them. 
     *    The recursion starts with <code>indices = new int[] {}</code>. 
     * @throws AssertionFailedError
     *    if the the two arrays do not coincide in their lenght 
     *    or in some entry.
     */
    private static void assertRecArraysEquals(Object expected, 
					      Object actual, 
					      int[] indices) {

	if (Array.getLength(expected) != Array.getLength(actual)) {

	    fail(failLengthMessage(expected,actual,indices));
	    //fail("expected length: <" + Array.getLength(expected) + 
	    //	 STR_BUTWAS +       Array.getLength(actual) + STR_ASTOP);
	}
	// Here, both are arrays and their lengths coincide. 

	// Check the entries of the arrays. 
	Object expectedEntry;
	Object actualEntry;
	for (int i = 0; i < Array.getLength(expected); i++) {
	    // Check the i-th entries. 

	    // This works even for primitive types. 
	    // Instead their wrappers are returned. 
	    expectedEntry = Array.get(expected,i);
	    actualEntry   = Array.get(actual,  i);

	    int[] newInd = new int[indices.length+1];
	    System.arraycopy(indices,0,newInd,0,indices.length);
	    newInd[newInd.length-1] = i;

	    if (checkNulls(failMessage(expectedEntry,actualEntry,newInd),
			   expectedEntry,actualEntry)) {
		return;
	    }
	    // Here, neither "expectedEntry" nor "actualEntry" is null. 

	    if (expectedEntry.getClass().isArray()) {
		// Here, both of the objects are arrays with the same type. 
		// Exclude that either "expected" or "actual" is null. 

		assertRecArraysEquals(expectedEntry,
				      actualEntry,
				      newInd);
	    } else {
		// Here, neither of the objects are arrays 
		// (but their types coincide). 

		assertEquals(failMessage(expectedEntry,actualEntry,newInd),
			     expectedEntry,actualEntry);
	    }
	}
    }

/*
    private static String getEntries(int[] indices) {

	// Determine indices of entry
	StringBuffer message = new StringBuffer();
	if (indices.length == 0) {
	    return null;
	}
	message.append("In entry [" + indices[0]);
	for (int j = 1; j < indices.length; j++) {
	    message.append(", " + indices[j]);
	}
	message.append("]");
	return message.toString();
    }
*/

    private static String failInd(int[] indices) {

	StringBuffer message = new StringBuffer();
	// Determine indices of entry 
	if (indices.length > 0) {
	    message.append("In entry [");
	    for (int j = 0; j < indices.length-1; j++) {
		message.append("" + indices[j] + ", ");
	    }
	    message.append("" + indices[indices.length-1] + "] expected ");
	} else {
	    message.append("Expected ");
	}
	return message.toString();
    }


    private static String failLengthMessage(Object expectedEntry, 
					    Object actualEntry, 
					    int[] indices) {
	StringBuffer message = new StringBuffer(55);
	// Determine indices of entry
	message.append(failInd(indices));
	
	// ""+obj  works for null; obj.toString() does not
	message.append("array with length <");
	message.append(Array.getLength(expectedEntry));
	message.append("> but was array with length <");
	message.append(Array.getLength(actualEntry));
	message.append(STR_ASTOP);
	return message.toString();
    }

    private static String failMessage(Object expectedEntry, 
				      Object actualEntry, 
				      int[] indices) {

	// Determine indices of entry
	StringBuffer message = new StringBuffer();
	// Determine indices of entry
	message.append(failInd(indices));

	// ""+obj  works for null; obj.toString() does not
	message.append(" <");
	message.append(expectedEntry);
	message.append(STR_BUTWAS);
	message.append(actualEntry);
	message.append(STR_ASTOP);
	return message.toString();
    }

    /**
     * Returns a failure message indicating that 
     * in comparing nested arrays of final float type 
     * as <code>double[][]</code>, 
     * the corresponding entries given by index path <code>indices</code> 
     * deviate at least <code>delta</code>. 
     * 
     */
    // used in assertRecArraysEquals(Object, Object, int[], double)
    private static String failMessageDelta(double expectedEntry, 
					   double actualEntry, 
					   int[] indices, 
					   double delta) {

	// Determine indices of entry
	StringBuffer message = new StringBuffer(45);
	if (indices.length > 0) {
	    message.append("In entry [");
	    for (int j = 0; j < indices.length-1; j++) {
		message.append(indices[j]);
		message.append(", ");
	    }
	    message.append(indices[indices.length-1]);// NOPMD
	    message.append("] expected <");
	} else {
	    message.append("Expected <");
	}

	// ""+obj  works for null; obj.toString() does not
	message.append(expectedEntry);
	message.append(STR_BUTWAS);
	message.append(actualEntry);
	message.append(">: deviation exceeds ");
	message.append(delta);
	message.append(STR_IN_ABS_VAL);
	return message.toString();
    }

    private static String failMessageLength(int expectedLen, 
					    int actualLen, 
					    int[] indices) {
	// Determine indices of entry
	StringBuffer message = new StringBuffer(45);
	if (indices.length > 0) {
	    message.append("In entry [");
	    for (int j = 0; j < indices.length-1; j++) {
		message.append(indices[j]);
		message.append(", ");
	    }
	    message.append(indices[indices.length-1]);// NOPMD
	    message.append("] expected lengths <");
	} else {
	    message.append("Expected lengths <");
	}

	// ""+obj  works for null; obj.toString() does not
	message.append(expectedLen);
	message.append(STR_BUTWAS);
	message.append(actualLen);
	message.append(STR_ASTOP);
	return message.toString();
    }


    /**
     * Is a deep version of method 
     * <code>junit.framework.Assert.assertEquals(Object,Object)</code> 
     * for arrays: checks 
     * <ul>
     * <li>
     * whether the two arguments are arrays and whether they are arrays, 
     * <li>
     * recursively whether the lengths coincide 
     * and if the entries do so. 
     * </ul>
     *
     * @param expected 
     *    the expected array. 
     * @param actual 
     *    the actual array 
     *    which is assumed to be of same type as <code>expected</code>. 
     * @param delta 
     *    the allowed deviation as a <code>double</code> value. 
     * @exception IllegalArgumentException 
     *    if <code>expected</code> is not an array. 
     * @exception AssertionFailedError
     *    if the types of the two arguments do not coincide 
     *    (e.g. because the second one is not an array). 
     *    See also {@link #assertRecArraysEquals}. 
     * @throws AssertionFailedError
     *    if the the two arrays do not coincide in their lenght 
     *    or in some entry. 
     */
    public static void assertArraysEquals(Object expected, 
					  Object actual, 
					  double delta) {

	checkArraysSameClass(expected, actual);
	// Here, both are arrays or neither of them. 
	assertRecArraysEquals(expected, actual, new int[0], delta);
    }

    /**
     * Is a deep version of method 
     * <code>junit.framework.Assert.assertEquals(Object,Object)</code> 
     * for arrays: 
     * checks recursively whether the lengths coincide 
     * and if the entries do so. 
     * Contract: the classes of the two arguments coincide 
     * and both are arrays. 
     *
     * @param expected 
     *    the expected array. 
     * @param actual 
     *    the actual array 
     *    which is assumed to be of same type as <code>expected</code>. 
     * @param indices 
     *    contains the list of indices of the arrays 
     *    <code>expected</code> and <code>actual</code> 
     *    within the arrays comprising them. 
     *    The recursion starts with <code>indices = new int[] {}</code>. 
     *    This is for generating an appropriate message 
     *    for an <code>AssertionFailedError</code>. 
     * @param delta 
     *    the allowed deviation as a <code>double</code> value. 
     * @throws IllegalArgumentException
     *    if <code>expected</code> or <code>actual</code> is not an array. 
     * @throws AssertionFailedError
     *    if the the two arrays do not coincide in their length 
     *    or in some entry. 
     * @throws eu.simuline.util.NotYetImplementedException
     *    if <code>expected</code> is not 'finally' of primitive type. 
     */
    private static void assertRecArraysEquals(Object expected, 
					      Object actual, 
					      int[] indices,
					      double delta) {

	// throws IllegalArgumentException if not an array 
	if (Array.getLength(expected) != Array.getLength(actual)) {
	    fail(failMessageLength(Array.getLength(expected),
				   Array.getLength(actual),
				   indices));
	}
	// Here, both are arrays and their lengths coincide. 

	// Check the entries of the arrays. 
	Object expectedEntry;
	Object actualEntry;
	for (int i = 0; i < Array.getLength(expected); i++) {
	    // Check the i-th entries. 

	    // This works even for primitive types. 
	    // Instead their wrappers are returned. 
	    expectedEntry = Array.get(expected,i);
	    actualEntry   = Array.get(actual,  i);

	    int[] newInd = new int[indices.length+1];
	    System.arraycopy(indices, 0, newInd, 0, indices.length);
	    newInd[newInd.length-1] = i;

	    Class<?> entryClass = expectedEntry.getClass();
	    if (entryClass.isArray()) {
		// Here, both of the objects are arrays with the same type. 
		assertRecArraysEquals(expectedEntry,actualEntry,newInd,delta);
		return;
	    }

	    // Here, neither of the objects are arrays 
	    // (but their types coincide). 
	    if (entryClass.isPrimitive()) {
		// primitive: unwrap before use. 
		if (entryClass == Double.TYPE) {
		    assertEquals(failMessageDelta((double)expectedEntry,
						  (double)actualEntry,
						  newInd,
						  delta),
				 (double)expectedEntry,
				 (double)  actualEntry,
				 delta);
		} else if (entryClass == Float.TYPE) {
		    assertEquals(failMessageDelta((double)expectedEntry,
						  (double)actualEntry,
						  newInd,
						  delta),
				 (float)expectedEntry,
				 (float)  actualEntry,
				 (float)delta);
		} else {
		    throw new IllegalArgumentException
			("For primitive type " + entryClass + 
			 " no method assertEquals(" + entryClass + 
			 "," + entryClass + "," + entryClass + 
			 ") exists. ");
		}
	    } else {
		// not primitive: use as is. 
		// **** no, this requires a special norm or metric 
		// and can thus not be handled uniformly. 
		throw new eu.simuline.util.NotYetImplementedException();
		    // ("Primitive type expected; found " + 
		    //  expectedEntry.getClass() + ". ");
		// assertEquals(failMessage(expectedEntry,
		// 			     actualEntry,
		// 			     newInd),
		// 		 expectedEntry,actualEntry,delta);
	    } // else 
	} // for 
    }

    /**
     * Returns whether the relative deviation 
     * between <code>expected</code> and <code>actual</code> 
     * exceeds <code>reldiv</code> in absolute value. 
     *
     * @param expected 
     *    the <code>double</code> value expected. 
     *    This may be neither <code>0.0</code>, nor an infinite value 
     *    nor <code>NaN</code>. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param reldev 
     *    the maximum relative deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This must be a non-negative value; 
     *    in particular, <code>NaN</code> is not allowed. 
     * @return
     *    whether the relative deviation 
     *    between <code>expected</code> and <code>actual</code> 
     *    exceeds <code>reldiv</code> in absolute value. 
     * @throws IllegalArgumentException 
     *    <ul>
     *    <li>
     *    if <code>expected</code> is either <code>0.0</code>, 
     *    infinite or <code>NaN</code>. 
     *    <li>
     *    if <code>reldev</code> is negative or <code>NaN</code>. 
     *    </ul>
     * @see #assertRelEquals(String,double,double,double)
     */
   public static boolean testRelEquals(double expected,
				       double actual,
				       double reldev) {
	if (expected == 0.0 || 
	    Double.isInfinite(expected) || 
	    Double.isNaN(expected)) {
	    throw new IllegalArgumentException
		("Relative deviation for expected value <" + 
		 expected + "> is not defined. ");
	}

	if (Double.isNaN(reldev) || reldev < 0.0) {
	    throw new IllegalArgumentException
		("The relative deviation may not be <" + reldev + STR_ASTOP);
	}
	
	return Math.abs((expected-actual)/expected) <= reldev;
    }

    /**
     * Fails if the relative deviation 
     * between <code>expected</code> and <code>actual</code> 
     * exceeds <code>reldiv</code> in absolute value. 
     *
     * @param message 
     *    the error message used in case the assertion fails. 
     * @param expected 
     *    the <code>double</code> value expected. 
     *    This may be neither <code>0.0</code>, nor an infinite value 
     *    nor <code>NaN</code>. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param reldev 
     *    the maximum relative deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This must be a non-negative value; 
     *    in particular, <code>NaN</code> is not allowed. 
     * @throws IllegalArgumentException 
     *    <ul>
     *    <li>
     *    if <code>expected</code> is either <code>0.0</code>, 
     *    infinite or <code>NaN</code>. 
     *    <li>
     *    if <code>reldev</code> is negative or <code>NaN</code>. 
     *    </ul>
     * @see #assertRelEquals(double,double,double)
     * @see #testRelEquals(double,double,double)
     */
    public static void assertRelEquals(String message,
				       double expected,
				       double actual,
				       double reldev) {
	if (!testRelEquals(expected, actual,reldev)) {
	    fail(message);
	}
    }

    /**
     * Fails reporting a standard message if the relative deviation 
     * between <code>expected</code> and <code>actual</code> 
     * exceeds <code>reldiv</code> in absolute value. 
     *
     * @param expected 
     *    the <code>double</code> value expected. 
     *    This may be neither <code>0.0</code>, nor an infinite value 
     *    nor <code>NaN</code>. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param reldev 
     *    the maximum relative deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This must be a non-negative value; 
     *    in particular, <code>NaN</code> is not allowed. 
     * @throws IllegalArgumentException 
     *    <ul>
     *    <li>
     *    if <code>expected</code> is either <code>0.0</code>, 
     *    infinite or <code>NaN</code>. 
     *    <li>
     *    if <code>reldev</code> is negative or <code>NaN</code>. 
     *    </ul>
     * @see #assertRelEquals(String,double,double,double)
     */
    public static void assertRelEquals(double expected,
				       double actual,
				       double reldev) {
	assertRelEquals(expectedActual(expected,actual) + 
			"; relative deviation <" + 
			((expected-actual)/expected) + 
			"> exceeds <" + reldev + "> in absolute value. ",
			expected,actual,reldev);
    }

    /**
     * For <code>expected&lt;= separateAbsRel</code> behaves like 
     * {@link junit.framework.Assert#assertEquals(String,double,double,double)} 
     * ignoring <code>reldev</code>, whereas otherwise behaves like 
     * {@link #assertEquals(String,double,double,double)} 
     * ignoring <code>absdev</code>. 
     *
     * @param message 
     *    the error message used in case the assertion fails. 
     * @param expected 
     *    the <code>double</code> value expected. 
     * @param separateAbsRel 
     *    a non-negative <code>double</code> value 
     *    separating the two parts of the domain of this method. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param absdev 
     *    the maximum absolute deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This is relevant for <code>expected&lt;= separateAbsRel</code> 
     *    but ignored otherwise. 
     * @param reldev 
     *    the maximum relative deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This is relevant for <code>expected &gt; separateAbsRel</code> 
     *    but ignored otherwise. 
     * @throws IllegalArgumentException 
     *    for <code>separateAbsRel &lt; 0</code>. 
     */
    public static void assertAbsRelEquals(String message,
					  double expected,
					  double separateAbsRel,
					  double actual,
					  double absdev,
					  double reldev) {
	if (separateAbsRel < 0) {
	    throw new IllegalArgumentException
		("Found negative separator " + separateAbsRel +". ");
	}
	if (Math.abs(expected) <= separateAbsRel) {
	    assertEquals   (message,expected,actual,absdev);
	} else {
	    assertRelEquals(message,expected,actual,reldev);
	}
    }

    /**
     * For <code>expected&lt;= separateAbsRel</code> behaves like 
     * {@link junit.framework.Assert#assertEquals(String,double,double,double)} 
     * ignoring <code>reldev</code>, whereas otherwise behaves like 
     * {@link #assertEquals(String,double,double,double)} 
     * ignoring <code>absdev</code>. 
     *
     * @param expected 
     *    the <code>double</code> value expected. 
     * @param separateAbsRel 
     *    a non-negative <code>double</code> value 
     *    separating the two parts of the domain of this method. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param absdev 
     *    the maximum absolute deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This is relevant for <code>expected&lt;= separateAbsRel</code> 
     *    but ignored otherwise. 
     * @param reldev 
     *    the maximum relative deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This is relevant for <code>expected &gt; separateAbsRel</code> 
     *    but ignored otherwise. 
     * @throws IllegalArgumentException 
     *    for <code>separateAbsRel &lt; 0</code>. 
     */
    public static void assertAbsRelEquals(double expected,
					  double separateAbsRel,
					  double actual,
					  double absdev,
					  double reldev) {
	if (separateAbsRel < 0) {
	    throw new IllegalArgumentException
		("Found negative separator " + separateAbsRel +". ");
	}
	if (Math.abs(expected) <= separateAbsRel) {
	    assertEquals   (expected,actual,absdev);
	} else {
	    assertRelEquals(expected,actual,reldev);
	}
    }

    /**
     * Returns whether the absolute deviation 
     * between <code>expected</code> and <code>actual</code> 
     * exceeds <code>absdiv</code> in absolute value. 
     *
      * @param expected 
     *    the <code>double</code> value expected. 
     *    This may not be <code>NaN</code>. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param absdev 
     *    the maximum absolute deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This must be a non-negative value; 
     *    in particular, <code>NaN</code> is not allowed. 
     * @return
     *    whether the absolute deviation 
     *    between <code>expected</code> and <code>actual</code> 
     *    exceeds <code>absdiv</code> in absolute value. 
     * @throws IllegalArgumentException 
     *    <ul>
     *    <li>
     *    if <code>expected</code> is <code>NaN</code>. 
     *    <li>
     *    if <code>absdev</code> is negative or <code>NaN</code>. 
     *    </ul>
     * @see #assertAbsEquals(String,double,double,double)
     */
   public static boolean testAbsEquals(double expected,
				       double actual,
				       double absdev) {
	if (Double.isNaN(expected)) {
	    throw new IllegalArgumentException
		("Absolute deviation for expected value <" + 
		 expected + "> is not defined. ");
	}

	if (Double.isNaN(absdev) || absdev < 0.0) {
	    throw new IllegalArgumentException
		("The absolute deviation may not be <" + absdev + STR_ASTOP);
	}

	if (Double.isInfinite(expected)) {
	    return expected == actual;
	}

	
	return Math.abs(expected-actual) <= absdev;
    }

   /**
     * Fails if the absolute deviation 
     * between <code>expected</code> and <code>actual</code> 
     * exceeds <code>absdiv</code> in absolute value. 
     *
     * @param message 
     *    the error message used in case the assertion fails. 
     * @param expected 
     *    the <code>double</code> value expected. 
     *    This may not be neither <code>NaN</code>. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param absdev 
     *    the maximum absolute deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This must be a non-negative value; 
     *    in particular, <code>NaN</code> is not allowed. 
     * @throws IllegalArgumentException 
     *    <ul>
     *    <li>
     *    if <code>expected</code> is <code>NaN</code>. 
     *    <li>
     *    if <code>absdev</code> is negative or <code>NaN</code>. 
     *    </ul>
     * @see #assertAbsEquals(double,double,double)
     * @see #testAbsEquals(double,double,double)
     */
    public static void assertAbsEquals(String message,
				       double expected,
				       double actual,
				       double absdev) {
	if (!testAbsEquals(expected, actual,absdev)) {
	    fail(message);
	}
    }

   /**
     * Fails if the absolute deviation 
     * between <code>expected</code> and <code>actual</code> 
     * exceeds <code>absdiv</code> in absolute value. 
     *
     * @param expected 
     *    the <code>double</code> value expected. 
     *    This may not be neither <code>NaN</code>. 
     * @param actual 
     *    the actual <code>double</code> value. 
     * @param absdev 
     *    the maximum absolute deviation 
     *    between <code>expected</code> and <code>actual</code>. 
     *    This must be a non-negative value; 
     *    in particular, <code>NaN</code> is not allowed. 
     * @throws IllegalArgumentException 
     *    <ul>
     *    <li>
     *    if <code>expected</code> is <code>NaN</code>. 
     *    <li>
     *    if <code>absdev</code> is negative or <code>NaN</code>. 
     *    </ul>
     * @see #assertAbsEquals(String,double,double,double)
     */
    public static void assertAbsEquals(double expected,
				       double actual,
				       double absdev) {
	assertAbsEquals(expectedActual(expected,actual) + 
			"; absolute deviation <" + 
			((expected-actual)) + 
			"> exceeds <" + absdev + "> in absolute value. ",
			expected,actual,absdev);
    }


    /**
     * Special case of <code>assertEquals(Object,Object)</code> 
     * which provides an error message specifying the common prefix 
     * of <code>expected</code> with <code>actual</code>. 
     * This is suitable for keeping long strings under control. 
     * If one of the arguments is <code>null</code>, 
     * this method behaves like 
     * {@link junit.framework.Assert#assertEquals(Object,Object)}. 
     *
     * @param expected 
     *    the <code>String</code> expected. 
     *    This ma
     * @param actual 
     *    a <code>String</code> value
     */
    public static void assertStringEquals(String expected,String actual) {
	if (expected == null || actual == null) {
	    assertEquals(expected,actual);
	}
	// Here, neither expected nor actual is null. 

	int minLen = Math.min(expected.length(),actual.length());
	for (int i = 0; i < minLen; i++) {
	    if (expected.charAt(i) != actual.charAt(i)) {
		throw new AssertionFailedError
		    (expectedActual(expected,actual) + ". " + 
		     "Common prefix is <" + expected.substring(0,i) + STR_ASTOP);
	    }
	} // end of for ()

	if (expected.length() < actual.length()) {
	    throw new AssertionFailedError
		("Expected: <" + expected + 
		 "> but was prolongation <" + actual + STR_ASTOP);
	}

	if (expected.length() > actual.length()) {
	    throw new AssertionFailedError
		("Expected: <" + expected + 
		 "> but was prefix <" + actual + STR_ASTOP);
	}
    }


    public static double test() {
	return Double.NaN;
    }

    // public static void main(String[] args) throws Exception {
    // 	Double res = (Double)Assert.class.getMethod("test").invoke(null);
    // }
}

// Assert.java:63: warning: [deprecation] Assert in junit.framework has been deprecated
// public abstract class Assert<E> extends junit.framework.Assert {
//                                                        ^
// 1 warning

// Compilation finished at Tue Jun 21 20:36:15
