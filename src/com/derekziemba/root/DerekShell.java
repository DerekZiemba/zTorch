package com.derekziemba.root;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;


public class DerekShell {

    private static int shellTimeout = 10000;
    private static DerekShell rootShell = null;
    
  
    private final Process proc;
    private final OutputStreamWriter out;

    
    private final List<Command> commands = new ArrayList<Command>();
    //indicates whether or not to close the shell
    private boolean close = false;
    public boolean isExecuting = false;
    public boolean isReading = false;
    boolean finished = false;
    private boolean isCleaning = false;
    private int totalExecuted = 0;
  
    private int write = 0;
    private int maxCommands = 5000;
    
    private DerekShell(String cmd) throws IOException, TimeoutException, RootDeniedException {

        this.proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        this.out = new OutputStreamWriter(this.proc.getOutputStream(), "UTF-8");

        /**
         * Thread responsible for carrying out the requested operations
         */
        Worker worker = new Worker(this);
        worker.start();

        try {
            worker.join(DerekShell.shellTimeout);
            if (worker.exit == -911) { //The operation could not be completed before the timeout occured.

                try {
                    this.proc.destroy();
                } catch (Exception e) {}

                throw new TimeoutException("Timeout Error");
            }
            else if (worker.exit == -42) { // Root access denied?

                try {
                    this.proc.destroy();
                } catch (Exception e) {}

                throw new RootDeniedException("Root Access Denied");
            }

            else { //Normal exit

                Thread si = new Thread(this.input, "Shell Input");
                si.setPriority(Thread.NORM_PRIORITY);
                si.start();

            }
        } catch (InterruptedException ex) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new TimeoutException();
        }
    }
    

    
    protected static class Worker extends Thread {
        public int exit = -911;

        public DerekShell shell;

        private Worker(DerekShell derekShell) {
            this.shell = derekShell;
        }

        public void run() {
            try {
                shell.out.flush();
                        
                 try {
					setShellOom();
				} catch (IllegalAccessException | IllegalArgumentException
						| NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                 this.exit = 1;
            } catch (IOException e) { }
        }

        /*
         * setOom for shell processes (sh and su if root shell)
         * and discard outputs
         * 
         */
        private void setShellOom() throws IOException, IllegalAccessException, IllegalArgumentException, NoSuchFieldException   {

			Class<?> processClass = shell.proc.getClass();
			Field field;
			try {
				field = processClass.getDeclaredField("pid");
			} catch (NoSuchFieldException e) {
				field = processClass.getDeclaredField("id");
			}
			field.setAccessible(true);
			int pid = (Integer) field.get(shell.proc);
            shell.out.write("(echo -19 > /proc/" + pid + "/oom_adj) &> /dev/null\n");
            shell.out.write("(echo -19 > /proc/$$/oom_adj) &> /dev/null\n");
            shell.out.flush();
		}
    }
    
    /**
     * Runnable to write commands to the open shell.
     * <p/>
     * When writing commands we stay in a loop and wait for new
     * commands to added to "commands"
     * <p/>
     * The notification of a new command is handled by the method add in this class
     */
    private Runnable input = new Runnable() {
        public void run() {
            try {
                while (true) {
                    synchronized (commands) {
                        /**
                         * While loop is used in the case that notifyAll is called
                         * and there are still no commands to be written, a rare
                         * case but one that could happen.
                         */
                        while (!close && write >= commands.size()) {
                            isExecuting = false;
                            commands.wait();
                        }
                    }

                    if (write >= maxCommands) { //Clean up the commands, stay neat.
                        cleanCommands();
                    }

                    /**
                     * Write the new command
                     * We write the command followed by the token to indicate
                     * the end of the command execution
                     */
                    if (write < commands.size()) {
                        isExecuting = true;
                        Command cmd = commands.get(write);
                        cmd.startExecution();
                      //  RootTools.log("Executing: " + cmd.getCommand());

                        out.write(cmd.getCommand());
                        String line = "\necho " + token + " " + totalExecuted + " $?\n";
                        out.write(line);
                        out.flush();
                        write++;
                        totalExecuted++;
                    } else if (close) {
                        /**
                         * close the thread, the shell is closing.
                         */
                        isExecuting = false;
                        out.write("\nexit 0\n");
                        out.flush();
                      //  RootTools.log("Closing shell");
                        return;
                    }
                }
            } catch (IOException e) {
              //  RootTools.log(e.getMessage(), 2, e);
            } catch (InterruptedException e) {
             //   RootTools.log(e.getMessage(), 2, e);
            } finally {
                write = 0;
                closeQuietly(out);
            }
        }
    };
    


    private void closeQuietly(final Writer output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception ignore) {}
    }
    

    
    private void cleanCommands() {
        this.isCleaning = true;
        int toClean = Math.abs(this.maxCommands - (this.maxCommands / 4));

        for (int i = 0; i < toClean; i++) {
            this.commands.remove(0);
        }

        this.read = this.commands.size() - 1;
        this.write = this.commands.size() - 1;
        this.isCleaning = false;
    }
    
    public Command add(Command command) //throws IOException 
    {
        if (this.close)
            throw new IllegalStateException(
                    "Unable to add commands to a closed shell");

        while (this.isCleaning) {
            //Don't add commands while cleaning
            ;
        }
        this.commands.add(command);

        this.notifyThreads();

        return command;
    }
    
    public static DerekShell startRootShell() throws IOException, TimeoutException, RootDeniedException {
        return DerekShell.startRootShell(20000, 3);
    }
    
    /**
     * Create a root shell to execute commands 
     * @param timeout
     * @param retry
     * @return
     * @throws IOException
     * @throws TimeoutException
     * @throws RootDeniedException
     */
    public static DerekShell startRootShell(int timeout, int retry) throws IOException, TimeoutException, RootDeniedException {
        DerekShell.shellTimeout = timeout;

        if (DerekShell.rootShell == null) {
            String cmd = "su";
            // keep prompting the user until they accept for x amount of times...
            int retries = 0;
            while (DerekShell.rootShell == null) {
                try {
                	DerekShell.rootShell = new DerekShell(cmd);
                } catch (IOException e) {
                    if (retries++ >= retry) {
                     //   RootTools.log("IOException, could not start shell");
                        throw e;
                    }
                }
            }
        } 

        return DerekShell.rootShell;
    }
}
