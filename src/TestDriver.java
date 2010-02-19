
import org.clapper.editline.*;

import java.io.File;
import java.io.IOException;

public class TestDriver implements EditLine.CompletionHandler
{
    TestDriver()
    {
    }

    private String[] completions = new String[]
    {
        "axel",
        "betty",
        "linux",
        "freebsd",
        "winders"
    };

    public String complete(String token, String line, int cursor)
    {
        String completion = null;

        if (cursor > 0)
        {
            for (String s : completions)
            {
                if (s.startsWith(token))
                {
                    completion = s;
                    break;
                }
            }
        }

        return completion;
    }

    void run() throws IOException
    {
        EditLine e = EditLine.init("test", "editrc");
        e.setHistorySize(100);
        e.setHistoryUnique(true);
        e.setCompletionHandler(this);

        File historyFile = new File("test.history");
        if (historyFile.exists())
            e.loadHistory(historyFile);

        e.invokeCommand("bind", "^I", "ed-complete");

        //e.enableSignalHandling(true);
        e.setPrompt("[" + e.historySize() + "] Well? ");
        String line = e.getString();
        while (line != null)
        {
            System.out.println("Got: \"" + line + "\"");
            e.addToHistory(line);
            if (line.equals("h"))
            {
                for (String s: e.getHistory())
                    System.out.println(s);
            }

            e.setPrompt("[" + e.historySize() + "] Well? ");
            line = e.getString();
        }

        e.saveHistory(historyFile);

        e.cleanup();
    }

    public static void main (String[] args) throws IOException
    {
        new TestDriver().run();
    }
}
