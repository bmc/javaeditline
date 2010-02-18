package org.clapper.editline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class EditLine
{
    /*----------------------------------------------------------------------*\
                            Instance Variables
    \*----------------------------------------------------------------------*/

    private static final String INITIAL_PROMPT = "? ";
    private boolean initialized = false;
    private CompletionHandler completionHandler = null;
    private boolean historyUnique = false;

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
        EditLine editLine = new EditLine();
        n_el_init(program, editLine);
        n_el_source(initFile);
        editLine.setPrompt(INITIAL_PROMPT);
        return editLine;
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
                n_el_end();
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
        n_el_set_prompt(_prompt);
    }

    public String getString()
    {
        String s = n_el_gets();
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
        n_history_set_size(size);
    }

    public int getHistorySize()
    {
        return n_history_get_size();
    }

    public void clearHistory()
    {
        n_history_clear();
    }

    public void addToHistory(String line)
    {
        n_history_append(line);
    }

    public String[] getHistory()
    {
        return n_history_get_all();
    }

    public int historySize()
    {
        return n_history_get_size();
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

    public void enableSignalHandling(boolean on)
    {
        n_el_trap_signals(on);
    }

    public boolean getHistoryUnique()
    {
        return historyUnique;
    }

    public void setHistoryUnique(boolean unique)
    {
        n_history_set_unique(unique);
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

    private native static void n_el_trap_signals(boolean onOff);
    private native static void n_el_init(String program, EditLine editLine);
    private native static void n_el_source(String path);
    private native static void n_el_end();
    private native static void n_el_set_prompt(String prompt);
    private native static String n_el_gets();
    private native static int n_history_get_size();
    private native static void n_history_set_size(int size);
    private native static void n_history_clear();
    private native static void n_history_append(String line);
    private native static String[] n_history_get_all();
    private native static void n_history_set_unique(boolean on);
}
