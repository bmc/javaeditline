package org.clapper.editline;

public class EditLine
{
    static
    {
        System.loadLibrary("EditLine");
    }

    private String prompt = "? ";
    private boolean initialized = false;

    private EditLine()
    {
        initialized = true;
        setPrompt(prompt);
    }

    protected void finalize()
    {
        cleanup();
    }

    public static EditLine init(String program)
    {
        n_el_init(program);
        return new EditLine();
    }

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

    public void setPrompt(String _prompt)
    {
        n_el_set_prompt(_prompt);
    }

    public String getString()
    {
        return n_el_gets();
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

    /**
     * Get the editing information about the current line.
     */
    public LineInfo getLineInfo()
    {
        LineInfo info = new LineInfo();
        n_el_get_lineinfo(info);
        return info;
    }

    public String[] getHistory()
    {
        return n_history_get_all();
    }

    public native static void n_el_init(String program);
    public native static void n_el_end();
    public native static void n_el_set_prompt(String prompt);
    public native static void n_el_get_lineinfo(LineInfo info);
    public native static String n_el_gets();
    public native static int n_history_get_size();
    public native static void n_history_set_size(int size);
    public native static void n_history_clear();
    public native static void n_history_append(String line);
    public native static String[] n_history_get_all();
}

