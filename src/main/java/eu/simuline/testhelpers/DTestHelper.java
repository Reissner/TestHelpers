package eu.simuline.testhelpers;

import java.util.List;
import java.util.ArrayList;

/**
 * To create double values for tests 
 *
 *
 * Created: Tue Mar 20 01:05:54 2012
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
public abstract class DTestHelper {

    /**
     * Returns a random number with absolute value in <code>[0,1]</code>. 
     *
     * @param signed 
     *    whether the number returned may be negative. 
     * @return 
     *    a random number 
     *    <ul>
     *    <li> in <code>[-1,1]</code> if <code> signed</code>. 
     *    <li> in <code>[ 0,1]</code> if <code>!signed</code>. 
     *    </ul>
     */
    static double random(boolean signed) {
	return signed ? 2*(Math.random()-0.5) : Math.random();
    }

    public static double createArgD(boolean isSigned) {
	double cand;
	cand = Math.random();
	cand *= Math.pow(2.0,100*Math.random()-50);
	cand *= isSigned ? Math.signum(Math.random() - 0.5) : 1;
	return cand;
    }

    /**
     * Returns a random number which is signed 
     * if demanded by <code>isSigned</code> 
     * and which ranges from the given exponents. 
     */
    public static double createArgD(boolean isSigned, int exp0, int exp1) {
	double cand;
	cand = Math.random();
	cand *= Math.pow(2.0,(exp1-exp0)*Math.random()+exp0);
	cand *= isSigned ? Math.signum(Math.random() - 0.5) : 1;
	return cand;
    }

    /**
     * Returns a list of <code>numArgs</code> arguments as double values. 
     * The sum of all numbers are restricted as specified by the parameters. 
     *
     * @param numArgs
     *    the length of the argument list to be returned. 
     * @param signed 
     *    whether the arguments may be negative. 
     * @param inRange 
     *    whether the sum of the entries in the result List 
     *    is in <code>[0,1)</code> or, 
     *    if <code>signed</code> in <code>(-1,1)</code>. 
     * @return
     *    a list of <code>numArgs</code> arguments as double values. 
     *    The sum of all numbers is in <code>[0,1)</code> 
     *    and for <code>!signed</code> even in <code>[0.5,1)</code>. 
     *    The entries are in <code>[0,1)</code> for <code>!signed</code> 
     *    and in <code>(-1,1)</code> for <code>signed</code>. 
     */
    public static List<Double> createMultArgsSumD(int numArgs,
						  boolean signed,
						  boolean inRange) {

	// create a list of random numbers in Double-format. 
	List<Double> resultD = new ArrayList<Double>(numArgs);

	if (numArgs == 0) {
	    return resultD;
	}

	double sum = 0;
	double max = 0;
	double cand;
	for (int i = 0; i < numArgs; i++) {
	    cand = random(signed);
	    // cand in [-1,1] or in [ 0,1] depending on signed
	    max = Math.max(max,Math.abs(cand));
	    resultD.add(cand);
	    sum += cand;
	} // for i 
	// Here, sum contains the sum of the entries of resultD 
	// whereas max contains the maximum of the absolute values. 

	if (inRange) {
	    int sgn = (int)Math.signum(sum);

	    // make sure that the sum of all arguments does not reach 1. 
	    max = Math.max(max,Math.abs(sum));
	    // TBC: why is this necessary? 
	    double shift = Math.pow(2,
				    //Math.ceil(MathExt.ld(max)));
				    Math.ceil(Math.log(max)/Math.log(2)));
	    // Note that shift is in fact an integer. 
	    // leaving out Math.ceil, we obtain 
	    // shift <= Math.pow(2,MathExt.ld(max))=max 

	    assert -1 < sum/shift && sum/shift < 1;
	    assert -1 < max/shift && max/shift < 1;

	    for (int i = 0; i < numArgs; i++) {
		// "/shift" that all entries and the sum are in (-1,1)
		// "*sgn"   that the sum is even in [0,1) 
		resultD.set(i,(double)resultD.get(i)/shift*sgn);
	    }
	} // inRange 
	
	return resultD;
    }


    public static List<Double> createMultArgsSumD(boolean signed,
						  boolean inRange) {
	int numArgs = 1+(int)Math.round(10*Math.random());
	return createMultArgsSumD(numArgs, signed, inRange);
    }

    public static List<Double> createMultArgsD(int numArgs,
					       boolean allowsSigned,
					       boolean inRange, 
					       boolean allowsNaN) {
	// create a list of random numbers in Double-format. 
	List<Double> resultD = new ArrayList<Double>(numArgs);
	double num;
	for (int i = 0; i < numArgs; i++) {
	    if (allowsNaN && Math.random() > 0.95) {
		num = Double.NaN;
	    } else {
		num = Math.random();
		num *= inRange ? 1 : Math.pow(2.0,100*Math.random()-50);
		num *= allowsSigned ? Math.signum(Math.random() - 0.5) : 1;
	    }


	    resultD.add(num);
	}

	return resultD;
    }

    public static List<Double> createMultArgsD(boolean signed,
					       boolean inRange, 
					       boolean allowsNaN) {
	int numArgs = 1+(int)Math.round(10*Math.random());
	return createMultArgsD(numArgs, signed, inRange, allowsNaN);
    }

}
