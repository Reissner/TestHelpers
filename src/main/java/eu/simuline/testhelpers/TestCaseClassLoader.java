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
 * The class loader can be configured with a list of package paths 
 * that should be excluded from loading. 
 * The loading of these packages is delegated to the system class loader. 
 * They will be shared across test runs. 
 * <p>
 * The list of excluded package paths 
 * is either hardcoded in {@link #defaultExclusions}, 
 * specified as property with key {@link #PROP_KEY_NO_CLS_RELOAD}. 
 * in a properties file {@link #EXCLUDED_FILE} 
 * that is located in the same place as the TestCaseClassLoader class.
 * <p>
 * <b>Known limitation:</b> 
 * <ul>
 * <li>
 * the TestCaseClassLoader cannot load classes from jar files. 
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
     * and used by {@link #isExcluded}. 
     */
    private List<String> excluded;

    /** 
     * Default excluded paths. 
     * @see #isExcluded
     */
    private final String[] defaultExclusions =
            {"junit.", "org.", "java.", "javax.", "com.", "sun."};

    /* -------------------------------------------------------------------- *
     * constructor. *
     * -------------------------------------------------------------------- */

    /**
     * Constructs a TestCaseLoader. 
     * It scans the class path and the excluded package paths. 
     */
    public TestCaseClassLoader() {
        this(System.getProperty("java.class.path"));
    }

    /**
     * Constructs a TestCaseLoader. 
     * It scans the class path and the excluded package paths. 
     *
     * @param classPath
     *    the classpath. 
     */
    private TestCaseClassLoader(String classPath) {
        this.jPath = new JavaPath(classPath);
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
    private boolean isExcluded(String name) {
        for (int i = 0; i < this.excluded.size(); i++) {
            if (name.startsWith(this.excluded.get(i))) {
                return true;
            }
        }
        return false;
    }

    Class<?> defineClass(String name) throws ClassNotFoundException {
        byte[] data = lookupClassData(name);
        return defineClass(name, data, 0, data.length);
    }

    public synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        Class<?> cls = findLoadedClass(name);
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
            } catch (ClassNotFoundException e) { // NOPMD 
                System.out.println("keep searching **** although excluded. ");
                // keep searching **** although excluded. 
            }
        }
        byte[] data = lookupClassData(name);
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

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream(LEN_CLS_STREAM);
            byte[] data = new byte[LEN_CLS_STREAM];
            int numRead;
            while ((numRead = stream.read(data)) != -1) {
                out.write(data, 0, numRead);
            }
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(); // NOPMD
        }
    }

    /**
     * Initializes {@link #excluded} using {@link #defaultExclusions}, 
     * {@link #PROP_KEY_NO_CLS_RELOAD} and {@link #EXCLUDED_FILE}. 
     * <ol>
     * <li>
     * First the entries of {@link #defaultExclusions} 
     * are added to {@link #excluded}. 
     * <li>
     * Then {@link #PROP_KEY_NO_CLS_RELOAD} is interpreted 
     * as a ":"-seprated list of entries 
     * each of which is added to {@link #excluded}. 
     * <li>
     * Finally, {@link #EXCLUDED_FILE} is interpreted as filename 
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
        for (int i = 0; i < this.defaultExclusions.length; i++) {
            this.excluded.add(defaultExclusions[i]);
        }
        Properties prop;

        String excludesPathProp = System.getProperty(PROP_KEY_NO_CLS_RELOAD);
        if (excludesPathProp != null) {
            String[] excludesProp = excludesPathProp.split(":");
            for (int i = 0; i < excludesProp.length; i++) {
                this.excluded.add(excludesProp[i]);
            }
        }

        InputStream inStream = getClass().getResourceAsStream(EXCLUDED_FILE);
        System.out.println("URL" + getClass().getResource(EXCLUDED_FILE));
        if (inStream == null) {
            return;
        }
        //assert false;
        prop = new Properties();
        try {
            prop.load(inStream);
        } catch (IOException e) {
            return;
        } finally {
            try {
                inStream.close();
            } catch (IOException e) { // NOPMD 
                // already closed *****?
            }
        }

        for (Enumeration<?> e = prop.propertyNames(); e.hasMoreElements();) {
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
    } // readExcludedPackages() 
}
