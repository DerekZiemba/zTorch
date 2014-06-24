package com.derekziemba.misc;
import java.io.*;

public class Shell {
    private static Shell rootShell = null;
	private final Process proc;
	private final OutputStreamWriter writer;

    private Shell(String cmd) throws IOException {
        this.proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        this.writer = new OutputStreamWriter(this.proc.getOutputStream(), "UTF-8");
    }

    public void cmd(String command)  {
        try {
			writer.write(command+'\n');
	        writer.flush();
		} catch (IOException e) {	}
    }

    public void close() {
        try {
            if (writer != null) {  writer.close();
                if(proc != null) { 	proc.destroy();    }
            }
        } catch (IOException ignore) {}
    }

    public static void exec(String command) {   Shell.get().cmd(command);   }
    
    public static Shell get() {
        if (Shell.rootShell == null) {
            while (Shell.rootShell == null) {
                try {	Shell.rootShell = new Shell("su"); //Open with Root Privileges 
				} catch (IOException e) {	}
            }
        } 
        return Shell.rootShell;
    }
}
