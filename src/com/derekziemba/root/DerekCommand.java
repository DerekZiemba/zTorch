package com.derekziemba.root;

import java.io.IOException;

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;



public class DerekCommand {
	
    ExecutionMonitor executionMonitor = null;
    boolean finished = false;
    boolean terminated = false;
    
    int command_timeout = 5000;
    boolean executing = false;
	
    protected void startExecution() {
        executionMonitor = new ExecutionMonitor();
        executionMonitor.setPriority(Thread.MIN_PRIORITY);
        executionMonitor.start();
        executing = true;
    }
    
    private class ExecutionMonitor extends Thread {
        public void run() {
            while (!finished) {

                synchronized (DerekCommand.this) {
                    try {
                    	DerekCommand.this.wait(command_timeout);
                    } catch (InterruptedException e) {}
                }

                if (!finished) {
                    terminate("Timeout Exception");
                }
            }
        }
    }
    
    public void terminate(String reason) {
        try {
            Shell.closeRootShell();
            terminated(reason);
        } catch (IOException e) {}
    }

    protected void terminated(String reason) {
        synchronized (DerekCommand.this) {

           commandTerminated(id, reason);
        

          //  RootTools.log("Command " + id + " did not finish because it was terminated. Termination reason: " + reason);
            setExitCode(-1);
            terminated = true;
            finishCommand();
        }
    }
    
}
