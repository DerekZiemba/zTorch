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

package com.stericson.RootTools.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

public final class RootToolsInternalMethods {

    // --------------------
    // # Internal methods #
    // --------------------

    protected RootToolsInternalMethods() {}

    public static void getInstance() {
        //this will allow RootTools to be the only one to get an instance of this class.
        RootTools.setRim(new RootToolsInternalMethods());
    }


    /**
     * Use this to check whether or not a file exists on the filesystem.
     *
     * @param file String that represent the file, including the full path to the
     *             file and its name.
     * @return a boolean that will indicate whether or not the file exists.
     */
    public boolean exists(final String file) {
        final List<String> result = new ArrayList<String>();

        CommandCapture command = new CommandCapture(0,  "ls " + file) {
            @Override
            public void output(int arg0, String arg1) {
           //     RootTools.log(arg1);
                result.add(arg1);
            }
        };

        try {
                Shell.getRootShell().add(command);
                commandWait(Shell.getRootShell(), command);
            
        } catch (Exception e) {
            return false;
        }

        for (String line : result) {
            if (line.trim().equals(file)) {
                return true;
            }
        }

        result.clear();
        try {
            Shell.startRootShell().add(command);
            commandWait(Shell.startRootShell(), command);

        } catch (Exception e) {
            return false;
        }

        //Avoid concurrent modification...
        List<String> final_result = new ArrayList<String>();
        final_result.addAll(result);

        for (String line : final_result) {
            if (line.trim().equals(file)) {
                return true;
            }
        }

        return false;

    }



    /**
     * @param binaryName String that represent the binary to find.
     * @return <code>true</code> if the specified binary was found. Also, the path the binary was
     *         found at can be retrieved via the variable lastFoundBinaryPath, if the binary was
     *         found in more than one location this will contain all of these locations.
     */
    public boolean findBinary(final String binaryName) {
        boolean found = false;
        RootTools.lastFoundBinaryPaths.clear();

        final List<String> list = new ArrayList<String>();
        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};

       // RootTools.log("Checking for " + binaryName);

        //Try to use stat first
        try {
            for(final String path : places) {
                CommandCapture cc = new CommandCapture(0,  "stat " + path + binaryName) {
                    @Override
                    public void commandOutput(int id, String line) {
                        if(line.contains("File: ") && line.contains(binaryName)) {
                            list.add(path);

                          //  RootTools.log(binaryName + " was found here: " + path);
                        }

                     //   RootTools.log(line);
                    }
                }; 

                RootTools.startRootShell().add(cc);
                commandWait(RootTools.startRootShell(), cc);

            }

            found = !list.isEmpty();
        } catch (Exception e) {
         //   RootTools.log(binaryName + " was not found, more information MAY be available with Debugging on.");
        }

        if (!found) {
        //    RootTools.log("Trying second method");

            for (String where : places) {
                if (RootTools.exists(where + binaryName)) {
                  //  RootTools.log(binaryName + " was found here: " + where);
                    list.add(where);
                    found = true;
                } else {
                 //   RootTools.log(binaryName + " was NOT found here: " + where);
                }
            }
        }

        if(!found) {
         //   RootTools.log("Trying third method");

            try {
                List<String> paths = RootTools.getPath();

                if (paths != null) {
                    for (String path : paths) {
                        if (RootTools.exists(path + "/" + binaryName)) {
                     //       RootTools.log(binaryName + " was found here: " + path);
                            list.add(path);
                            found = true;
                        } else {
                     //       RootTools.log(binaryName + " was NOT found here: " + path);
                        }
                    }
                }
            } catch (Exception e) {
            //    RootTools.log(binaryName + " was not found, more information MAY be available with Debugging on.");
            }
        }

        Collections.reverse(list);

        RootTools.lastFoundBinaryPaths.addAll(list);

        return found;
    }


    /**
     * @return <code>true</code> if your app has been given root access.
     * @throws TimeoutException if this operation times out. (cannot determine if access is given)
     */
    public boolean isAccessGiven() {
        try {
          //  RootTools.log("Checking for Root access");
            RootTools.accessGiven = false;

            CommandCapture command = new CommandCapture(RootTools.IAG, "id") {
                @Override
                public void output(int id, String line) {
                    if (id == RootTools.IAG) {
                        Set<String> ID = new HashSet<String>(Arrays.asList(line.split(" ")));
                        for (String userid : ID) {
                       //     RootTools.log(userid);

                            if (userid.toLowerCase().contains("uid=0")) {
                                RootTools.accessGiven = true;
                          //      RootTools.log("Access Given");
                                break;
                            }
                        }
                        if (!RootTools.accessGiven) {
                         //   RootTools.log("Access Denied?");
                        }
                    }
                }
            };
            Shell.startRootShell().add(command);
            commandWait(Shell.startRootShell(), command);

            return RootTools.accessGiven;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //TODO:Command Wait has delay
    private void commandWait(Shell shell, Command cmd) throws Exception {

        while (!cmd.isFinished()) {

          //  RootTools.log(RootTools.TAG, shell.getCommandQueuePositionString(cmd));

            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!cmd.isExecuting() && !cmd.isFinished()) {
                if (!shell.isExecuting && !shell.isReading) {
                    Log.e(RootTools.TAG, "Waiting for a command to be executed in a shell that is not executing and not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else if (shell.isExecuting && !shell.isReading) {
                    Log.e(RootTools.TAG, "Waiting for a command to be executed in a shell that is executing but not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else {
                    Log.e(RootTools.TAG, "Waiting for a command to be executed in a shell that is not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                }
            }

        }
    }
}
