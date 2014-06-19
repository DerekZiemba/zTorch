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

package com.stericson.RootTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;
import com.stericson.RootTools.internal.RootToolsInternalMethods;
import com.stericson.RootTools.internal.Runner;

public final class RootTools {
	
	

    /**
     * This class is the gateway to every functionality within the RootTools library.The developer
     * should only have access to this class and this class only.This means that this class should
     * be the only one to be public.The rest of the classes within this library must not have the
     * public modifier.
     * <p/>
     * All methods and Variables that the developer may need to have access to should be here.
     * <p/>
     * If a method, or a specific functionality, requires a fair amount of code, or work to be done,
     * then that functionality should probably be moved to its own class and the call to it done
     * here.For examples of this being done, look at the remount functionality.
     */

	/**
	 * June 18, 2014
	 * Derek Ziemba
	 * This implementation of RootTools has been trimed and customized to work with zTorch.  Many things have been cut out.  
	 * Shells that are not root access have all been cut out.
	 * BusyBox methods and applets have been cut out. 
	 * File utilities and management have been removed as well as the ability to write and install things
	 */
	
	
    private static RootToolsInternalMethods rim = null;

	public static final String TAG = "RootTools v3.4";
	public static final int IAG = 2;
	public static boolean accessGiven = false;

    public static void setRim(RootToolsInternalMethods rim) {
        RootTools.rim = rim;
    }

    private static final RootToolsInternalMethods getInternals() {
        if (rim == null) {
            RootToolsInternalMethods.getInstance();
            return rim;
        } else {
            return rim;
        }
    }

    // --------------------
    // # Public Variables #
    // --------------------

    public static boolean debugMode = true;
    public static List<String> lastFoundBinaryPaths = new ArrayList<String>();
    public static String utilPath;

    /**
     * Setting this to false will disable the handler that is used
     * by default for the 3 callback methods for Command.
     *
     * By disabling this all callbacks will be called from a thread other than
     * the main UI thread.
     */
    public static boolean handlerEnabled = true;


    /**
     * Setting this will change the default command timeout.
     *
     * The default is 20000ms
     */
    public static int default_Command_Timeout = 10000;


    // ---------------------------
    // # Public Variable Getters #
    // ---------------------------

    // ------------------
    // # Public Methods #
    // ------------------


    /**
     * This will close either the root shell or the standard shell depending on what you specify.
     *
     * @param root a <code>boolean</code> to specify whether to close the root shell or the standard shell.
     * @throws IOException
     */
    public static void closeRootShell() throws IOException {
            Shell.closeRootShell();
    }


    /**
     * Use this to check whether or not a file exists on the filesystem.
     *
     * @param file String that represent the file, including the full path to the
     *             file and its name.
     * @return a boolean that will indicate whether or not the file exists.
     */
    public static boolean exists(final String file) {
        return getInternals().exists(file);
    }


    /**
     * @param binaryName String that represent the binary to find.
     * @return <code>true</code> if the specified binary was found. Also, the path the binary was
     *         found at can be retrieved via the variable lastFoundBinaryPath, if the binary was
     *         found in more than one location this will contain all of these locations.
     */
    public static boolean findBinary(String binaryName) {
        return getInternals().findBinary(binaryName);
    }


