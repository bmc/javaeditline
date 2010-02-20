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

import org.clapper.editline.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestDriver implements EditLine.CompletionHandler
{
    TestDriver()
    {
    }

    private String[] POSSIBLE_COMPLETIONS = new String[]
    {
        "alice",
        "betty",
        "bob",
        "linux",
        "freebsd",
        "winders"
    };

    private String[] TO_ARRAY_PROTOTYPE = new String[0];

    public String[] complete(String token, String line, int cursor)
    {
        String[] result = null;

        if (token.length() == 0)
            result = POSSIBLE_COMPLETIONS;

        else
        {
            ArrayList<String> completions = new ArrayList<String>();

            for (String s : POSSIBLE_COMPLETIONS)
            {
                if (s.startsWith(token))
                    completions.add(s);
            }

            if (completions.size() > 0)
                result = completions.toArray(TO_ARRAY_PROTOTYPE);
        }

        return result;
    }

    void run() throws IOException
    {
        EditLine e = EditLine.init("test", new File("editrc"));
        e.setHistorySize(100);
        e.setHistoryUnique(true);
        e.setCompletionHandler(this);

        File historyFile = new File("test.history");
        if (historyFile.exists())
            e.loadHistory(historyFile);

        e.invokeCommand("bind", "^I", "ed-complete");

        //e.enableSignalHandling(true);
        e.setPrompt("[" + e.historyTotal() + "] Well? ");
        String line;
        while ((line = e.getLine()) != null)
        {
            String tline = line.trim();
            if (tline.equals("!!"))
            {
                line = e.currentHistoryLine();
                if (line == null)
                {
                    System.out.println("Empty history.");
                    continue;
                }

                tline = line.trim();
            }

            e.addToHistory(line);
            if (tline.equals("h"))
            {
                for (String s: e.getHistory())
                    System.out.println(s);
            }

            System.out.println("Got: \"" + line + "\"");
            e.setPrompt("[" + e.historyTotal() + "] Well? ");
        }

        e.saveHistory(historyFile);

        e.cleanup();
    }

    public static void main (String[] args) throws IOException
    {
        new TestDriver().run();
    }
}
