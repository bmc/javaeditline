/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php.

  Copyright (c) 2010 Brian M. Clapper
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
  
  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the names "clapper.org", "Java EditLine", nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/
  
package org.clapper.editline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <p>This class provides a Java interface to the BSD Editline library,
 * which is available on BSD systems and Mac OS X, and can be installed on
 * most Linux systems. Editline is a replacement for the GNU Readline
 * library, with a more liberal BSD-style license. Linking Editline into
 * your code (and, similarly, using this Java interface to it) will not
 * change the license you have assigned to your code.</p>
 *
 * <p>An <tt>EditLine</tt> object is instantiated via a factory method.
 * The finalizer for the returned object restores the terminal environment,
 * if you don't do it yourself--but you're better off doing it yourself.
 * Here's the typical usage pattern:</p>
 *
 * <blockquote><pre>
 * EditLine el = EditLine.init("myprogram")
 * try
 * {
 *     el.setPrompt("myprogram> ");
 *     String line;
 *     while ((line = el.getString()) != null)
 *     {
 *         // ...
 *     }
 * }
 * finally
 * {
 *     el.cleanup();
 * }
 * </pre></blockquote>
 *
 * <h2>Restrictions</h2>
 *
 * This Java wrapper does not expose all the functionality of the underlying
 * Editline library.
 *
 * <ul>
 *  <li> This wrapper does not support the use of alternate file descriptors.
 *       All <tt>EditLine</tt> instances use standard input, output and error.
 *       While this library theoretically permits multiple <tt>EditLine</tt>
 *       instances to be constructed, all such instances use the same file
 *       descriptors (which limits the utility of having multiple instances).
 *       In practice, this is generally not a problem.
 *  <li> This class does not currently expose the Editline library's 
 *       tokenizer functionality (e.g., the <tt>tok_init()</tt>,
 *       <tt>tok_line()</tt>, <tt>tok_str()</tt>, <tt>tok_reset()</tt> and
 *       <tt>tok_end()</tt> functions).
 *  <li> Signal handling is currently omitted, as it doesn't play well with
 *       the JVM.
 *  <li> Certain Editline functions are not currently exposed, among
 *       them:
 *       <ul>
 *         <li> <tt>el_insertstr()</tt>
 *         <li> <tt>el_deletestr()</tt>
 *         <li> <tt>el_set()</tt>
 *         <li> <tt>el_get()</tt>
 *         <li> <tt>el_getc()</tt>
 *         <li> <tt>el_reset()</tt>
 *         <li> <tt>el_push()</tt>
 *       </ul>
 * </ul>
 */
public class EditLine
{
    /*----------------------------------------------------------------------*\
                            Instance Variables
    \*----------------------------------------------------------------------*/

    private static final String INITIAL_PROMPT = "? ";

    private boolean initialized = false;
    private CompletionHandler completionHandler = null;
    private boolean historyUnique = false;
    private long handle = 0;

    /*----------------------------------------------------------------------*\
                           Static Initialization
    \*----------------------------------------------------------------------*/

    static
    {
        System.loadLibrary("EditLine");
    }

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Any class wishing to act as a completion handler for EditLine must
     * implement this interface.
     */
    public interface CompletionHandler
    {
        /**
         * Called by EditLine in response to a token completion request
         * (typically bound to TAB).
         *
         * @param token  the token being completed. Can be "".
         * @param line   the current line being completed
         * @param cursor index where cursor is, within the line (not within
         *               the token)
         *
         * @return the completion string, or null for none
         */
        public String complete(String token, String line, int cursor);
    }

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new EditLine instance.
     */
    private EditLine()
    {
        initialized = true;
    }

    /*----------------------------------------------------------------------*\
                                Destructor
    \*----------------------------------------------------------------------*/

    /**
     * Finalizer.
     */
    protected void finalize()
    {
        cleanup();
    }

