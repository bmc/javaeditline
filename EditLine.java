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

    public static EditLine init(String program, String initFile)
    {
        n_el_init(program);
        n_el_source(initFile);
        return new EditLine();
    }

    public static EditLine init(String program)
    {
        return init(program, null);
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

    public int historySize()
    {
        return n_history_get_size();
    }

    private native static void n_el_init(String program);
    private native static void n_el_source(String path);
    private native static void n_el_end();
    private native static void n_el_set_prompt(String prompt);
    private native static void n_el_get_lineinfo(LineInfo info);
    private native static String n_el_gets();
    private native static int n_history_get_size();
    private native static void n_history_set_size(int size);
    private native static void n_history_clear();
    private native static void n_history_append(String line);
    private native static String[] n_history_get_all();
}

