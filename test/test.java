import org.clapper.editline.*;

public class test
{
    public static void main (String[] args)
    {
        EditLine e = EditLine.init("test", "editrc");
        e.setPrompt("[0] Well? ");
        String line = e.getString();
        while (line != null)
        {
            System.out.println("Got: \"" + line + "\"");
            e.addToHistory(line);
            e.setPrompt("[" + e.historySize() + "] Well? ");
            line = e.getString();
        }

        e.cleanup();
    }
}
