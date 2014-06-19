/* 
 * This file is part of the RootTools Project: http://code.google.com/p/roottools/
 *  
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
 *  
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 * 
 * The terms of each license can be found in the root directory of this project's repository as well as at:
 * 
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */
package com.stericson.RootTools.execution;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootDeniedException;


/**
 * 
 * @author Derek Ziemba
 * Changes
 * Shell OOM increased from -17 to -19
 * Decreased shell timeout from 25ms to 5 ms
 *
 */

public class Shell {

    //Statics -- visible to all
    private static int shellTimeout = 10000;
    private static final String token = "F*D^W@#FGF";
    private static Shell rootShell = null;

    private String error = "";

    private final Process proc;
    private final OutputStreamWriter out;
    private final List<Command> commands = new ArrayList<Command>();

    //indicates whether or not to close the shell
    private boolean close = false;

    public boolean isExecuting = false;
    public boolean isReading = false;

    private int maxCommands = 5000;
 
    private int write = 0;
    private int totalExecuted = 0;
    private boolean isCleaning = false;

    //private constructor responsible for opening/constructing the shell
    private Shell(String cmd) throws IOException, TimeoutException, RootDeniedException {

        this.proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        this.out = new OutputStreamWriter(this.proc.getOutputStream(), "UTF-8");

        /**
         * Thread responsible for carrying out the requested operations
         */
        Worker worker = new Worker(this);
        worker.start();

        try {
            /**
             * The flow of execution will wait for the thread to die or wait until the
             * given timeout has expired.
             *
             * The result of the worker, which is determined by the exit code of the worker,
             * will tell us if the operation was completed successfully or it the operation
             * failed.
             */
            worker.join(Shell.shellTimeout);

            /**
             * The operation could not be completed before the timeout occured.
             */
            if (worker.exit == -911) {

                try {
                    this.proc.destroy();
                } catch (Exception e) {}
                closeQuietly(this.out);

                throw new TimeoutException(this.error);
            }
            /**
             * Root access denied?
             */
            else if (worker.exit == -42) {

                try {
                    this.proc.destroy();
                } catch (Exception e) {}
                closeQuietly(this.out);

                throw new RootDeniedException("Root Access Denied");
            }
            /**
             * Normal exit
             */
            else {
                /**
                 * The shell is open.
                 *
                 * Start two threads, one to handle the input and one to handle the output.
                 *
                 * input, and output are runnables that the threads execute.
                 */
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


    private void closeQuietly(final Writer output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception ignore) {}
    }

    public void close() throws IOException {
        if (this == Shell.rootShell)   Shell.rootShell = null;
        synchronized (this.commands) {
            /**
             * instruct the two threads monitoring input and output
             * of the shell to close.
             */
            this.close = true;
            this.notifyThreads();
        }
    }

    public static void closeRootShell() throws IOException {
        if (Shell.rootShell == null)
            return;
        Shell.rootShell.close();
    }


    public int getCommandQueuePosition(Command cmd) {
        return this.commands.indexOf(cmd);
    }

    public String getCommandQueuePositionString(Command cmd) {
        return "Command is in position " + getCommandQueuePosition(cmd) + " currently executing command at position " + this.write + " and the number of commands is " + commands.size();
    }

    public static Shell getRootShell() {
            return Shell.rootShell;
    }

    public static boolean isRootShellOpen() {
        return Shell.rootShell == null;
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

                    if (write >= maxCommands) {

                        /**
                         * wait for the read to catch up.
                         */
                        while (read != write)
                        {
                       //     RootTools.log("Waiting for read and write to catch up before cleanup.");
                        }
                        /**
                         * Clean up the commands, stay neat.
                         */
                        cleanCommands();
                    }

                    /**
                     * Write the new command
                     *
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

    protected void notifyThreads() {
        Thread t = new Thread() {
            public void run() {
                synchronized (commands) {
                    commands.notifyAll();
                }
            }
        };

        t.start();
    }



    public static void runRootCommand(Command command) throws IOException, TimeoutException, RootDeniedException {
        Shell.startRootShell().add(command);
    }

    public static Shell startRootShell() throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(20000, 3);
    }


    public static Shell startRootShell(int timeout, int retry) throws IOException, TimeoutException, RootDeniedException {

        Shell.shellTimeout = timeout;

        if (Shell.rootShell == null) {
          //  RootTools.log("Starting Root Shell!");
            String cmd = "su";
            // keep prompting the user until they accept for x amount of times...
            int retries = 0;
            while (Shell.rootShell == null) {
                try {
                    Shell.rootShell = new Shell(cmd);
                } catch (IOException e) {
                    if (retries++ >= retry) {
                     //   RootTools.log("IOException, could not start shell");
                        throw e;
                    }
                }
            }
        } else {
        //    RootTools.log("Using Existing Root Shell!");
        }

        return Shell.rootShell;
    }




    protected static class Worker extends Thread {
        public int exit = -911;

        public Shell shell;

        private Worker(Shell shell) {
            this.shell = shell;
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
}
