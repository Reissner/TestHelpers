
package eu.simuline.testhelpers;

import eu.simuline.util.BasicTypesCompatibilityChecker;

import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Provides access even to private fields, methods, constructors 
 * and inner classes, static or not, via reflection. 
 * This is necessary for white-box-tests. 
 * Note that class objects for instances <code>i</code> 
 * are given by <code>i.getClass()</code> 
 *(except for instances of primitive types) 
 * but also if no instance is available, 
 * the class object is given by 
 * <ul>
 * <li>
 * <code>&lt;classname>.class</code> for all types even the primitive ones 
 * and also for arrays 
 * (<code>int.class</code> and <code>int[].class</code> are valid) 
 * but not for hidden inner classes, e.g. <code>private</code> ones, 
 * <li>
 * <code>Boolean.TYPE</code>, 
 * <code>Character.TYPE</code> and so on for the primitive types 
 * (which is superfluous because <code>boolean.class</code> 
 * works as well as <code>Boolean.TYPE</code>, 
 * except for <code>java.lang.Void.TYPE</code> which I never used so far), 
 * <li>
 * methods {@link #getInnerClass(Class,String)} and 
 * {@link #getInnerClass(Class,String[])} for hidden inner classes. 
 * Note that formally this even works for anonymous classes 
 * but since names of anonymous classes may change 
 * if another anonymous inner class is inserted, 
 * this feature should be used with caution. 
 * I personally feel it is best not to use it at all. 
 * </ul>
 * Avoid using <code>Class.forName(String className)</code>. 
 * <p>
 * The method {@link #getField(Class,String)} 
 * returns the value of the specified static field. 
 * Note that if the field has primitive type, 
 * the wrapper of its content is returned. 
 * Correspondingly, {@link #getField(Object,String)} 
 * returns the specified instant field. 
 * Note that since the class <code>Class</code> is final, 
 * and has no (non-public) fields, there should be no reason, 
 * to access a <code>Class</code>-object. 
 * In case of a very special application, 
 * casts like <code>getField((Object)ls,name)</code> 
 * should resolve the ambiguity. 
 * The two methods described above, should cover the typical situations. 
 * In some special cases however, 
 * including overwritten fields and fields of inner classes, 
 * {@link #getField(Class,Object,String)} 
 * may provide the only way to access a field. 
 * <p>
 * What can be said about the methods <code>getField</code> 
 * applies correspondingly to methods <code>setField</code>. 
 * Note that there is no way to change fields 
 * which are declared <code>final</code>. 
 * To be more precise, the pointer in a final field 
 * <p>
 * Similarly for invoking methods: 
 * Except for very special cases, 
 * {@link #invokeStatic(Class,String,Object...)} for static methods and 
 * {@link #invoke(Object,String,Object...)} for instance methods 
 * will be sufficient, but for some special cases 
 * including overwriting and inner classes 
 * {@link #invoke(Class,Object,String,Object...)} 
 * may be the only way to invoke a method. 
 * Note that these methods apply to parameters with primitive types as well 
 * by passing the appropriate wrapper object: 
 * For example <code>Integer.toString(int i)</code> may be invoked by 
 * <code>invoke(Integer.class,"toString",new Integer(i))</code>. 
 * <p>
 * Still there is a problem: 
 * if wrapping parameters makes signatures ambiguous, 
 * the parameter types must be specified explicitly 
 * using method {@link #invoke(Class,Object,String,Class[],Object[])}. 
 * For this case, which seems to be quite rare, 
 * there are no convenience methods. 
 * <p>
 * Note that the return type is always <code>Object</code>. 
 * Of course, the return value may be casted 
 * to the return type of the specified method, 
 * provided the return type is an object and 
 * neither void nor a primitive type. 
 * In the latter case, the return value is wrapped in the corresponding 
 * wrapper class unlike the parameters. 
 * <p>
 * For the last case, creating objects using constructors, 
 * in most cases {@link #create(Class,Object[])} is sufficient; 
 * to avoid ambiguities, one has to specify the types of the parameters 
 * and use {@link #create(Class,Class[],Object[])} instead. 
 *
 * @author <a href="mailto:">Ernst Reissner</a>
 * @version 1.0
 */
public final class Accessor<T> {

    /* -------------------------------------------------------------------- *
     * constants.                                                           *
     * -------------------------------------------------------------------- */

    /**
     * The separator between a class and its enclosing class 
     * for inner classes. 
     */
    private final static String INNER_SEPARATOR = "$";

    /**
     * String denoting an unspecified class. 
     * This is needed for producing output only. 
     *
     * @see #paramsToString
     */
    private final static String UNSPECIFIED_CLASS = "<unspecified class>";

    // several string literals occurring more than once. 
    private final static String STR_DNE = " does not exist. ";
    private final static String STR_IN_CLS = "\" in class \"";
    private final static String STR_SPEC_NULL_CLS = "Specified null-class. ";

    /* -------------------------------------------------------------------- *
     * private constructor.                                                 *
     * -------------------------------------------------------------------- */

    /**
     * Formally to create a new <code>Accessor</code> instance 
     * but intended in contrary 
     * to prevent an <code>Accessor</code> from being instantiated. 
     * Note that the modifiers <code>final</code> and <code>abstract</code> 
     * are mutually exclusive 
     * and so this trick is the only remaining possibility. 
     */
    private Accessor() {
    }

    /* -------------------------------------------------------------------- *
     * private methods.                                                     *
     * -------------------------------------------------------------------- */

    /**
     * Converts a list of classes into their string-representation. 
     *
     * @param paramCls 
     *    an array of <code>Class</code>es. 
     *    The entries in the array may well be <code>null</code>. 
     * @return 
     *    a comma separated sequence of the classes given. 
     */
    private static String paramsToString(Class... paramCls) {
	StringBuffer ret = new StringBuffer();
	String clsString;
	ret.append("(");
	if (paramCls.length != 0) {
	    clsString = paramCls[0] == null 
		?  UNSPECIFIED_CLASS
		: paramCls[0].getName(); 
	    ret.append(clsString);
	    for (int i = 1; i < paramCls.length; i++) {
		ret.append(", ");
		clsString = paramCls[i] == null 
		    ?  UNSPECIFIED_CLASS
		    : paramCls[i].getName(); 
		ret.append(clsString);
	    }
	}
	ret.append(")");
	return ret.toString();
    }

    /**
     * Invokes the specified method with the given parameters 
     * and returns the value (which may be void of course. ) 
     *
     * @param method 
     *    a <code>Method</code>. 
     * @param target 
     *    the target to which the specified method is to be applied. 
     *    the target <em>must</em> be <code>null</code>
     *    if and only if the method specified is static. 
     * @param parameters 
     *    the list of parameters used when invoking <code>method</code>. 
     *    Note that parameters of elementary types 
     *    have to be wrapped in an object 
     *    (e.g. write <code>new Integer(8)</code> 
     *    instead of just <code>8</code>). 
     * @return 
     *    The result of invoking the specified method 
     *    with the given parameters. 
     *    If the method has no return value, <code>null</code> is returned. 
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    if the specified method is static but <code>target != null</code>. 
     *    <li>
     *    if the specified method is a member method 
     *    but <code>target == null</code>. 
     *    </ul>
     * @throws IllegalStateException 
     *    with message "Method should be accessible; still is not. " 
     *    if the method is not accessible. 
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the method invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     */
    private static Object invoke(Method method, 
				 Object target,
				 Object... parameters) 
	throws InvocationTargetException {

	if (Modifier.isStatic(method.getModifiers()) != 
	    (target == null)) {
	    if (Modifier.isStatic(method.getModifiers())) {
		throw new IllegalArgumentException
		    ("For static method " + method.getName() + 
		     " no target has to be provided (i.e. null). ");
	    } else {
		throw new IllegalArgumentException
		    ("For member method " + method.getName() + 
		     " a target has to be provided (not null). ");
	    }
	}

	try {
	    return method.invoke(target,parameters);
	} catch(IllegalAccessException ie) {
	    throw new IllegalStateException
		("Method should be accessible; still is not. "); 
	}
    }

    /**
     * Returns the classes of the given parameters. 
     *
     * @param parameters 
     *    a parameter list represented as an array of <code>Object</code>s. 
     * @return 
     *    the classes of the given parameters. 
     *    For a <code>null</code>-parameter, 
     *    the <code>null</code>-class is returned. 
     */
    private static Class[] getParamCls(Object... parameters) {
	Class[] paramCls = new Class[parameters.length];
	for (int i = 0; i < parameters.length; i++) {
	    paramCls[i] = parameters[i] == null 
		? null 
		: parameters[i].getClass();
	}
	return paramCls;
    }

    /*----------------------------------------------------------------------*
     * methods for public access.                                           *
     *----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*
     * getField methods                                                     *
     *----------------------------------------------------------------------*/

    /**
     * Returns the value of the specified member field resp. its wrapper. 
     *
     * @param target 
     *    the instance for which a field is to be accessed. 
     * @param fieldName 
     *     the name of the field. 
     * @return 
     *    If the type of the specified field is <code>Object</code>, 
     *    this object is returned directly; 
     *    otherwise the value of the field is wrapped 
     *    in an object of the appropriate class. 
     *    E.g. <code>new Integer(5)</code> is returned 
     *    instead of the <code>int</code>-value <code>5</code>. 
     * @throws NoSuchFieldException
     *    if the specified object 
     *    does not contain a field with the given name, 
     *    e.g. because <code>fieldName == null</code>. 
     * @throws IllegalArgumentException
     *    if the target is <code>null</code> 
     *    or if the specified field is static. 
     * @see #setField(Object,String,Object)
     */
    public static Object getField(Object target,
				  String fieldName) 
	throws NoSuchFieldException {

	if (target == null) {
	    throw new IllegalArgumentException
		("Specified null-target. ");
	}

	return getField(target.getClass(),target,fieldName);
    }

    /**
     * Returns the value of the specified static field resp. its wrapper. 
     *
     * @param aClass 
     *    the class for which a static field is to be accessed. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> to determine the class-object. 
     * @param fieldName 
     *     the name of the field. 
     * @return 
     *    If the value of the specified field is an Object, 
     *    this object is returned directly; 
     *    otherwise the value of the field is wrapped 
     *    in an object of the appropriate class. 
     *    E.g. <code>new Integer(5)</code> is returned 
     *    instead of the <code>int</code>-value <code>5</code>. 
     * @throws NoSuchFieldException
     *    if the specified class 
     *    does not contain a field with the given name, 
     *    e.g. because <code>fieldName == null</code>. 
     * @throws IllegalArgumentException
     *    if the class-parameter is <code>null</code> 
     *    or if the specified field is not static. 
     * @see #setField(Class,String,Object)
     */
    public static Object getField(Class aClass,
				  String fieldName) 
	throws NoSuchFieldException {

	return getField(aClass,null,fieldName);
    }

    /**
     * Returns the specified <code>Field</code> object if possible. 
     * This method is commonly used by the methods 
     * named <code>getField</code> and <code>setField</code>. 
     *
     * @param aClass 
     *    a <code>Class</code> object. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param fieldName 
     *    the name of a field to look for in the specified class 
     *    and its superclasses. 
     * @param shouldBeStatic
     *    whether the specified field should static. 
     * @return 
     *    the specified <code>Field</code> object 
     *    (Should never return <code>null</code>) 
     *    made accessible if it exists. 
     * @throws IllegalArgumentException
     *    if the "<code>null</code>-class" is specified. 
     * @throws NoSuchFieldException
     *    if the specified class and none of its superclasses
     *    containd a field with the given name, 
     *    e.g. because <code>fieldName == null</code>. 
     */
    private static Field getFieldObj(Class aClass,
				     String fieldName,
				     boolean shouldBeStatic) 
	throws NoSuchFieldException {

	if (aClass == null) {
	    throw new IllegalArgumentException(STR_SPEC_NULL_CLS);
	}

	Field[] cands;
	Class candClass = aClass;

	do {
	    // look for the specified field in candClass. 
	    cands = candClass.getDeclaredFields();
	    for (int i = 0; i < cands.length; i++) {
		if (cands[i].getName().equals(fieldName)) {
		    cands[i].setAccessible(true);
		    // Here, aField is not null 
		    // (if no field: Exception thrown). 

		    if (shouldBeStatic != 
			Modifier.isStatic(cands[i].getModifiers())) {
			throw new IllegalArgumentException
			    ("The specified field \"" + fieldName + 
			     "\" should " + 
			     (shouldBeStatic ? "" : "not ") + 
			     "be static. ");
		    }

		    return cands[i];
		}
	    }
	    // Here, no such field is found. 

	    // prepare search in superclass. 
	    candClass = candClass.getSuperclass();
	} while (candClass != null);
	// Here, the specified field is not found. 

	// throws a NoSuchFieldException
	aClass.getDeclaredField(fieldName);
	throw new IllegalStateException
	    ("Should throw a NoSuchFieldException. ");
    }

    /**
     * Returns the value of the specified static field 
     * or member field resp. its wrapper. 
     * Use {@link #getField(Class,String)} or 
     * {@link #getField(Object,String)} if possible. 
     *
     * @param aClass 
     *    Some class object. 
     *    Either <code>target == null</code> 
     *    or <code>target instanceof aClass</code>. 
     * @param target 
     *    the object for which a field is to be accessed. 
     *    For static fields, this <em>must</em> be <code>null</code>; 
     *    whereas for memeber fields 
     *    this has to be the corresponding instance. 
     * @param fieldName 
     *     the name of the field. 
     * @return 
     *    If the value of the specified field is an Object, 
     *    this object is returned directly; 
     *    otherwise the value of the field is wrapped 
     *    in an object of the appropriate class. 
     *    E.g. <code>new Integer(5)</code> is returned 
     *    instead of the <code>int</code>-value <code>5</code>. 
     * @throws NoSuchFieldException
     *    if the specified class 
     *    does not contain a field with the given name, 
     *    e.g. because <code>fieldName == null</code>. 
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    if the <code>null</code>-class is specified 
     *    <li>
     *    if the target is <code>null</code> 
     *    whereas the specified field is a member field. 
     *    <li>
     *    if the target is not <code>null</code> 
     *    whereas the specified field is static. 
     *    </ul>
     * @see #setField(Class,Object,String,Object)
     */
    public static Object getField(Class aClass,
				  Object target,
				  String fieldName) 
	throws NoSuchFieldException {

	Field aField = getFieldObj(aClass,fieldName,target == null);

	try {
	    return aField.get(target);
	} catch (IllegalAccessException e) {
	    throw new IllegalStateException
		("Field \"" + fieldName + STR_IN_CLS + 
		 (aClass != null ? aClass : target.getClass()).getName() + 
		 "is not accessible although it should. ");
	}
    }

    /*----------------------------------------------------------------------*
     * setField methods                                                     *
     *----------------------------------------------------------------------*/

    /**
     * If the type of the specified field extends <code>Object</code>, 
     * invoking this method acts like <code>target.fieldName = value</code>; 
     * otherwise the argument <code>value</code> is unwrapped first. 
     * Note that there is no way to assign a value 
     * to a field which is declared <code>final</code>. 
     *
     * @param target 
     *    the object for which a field is to be accessed. 
     * @param fieldName 
     *     the name of the field. 
     * @param value
     *    If the type of the specified field extends <code>Object</code>, 
     *    the type of <code>value</code> must be a supertype; 
     *    otherwise it must be the corresponding wrapper. 
     *    E.g. the field declared by <code>int intField;</code> 
     *    is set by <code>setField(target,"intField",new Integer(5))</code> 
     *    instead of <code>setField(target,"intField",5)</code>. 
     * @throws NoSuchFieldException
     *    if the specified class 
     *    does not contain a field with the given name. 
     * @throws IllegalArgumentException
     *    if the target is <code>null</code> 
     *    or if the specified field is static 
     *    or if the specified field is declared <code>final</code>. 
     * @see #getField(Object,String)
     */
    public static void setField(Object target,
				String fieldName,
				Object value) 
	throws NoSuchFieldException {

	if (target == null) {
	    throw new IllegalArgumentException
		("Specified null-target. ");
	}
	setField(target.getClass(),target,fieldName,value);
    }

    /**
     * If the type of the specified field extends <code>Object</code>, 
     * invoking this method acts like <code>aClass.fieldName = value</code>; 
     * otherwise the argument <code>value</code> is unwrapped first. 
     * Note that there is no way to assign a value 
     * to a field which is declared <code>final</code>. 
     *
     * @param aClass 
     *    the class for which a static field is to be accessed. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param fieldName 
     *     the name of the field. 
     * @param value
     *    If the type of the specified field extends <code>Object</code>, 
     *    the type of <code>value</code> must be a supertype; 
     *    otherwise it must be the corresponding wrapper. 
     *    E.g. the field declared by <code>static int intField;</code> 
     *    is set by <code>setField(aClass,"intField",new Integer(5))</code> 
     *    instead of <code>setField(aClass,"intField",5)</code>. 
     * @throws NoSuchFieldException
     *    if the specified class 
     *    does not contain a field with the given name. 
     * @throws IllegalArgumentException
     *    if <code>aClass == null</code> 
     *    or if the specified field is not static 
     *    or if the specified field is declared <code>final</code>. 
     * @see #getField(Class,String)
     */
    public static void setField(Class aClass,
				String fieldName,
				Object value) 
	throws NoSuchFieldException {

	setField(aClass,null,fieldName,value);
    }

    /**
     * If the type of the specified field extends <code>Object</code>, 
     * invoking this method acts like <code>target.field = value</code>; 
     * otherwise the argument <code>value</code> is unwrapped first. 
     * Note that there is no way to assign a value 
     * to a field which is declared <code>final</code>. 
     *
     * @param aClass 
     *    Some class object. 
     *    Either <code>target == null</code> 
     *    or <code>target instanceof aClass</code>. 
     * @param target 
     *    the object for which a field is to be accessed. 
     * @param fieldName 
     *     the name of the field. 
     * @param value
     *    If the type of the specified field extends <code>Object</code>, 
     *    the type of <code>value</code> must be the same; 
     *    otherwise it must be the corresponding wrapper. 
     * @throws NoSuchFieldException
     *    if the specified class 
     *    does not contain a field with the given name. 
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    if the <code>null</code>-class is specified 
     *    <li>
     *    if the target is <code>null</code> 
     *    whereas the specified field is a member field. 
     *    <li>
     *    if the target is not <code>null</code> 
     *    whereas the specified field is static. 
     *    <li>
     *    if the specified field is declared <code>final</code>. 
     *    <li>
     *    if the specified field is primitive 
     *    but a <code>null</code>-value is tried to be assigned. 
     *    </ul>
     * @see #getField(Class,Object,String)
     */
    public static void setField(Class aClass,
				Object target,
				String fieldName,
				Object value) 
	throws NoSuchFieldException {

	Field aField = getFieldObj(aClass,fieldName,target == null);

	if (aField.getType().isPrimitive() && value == null) {
	    throw new IllegalArgumentException
		("Tried to assign null-value to field \"" + fieldName + 
		 STR_IN_CLS + 
		 (aClass != null ? aClass : target.getClass()).getName() + 
		 "\" although its type \"" + aField.getType() + 
		 "\" is primitive. ");
	}
	
	try {
	    aField.set(target,value);
	} catch (IllegalAccessException e) {
	    if (aClass == null) {
		aClass = target.getClass();
	    }
	    String clsName = aClass.getName();
	    if (Modifier.isFinal(aField.getModifiers())) {
		throw new IllegalArgumentException
		    ("Field \"" + fieldName + STR_IN_CLS + clsName + 
		     "\" is declared final and is hence not accessible. ");
	    }
	    throw new IllegalStateException
		("Field \"" + fieldName + STR_IN_CLS + clsName + 
		 "\" is not accessible although it should. ");
	}
    }


    /*----------------------------------------------------------------------*
     * invoke methods with implicitly specified parameter types             *
     *----------------------------------------------------------------------*/

    /**
     * For invoking static methods. 
     *
     * @param aClass 
     *    The class of the static method to be invoked. 
     *    The method is looked up recursively 
     *    in the superclasses of <code>aClass</code>. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param methodName 
     *    the short name of a static method. 
     *    Short means without package or class. 
     * @param parameters 
     *    the list of parameters used when invoking <code>methodName</code>. 
     *    Note that parameters of elementary types 
     *    have to be wrapped in an object 
     *    (e.g. write <code>new Integer(8)</code> 
     *    instead of just <code>8</code>). 
     * @return
     *    <ul>
     *    <li>
     *    If the return type of the specified method 
     *    is <code>Object</code> or a subclass, 
     *    the return value is returned directly; 
     *    <li>
     *    if it is a primitive type, it is wrapped 
     *    in an object of the appropriate class. 
     *    E.g. <code>new Integer(5)</code> is returned 
     *    instead of the <code>int</code>-value <code>5</code>. 
     *    <li>
     *    If the return type is void, 
     *    i.e. <code>java.lang.Void.TYPE</code>, 
     *    <code>null</code> is returned. 
     *    </ul>
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    if <code>aClass == null</code>. 
     *    <li>
     *    if the specified method does not exist or is not unique. 
     *    </ul>
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the method invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     * @see #invoke(Class,Object,String,Object...)
     */
    public static Object invokeStatic(Class aClass,
				      String methodName,
				      Object... parameters) 
	throws InvocationTargetException {

	return invoke(aClass,null,methodName,parameters);
    }

    /**
     * For invoking member methods. 
     *
     * @param target 
     *    the target on which the specified member method is to be applied. 
     * @param methodName 
     *    the short name of a static method. 
     *    Short means without package or class. 
     * @param parameters 
     *    the list of parameters used when invoking <code>methodName</code>. 
     *    Note that parameters of elementary types 
     *    have to be wrapped in an object 
     *    (e.g. write <code>new Integer(8)</code> 
     *    instead of just <code>8</code>). 
     * @return
     *    <ul>
     *    <li>
     *    If the return type of the specified method 
     *    is <code>Object</code> or a subclass, 
     *    the return value is returned directly; 
     *    <li>
     *    if it is a primitive type, it is wrapped 
     *    in an object of the appropriate class. 
     *    E.g. <code>new Integer(5)</code> is returned 
     *    instead of the <code>int</code>-value <code>5</code>. 
     *    <li>
     *    If the return type is void, 
     *    i.e. <code>java.lang.Void.TYPE<code>, 
     *    <code>null</code> is returned. 
     *    </ul>
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    if <code>target == null</code>. 
     *    <li>
     *    if the specified method does not exist or is not unique. 
     *    </ul>
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the method invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     * @see #invoke(Class,Object,String,Object...)
     */
    public static Object invoke(Object target,
				String methodName,
				Object... parameters) 
	throws InvocationTargetException {

	return invoke(target.getClass(),target,methodName,parameters);
    }

    /**
     * Invokes the specified method with the given parameters 
     * and returns the value (which may be void of course. ) 
     * <p>
     * CAUTION: This may cause an exception although the runtime system 
     * finds out which method is ment. 
     * This is the case if for example 
     * methods <code>exa(int)</code> and <code>exa(Integer)</code> 
     * are present: 
     * both are invoked with 
     * <code>invoke(cls,target,"exa",new Integer(0))}</code>. 
     * In this case 
     * use {@link #invoke(Class,Object,String,Class[],Object[])} instead. 
     * 
     * @param aClass 
     *    The class of the method to be invoked. 
     *    The method is looked up recursively 
     *    in the superclasses of <code>aClass</code>. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param target 
     *    the target to which the specified method is to be applied. 
     *    the target <em>must</em> be <code>null</code>
     *    if and only if the method specified is static. 
     * @param methodName 
     *    the short name of a method. 
     *    Short means without package or class. 
     * @param parameters 
     *    the list of parameters used when invoking <code>methodName</code>. 
     *    Note that parameters of elementary types 
     *    have to be wrapped in an object 
     *    (e.g. write <code>new Integer(8)</code> 
     *    instead of just <code>8</code>). 
     * @return
     *    <ul>
     *    <li>
     *    If the return type of the specified method 
     *    is <code>Object</code> or a subclass, 
     *    the return value is returned directly; 
     *    <li>
     *    if it is a primitive type, it is wrapped 
     *    in an object of the appropriate class. 
     *    E.g. <code>new Integer(5)</code> is returned 
     *    instead of the <code>int</code>-value <code>5</code>. 
     *    <li>
     *    If the return type is void, 
     *    i.e. <code>java.lang.Void.TYPE<code>, 
     *    <code>null</code> is returned. 
     *    </ul>
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the method invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     * @throws IllegalArgumentException
     *    <ul>
     *    <li>
     *    if <code>aClass == null</code>. 
     *    <li>
     *    if the specified method is static but <code>target != null</code>. 
     *    <li>
     *    if the specified method is a member method 
     *    but <code>target == null</code>. 
     *    <li>
     *    if the specified method does not exist or is not unique. 
     *    </ul>
     */
    public static Object invoke(Class aClass,
				Object target,
				String methodName,
				Object... parameters) 
	throws InvocationTargetException {
	
	if (aClass == null) {
	    throw new IllegalArgumentException(STR_SPEC_NULL_CLS);
	}

	Method[] cands;
	Class candClass = aClass;
	Method toBeInvoked;
 
	// Find out the methods matching the signature 
	// and collect them in "cands". 
	do {
	    cands = candClass.getDeclaredMethods();

	    toBeInvoked = getMethod(aClass,
				    methodName,
				    cands,
				    parameters);
	    
	    if (toBeInvoked != null) {
		return invoke(toBeInvoked,target,parameters);
	    }
	    // prepare search in superclass. 
	    candClass = candClass.getSuperclass();
	} while (candClass != null);
	// Here, the desired method is not found. 

	throw new IllegalArgumentException
	    ("Method " + aClass.getName() + "." + methodName + 
	     paramsToString(getParamCls(parameters)) + STR_DNE);
    }	

    /**
     * If more than one method with the same parameter types 
     * is declared in a class, 
     * and one of these methods has a return type 
     * that is more specific than any of the others, 
     * that method is returned; 
     * otherwise one of the methods is chosen arbitrarily. 
     * 
     * @param aClass 
     *    The class of the method to be invoked. 
     *    The method is looked up recursively 
     *    in the superclasses of <code>aClass</code>. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param target 
     *    the target to which the specified method is to be applied. 
     *    If the method specified is static, the target is ignored. 
     *    Convention: should be <code>null</code> in this case. 
     * @param methodName 
     *    the short name of a method. 
     *    Short means without package or class. 
     * @param paramCls 
     *    the types specifying the parameter list of the desired method. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to denote a class, even a primitive one. 
     *    For primitive types, the alternatives 
     *            java.lang.Boolean.TYPE 
     *            java.lang.Character.TYPE 
     *            java.lang.Byte.TYPE 
     *            java.lang.Short.TYPE 
     *            java.lang.Integer.TYPE 
     *            java.lang.Long.TYPE 
     *            java.lang.Float.TYPE 
     *            java.lang.Double.TYPE 
     *           (java.lang.Void.TYPE) 
     *    are available. 
     *    For hidden inner classes, e.g. <code>private</code> ones, 
     *    {@link #getInnerClass(Class,String[])} and 
     *    {@link #getInnerClass(Class,String)} 
     *    may be used. 
     *    If an object <code>i</code> of a desired class is present 
     *    and if the class of is <code>i</code> not primitive, 
     *    <code>i.getClass()</code> returns the desired class object as well. 
     * @param parameters 
     *    the list of parameters used when invoking <code>methodName</code>. 
     *    Note that parameters of elementary types 
     *    have to be wrapped in an object 
     *    (e.g. write <code>new Integer(8)</code> 
     *    instead of just <code>8</code>). 
     * @return
     *    <ul>
     *    <li>
     *    If the return type of the specified method 
     *    is <code>Object</code> or a subclass, 
     *    the return value is returned directly; 
     *    <li>
     *    if it is a primitive type, it is wrapped 
     *    in an object of the appropriate class. 
     *    E.g. <code>new Integer(5)</code> is returned 
     *    instead of the <code>int</code>-value <code>5</code>. 
     *    <li>
     *    If the return type is void, 
     *    i.e. <code>java.lang.Void.TYPE<code>, 
     *    <code>null</code> is returned. 
     *    </ul>
     * @throws IllegalArgumentException 
     *    if the specified method does not exist. 
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the method invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     * @see #invoke(Class,Object,String,Class[],Object[])
     */
    public static Object invoke(Class aClass,
				Object target,
				String methodName,
				Class[] paramCls,
				Object[] parameters) 
	throws InvocationTargetException {

	if (aClass == null) {
	    throw new IllegalArgumentException(STR_SPEC_NULL_CLS);
	}
	Method toBeInvoked = getToBeInvoked(aClass,methodName,paramCls);
	if (toBeInvoked != null) {
	    return invoke(toBeInvoked,target,parameters);
	}
	// Here, the desired method is not found. 

	throw new IllegalArgumentException
	    ("Method " + aClass.getName() + "." + methodName + 
	     paramsToString(getParamCls(parameters)) + STR_DNE);
    }

    /**
     * Returns the specified method if it exists; 
     * otherwise <code>null</code>. 
     * Note that it is searched in the superclasses as well. 
     *
     * @param aClass 
     *    the <code>Class</code> to start searching the method. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param methodName 
     *    the name of the method to be returned. 
     * @param paramCls 
     *    the types specifying the parameter list of the desired method. 
     * @return 
     *    the method with the specified name and parameter list. 
     *    The search is started in <code>candClass</code> 
     *    and descends recursively until a method is found. 
     *    If no method is found, <code>null</code> is returned. 
     */
    static Method getToBeInvoked(Class aClass,
				 String methodName,
				 Class<?>... paramCls) {

	Method toBeInvoked;
	for (Class<?> candClass = aClass; 
	     candClass != null;
	     candClass = candClass.getSuperclass()) {
	    try {
		toBeInvoked = candClass.getDeclaredMethod(methodName,paramCls);
	    } catch (NoSuchMethodException e) {
		// method is not found: look it up in subclass. 
		continue;
	    }
	    if (Modifier.isAbstract(toBeInvoked.getModifiers())) {
		return null;
	    }
	    toBeInvoked.setAccessible(true);
	    return toBeInvoked;
	}
	// Here, the desired method is not found. 

	return null;
    }
// cd /home/ernst/Software/src/eu/simuline/testhelpers/
// /usr/local/java/latest/bin/javac -classpath /home/ernst/Software/cls:/home/ernst/Software/jars/junit-4.1.jar:/home/ernst/SysAdmin/JUnitExt/junitext-0.1.2/build/lib/junit-ui-runners-3.8.2.jar:/usr/local/java/latest/lib/tools.jar:/home/ernst/Software/jars/profile.jar -sourcepath /home/ernst/Software/src -encoding ISO-8859-1 -g:lines,vars,source -d /home/ernst/Software/cls -deprecation -target 1.6 -source 1.6 -Xlint Accessor.java

// Accessor.java:955: warning: [unchecked] unchecked call to getDeclaredMethod(java.lang.String,java.lang.Class<?>...) as a member of the raw type java.lang.Class
// 		toBeInvoked = candClass.getDeclaredMethod(methodName,paramCls);
// 		                                         ^
// Accessor.java:1017: warning: [unchecked] unchecked cast
// found   : java.lang.reflect.Constructor<?>[]
// required: java.lang.reflect.Constructor<T>[]
// 			   (Constructor<T>[])aClass.getDeclaredConstructors(),
// 			                                                   ^
// 2 warnings


    /*----------------------------------------------------------------------*
     * create methods                                                       *
     *----------------------------------------------------------------------*/

    /**
     * Returns an object created by the specified constructor. 
     * Note that this method is not applicable to 
     * interfaces, primitive types, array classes, or void. 
     * In the context of this package, these restrictions are not severe. 
     *
     * @param aClass 
     *    the class of the instance to be created. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param parameters 
     *    the list of parameters of the specified constructor. 
     *    Note that parameters of elementary types 
     *    have to be wrapped in an object 
     *    (e.g. write <code>new Integer(8)</code> 
     *    instead of just <code>8</code>). 
     *    <p>
     *    Note also that for static inner classes, 
     *    formally the surrounding instance is prefixed as a parameter. 
     *    ****** i think this does not apply to methods, 
     *    but actually i did not try yet. 
     * @return 
     *    an <code>Object</code> created by the specified constructor. 
     * @throws InstantiationException 
     *    if the instantiation with the specified constructor failed. 
     * @throws IllegalArgumentException 
     *    if the specified constructor does not exist or is not unique. 
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the constructor invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     */
    public static <T> T create(Class<T> aClass, 
			       Object... parameters) 
	throws InstantiationException, InvocationTargetException {
	
	
	Constructor<T> toBeInvoked = 
	    getConstructor(aClass,
			   // **** the cast is a weakness in the jdk5 (reported)
			   // **** in jdk6 hardly any improvement (to be rep.)
			   (Constructor<T>[])aClass.getDeclaredConstructors(),
			   parameters);
	// toBeInvoked may also be the default constructor, 
	// although this is not EXPLICITLY declared, of course. 

	if (toBeInvoked == null) {
	    throw new IllegalArgumentException
		("Constructor " + aClass.getName() + 
		 paramsToString(getParamCls(parameters)) + 
		 STR_DNE);
	}
	return create(toBeInvoked,parameters);
    }

    /*
     * Returns an instance of the class given by <code>aClass</code> 
     * generated by the default constructor. 
     *
     * @param aClass 
     *    a <code>Class</code> object. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @return 
     *    an <code>Object</code> created by the default constructor 
     *    of the specified class. 
     * @exception InstantiationException 
     *    if an error occurs in the default constructor. 
     */
    /*
    private static Object createDefault(Class aClass) 
    throws InstantiationException {
	try {
	    return aClass.newInstance();
	} catch (IllegalAccessException e) {
	    throw new IllegalStateException
		("Found unexpected exception " + e + 
		 " while trying to invoke default constructor " + 
		 "which is not declared. ");
	}
    }
    */

    /**
     * Returns an object created by the specified constructor. 
     * Note that this method is not applicable to 
     * interfaces, primitive types, array classes, or void. 
     * In the context of this package, these restrictions are not severe. 
     *
     * @param aClass 
     *    the class of the instance to be created 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to determine the class-object. 
     * @param paramCls 
     *    the types specifying the parameter list of the desired method. 
     *    Typically one will use the expression 
     *    <code>&lt;classname>.class</code> 
     *    to denote a class, even a primitive one. 
     *    For primitive types, the alternatives 
     *            java.lang.Boolean.TYPE 
     *            java.lang.Character.TYPE 
     *            java.lang.Byte.TYPE 
     *            java.lang.Short.TYPE 
     *            java.lang.Integer.TYPE 
     *            java.lang.Long.TYPE 
     *            java.lang.Float.TYPE 
     *            java.lang.Double.TYPE 
     *           (java.lang.Void.TYPE) 
     *    are available. 
     *    For hidden inner classes, e.g. <code>private</code> ones, 
     *    {@link #getInnerClass(Class,String[])} and 
     *    {@link #getInnerClass(Class,String)} 
     *    may be used. 
     *    If an object <code>i</code> of a desired class is present 
     *    and if the class of is <code>i</code> not primitive, 
     *    <code>i.getClass()</code> returns the desired class object as well. 
     *    <p>
     *    Note also that for static inner classes, 
     *    formally the surrounding class is prefixed as a parameter. 
     *    ****** i think this does not apply to methods, 
     *    but actually i did not try yet. 
     * @param parameters 
     *    the list of parameters of the specified constructor. 
     *    Note that parameters of elementary types 
     *    have to be wrapped in an object 
     *    (e.g. write <code>new Integer(8)</code> 
     *    instead of just <code>8</code>). 
     *    <p>
     *    Note also that for static inner classes, 
     *    formally the surrounding instance is prefixed as a parameter. 
     *    ****** i think this does not apply to methods, 
     *    but actually i did not try yet. 
     * @return 
     *    an <code>Object</code> created by the specified constructor. 
     * @throws InstantiationException 
     *    if the instantiation with the specified constructor failed. 
     * @throws NoSuchMethodException 
     *    if the specified constructor does not exist. 
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the constructor invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     */
    public static <T> T create(Class<T> aClass,
			       Class[] paramCls,
			       Object ... parameters) 
	throws NoSuchMethodException, 
	InstantiationException, 
	InvocationTargetException {

	// includes the default constructor?!? -- not specified! 
	Constructor<T> toBeInvoked = aClass
	    .getDeclaredConstructor(paramCls);
	if (toBeInvoked == null) {
	    throw new IllegalArgumentException
		("Constructor " + aClass.getName() + 
		 paramsToString(paramCls) + STR_DNE);
	}
	toBeInvoked.setAccessible(true);
	return create(toBeInvoked,parameters);
    }

    /**
     * Invoke the specified constructor with the given arguments. 
     *
     * @param toBeInvoked 
     *    some <code>Constructor</code>. 
     * @param parameters
     *    parameters fitting the given <code>Constructor</code>. 
     * @throws InstantiationException 
     *    if the instantiation with the specified constructor failed. 
     * @throws InvocationTargetException 
     *   to wrap an exception thrown by the constructor invoked. 
     *   Unwrap it using {@link Throwable#getCause}. 
     */
    private static <T> T create(Constructor<T> toBeInvoked,
				Object... parameters) 
	throws InstantiationException, InvocationTargetException {

	try {
	    return toBeInvoked.newInstance(parameters);
	} catch(IllegalAccessException ie) {
	    throw new IllegalStateException
		("Constructor should be accessible; still is not. "); 
	}
    }

    /**
     * Returns whether the given method 
     * matches the name and the parameter list. 
     *
     * @param cand 
     *    either a <code>Constructor</code> or a <code>Method</code>. 
     * @param methodName  
     *    the required name of a <code>Method</code>. 
     *    If <code>cand</code> is a constructor, this field is ignored; 
     *    if it is a method the name of which does not match, 
     *    <code>null</code> is returned. 
     * @param parameters 
     *    the list of parameters. 
     * @return 
     *    <code>true</code> if and only if 
     *    all of the following conditions are satisfied: 
     *    <ul>
     *    <li>
     *    <code>cand</code> is either a non-abstract method 
     *    named <code>methodName</code>. 
     *    <li>
     *    the parameter list of <code>cand</code> 
     *    matches <code>parameters</code> 
     *    as specified for {@link #paramsMatch}. 
     *    </ul>
     * @throws IllegalStateException 
     *    if <code>cand</code> is neither a method nor a constructor. 
     */
    private static boolean methodMatches(Method cand,
					 String methodName,
					 Object... parameters) {
	// Check name. 
	if (!cand.getName().equals(methodName)) {
	    return false;
	}
	// Here, cand has the right name. 

	// Check not abstract. 
	if (Modifier.isAbstract(cand.getModifiers())) {
	    return false;
	}
	// Here, cand is not abstract. 

	// Here, paramTypes contains the parameter types of cand. 

	return paramsMatch(cand.getParameterTypes(),parameters);
    }

    /**
     * Returns whether the given constructor matches the parameter list. 
     *
     * @param cand 
     *    either a <code>Constructor</code> or a <code>Method</code>. 
     * @param parameters 
     *    the list of parameters. 
     * @return a 
     *    <code>true</code> if and only if 
     *    the parameter list of <code>cand</code> 
     *    matches <code>parameters</code> 
     *    as specified for {@link #paramsMatch}. 
     * @throws IllegalStateException 
     *    if <code>cand</code> is neither a method nor a constructor. 
     */
    private static boolean constructorMatches(Constructor cand,
					      Object... parameters) {
	return paramsMatch(cand.getParameterTypes(),parameters);
    }

    /**
     * Returns whether the given the parameter list matches the classes. 
     *
     * @param paramTypes 
     *    the list of classes of parameters. 
     * @param parameters 
     *    the list of parameters. 
     * @return a 
     *    <code>true</code> if and only if 
     *    the parameter list of <code>cand</code> 
     *    matches <code>parameters</code> 
     *    up to equivalence of primitive types and their wrappers. 
     *    This includes that the two arrays have the same length. 
     * @throws IllegalStateException 
     *    if <code>cand</code> is neither a method nor a constructor. 
     */
    private static boolean paramsMatch(Class[] paramTypes,
				       Object... parameters) {
	if (paramTypes.length != parameters.length) {
	    return false;
	}
	// Here, the name and the number of parameters match. 

	for (int j = 0; j < parameters.length; j++) {
	    if (!BasicTypesCompatibilityChecker
		.areCompatible(paramTypes[j],parameters[j])) {
		return false;
	    }
	}
	// Here, the complete signature matches 
	// (except for the return type). 
	return true;
    }

    /**
     * Returns the specified method 
     * if <code>cands</code> offers exactly one possibility. 
     *
     * @param aClass 
     *    the <code>Class</code> 
     *    in which to search a <code>Method</code>. 
     *    For error/exception messages only. 
     * @param methodName 
     *    the name of the desired <code>Method</code>. 
     * @param cands 
     *    an array of <code>Method</code>s 
     *    the return value of this method is choosen from. 
     * @param parameters 
     *    the list of parameters. 
     * @return 
     *    a <code>Method</code>; possibly <code>null</code> 
     *    if the specified <code>Method</code> 
     *    does not exist in the specified class. 
     * @throws IllegalArgumentException 
     *    if the specified constructor or method is not unique. 
     */
    private static Method getMethod(Class aClass,
				    String methodName,
				    Method[] cands,
				    Object... parameters) {

	// "null" means that no method or constructor has been found so far. 
	Method result = null;

	for (int i = 0; i < cands.length; i++) {
	    if (!methodMatches(cands[i],methodName,parameters)) {
		continue;
	    }

	    if (result != null) {
		// Found more than one method/constructor. 
		throw new IllegalArgumentException
		    ("Method " + aClass.getName() + "." + methodName + 
		     paramsToString(getParamCls(parameters)) + 
		     " is not unique: cannot distinguish " + result + 
		     " from " + cands[i] + ". ");
	    }
	    // cands[i] is the first method that matches. 
	    result = cands[i];
	} // for all cands 

	if (result != null) {
	    result.setAccessible(true);
	}
	return result;
    }

    /**
     * Returns the specified constructor 
     * if <code>cands</code> offers exactly one possibility. 
     *
     * @param aClass 
     *    the <code>Class</code> 
     *    in which to search a <code>Constructor</code>. 
     *    For error/exception messages only. 
     * @param cands 
     *    an array of Constructors. 
     * @param parameters 
     *    the list of parameters. 
     * @return 
     *    a <code>Constructor</code> or <code>null</code> 
     *    if the specified constructor 
     *    does not exist in the specified class. 
     * @throws IllegalArgumentException 
     *    if the specified constructor or method is not unique. 
     */
    private static <T> Constructor<T> getConstructor(Class<T> aClass,
						     Constructor<T>[] cands,
						     Object... parameters) {

	// "null" means that no method or constructor has been found so far. 
	Constructor<T> result = null;

	for (int i = 0; i < cands.length; i++) {
	    if (!constructorMatches(cands[i],parameters)) {
		continue;
	    }

	    if (result != null) {
		// Found more than one method/constructor. 
		throw new IllegalArgumentException
		    ("Constructor " + aClass.getName() + 
		     paramsToString(getParamCls(parameters)) + 
		     " is not unique: cannot distinguish " + result + 
		     " from " + cands[i] + ". ");
	    }
	    // cands[i] is the first constructor that matches. 
	    result = cands[i];
	} // for all cands 

	if (result != null) {
	    result.setAccessible(true);
	}
	return result;
    }

    /*----------------------------------------------------------------------*
     * getInnerClass methods                                                *
     *----------------------------------------------------------------------*/


    /**
     * Returns the inner class of <code>enclosingCls</code> 
     * with the specified name <code>innerClsName</code>. 
     * By inner classes we mean both static inner classes and member classes. 
     * Also inherited classes are included. 
     *
     * @param enclosingCls 
     *    a <code>Class</code> object which may also be an inner class, 
     *    static or not. 
     * @param pathToInner
     *    a short path of a class enclosed by <code>enclosingCls</code>. 
     *    If the name of the enclosing class is <tt>enclosing</tt> 
     *    then the name of the inner class has the form 
     *    <tt>enclosing$shortName</tt>. 
     *    Here, "$" is as specified in {@link #INNER_SEPARATOR}. 
     *    This remains also true for nested inner classes. 
     *    In this case, <tt>shortName</tt> itself has the form 
     *    <tt>cls1$...$clsN</tt>. 
     *    The the corresponding short path is 
     *    <code>new String[] {cls1,...,clsN}</code>. 
     *    For paths with length <code>1</code> 
     *    one may use {@link #getInnerClass(Class,String)} instead. 
     * @return 
     *    the <code>Class</code> object represented by the parameters. 
     * @throws IllegalArgumentException
     *    if either of the parameters is <code>null</code>. 
     * @throws NoSuchInnerClassException
     *    if the specified class does not exist. 
     * @see #getInnerClass(Class,String)
     */
    public static Class getInnerClass(Class enclosingCls,
				      String[] pathToInner) {
	Class result = enclosingCls;
	for (int i = 0; i < pathToInner.length; i++) {
	    result = getInnerClass(result,pathToInner[i]);
	}
	return result;
    }

    /**
     * Returns the inner class of <code>enclosingCls</code> 
     * with the specified name <code>innerClsName</code>. 
     * By inner classes we mean both static inner classes and member classes. 
     * Also inherited classes are included. 
     *
     * @param enclosingCls 
     *    a <code>Class</code> object which may also be an inner class, 
     *    static or not. 
     * @param innerClsName 
     *    a short name of a class enclosed by <code>enclosingCls</code>. 
     *    If the name of the enclosing class is <tt>enclosing</tt> 
     *    then the name of the inner class has the form 
     *    <tt>enclosing$shortName</tt>. 
     *    Here, "$" is as specified in {@link #INNER_SEPARATOR}. 
     *    This remains also true for nested inner classes. 
     *    In this case, <tt>shortName</tt> itself has the form 
     *    <tt>cls1$...$clsN</tt>. 
     *    <em>CAUTION</em> 
     *    In this case apply method {@link #getInnerClass(Class,String[])} 
     *    instead. 
     * @return 
     *    the <code>Class</code> object represented by the parameters. 
     * @throws IllegalArgumentException
     *    if either of the parameters is <code>null</code> or 
     *    if the specified class does not exist. 
     * @see #getInnerClass(Class,String[])
     */
    public static Class getInnerClass(Class enclosingCls,
				      String innerClsName) {

	if (enclosingCls == null) {
	    throw new IllegalArgumentException(STR_SPEC_NULL_CLS);
	}
	if (innerClsName == null) {
	    throw new IllegalArgumentException
		("Specified null-class-name. ");
	}

	Class[] cands;
	Class candCls = enclosingCls;
	String candClsName;

	do {
	    // look for the specified inner class in candClass. 

	    cands = candCls.getDeclaredClasses();
	    candClsName = candCls.getName()+INNER_SEPARATOR;

	    for (int i = 0; i < cands.length; i++) {
		if (cands[i].getName().equals(candClsName+innerClsName)) {
		    return cands[i];
		}
	    }
	    // prepare search in superclass. 
	    candCls = candCls.getSuperclass();
	} while (candCls != null);
	// Here, the specified inner class is not found. 

	throw new IllegalArgumentException
	    ("Class \"" + enclosingCls.getName() + 
	     "\" has no inner class named \"" + innerClsName + "\". ");
    }
}

