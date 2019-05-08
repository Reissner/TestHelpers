/**
 * Classes needed for testing. 
 * Essentially, these are one of the following: 
 * <ul>
 * <li>
 * Supporting additional assertions and white box tests 
 * ({@link Accessor} and {@link Assert}))
 * <li>
 * defining user interfaces for test-software: 
 * A single class is run with a GUI using 
 * {@link Actions#runTestClass(String)} 
 * or {@link Actions#runFromMain()}. 
 * Both use a GUI implemented by {@link GUIRunner}. 
 * More general, {@link Actions} defines the actions 
 * of a user of the test software. 
 * To run a single test class or a package of testclasses 
 * (also all simuline-classes <code>eu.simuline</code>) 
 * using {@link Actions#runTestClass(String)} 
 * is implemented in {@link JUnitSingleTester}. 
 * <li>
 * The other classes are auxiliary classes for the user interface: 
 * to run single test classes or packages of testclasses. 
 * {@link ExtRunListener} and implementations 
 * {@link GUIRunListener}, {@link SeqRunListener} and {@link TextRunListener} 
 * react on test events
 * Auxiliary classes {@link TestCase} and therein {@link Quality} 
 * represent test cases in their suite and test runs with their their states. 
 * A special role plays {@link TestCaseClassLoader} 
 * which enables test runners to to unload classes 
 * just by dropping the loader. 
 * </ul>
 *
 * <code>x</code> ***** NOT YET COMPLETE. 
 */
package eu.simuline.testhelpers;
