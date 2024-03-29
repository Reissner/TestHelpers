/*
 * Copyright (C) Simuline Inc, Ernst Rei3ner
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 */

package eu.simuline.testhelpers;

import eu.simuline.util.JavaPath;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.URL;


/**
 * A custom class loader which allows to reload classes for each test run. 
 * This is done for classes to be tested determined by {@link #PROP_KEY_TB_TESTED_CLSPATH} 
 * and by the classes performing the tests determined by {@link GUIRunner#CHOOSE_CLASSPATH}. 
 * Any class but these are loaded by delegating to the system class loader. 
 * Classes loaded by the system class loader will be shared across test runs. 
 * <p>
 * The class loader can be configured with a list of package paths 
 * that should be excluded from loading and delegated to the system class loader also. 
 * <p>
 * Excluded package paths 
 * can be specified as property with key {@link #PROP_KEY_NO_CLS_RELOAD} and
 * in a properties file {@link #EXCLUDED_FILE} 
 * that is located in the same place as the TestCaseClassLoader class.
 * <p>
 * <b>Known limitation:</b> 
 * <ul>
 * <li>
 * the TestCaseClassLoader cannot load classes from jar files. TBD: clarify whether this is true. 
 * <li>
 * service providers must be excluded from reloading. 
 * </ul>
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class TestCaseClassLoader extends ClassLoader {

    /* -------------------------------------------------------------------- *
     * class constants. *
     * -------------------------------------------------------------------- */

     /**
      * Key of a property containing the classes to be tested. 
      * This does not include the testclasses performing the tests. 
      * The latter are from {@link GUIRunner#CHOOSE_CLASSPATH}. 
      * Together, 
      * they point to the classes to be reloaded by this class loader. 
      */
    private static final String PROP_KEY_TB_TESTED_CLSPATH = 
        "tbTestedClasspath";

    /**
     * Key of system property 
     * containing a ":"-separated list of packages or classes 
     * to be excluded from reloading. 
     * Each is read into {@link #excluded}. 
     */
    private static final String PROP_KEY_NO_CLS_RELOAD = "noClsReload";

    /**
     * Name of excluded properties file. 
     * This shall be located in the same folder as this class loader. 
     * The property keys shall have the form <code>exlude.xxx</code> 
     * and are read in {@link #excluded}. 
     */
    static final String EXCLUDED_FILE = "noClsReload.properties";

    /**
     * The initial length of a stream to read class files from. 
     */
    private static final int LEN_CLS_STREAM = 1000;

    /* -------------------------------------------------------------------- *
     * fields. *
     * -------------------------------------------------------------------- */

    /**
     * The scanned class path. 
     */
    private JavaPath jPath;

    /**
     * Holds the excluded paths. 
     * This is initialized by {@link #readExcludedPackages} 
     * and used by {@link #isIncluded}. 
     */
    private List<String> excluded;

    /* -------------------------------------------------------------------- *
     * constructor. *
     * -------------------------------------------------------------------- */

    /**
     * Constructs a TestCaseLoader with the system class loader as its parent. 
     * It loads the classes given by {@link #PROP_KEY_TB_TESTED_CLSPATH} 
     * and in {@link GUIRunner#CHOOSE_CLASSPATH}, 
     * except if excluded explicitly (see {@link #readExcludedPackages()}); 
     * the rest is delegated to be loaded by the according system class loader. 
     * 
     * @throws IllegalArgumentException
     *    If the test runner is not invoked with the options 
     *    defining tested classes and testing classes. 
     */
    public TestCaseClassLoader() {
        String    clsPath = System.getProperty(PROP_KEY_TB_TESTED_CLSPATH);
        String tstClsPath = System.getProperty(GUIRunner.CHOOSE_CLASSPATH);
        if (clsPath == null || tstClsPath == null) {
            throw new IllegalArgumentException
            ("Classpaths for tested code '" + clsPath +
            " 'and for testing code '" + tstClsPath +
            "' are not both given. ");
        }
        this.jPath = new JavaPath(clsPath + ":" + tstClsPath);
        readExcludedPackages();
    }

    /* -------------------------------------------------------------------- *
     * methods. *
     * -------------------------------------------------------------------- */

    public URL getResource(String name) {
        return ClassLoader.getSystemResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    }

    /**
     * Returns whether the name with the given name is included; 
     * else class loading is delegated to the parent class loader 
     * which is the system class loader. 
     * 
     * @param name 
     *    the fully qualified name of a class as a <code>String</code>. 
     * @return 
     *    a <code>boolean</code> which shows whether <code>name</code> 
     *    starts with one of the prefixes given by {@link #excluded}. 
     *    In this case the corresponding class is not loaded. 
     */
    private boolean isIncluded(String name) {
        for (int i = 0; i < this.excluded.size(); i++) {
            if (name.startsWith(this.excluded.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads a class with the specified binary name <code>name</code> 
     * as usual for {@link ClassLoader#loadClass(String, boolean)} 
     * and resolve if specified so. 
     * Internal loading is reserved to classes 
     * which should be reloaded after modification. 
     * These are the classes to be tested and those performing the tests. 
     * Since all classes are loadable by the system class loader 
     * (because tests ares started from the main method of the testing classes), 
     * the loading strategy must be trying to load internally 
     * <em>before</em> delegating to the parent class loader 
     * which is the system class loader. 
     * <p>
     * While loading internally by accident is not so problematic, 
     * unintended delegation is quite error prone, 
     * because then silently the classes are not reloaded although expected. 
     * Thus an exception must be thrown for reloadable classes if internal loading fails 
     * due to an IO issue, preventing delegation which would maybe succeed. 
     * As a consequence, 
     * meaning of the exception is a bit special but conform with the spec. 
     * 
     * @param name
     *   The binary name of the class
     * @param resolve
     *   whether to resolve the class
     * @return
     *   the class with the given name, 
     *   internally loaded if to be reloaded each run; 
     *   else by the parent classloader. 
     * @throws ClassNotFoundException
     *   If either internal loading did not work because of some IO issues 
     *   or caused by the system class loader. 
     *   Note that if during searching the class an IO exception is thrown, 
     *   this results in a ClassNotFoundException 
     *   even if the system class loader would find that class. 
     *   It means that there is a risk that the class is not found at proper place. 
     *   That way it is avoided that a tested class or a testing class 
     *   is erroneously loaded by the system classloader 
     *   and is thus <em>silently</em> not reloaded 
     *   when modifying and recompiling that class. 
     *   This would be a dangerous situation. 
     *   <p>
     *   If in contrast the class to be loaded 
     *   is not found among the classes to be tested and testing, 
     *   although the paths are searched completely 
     *   without throwing an exception, 
     *   then this ensures that the class is not to be reloaded 
     *   and so delegating to the system classloader is not dangerous. 
     *   Still then an exception could be thrown 
     *   but then it comes from the system classloader. 
     */
    public synchronized Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> cls = findLoadedClass(name);
            if (cls == null) {
                // The system class loader is the parent 
                if (isIncluded(name)) {
                    // may throw ClassNotFoundException
                    cls = findClass(name);
                    if (cls == null) {
                        // may throw ClassNotFoundException
                        cls = findSystemClass(name);
                    }
                } else {
                   // may throw ClassNotFoundException
                   cls = findSystemClass(name);
                }
            }
            if (resolve) {
                resolveClass(cls);
            }
            return cls;
        } // end synchronized 
    }

    /**
     * Finds the given class if possible. 
     * 
     * @param name
     *   the name of the class. 
     * @return
     *   If the class is not at the locations given by this constructor, 
     *   returns <code>null</code>, else the class as returned by 
     *   {@link ClassLoader#defineClass(String, byte[], int, int)}. 
     * @throws ClassNotFoundException
     *   if while searching the class, a file is not readable. 
     *   This need not be a file containing the class actually searched for. 
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // may throw ClassNotFoundException
        byte[] clsData = loadClassData(name);
        if (clsData == null) {
            return null;
        }
        // may throw ClassFormatError, but unchecked 
        return defineClass(name, clsData, 0, clsData.length);
    }

    /**
     * Returns the class given by <code>className</code> as a byte array, 
     * if possible. 
     * If not found returns <code>null</code>; 
     * if not readable throws an exception. 
     * 
     * @param className
     *   The name of the class to be loaded. 
     * @return
     *   The class description as a byte array 
     *   or <code>null</code> if not found. 
     * @throws ClassNotFoundException
     *   if while searching the class, a file is not readable. 
     *   This need not be a file containing the class actually searched for. 
     */
    private byte[] loadClassData(String className)
            throws ClassNotFoundException {
        try (InputStream inStream = this.jPath.getInputStream(className);
            ByteArrayOutputStream outStream =
                    new ByteArrayOutputStream(LEN_CLS_STREAM)) {
            if (inStream == null) {
                return null;// NOPMD
            }
            byte[] data = new byte[LEN_CLS_STREAM];
            int numRead;
            while ((numRead = inStream.read(data)) != -1) {
                outStream.write(data, 0, numRead);
            }
            return outStream.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(// NOPMD
                    "Class file for '" + className + "' not readable. ", e);
        }
    }

    /**
     * Initializes {@link #excluded} using 
     * {@link #PROP_KEY_NO_CLS_RELOAD} and {@link #EXCLUDED_FILE}. 
     * <ol>
     * <li>
     * First {@link #PROP_KEY_NO_CLS_RELOAD} is interpreted 
     * as a ":"-seprated list of entries 
     * each of which is added to {@link #excluded}. 
     * <li>
     * Then, {@link #EXCLUDED_FILE} is interpreted as filename 
     * and the file is interpreted as Properties File with property keys 
     * starting with <code>excluded.</code>; 
     * all the other properties are ignored. 
     * The values are trimmed 
     * and a trailing <code>*</code> is removed if present. 
     * If the remaining path is nontrivial, it is added to {@link #excluded}. 
     * </ol>
     */
    @SuppressWarnings("PMD.NPathComplexity")
    private void readExcludedPackages() {
        this.excluded = new ArrayList<String>();

        String excludesPathProp = System.getProperty(PROP_KEY_NO_CLS_RELOAD);
        if (excludesPathProp != null) {
            String[] excludesProps = excludesPathProp.split(":");
            for (String excludeProp : excludesProps) {
                this.excluded.add(excludeProp);
            }
        }

        System.out.println("URL of excluded-file: "
            + getClass().getResource(EXCLUDED_FILE));
        Properties prop;
        try (InputStream inStream =
                getClass().getResourceAsStream(EXCLUDED_FILE)) {
            if (inStream == null) {
                return;
            }
            //assert false;
            prop = new Properties();
            prop.load(inStream);

            for (Enumeration<?> e = prop.propertyNames(); e
                    .hasMoreElements();) {
                String key = (String) e.nextElement();
                if (key.startsWith("excluded.")) {
                    String path = prop.getProperty(key);
                    path = path.trim();
                    if (path.endsWith("*")) {
                        path = path.substring(0, path.length() - 1);
                    }

                    if (path.length() > 0) {
                        this.excluded.add(path);
                    }
                } // if 
            } // for 
        } catch (IOException e) {
            throw new RuntimeException
                ("Could not read excludes from file. ", e);
        }
    } // readExcludedPackages() 
}
