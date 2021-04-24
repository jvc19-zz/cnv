	/* InstrumentTool.java
 * Sample program using BIT -- counts the number of instructions executed.
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 * 
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 */

import BIT.highBIT.*;
import java.io.*;
import java.util.*;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.lang.Thread;
import java.net.InetAddress;

public class InstrumentTool {
    private static PrintStream out = null;
    private static int i_count = 0, b_count = 0, m_count = 0;
    
    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();
        
        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
				// create class info object
				ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);
				
                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
					routine.addBefore("InstrumentTool", "mcount", new Integer(1));
                    
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("InstrumentTool", "count", new Integer(bb.size()));
                    }
                }
                ci.addAfter("InstrumentTool", "printResult", ci.getClassName());
		ci.addAfter("InstrumentTool", "testsaveToFile", ci.getClassName());
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }
    
    public static synchronized void printResult(String foo) {
	System.out.println(i_count + " instructions");
    }

    public static synchronized void testsaveToFile(String foo) {
	    long thread_id = Thread.currentThread().getId();
	    // String instance_id = InetAddress.getLocalHost().toString();
	    String dummyInstanceId = "instance";

	    Writer writer = null;

	    try {
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/vagrant/cnv/instrumented/test.txt", true), "utf-8"));
		writer.write("{ " + dummyInstanceId + ":{ " + thread_id + ":{ " + i_count + "} } }" + "\n");
	    } catch (IOException ex) {
		System.out.println("Error while trying to write to file");
	    } finally {
		try {writer.close();} catch (Exception ex) {/*ignore*/}
	    }
    }    

    public static synchronized void count(int incr) {
        i_count += incr;
        b_count++;
    }

    public static synchronized void mcount(int incr) {
		m_count++;
    }
}

