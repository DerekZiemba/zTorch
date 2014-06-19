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

import android.content.Context;

import java.io.IOException;


public abstract class Command {

    ExecutionMonitor executionMonitor = null;
    boolean executing = false;

    String[] command = {};
    boolean javaCommand = false;
    Context context = null;
    boolean finished = false;
    boolean terminated = false;
    int exitCode = -1;
    int id = 0;
    int command_timeout = 5000;

    public abstract void commandOutput(int id, String line);
    public abstract void commandTerminated(int id, String reason);
    public abstract void commandCompleted(int id, int exitCode);

    /**
     * Constructor for executing a normal shell command
     * @param id the id of the command being executed
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, String... command) {
        this.command = command;
        this.id = id;
    }


    /**
     * Constructor for executing a normal shell command
     * @param id the id of the command being executed
     * @param timeout the time allowed before the shell will give up executing the command
     *                and throw a TimeoutException.
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, int timeout, String... command) {
        this.command = command;
        this.id = id;
        this.command_timeout = timeout;
    }


    protected void finishCommand() {
        executing = false;
        finished = true;
        this.notifyAll();
    }

    protected void commandFinished() {
        if (!terminated) {
            synchronized (this) {
                commandCompleted(id, exitCode);
                finishCommand();
            }
        }
    }

    public String getCommand() {
        StringBuilder sb = new StringBuilder();

        if(javaCommand) {
            String filePath = context.getFilesDir().getPath();
            for (int i = 0; i < command.length; i++) {
                /*
                 * TODO Make withFramework optional for applications
                 * that do not require access to the fw. -CFR
                 */
                sb.append(
                        "dalvikvm -cp " + filePath + "/anbuild.dex"
                        + " com.android.internal.util.WithFramework"
                        + " com.stericson.RootTools.containers.RootClass "
                        + command[i]);
                sb.append('\n');
            }
        }
        else {
            for (int i = 0; i < command.length; i++) {
                sb.append(command[i]);
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public boolean isExecuting() {
        return executing;
    }


    public boolean isFinished() {
        return finished;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    protected void setExitCode(int code) {
        synchronized (this) {
            exitCode = code;
        }
    }

    protected void startExecution() {
        executionMonitor = new ExecutionMonitor();
        executionMonitor.setPriority(Thread.MIN_PRIORITY);
        executionMonitor.start();
        executing = true;
    }

    public void terminate(String reason) {
        try {
            Shell.closeRootShell();
            terminated(reason);
        } catch (IOException e) {}
    }

    protected void terminated(String reason) {
        synchronized (Command.this) {

           commandTerminated(id, reason);
        

          //  RootTools.log("Command " + id + " did not finish because it was terminated. Termination reason: " + reason);
            setExitCode(-1);
            terminated = true;
            finishCommand();
        }
    }

    protected void output(int id, String line) {
        commandOutput(id, line);
        
    }

    private class ExecutionMonitor extends Thread {
        public void run() {
            while (!finished) {

                synchronized (Command.this) {
                    try {
                        Command.this.wait(command_timeout);
                    } catch (InterruptedException e) {}
                }

                if (!finished) {
                    terminate("Timeout Exception");
                }
            }
        }
    }

   
}
