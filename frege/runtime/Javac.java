package frege.runtime;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;


public class Javac {
	
	final static String fregeJavac = System.getProperty("frege.javac");
	final static JavaCompiler compiler =
			fregeJavac == null || fregeJavac.startsWith("internal") ?
				ToolProvider.getSystemJavaCompiler() : null;
  final static StandardJavaFileManager fileManager = 
    		compiler == null ? null : compiler.getStandardFileManager(null, null, null);

	public static int runJavac(final String[] cmd) {
		StringBuilder sb = new StringBuilder();
		for (String s : cmd) { sb.append(s); sb.append(" "); }
		
		if (compiler != null && fileManager != null) {
			// use the internal compiler
			File[] files = new File[] { new File(cmd[cmd.length-1]) };
			String[] options = new String[cmd.length-2];
			for (int i=0; i<options.length; i++) options[i] = cmd[i+1];
		    Iterable<? extends JavaFileObject> compilationUnits1 =
		               fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
		    System.err.println("calling: " + sb.toString());
		    CompilationTask task = compiler.getTask(null, 
		    		fileManager, null, 
		    		Arrays.asList(options), 
		    		null, compilationUnits1);
		    
		    boolean success = task.call();
		    try {
				fileManager.flush();
			} catch (IOException e) {
				// what can we do here?
				System.err.println(e.getMessage() + " while flushing javac file manager.");
				return 1;
			}
		    return success ? 0 : 1;

		}
		try {
			// String cmd = "javac -cp " + cp + " -d " + d + " " + src;
			int cex = 0;
			
			System.err.println("running: " + sb.toString());
			Process jp = java.lang.Runtime.getRuntime().exec(cmd);
			// if (Common.verbose)
				
			java.io.InputStream is = jp.getErrorStream();
			while ((cex = is.read()) >= 0) {
				System.err.write(cex);
			}
			if ((cex = jp.waitFor()) != 0) {
				System.err.println("javac terminated with exit code " + cex);
			}
			return cex;
		} catch (java.io.IOException e) {
			System.err.println("Can't javac  (" + e.getMessage() + ")");
		} catch (InterruptedException e) {
			System.err.println("Can't javac  (" + e.getMessage() + ")");
		}
		return 1;
	}


}
