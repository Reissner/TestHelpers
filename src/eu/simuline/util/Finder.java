package eu.simuline.util;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Emulates the unix shell command <code>find</code> which is invoked as 
 * <code>find [-H] [-L] [-P] [-Olevel] 
 * [-D help|tree|search|stat|rates|opt|exec] [path...] (expression)*</code>. 
 * If <code>path</code> is left out (possibly more than one path?), 
 * then the default is <code>.</code>, i.e. the current directory. 
 * The <code>path</code> need not be a directory. 
 * If it is a proper file then typically wildcards are used. 
 * Then the name is matched. 
 * If no expression is given, the default is <code>-print</code>. 
 * So we may assume, 
 * that both, a path and a non-empty sequence of expressions, are given. 
 * <p>
 * The idea behind the <code>find</code> command is, 
 * that, starting with the files matching the <code>path</code>, 
 * each <code>expression</code> serves as a filter, 
 * feeding the filter corresponding to the following expression. 
 * Each filter can have a side effect. 
 * Expressions may be either tests or actions. 
 * For tests, the focus is on the filter-functionality, 
 * whereas actions are trivial filters just passing the files they receive. 
 * Actions are applied because of their side effects like <code>print</code>. 
 * <p>
 * The most basic kind of finder corresponds with the command 
 * <code>find path</code> iterating just over the files in the path. 
 * This kind of finder is returned by the static method {@link #path(File)}. 
 * Starting with this basic finder, further finders can be added to the pipe 
 * using the member methods 
 * {@link #name(String)} and {@link # print(PrintStream)}. 
 *
 * Created: Wed Nov 21 17:29:41 2012
 *
 * @author <a href="mailto:ernst@">Ernst Reissner</a>
 * @version 1.0
 */
public abstract class Finder {

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    /**
     * The most basic kind of finder: 
     * Method {@link #path(File)} returns an instance of this. 
     * {@link #next()} returns the given file and, 
     * if this is a folder all files therein. 
     */
    static class Primary extends Finder {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The list of files to be returned by {@link #next()} 
	 * unwrapping folders recursively. 
	 */
	private final Stack<File> files;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Pushes <code>file</code> to {@link #files}. 
	 */
	Primary(File file) {
	    this.files = new Stack<File>();
	    this.files.push(file);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Has a next file iff {@link #files} is not empty. 
	 */
	public boolean hasNext() {
	    return !this.files.isEmpty();
	}

	/**
	 * If the topmost entry of {@link #files} is no folder, 
	 * this is returned as is. 
	 * otherwise, in addition, this folder is unwrapped 
	 * and the contents is pushed onto the stack {@link #files}. 
	 */
	public File next() {
	    assert hasNext();
	    File file = this.files.pop();
	    if (file.isDirectory()) {
		File[] list = file.listFiles();
		if (list == null) {
		    System.out.println("cannot read "+file);
		} else {
		    // push in inverse order 
		    for (int i = list.length-1; i>=0; i--) {
			this.files.push(list[i]);
		    }
		    
		    //this.files.addAll(Arrays.asList(list));
		}
	    }
	    return file;
	}

    } // class Primary 

    /**
     * A finder wrapping a {@link Finder.Filter}. 
     * Files are read from {@link #encl} and are passed via {@link #next()} 
     * iff they pass the filter {@link #filter}. 
     */
    static class Secondary extends Finder {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The next file to be returned by {@link #next()} if any, 
	 * i.e. if  {@link #hasNext()} returns <code>true</code>; 
	 * otherwise this field is <code>null</code>. 
	 */
	protected File next;

	/**
	 * The source finder from which the stream of files is read. 
	 * **** this is superfluous if this is not static 
	 */
	Finder encl;

	/**
	 * The filter to be passed 
	 * before a file is returned by {@link #next()}. 
	 */
	Filter filter;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Create a finder receiving a stream of files from <code>encl</code> 
	 * and passing them further via {@link #next()} 
	 * if they pass the filter <code>filter</code>. 
	 */
	Secondary(Finder encl, Filter filter) {
	    super();
	    this.encl = encl;
	    this.filter = filter;
	    updateNext();
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	// **** better implementation if this class were not static 
	protected void updateNext() {
//	    while (Finder.this.hasNext()) {
	    while (this.encl.hasNext()) {
//		this.next = Finder.this.next();
		this.next = this.encl.next();
		assert this.next != null;
		if (this.filter.pass(this.next)) {
		    return;
		}
	    }
	    this.next = null;
	}

	public boolean hasNext() {
	    return this.next != null;
	}

	public File next() {
	    assert hasNext();
	    File res = this.next;
	    assert res != null;
	    updateNext();
	    return res;
	}
    } // class Secondary 

    /**
     *  <code></code>
     */
    class PrintFilter extends Secondary {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 *  <code></code>
	 */
	private final PrintStream str;


	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	PrintFilter(Finder encl, PrintStream str) {
	    super(encl, TRUE);
	    this.str = str;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	public File next() {
	    assert hasNext();
	    File res = this.next;
	    assert res != null;
	    this.str.println(res.toString());
	    updateNext();
	    return res;
	}
    } // class PrintFilter 

    /**
     * Represents a file filter. 
     */
    static interface Filter {
	/**
	 * Returns for the given file whether this file passes this filter. 
	 */
	boolean pass(File file);
    }

    /**
     * Filters files by name. See {@link Finder#name(String)}. 
     */
    static class NameFilter implements Filter {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The pattern to filter the filenames with. 
	 */
	private final Pattern pattern;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	NameFilter(String pattern) {
	    this.pattern = Pattern.compile(pattern);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	public boolean pass(File file) {
	    return this.pattern.matcher(file.getName()).matches();
	}

    } // class NameFilter 

    /**
     * Filter executing a shell command and passes the file received 
     * if the shell command succeeds according to its return code. 
     * Do not mix up with {@link Finder#IS_EXEC}. 
     * See {@link Finder#exec(String[])}. 
     */
    static class ExecFilter implements Filter {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The command to be executed including arguments 
	 * separated by space as to be passed to {@link Runtime#exec(String)}. 
	 * For more information on the entries 
	 * see {@link Finder#exec(String[])}. 
	 */
	private final String[] cmd;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates an execution filter from the given command. 
	 *
	 * @param cmd
	 *    The 0th entry is the command itself and the others are arguments. 
	 *    For details see {@link Finder#exec(String[])}. 
	 */
	ExecFilter(String[] cmd) {
	    this.cmd = cmd;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * The given file passes this filter, 
	 * i.e. this method returns <code>true</code> 
	 * if the shell command given by {@link #cmd} 
	 * succeeds according to its return value (which is then zero). 
	 * <p>
	 * Execution proceeds in the following steps: 
	 * <ul>
	 * <li>
	 * Replace the arguments {@link Finder#EXEC_ARG} 
	 * by the long name of <code>file</code>. 
	 * <li>
	 * Create a separate process to execute the command. 
	 * <li>
	 * Wait for execution end and 
	 * <li>
	 * pass if the return value 0 indicates success. 
	 * </ul>
	 */
	public boolean pass(File file) {
	    Process proc;
	    int exitVal;
	    String nextStr = file.toString();

	    // replace argument by filename 
	    String[] cmd = new String[this.cmd.length];
	    System.arraycopy(this.cmd, 0, cmd, 0, this.cmd.length);
	    // the 0th entry should not be EXEC_ARG 
	    for (int idx=0; idx < this.cmd.length; idx++) {
		if (cmd[idx] == Finder.EXEC_ARG) {
		    cmd[idx] = nextStr;
		}
	    }

	    try {
		//System.out.println(":"+(this.cmdName+" "+this.next));
		proc = Runtime.getRuntime().exec(cmd);
	    } catch (IOException e) {
		System.out.println("exec: "+e.getMessage());
		return false;
	    }
	    try {
		exitVal = proc.waitFor();// exitValue
	    } catch (InterruptedException e) {
		System.out.println("exec:"+e.getMessage());
		return false;
	    }
	    return exitVal == 0;
	}

    } // class ExecFilter 

    /**
     * One of the logical operations of filters: 
     * Returns a filter which passes a file 
     * iff the original filter {@link #negFilter} does not. 
     * <p>
     * See {@link Finder#neg(Filter)}. 
     */
    static class NegFilter implements Filter {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * The filter to be negated. 
	 */
	private final Filter negFilter;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	NegFilter(Filter negFilter) {
	    this.negFilter = negFilter;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Passes the given file iff {@link #negFilter} does not. 
	 */
	public boolean pass(File file) {
	    return !this.negFilter.pass(file);
	}

    } // class NegFilter 

    /**
     * One of the logical operations of filters: 
     * Returns a filter which passes a file 
     * iff all original filters in {@link #filter} do so. 
     * <p>
     * This is a lazy and-filter, i.e. if one of the filters rejects the file, 
     * the filters later in the sequence are not executed any more. 
     * So the ordering has an effect, if one of the filters has a side effect. 
     * Ordering may also affect performance.  
     * <p>
     * Maybe lazy and-filters are not so useful, because, 
     * unlike non-lazy and-filters and or-filters, 
     * they could be realized as a sequence of filters. 
     * <p>
     * See {@link Finder#and(Filter[])}. 
     */
    static class AndFilter implements Filter {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * This filter passes a file iff all of these pass 
	 * if invoked in the natural ordering. 
	 */
	private final Filter[] filters;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	AndFilter(Filter[] filters) {
	    this.filters = filters;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */


	public boolean pass(File file) {
	    for (int i = 0; i < filters.length; i++) {
		if (!this.filters[i].pass(file)) {
		    return false;
		}
	    } // for 

	    return true;
	}

    } // class AndFilter 

    /**
     * One of the logical operations of filters: 
     * Returns a filter which passes a file 
     * iff at least one of the original filters in {@link #filter} do so. 
     * <p>
     * This is a lazy or-filter, i.e. if one of the filters accepts the file, 
     * the filters later in the sequence are not executed any more. 
     * So the ordering has an effect, if one of the filters has a side effect.  
     * Ordering may also affect performance. 
     * <p>
     * See {@link Finder#or(Filter[])}. 
     */
    static class OrFilter implements Filter {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	/**
	 * This filter passes a file iff at least one of of these passes 
	 * if invoked in the natural ordering. 
	 */
	private final Filter[] filters;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	OrFilter(Filter[] filters) {
	    this.filters = filters;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */


	public boolean pass(File file) {
	    for (int i = 0; i < filters.length; i++) {
		if (this.filters[i].pass(file)) {
		    return true;
		}
	    } // for 

	    return false;
	}

    } // class OrFilter 


    /* -------------------------------------------------------------------- *
     * Class constants.                                                     *
     * -------------------------------------------------------------------- */

    /**
     * For {@link Finder.ExecFilter}s 
     * representing a filter invoking a shell command, 
     * this string represents the argument of the command 
     * which is in the original find a <code>{}</code> 
     * and which is replaced by the file name 
     * received by the preceding portion of the pipe. 
     * CAUTION: It must be used this instance 
     * and no other equivalent string <code>{}</code>. 
     */
    public final static String EXEC_ARG = "{}";

    /**
     * A filter passing all files. 
     * This corresponds the test <code>-true</code> 
     * in the original find command. 
     */
    public final static Filter TRUE = new Filter() {
	    public boolean pass(File file) {
		return true;
	    }
	};

    /**
     * A filter passing no file. 
     * This corresponds the test <code>-false</code> 
     * in the original find command. 
     */
    public final static Filter FALSE = new Filter() {
	    public boolean pass(File file) {
		return false;
	    }
	};

    /**
     * Filter passing the file received iff it is executable. 
     * This corresponds the test <code>-executable</code> 
     * in the original find command. 
     * Do not mix up with {@link Finder.ExecFilter}. 
     */
    public final static Filter CAN_EXEC  = new Filter() {
	    public boolean pass(File file) {
		return file.canExecute();
	    }
	};

    /**
     * Filter passing the file received iff it is readable. 
     * This corresponds the test <code>-readable</code> 
     * in the original find command. 
      */
    public final static Filter CAN_READ  = new Filter() {
	    public boolean pass(File file) {
		return file.canRead();
	    }
	};

    /**
     * Filter passing the file received iff it is writable. 
     * This corresponds the test <code>-writable</code> 
     * in the original find command. 
     */
    public final static Filter CAN_WRITE  = new Filter() {
	    public boolean pass(File file) {
		return file.canWrite();
	    }
	};

    /**
     * Filter passing the file received iff it is a regular file. 
     * This corresponds the test <code>-type f</code> 
     * in the original find command. 
     */
    public final static Filter IS_FILE  = new Filter() {
	    public boolean pass(File file) {
		return file.isFile();
	    }
	};

    /**
     * Filter passing the file received iff it is a regular file. 
     * This corresponds the test <code>-type d</code> 
     * in the original find command. 
     */
    public final static Filter IS_DIR  = new Filter() {
	    public boolean pass(File file) {
		return file.isDirectory();
	    }
	};



    /* -------------------------------------------------------------------- *
     * constructors and creator methods.                                    *
     * -------------------------------------------------------------------- */

    /**
     * This is declared <code>private</code> to prevent instantiation.
     */
    private Finder() {
    }

    /**
     * Returns a basic finder, emitting the given file 
     * if it exists and is accessible and, recursively, 
     * if it is a directory all files therein. 
     * The ordering is the same as in original find-command. 
     * The expression <code>Finder.path(file)</code> 
     * corresponds with <code>find file</code>, 
     * except that the find command implicitly adds a print filter 
     * as defined in {@link #print(PrintStream)}. 
     * Note also that, unlike th original find, 
     * no wildcards are supported. 
     *
     * @return
     *    an instance of {@link Finder.Primary}
     */
    public static Finder path(File file) {
	return new Finder.Primary(file);
    }

    /**
     * Filter without side effect: 
     * just passes the files received by this finder 
     * the (short) names of which 
     * match the regular expression <code>pattern</code>. 
     *
     * @return
     *    an instance of {@link Finder.Secondary} 
     *    instantiated with a filter of type {@link Finder.NameFilter} 
     */
    public Finder name(String pattern) {
	return new Secondary(Finder.this, new NameFilter(pattern));
    }

    /**
     * Filter passing all files received by this finder 
     * printing their full names to <code>str</code>. 
     */
    public Finder print(PrintStream str) {
	return new PrintFilter(Finder.this, str);
    }

    /**
     * Filter invoking a shell command: 
     * just passes the files received by this finder further 
     * if the command succeeds according to its return value. 
     * Example in original find-terminology: 
     * <code>find . -exec grep "pattern without quotes" {} \; -print</code>. 
     * Here, the portion specifying execution is 
     * <code>-exec grep "pattern without quotes" {} \;</code>: 
     * It starts with <code>-exec</code> and ends with <code>\;</code>. 
     * Note that the command <code>grep</code> 
     * has arguments <code>"pattern without quotes"</code> and <code>{}</code>. 
     * The first argument must be quoted because it contains whitespace 
     * and would otherwise be interpreted as three arguments. 
     * The second argument <code>{}</code> 
     * is silently replaced by the file name 
     * received by the preceeding portion of the find-command. 
     *
     * @param cmd
     *    a shell command with its arguments. 
     *    The 0th entry is the command itself and the others are arguments. 
     *    Be aware when escape sequences are needed. 
     *    Quotes ensuring that an argument is interpreted as a unit 
     *    <em>must</em> be omitted. 
     *    <p>
     *    In the original find command, 
     *    <code>{}</code> represents arguments to be replaced by the file name 
     *    received by the preceding portion of the pipe. 
     *    These must be represented by the object instance {@link #EXEC_ARG} 
     *    which is also a string <code>{}</code> 
     *    but it is wrong to put an equivalent string as e.g. 
     *    <code>new String("{}")</code>. 
     *    <p>
     *    For example to obtain a grep use 
     *    <code>new String{} {"grep", "pattern without quotes", EXEC_ARG}</code>
     * @return
     *    an instance of {@link Finder.Secondary} 
     *    instantiated with a filter of type {@link Finder.ExecFilter} 
     */
    public Finder exec(String[] cmd) {
	return new Secondary(Finder.this, new ExecFilter(cmd));
    }

    /**
     * Returns a filter which passes a file iff <code>filter</code> does not. 
     */
    public Finder neg(Filter filter) {
	return new Secondary(Finder.this, new NegFilter(filter));
    }

    /**
     * Returns a filter which passes a file 
     * iff so do all filters in <code>filters</code>. 
     * This corresponds the tests <code>expr1 -a .... exprn</code> 
     * and <code>expr1 -and .... exprn</code> 
     * in the original find command. 
     *
     * @param filters 
     *    a sequence of filters which may be empty. 
     *    If empty, this filter passes all files like {@link #TRUE}. 
     * @return 
     *    a lazy and-filter of type {@link Finder.AndFilter}. 
     *    For more details see there. 
     */
    public Finder and(Filter[] filters) {
	return new Secondary(Finder.this, new AndFilter(filters));
    }

    /**
     * Returns a filter which passes a file 
     * iff at least one filter in <code>filters</code> does so. 
     * This corresponds the tests <code>expr1 -o .... exprn</code> 
     * and <code>expr1 -or .... exprn</code> 
     * in the original find command. 
     *
     * @param filters 
     *    a sequence of filters which may be empty. 
     *    If empty, this filter passes no file like {@link #FALSE}. 
     * @return 
     *    a lazy and-filter of type {@link Finder.AndFilter}. 
     *    For more details see there. 
     */
    public Finder or (Filter[] filters) {
	return new Secondary(Finder.this, new  OrFilter(filters));
    }


    /* -------------------------------------------------------------------- *
     * abstract methods.                                                    *
     * -------------------------------------------------------------------- */

    /**
     * Returns whether this Finder can emit another file. 
     *
     * @see #next()
     */
    public abstract boolean hasNext();

    /**
     * Returns the next file this finder can emit. 
     * This does not throw an exception iff {@link #hasNext()} returns true. 
     */
    public abstract File next();

    public static void main(String[] args) throws Exception {

	File file = new File(args[0]);

	Finder finder = Finder.path(file)
	    .name(".*\\.java")
//	    .exec(new String[] {"grep", "matlab", EXEC_ARG})
	    .neg(new ExecFilter(new String[] {"grep", "class", EXEC_ARG}))
	    .or(new Filter[] {
		    new ExecFilter(new String[] {"grep", "enum", EXEC_ARG}),
		    new ExecFilter(new String[] {"grep", "interface", EXEC_ARG})
		}
		    )
	    .print(System.out);

	// Finder finder = Finder.path(file);
	// Multiplexer mult = finder.multiplexer();
	// Finder negFilter = mult.finder()
	//     .exec(new String[] {"grep", "'", EXEC_ARG});
	// finder = mult.finder().neg(negFilter);

	while (finder.hasNext()) {
	    finder.next();
//	    System.out.println(""+finder.next());
	}
    }

}