    /**
     * This will return the environment variable PATH
     *
     * @return <code>List<String></code> A List of Strings representing the environment variable $PATH
     */
    public static List<String> getPath() {
        return Arrays.asList(System.getenv("PATH").split(":"));
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param retry a <code>int</code> to indicate how many times the ROOT shell should try to open with root priviliges...
     * @throws TimeoutException
     * @throws RootDeniedException
     * @param    root a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     * @param    timeout an <code>int</code> to Indicate the length of time to wait before giving up on opening a shell.
     * @throws IOException
     */
    public static Shell getShell(boolean root, int timeout, int retry) throws IOException, TimeoutException, RootDeniedException {
          return Shell.startRootShell(timeout);
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @throws TimeoutException
     * @throws RootDeniedException
     * @param    root a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     * @param    timeout an <code>int</code> to Indicate the length of time to wait before giving up on opening a shell.
     * @throws IOException
     */
    public static Shell getShell(boolean root, int timeout) throws IOException, TimeoutException, RootDeniedException {
        return getShell(root, timeout, 3);
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @throws TimeoutException
     * @throws RootDeniedException
     * @param    root a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     * @throws IOException
     */
    public static Shell getShell(boolean root) throws IOException, TimeoutException, RootDeniedException {
        return RootTools.getShell(root, 5000);
    }

    /**
     * Warning: bypasses all checks, possible to get invalid shell or null
     * @return
     */
    public static Shell getExistingShell() {
    	return Shell.getRootShell();
    }
    

    /**
     * @return <code>true</code> if your app has been given root access.
     * @throws TimeoutException if this operation times out. (cannot determine if access is given)
     */
    public static boolean isAccessGiven() {
        return getInternals().isAccessGiven();
    }


    /**
     * @return <code>true</code> if su was found.
     */
    public static boolean isRootAvailable() {
        return findBinary("su");
    }


    /**
     * This will launch the Android market looking for SuperUser
     *
     * @param activity pass in your Activity
     */
    public static void offerSuperUser(Activity activity) {
        getInternals().offerSuperUser(activity);
    }
 
    /**
     * This will launch the Android market looking for SuperUser, but will return the intent fired
     * and starts the activity with startActivityForResult
     *
     * @param activity    pass in your Activity
     * @param requestCode pass in the request code
     * @return intent fired
     */
    public static Intent offerSuperUser(Activity activity, int requestCode) {
        return getInternals().offerSuperUser(activity, requestCode);
    }

    /**
     * Executes binary in a separated process. Before using this method, the binary has to be
     * installed in /data/data/app.package/files/ using the installBinary method.
     *
     * @param context    the current activity's <code>Context</code>
     * @param binaryName name of installed binary
     * @param parameter  parameter to append to binary like "-vxf"
     */
    public static void runBinary(Context context, String binaryName, String parameter) {
        Runner runner = new Runner(context, binaryName, parameter);
        runner.start();
    }

    /**
     * Executes a given command with root access or without depending on the value of the boolean passed.
     * This will also start a root shell or a standard shell without you having to open it specifically.
     * <p/>
     * You will still need to close the shell after you are done using the shell.
     *
     * @param shell   The shell to execute the command on, this can be a root shell or a standard shell.
     * @param command The command to execute in the shell
     * @throws IOException
     */
    public static void runShellCommand(Shell shell, Command command) throws IOException {
        shell.add(command);
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param msg The message to output.
     */
    public static void log(String msg) {
        log(null, msg, 3, null);
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param TAG Optional parameter to define the tag that the Log will use.
     * @param msg The message to output.
     */
    public static void log(String TAG, String msg) {
        log(TAG, msg, 3, null);
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param msg  The message to output.
     * @param type The type of log, 1 for verbose, 2 for error, 3 for debug
     * @param e    The exception that was thrown (Needed for errors)
     */
    public static void log(String msg, int type, Exception e) {
        log(null, msg, type, e);
    }

    /**
     * This method allows you to check whether logging is enabled.
     * Yes, it has a goofy name, but that's to keep it as short as possible.
     * After all writing logging calls should be painless.
     * This method exists to save Android going through the various Java layers
     * that are traversed any time a string is created (i.e. what you are logging)
     *
     * Example usage:
     * if(islog) {
     *     StrinbBuilder sb = new StringBuilder();
     *     // ...
     *     // build string
     *     // ...
     *     log(sb.toString());
     * }
     *
     *
     * @return true if logging is enabled
     */
    public static boolean islog() {
        return debugMode;
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param TAG  Optional parameter to define the tag that the Log will use.
     * @param msg  The message to output.
     * @param type The type of log, 1 for verbose, 2 for error, 3 for debug
     * @param e    The exception that was thrown (Needed for errors)
     */
    public static void log(String TAG, String msg, int type, Exception e) {
        if (msg != null && !msg.equals("")) {
            if (debugMode) {
                if (TAG == null) {
                    TAG = RootTools.TAG;
                }

                switch (type) {
                    case 1:
                        Log.v(TAG, msg);
                        break;
                    case 2:
                        Log.e(TAG, msg, e);
                        break;
                    case 3:
                        Log.d(TAG, msg);
                        break;
                }
            }
        }
    }



}