    /*----------------------------------------------------------------------*\
                              Static Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initialize a new <tt>EditLine</tt> instance.
     *
     * @param program   the calling program's name
     * @param initFile  an intialization file of Editline directives,
     *                  or null for none. This file is passed directly to
     *                  the underlying Editline library's
     *                  <tt>el_source()</tt> function. Consult the manual
     *                  page for Editline for details.
     *
     * @return a new <tt>EditLine</tt> instance.
     */
    public static EditLine init(String program, String initFile)
    {
        EditLine el = new EditLine();
        el.handle = n_el_init(program, el);
        n_el_source(el.handle, initFile);
        el.setPrompt(INITIAL_PROMPT);
        return el;
    }

    public static EditLine init(String program)
    {
        return init(program, null);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    public synchronized void cleanup()
    {
        if (initialized)
        {
            try
            {
                n_el_end(handle);
            }

            finally
            {
                initialized = false;
            }
        }
    }

    public CompletionHandler getCompletionHandler()
    {
        return completionHandler;
    }

    public void setCompletionHandler(CompletionHandler handler)
    {
        this.completionHandler = handler;
    }

    public void clearCompletionHandler()
    {
        setCompletionHandler(null);
    }

    public void setPrompt(String _prompt)
    {
        n_el_set_prompt(handle, _prompt);
    }

    public String getString()
    {
        String s = n_el_gets(handle);
        if (s != null)
        {
            int len = s.length();
            if ((len > 0) && (s.charAt(len - 1) == '\n'))
                s = s.substring(0, len - 1);
        }

        return s;
    }

    public void invokeCommand(String... args)
    {
        n_el_parse(handle, args, args.length);
    }

    public void setHistorySize(int size)
    {
        n_history_set_size(handle, size);
    }

    public int getHistorySize()
    {
        return n_history_get_size(handle);
    }

    public void clearHistory()
    {
        n_history_clear(handle);
    }

    public void addToHistory(String line)
    {
        if ((line != null) && (line.trim().length() > 0))
            n_history_append(handle, line);
    }

    public String[] getHistory()
    {
        return n_history_get_all(handle);
    }

    public int historySize()
    {
        return n_history_get_size(handle);
    }

    public void loadHistory(File f)
        throws FileNotFoundException,
               IOException
    {
        if (! f.exists())
            throw new FileNotFoundException(f.getPath());

        BufferedReader r = new BufferedReader(new FileReader(f));
        for (String line = r.readLine(); line != null; line = r.readLine())
            addToHistory(line);

        r.close();
    }

    public void saveHistory(File f)
        throws IOException
    {
        String[] history = getHistory();
        if ((history != null) && (history.length > 0))
        {
            FileWriter w = new FileWriter(f);
            for (String line : history)
                w.write(line + "\n");
            w.close();
        }
    }

    public boolean getHistoryUnique()
    {
        return historyUnique;
    }

    public void setHistoryUnique(boolean unique)
    {
        n_history_set_unique(handle, unique);
        historyUnique = unique;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    public String handleCompletion(String token, String line, int cursor)
    {
        String result = "";

        if (completionHandler != null)
            result = completionHandler.complete(token, line, cursor);

        return result;
    }

    /*----------------------------------------------------------------------*\
                              Native Methods
    \*----------------------------------------------------------------------*/

    private native static long n_el_init(String program, EditLine editLine);
    private native static void n_el_source(long handle, String path);
    private native static void n_el_end(long handle);
    private native static void n_el_set_prompt(long handle, String prompt);
    private native static String n_el_gets(long handle);
    private native static int n_history_get_size(long handle);
    private native static void n_history_set_size(long handle, int size);
    private native static void n_history_clear(long handle);
    private native static void n_history_append(long handle, String line);
    private native static String[] n_history_get_all(long handle);
    private native static void n_history_set_unique(long handle, boolean on);
    private native static void n_el_parse(long handle, String[] args, int len);
}

