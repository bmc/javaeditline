package org.clapper.editline;

public class LineInfo
{
    private String line;
    private int    cursor;

    public LineInfo()
    {
    }

    public LineInfo(String _line, int _cursor)
    {
        this.line = _line;
        this.cursor = _cursor;
    }

    public int getCursor()
    {
        return cursor;
    }

    public void setCursor(int _cursor)
    {
        this.cursor = _cursor;
    }

    public String getLine()
    {
        return line;
    }

    public void setLine(String _line)
    {
        this.line = _line;
    }
}

