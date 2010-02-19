package org.clapper.editline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class provides a Java interface to the BSD <tt>editline</tt>
 * library, which is available on BSD systems and Mac OS X, and can be
 * installed on most Linux systems. <tt>editline</tt> is a replacement for
 * the GNU Readline library, with a more liberal BSD-style license. Linking
 * <tt>editline</tt> into your code (and, similarly, using this Java
 * interface to it) will not change the license you have assigned to your
 * code.
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

    public interface CompletionHandler
    {
        public String complete(String token, String line, int cursor);
    }

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private EditLine()
    {
        initialized = true;
    }

    /*----------------------------------------------------------------------*\
                                Destructor
    \*----------------------------------------------------------------------*/

    protected void finalize()
    {
        cleanup();
    }

    /*----------------------------------------------------------------------*\
                              Static Methods
    \*----------------------------------------------------------------------*/

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
}

