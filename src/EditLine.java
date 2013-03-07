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
import java.util.Arrays;

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
 *     el.setPrompt("myprogram? ");
 *     el.setHistorySize(1024);
 *     String line;
 *     while ((line = el.getLine()) != null)
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
                            Constants
    \*----------------------------------------------------------------------*/

    private static final String INITIAL_PROMPT = "? ";
    public static final String VERSION = "0.3.1";

    /*----------------------------------------------------------------------*\
                            Instance Variables
    \*----------------------------------------------------------------------*/

    private boolean initialized = false;
    private CompletionHandler completionHandler = null;
    private boolean historyUnique = false;
    private long handle = 0;
    private String currentPrompt = null;
    private int maxShownCompletions = 30;

    private PossibleCompletionsDisplayer completionsDisplayer =
        new DefaultCompletionDisplayer();

    /*----------------------------------------------------------------------*\
                           Static Initialization
    \*----------------------------------------------------------------------*/

    static
    {
        System.loadLibrary("javaeditline");
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
         * @return the completion strings, or null for none. An array with
         *         one element indicates that there is a single completion
         *         for the token. An empty or null array means no completions.
         *         An array with multiple elements means there are multiple
         *         possible completions.
         */
        public String[] complete(String token, String line, int cursor);
    }

    /**
     * Defines the interface for a class that will display multiple completions,
     * when multiple completions exist for a token. The default handler
     * simply displays the completions, up to a fixed number, one per line.
     * A caller is free to register a handler that is more appropriate for
     * its application.
     */
    public interface PossibleCompletionsDisplayer
    {
        /**
         * Called by EditLine to show the list of possible completions, when
         * multiple completions exist for a token.
         *
         * @param tokens  An <tt>Iterable</tt> of matching strings.
         */
        public void showCompletions(Iterable<String> tokens);
    }

    /**
     * Default completion displayer.
     */
    private class DefaultCompletionDisplayer
        implements PossibleCompletionsDisplayer
    {
        DefaultCompletionDisplayer()
        {
        }

        public void showCompletions(Iterable<String> tokens)
        {
            int max = EditLine.this.getMaxShownCompletions();
            int total = 0;
            System.err.println("\nPossible completions:");
            for (String token : tokens)
            {
                total++;
                if (total <= max)
                    System.err.println(token);
            }

            if (total > max)
                System.err.println("[..." + (total - max) + " more ...]");
        }
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
    public static EditLine init(String program, File initFile)
    {
        EditLine el = new EditLine();
        el.handle = n_el_init(program, el);
        el.source(initFile);
        el.setPrompt(INITIAL_PROMPT);

        // Bind TAB to complete.
        el.invokeCommand("bind", "^I", "ed-complete");
        return el;
    }

    /**
     * Initialize a new <tt>EditLine</tt> instance, without reading a
     * initialization file.
     *
     * @param program   the calling program's name
     *
     * @return a new <tt>EditLine</tt> instance.
     */
    public static EditLine init(String program)
    {
        return init(program, null);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Clean up the <tt>EditLine</tt> environment. Once this method is called,
     * the <tt>EditLine</tt> instance is no longer usable.
     */
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

    /**
     * Get the current completion handler. The completion handler is the
     * object whose <tt>complete()</tt> method is called when the user
     * presses the completion key (usually the TAB key). There is no default
     * completion handler.
     *
     * @return the completion handler, or null if there is no completion handler
     */
    public CompletionHandler getCompletionHandler()
    {
        return completionHandler;
    }

    /**
     * Set (replace) the completion handler. The completion handler is the
     * object whose <tt>complete()</tt> method is called when the user
     * presses the completion key (usually the TAB key). There is no default
     * completion handler.
     *
     * @param handler the completion handler, or null to clear the handler
     */
    public void setCompletionHandler(CompletionHandler handler)
    {
        this.completionHandler = handler;
    }

    /**
     * Clear's the completion handler. This method is just a convenience for:
     *
     * <blockquote><pre>setCompletionHandler(null);</pre></blockquote>
     */
    public void clearCompletionHandler()
    {
        setCompletionHandler(null);
    }

    /**
     * Set (replace) the completions displayer. The completion displayer is the
     * object whose <tt>showCompletions()</tt> method is called when the user
     * presses the completion key (usually the TAB key) and there are multiple
     * completions for the current token. The default displayer just lists up
     * to <tt>getMaxShownCompletions()</tt> matches, one per line.
     *
     * @param handler the handler, or null to clear the handler
     */
    public void setCompletionsDisplayer(PossibleCompletionsDisplayer handler)
    {
        if (handler == null)
            this.completionsDisplayer = new DefaultCompletionDisplayer();
        else
            this.completionsDisplayer = handler;
    }

    /**
     * Clear's the completions displayer. This method is just a convenience for:
     *
     * <blockquote><pre>setCompletionsDisplayer(null);</pre></blockquote>
     */
    public void clearCompletionsDisplayer()
    {
        setCompletionsDisplayer(null);
    }

    /**
     * Set the prompt that is displayed to the user. The default prompt is
     * "? ".
     *
     * @param prompt  the new prompt. Must not be null.
     */
    public void setPrompt(String prompt)
    {
        assert(prompt != null);
        n_el_set_prompt(handle, prompt);
        this.currentPrompt = prompt;
    }

    /**
     * Get the current prompt.
     *
     * @return the prompt. Never null.
     */
    public String getPrompt()
    {
        return currentPrompt;
    }

    /**
     * Prompt the user for a line of input. This method displays the prompt
     * and reads a line of input from the user, allowing the user to edit
     * that line in place, traverse the history, etc.
     *
     * @return the input line, or null on end-of-file.
     */
    public String getLine()
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

    /**
     * Invoke an <tt>editline</tt> command. This method is functionally
     * equivalent to the underlying <tt>editline</tt> library's
     * <tt>el_parse()</tt> function. Consult the documentation for the
     * C <tt>editline</tt> library for a description of what you can pass
     * to this function.
     *
     * @param args  one or more arguments to pass to the underlying
     *              <tt>editline</tt> <tt>el_parse()</tt> function.
     */
    public void invokeCommand(String... args)
    {
        n_el_parse(handle, args, args.length);
    }

    /**
     * <p>Set the size of the history. The history defaults to size 0,
     * which means no history is maintained. Setting the history size to a
     * positive <i>n</i> instructs <tt>EditLine</tt> to maintain a history
     * of, at most, <i>n</i> lines. While entering a line of input, the
     * user can traverse the history (provided the appropriate keys are
     * bound) and select from previously entered input lines.</p>
     *
     * <p>NOTE: Lines of input are <i>not</i> automatically added to the 
     * history. You must call the <tt>addToHistory()</tt> method to put a
     * line into the history. This policy affords the calling program the
     * most control over the history buffer.</p>
     *
     * @param size  the new history size. Must not be negative. A value of
     *              0 disables the history.
     */
    public void setHistorySize(int size)
    {
        assert(size >= 0);
        n_history_set_size(handle, size);
    }

    /**
     * Get the size of the history. The history defaults to size 0, which means
     * no history is maintained. Setting the history size to a positive <i>n</i>
     * instructs <tt>EditLine</tt> to maintain a history of, at most, <i>n</i>
     * lines. While entering a line of input, the user can traverse the
     * history (provided the appropriate keys are bound) and select from 
     * previously entered input lines.
     *
     * @return  the current history size. A value of 0 means that the history
     *          is currently disabled.
     */
    public int getHistorySize()
    {
        return n_history_get_size(handle);
    }

    /**
     * Clear the contents of the history buffer.
     */
    public void clearHistory()
    {
        n_history_clear(handle);
    }

    /**
     * <p>Add a line to the history buffer. If the history buffer is already
     * full, the oldest line is discarded before the new line is added.
     * Lines of input are <i>not</i> automatically added to the history.
     * You must call the <tt>addToHistory()</tt> method to put a line into
     * the history. This policy affords the calling program the most
     * control over the history buffer.</p>
     *
     * <p>If history uniqueness is enabled (see <tt>setHistoryUnique()</tt>)
     * and the input line matches the most recent line in the history, the
     * input line will not be added to the history. Similarly, this method
     * will not add an empty or completely blank line to the history.</p>
     *
     * @param line  the line to add to the history.
     */
    public void addToHistory(String line)
    {
        if ((line != null) && (line.trim().length() > 0))
            n_history_append(handle, line);
    }

    /**
     * Get the most recent line in the history.
     *
     * @return the most recent line, or null if the history is empty.
     */
    public String currentHistoryLine()
    {
        return n_history_current(handle);
    }

    /**
     * Get the contents of the history buffer.
     *
     * @return the lines in the history buffer. An empty array is returned if
     *         the history buffer is empty or not enabled.
     */
    public String[] getHistory()
    {
        return n_history_get_all(handle);
    }

    /**
     * Get the number of lines in the current history buffer.
     *
     * @return the number of lines in the history buffer
     */
    public int historyTotal()
    {
        return n_history_get_size(handle);
    }

    /**
     * Load the contents of a text file, adding its lines to the history
     * buffer.
     *
     * @param f  the file to read
     *
     * @throws FileNotFoundException  if the file doesn't exist.
     * @throws IOException            if the file cannot be read.
     */
    public void loadHistory(File f)
        throws FileNotFoundException,
               IOException
    {
        BufferedReader r = new BufferedReader(new FileReader(f));
        for (String line = r.readLine(); line != null; line = r.readLine())
            addToHistory(line);

        r.close();
    }

    /**
     * Save the history buffer to a file. The file is overwritten, not appended.
     *
     * @param f  the file to write
     *
     * @throws IOException if the file cannot be written.
     */
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

    /**
     * Get the history uniqueness setting. If history uniqueness is enabled
     * and an input line passed to <tt>addToHistory()</tt> matches the most
     * recent line in the history, the input line will not be added to the
     * history. History uniqueness is disabled by default.
     *
     * @return whether or not history uniqueness is enabled
     */
    public boolean getHistoryUnique()
    {
        return historyUnique;
    }

    /**
     * Change the history uniqueness setting. If history uniqueness is enabled
     * and an input line passed to <tt>addToHistory()</tt> matches the most
     * recent line in the history, the input line will not be added to the
     * history. History uniqueness is disabled by default.
     *
     * @param unique  whether or not to enable history uniqueness.
     */
    public void setHistoryUnique(boolean unique)
    {
        n_history_set_unique(handle, unique);
        historyUnique = unique;
    }

    /**
     * Get the maximum number of completions displayed, when more than one
     * string could match a completed string. If there are more than that
     * many matches, the code displays that many, followed by an ellipsis.
     *
     * @return  total to show
     */
    public int getMaxShownCompletions()
    {
        return this.maxShownCompletions;
    }

    /**
     * Set the maximum number of completions displayed, when more than one
     * string could match a completed string. If there are more than that
     * many matches, the code displays that many, followed by an ellipsis.
     *
     * @param total  total to show
     */
    public void setMaxShownCompletions(int total)
    {
        this.maxShownCompletions = total;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void showCompletions(String[] tokens)
    {
        completionsDisplayer.showCompletions(Arrays.asList(tokens));
    }

    private String[] handleCompletion(String token, String line, int cursor)
    {
        String[] result = null;

        if (completionHandler != null)
            result = completionHandler.complete(token, line, cursor);

        return result;
    }

    private void source(File initFile)
    {
        n_el_source(handle, initFile == null ? null : initFile.getPath());
    }

    /*----------------------------------------------------------------------*\
                              Native Methods
    \*----------------------------------------------------------------------*/

    private native static long n_el_init(String program, EditLine editLine);
    private native static void n_el_source(long handle, String path);
    private native static void n_el_end(long handle);
    private native static void n_el_set_prompt(long handle, String prompt);
    private native static String n_el_gets(long handle);
    private native static void n_el_parse(long handle, String[] args, int len);
    private native static int n_history_get_size(long handle);
    private native static void n_history_set_size(long handle, int size);
    private native static void n_history_clear(long handle);
    private native static void n_history_append(long handle, String line);
    private native static String[] n_history_get_all(long handle);
    private native static String n_history_current(long handle);
    private native static void n_history_set_unique(long handle, boolean on);
}
