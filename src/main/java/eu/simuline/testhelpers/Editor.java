package eu.simuline.testhelpers;

/**
 * Enumeration of allowed editors and how to open it at the location where a failure occurs. 
 * More specifically: 
 * If a testmethod fails, an exception with a stacktrace is thrown. 
 * On click the editor is opened at the according source file in the given line. 
 * This enumeration implements for each editor supported 
 * the invocation 
 */
public enum Editor {
  emacsclient {
    String[] invocation(String path, int lineNumber) {
      return new String[] {
        this.name(), 
        "--no-wait", 
        "+" + lineNumber, 
        path
          };
    }
  }, 
  code {
    String[] invocation(String path, int lineNumber) {
      return new String[] {
        this.name(),
        "-g", 
        path + ":" + lineNumber
      };
    }
  };

  /**
   * Given the path to a file an a line number within that file, 
   * returns the command with parameters as a string array. 
   * 
   * @param path
   *   the path of the file to open with this editor. 
   * @param lineNumber
   *   the line number to open this editor at. 
   * @return
   *    A string array where the 0th entry is the command 
   *    and the other entries are one option each. 
   *    The command with options is to open the file with the given path 
   *    at the line with the number specified 
   *    for the editor represented by this instance. 
   */
  abstract String[] invocation(String path, int lineNumber);
}
