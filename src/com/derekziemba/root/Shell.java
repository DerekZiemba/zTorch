
package com.derekziemba.root;

import java.io.*;

public class Shell {
    private static Shell rootShell = null;

    private final Process proc;
    private final OutputStreamWriter out;


    private Shell(String cmd) throws IOException {
        this.proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        this.out = new OutputStreamWriter(this.proc.getOutputStream(), "UTF-8");
    }

    public void exec(String command)  {
        try {
			out.write(command+'\n');
	        out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void close() {
        try {
            if (out != null) {
                out.close();
                if(proc != null) {
                	proc.destroy();
                }
            }
        } catch (Exception ignore) {}
    }

    public static Shell getShell() {
        if (Shell.rootShell == null) {
            String cmd = "su";
            while (Shell.rootShell == null) {
                try {
					Shell.rootShell = new Shell(cmd);
				} catch (IOException e) {	}
            }
        } 
        return Shell.rootShell;
    }
}