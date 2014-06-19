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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.stericson.RootTools.Constants;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.containers.Mount;
import com.stericson.RootTools.containers.Permissions;
import com.stericson.RootTools.containers.Symlink;
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

    public ArrayList<Symlink> getSymLinks() throws IOException {

        LineNumberReader lnr = null;
        FileReader fr = null;

        try {

            fr = new FileReader("/data/local/symlinks.txt");
            lnr = new LineNumberReader(fr);

            String line;
            ArrayList<Symlink> symlink = new ArrayList<Symlink>();

            while ((line = lnr.readLine()) != null) {

                RootTools.log(line);

                String[] fields = line.split(" ");
                symlink.add(new Symlink(new File(fields[fields.length - 3]), // file
                        new File(fields[fields.length - 1]) // SymlinkPath
                ));
            }
            return symlink;
        } finally {
            try {
                fr.close();
            } catch (Exception e) {}

            try {
                lnr.close();
            } catch (Exception e) {}
        }
    }

    public Permissions getPermissions(String line) {

        String[] lineArray = line.split(" ");
        String rawPermissions = lineArray[0];

        if (rawPermissions.length() == 10
                && (rawPermissions.charAt(0) == '-'
                || rawPermissions.charAt(0) == 'd' || rawPermissions
                .charAt(0) == 'l')
                && (rawPermissions.charAt(1) == '-' || rawPermissions.charAt(1) == 'r')
                && (rawPermissions.charAt(2) == '-' || rawPermissions.charAt(2) == 'w')) {
            RootTools.log(rawPermissions);

            Permissions permissions = new Permissions();

            permissions.setType(rawPermissions.substring(0, 1));

            RootTools.log(permissions.getType());

            permissions.setUserPermissions(rawPermissions.substring(1, 4));

            RootTools.log(permissions.getUserPermissions());

            permissions.setGroupPermissions(rawPermissions.substring(4, 7));

            RootTools.log(permissions.getGroupPermissions());

            permissions.setOtherPermissions(rawPermissions.substring(7, 10));

            RootTools.log(permissions.getOtherPermissions());

            StringBuilder finalPermissions = new StringBuilder();
            finalPermissions.append(parseSpecialPermissions(rawPermissions));
            finalPermissions.append(parsePermissions(permissions.getUserPermissions()));
            finalPermissions.append(parsePermissions(permissions.getGroupPermissions()));
            finalPermissions.append(parsePermissions(permissions.getOtherPermissions()));

            permissions.setPermissions(Integer.parseInt(finalPermissions.toString()));

            return permissions;
        }

        return null;
    }

    public int parsePermissions(String permission) {
        int tmp;
        if (permission.charAt(0) == 'r')
            tmp = 4;
        else
            tmp = 0;

        RootTools.log("permission " + tmp);
        RootTools.log("character " + permission.charAt(0));

        if (permission.charAt(1) == 'w')
            tmp += 2;
        else
            tmp += 0;

        RootTools.log("permission " + tmp);
        RootTools.log("character " + permission.charAt(1));

        if (permission.charAt(2) == 'x')
            tmp += 1;
        else
            tmp += 0;

        RootTools.log("permission " + tmp);
        RootTools.log("character " + permission.charAt(2));

        return tmp;
    }

    public int parseSpecialPermissions(String permission) {
        int tmp = 0;
        if (permission.charAt(2) == 's')
            tmp += 4;

        if (permission.charAt(5) == 's')
            tmp += 2;

        if (permission.charAt(8) == 't')
            tmp += 1;

        RootTools.log("special permissions " + tmp);

        return tmp;
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

        CommandCapture command = new CommandCapture(0, false, "ls " + file) {
            @Override
            public void output(int arg0, String arg1) {
                RootTools.log(arg1);
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

        RootTools.log("Checking for " + binaryName);

        //Try to use stat first
        try {
            for(final String path : places) {
                CommandCapture cc = new CommandCapture(0, false, "stat " + path + binaryName) {
                    @Override
                    public void commandOutput(int id, String line) {
                        if(line.contains("File: ") && line.contains(binaryName)) {
                            list.add(path);

                            RootTools.log(binaryName + " was found here: " + path);
                        }

                        RootTools.log(line);
                    }
                };

                RootTools.getShell(false).add(cc);
                commandWait(RootTools.getShell(false), cc);

            }

            found = !list.isEmpty();
        } catch (Exception e) {
            RootTools.log(binaryName + " was not found, more information MAY be available with Debugging on.");
        }

        if (!found) {
            RootTools.log("Trying second method");

            for (String where : places) {
                if (RootTools.exists(where + binaryName)) {
                    RootTools.log(binaryName + " was found here: " + where);
                    list.add(where);
                    found = true;
                } else {
                    RootTools.log(binaryName + " was NOT found here: " + where);
                }
            }
        }

        if(!found) {
            RootTools.log("Trying third method");

            try {
                List<String> paths = RootTools.getPath();

                if (paths != null) {
                    for (String path : paths) {
                        if (RootTools.exists(path + "/" + binaryName)) {
                            RootTools.log(binaryName + " was found here: " + path);
                            list.add(path);
                            found = true;
                        } else {
                            RootTools.log(binaryName + " was NOT found here: " + path);
                        }
                    }
                }
            } catch (Exception e) {
                RootTools.log(binaryName + " was not found, more information MAY be available with Debugging on.");
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
            RootTools.log("Checking for Root access");
            InternalVariables.accessGiven = false;

            CommandCapture command = new CommandCapture(Constants.IAG, false, "id") {
                @Override
                public void output(int id, String line) {
                    if (id == Constants.IAG) {
                        Set<String> ID = new HashSet<String>(Arrays.asList(line.split(" ")));
                        for (String userid : ID) {
                            RootTools.log(userid);

                            if (userid.toLowerCase().contains("uid=0")) {
                                InternalVariables.accessGiven = true;
                                RootTools.log("Access Given");
                                break;
                            }
                        }
                        if (!InternalVariables.accessGiven) {
                            RootTools.log("Access Denied?");
                        }
                    }
                }
            };
            Shell.startRootShell().add(command);
            commandWait(Shell.startRootShell(), command);

            return InternalVariables.accessGiven;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isNativeToolsReady(int nativeToolsId, Context context) {
        RootTools.log("Preparing Native Tools");
        InternalVariables.nativeToolsReady = false;

        Installer installer;
        try {
            installer = new Installer(context);
        } catch (IOException ex) {
            if (RootTools.debugMode) {
                ex.printStackTrace();
            }
            return false;
        }

        if (installer.isBinaryInstalled("nativetools")) {
            InternalVariables.nativeToolsReady = true;
        } else {
            InternalVariables.nativeToolsReady = installer.installBinary(nativeToolsId,
                    "nativetools", "700");
        }
        return InternalVariables.nativeToolsReady;
    }








    /**
     * This method checks whether a binary is installed.
     *
     * @param context    the current activity's <code>Context</code>
     * @param binaryName binary file name; appended to /data/data/app.package/files/
     * @return a <code>boolean</code> which indicates whether or not
     *         the binary already exists.
     */
    public boolean isBinaryAvailable(Context context, String binaryName) {
        Installer installer;

        try {
            installer = new Installer(context);
        } catch (IOException ex) {
            if (RootTools.debugMode) {
                ex.printStackTrace();
            }
            return false;
        }

        return (installer.isBinaryInstalled(binaryName));
    }



    /**
     * This method can be used to to check if a process is running
     *
     * @param processName name of process to check
     * @return <code>true</code> if process was found
     * @throws TimeoutException (Could not determine if the process is running)
     */
    public boolean isProcessRunning(final String processName) {

        RootTools.log("Checks if process is running: " + processName);

        InternalVariables.processRunning = false;

        try {
            CommandCapture command = new CommandCapture(0, false, "ps") {
                @Override
                public void output(int id, String line) {
                    if (line.contains(processName)) {
                        InternalVariables.processRunning = true;
                    }
                }
            };
            RootTools.getShell(true).add(command);
            commandWait(RootTools.getShell(true), command);

        } catch (Exception e) {
            RootTools.log(e.getMessage());
        }

        return InternalVariables.processRunning;
    }

    /**
     * This method can be used to kill a running process
     *
     * @param processName name of process to kill
     * @return <code>true</code> if process was found and killed successfully
     */
    public boolean killProcess(final String processName) {
        RootTools.log("Killing process " + processName);

        InternalVariables.pid_list = "";

        //Assume that the process is running
        InternalVariables.processRunning = true;

        try {

            CommandCapture command = new CommandCapture(0, false, "ps") {
                @Override
                public void output(int id, String line) {
                    if (line.contains(processName)) {
                        Matcher psMatcher = InternalVariables.psPattern.matcher(line);

                        try {
                            if (psMatcher.find()) {
                                String pid = psMatcher.group(1);

                                InternalVariables.pid_list += " " + pid;
                                InternalVariables.pid_list = InternalVariables.pid_list.trim();

                                RootTools.log("Found pid: " + pid);
                            } else {
                                RootTools.log("Matching in ps command failed!");
                            }
                        } catch (Exception e) {
                            RootTools.log("Error with regex!");
                            e.printStackTrace();
                        }
                    }
                }
            };
            RootTools.getShell(true).add(command);
            commandWait(RootTools.getShell(true), command);

            // get all pids in one string, created in process method
            String pids = InternalVariables.pid_list;

            // kill processes
            if (!pids.equals("")) {
                try {
                    // example: kill -9 1234 1222 5343
                    command = new CommandCapture(0, false, "kill -9 " + pids);
                    RootTools.getShell(true).add(command);
                    commandWait(RootTools.getShell(true), command);

                    return true;
                } catch (Exception e) {
                    RootTools.log(e.getMessage());
                }
            } else {
                //no pids match, must be dead
                return true;
            }
        } catch (Exception e) {
            RootTools.log(e.getMessage());
        }

        return false;
    }


    /**
     * This will launch the Android market looking for SuperUser
     *
     * @param activity pass in your Activity
     */
    public void offerSuperUser(Activity activity) {
        RootTools.log("Launching Market for SuperUser");
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.noshufou.android.su"));
        activity.startActivity(i);
    }

    /**
     * This will launch the Android market looking for SuperUser, but will return the intent fired
     * and starts the activity with startActivityForResult
     *
     * @param activity    pass in your Activity
     * @param requestCode pass in the request code
     * @return intent fired
     */
    public Intent offerSuperUser(Activity activity, int requestCode) {
        RootTools.log("Launching Market for SuperUser");
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.noshufou.android.su"));
        activity.startActivityForResult(i, requestCode);
        return i;
    }

    //TODO:Command Wait has delay
    private void commandWait(Shell shell, Command cmd) throws Exception {

        while (!cmd.isFinished()) {

            RootTools.log(Constants.TAG, shell.getCommandQueuePositionString(cmd));

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
                    Log.e(Constants.TAG, "Waiting for a command to be executed in a shell that is not executing and not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else if (shell.isExecuting && !shell.isReading) {
                    Log.e(Constants.TAG, "Waiting for a command to be executed in a shell that is executing but not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else {
                    Log.e(Constants.TAG, "Waiting for a command to be executed in a shell that is not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                }
            }

        }
    }
}
