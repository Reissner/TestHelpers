package eu.simuline.testhelpers;

import eu.simuline.util.JavaPath;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;

import java.util.concurrent.RejectedExecutionException;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.URL;


/**
 * A custom class loader which enables the reloading
 * of classes for each test run. 
 * The class loader can be configured with a list of package paths 
 * that should be excluded from loading. 
 * The loading of these packages is delegated to the system class loader. 
 * They will be shared across test runs. 
 * <p>
 * The list of excluded package paths 
 * is specified in a properties file "excluded.properties" 
 * that is located in the same place as the TestCaseClassLoader class.
 * <p>
 * <b>Known limitation:</b> 
 * the TestCaseClassLoader cannot load classes from jar files. 
 */
public class TestCaseClassLoader extends ClassLoader {

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    // **** better done with instrumentation. 
    static boolean stop;// NOPMD


    /** name of excluded properties file */
    static final String EXCLUDED_FILE = "excluded.properties";

    /** scanned class path */
    private JavaPath jPath;

    /**
     * excluded paths 
     * This is initialized by {@link #readExcludedPackages} 
     * and used by {@link #isExcluded}. 
     */
    private List<String> excluded;
	 
    /** 
     * Default excluded paths. 
     * @see #isExcluded
     */
    private final String[] defaultExclusions = {
	"junit.",
	"org.",
	"java.",
	"javax.",
	"com.",
	"sun."
    };

    /* -------------------------------------------------------------------- *
     * constructor.                                                         *
     * -------------------------------------------------------------------- */

    /**
     * Constructs a TestCaseLoader. 
     * It scans the class path and the excluded package paths
     */
    public TestCaseClassLoader() {
	this(System.getProperty("java.class.path"));
    }
	
    /**
     * Constructs a TestCaseLoader. 
     * It scans the class path and the excluded package paths
     */
    private TestCaseClassLoader(String classPath) {
	this.jPath = new JavaPath(classPath);
	readExcludedPackages();
	this.stop = false;
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public URL getResource(String name) {
	return ClassLoader.getSystemResource(name);
    }

    public InputStream getResourceAsStream(String name) {
	return ClassLoader.getSystemResourceAsStream(name);
    } 

    /**
     * Returns whether the name with the given name 
     * is excluded from being loaded. 
     * 
     *
     * @param name 
     *    the fully qualified name of a class as a <code>String</code>. 
     * @return 
     *    a <code>boolean</code> which shows whether <code>name</code> 
     *    starts with one of the prefixes given by {@link #excluded}. 
     *    In this case the corresponding class is not loaded. 
     */
    public boolean isExcluded(String name) {
	for (int i= 0; i < this.excluded.size(); i++) {
	    if (name.startsWith(this.excluded.get(i))) {
		return true;
	    }
	}
	return false;	
    }

    Class<?> defineClass(String name)
	throws ClassNotFoundException {
	byte[] data = lookupClassData(name);
	return defineClass(name, data, 0, data.length);
    }

    void pleaseBreak() {
	this.stop = true;
    }
	
    public synchronized Class loadClass(String name, boolean resolve)
	throws ClassNotFoundException {
	if (this.stop) {
	    throw new RejectedExecutionException("User interrupt");// **** 
	}

	Class cls = findLoadedClass(name);
	if (cls != null) {
	    return cls;
	}
	//
	// Delegate the loading of excluded classes to the 
	// standard class loader.
	//
	if (isExcluded(name)) {
	    try {
		cls = findSystemClass(name);
		return cls;
	    } catch (ClassNotFoundException e) {// NOPMD 
		// keep searching **** although excluded. 
	    }
	}
	byte[] data= lookupClassData(name);
	if (data == null) {
	    throw new ClassNotFoundException();
	}
	cls = defineClass(name, data, 0, data.length);
//System.out.println("loaded: "+name);
	if (resolve) {
	    resolveClass(cls);
	}
	return cls;
    }

    private byte[] lookupClassData(String className) 
	throws ClassNotFoundException {
	try {
	    InputStream stream = this.jPath.getInputStream(className);
	    if (stream == null) {
		throw new ClassNotFoundException();
	    }

	    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
	    byte[] data = new byte[1000];
	    int numRead;
	    while ((numRead = stream.read(data)) != -1) {
		out.write(data, 0, numRead);
	    }
	    stream.close();
	    out.close();
	    return out.toByteArray();
	} catch (IOException e) {
	    throw new ClassNotFoundException();// NOPMD
	}
    }
	
    /**
     * Initializes {@link #excluded} using {@link #defaultExclusions} 
     * and {@link #EXCLUDED_FILE}. 
     * <ol>
     * <li>
     * First the entries of {@link #defaultExclusions} 
     * are added to {@link #excluded}. 
     * <li>
     * Then {@link #EXCLUDED_FILE} is interpreted as filename 
     * and the file is interpreted as Properties File with property keys 
     * starting with <code>excluded.</code>; 
     * all the other properties are ignored. 
     * The values are trimmed 
     * and a trailing <code>*</code> is removed if present. 
     * If the remaining path is nontrivial, it is added to {@link #excluded}. 
     * </ol>
     */
    private void readExcludedPackages() {		
	this.excluded = new ArrayList<String>(10);
	for (int i= 0; i < this.defaultExclusions.length; i++) {
	    this.excluded.add(defaultExclusions[i]);
	}
	InputStream inStream = getClass().getResourceAsStream(EXCLUDED_FILE);
	if (inStream == null) {
	    return;
	}
	Properties prop = new Properties();
	try {
	    prop.load(inStream);
	} catch (IOException e) {
	    return;
	} finally {
	    try {
		inStream.close();
	    } catch (IOException e) {// NOPMD 
		// already closed *****?
	    }
	}
	for (Enumeration e = prop.propertyNames(); e.hasMoreElements(); ) {
	    String key = (String)e.nextElement();
	    if (key.startsWith("excluded.")) {
		String path = prop.getProperty(key);
		path = path.trim();
		if (path.endsWith("*")) {
		    path = path.substring(0, path.length()-1);
		}
		if (path.length() > 0) {
		    this.excluded.add(path);
		}
	    }
	}
    }
}
