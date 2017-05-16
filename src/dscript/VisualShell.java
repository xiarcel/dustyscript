package dscript;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class VisualShell extends Thread implements KeyListener
{

    private TextAreaConsole tac = new TextAreaConsole();

    private PrintWriter write_to_in;
    private String input_cache = "";
    private TextArea ta;

    private Frame fram;
    private Panel pan;

    private boolean as_panel;

    public VisualShell(PrintWriter IN)
    {
        write_to_in = IN;
        fram = tac.asFrame("Dustyscript GUI Shell");
        ta = tac.getTextArea();
    }

    public VisualShell(PrintWriter IN, TextAreaConsole TAC, boolean aspanel)
    {
        as_panel = aspanel;
        write_to_in = IN;
        tac = TAC;
        ta = tac.getTextArea();
        pan = new Panel();
        pan.setSize(ta.getPreferredSize());
        pan.add(ta);
        pan.setVisible(true);
    }

    public void run()
    {
        if (!as_panel)
        {
            setup_frame();
        }
        else
        {
            setup_panel();
        }
    }

    public void setup_panel()
    {
        ta.addKeyListener(this);
    }

    public Panel getPanel() throws Exception
    {
        if (pan == null)
        {
            throw new Exception();
        }
        return pan;
    }

    public Frame getFrame() throws Exception
    {
        if (fram == null)
        {
            throw new Exception();
        }
        return fram;
    }

    public TextArea getTextArea()
    {
        return tac.getTextArea();
    }

    public void setup_frame()
    {

        ta.addKeyListener(this);

        fram.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we)
            {
                System.exit(0);
            }
        });
        fram.pack();
        fram.setVisible(true);

    }

    public OutputStream getOutputStream()
    {
        return tac;
    }

    public void keyPressed(KeyEvent ke)
    {
        int i = ke.getKeyCode();
        if (i == KeyEvent.VK_SHIFT)
        {
            return;
        }
        if (i == KeyEvent.VK_ALT)
        {
            return;
        }
        if (i == KeyEvent.VK_CONTROL)
        {
            return;
        }
        if (i == KeyEvent.VK_BACK_SPACE)
        {
            if (input_cache.length() >= 1)
            {
                input_cache = input_cache.substring(0, input_cache.length() - 1);
            }
            return;
        }

        if (i == KeyEvent.VK_ENTER)
        {
		
        
	    while (input_cache.endsWith("\n")) {input_cache=input_cache.substring(0,input_cache.length()-1);}
	    while (input_cache.startsWith("\n")) {input_cache=input_cache.substring(1,input_cache.length());}
	    //new Thread(new ThreadPrinter(input_cache,write_to_in)).start();
	    write_to_in.println(input_cache);
            write_to_in.flush();
	    //try {sleep(200);} catch(Exception e){}
            input_cache = "";
	    return;
        }
        else
        {
            char c = ke.getKeyChar();
            if ((int) c != 0)
            {
                input_cache = input_cache + c;
            }
        }
    }

    public void keyReleased(KeyEvent ke)
    {

    }

    public void keyTyped(KeyEvent ke)
    {

    }

    public static void main(String[] args) throws Exception
    {
        PipedInputStream pis = null;
        PrintWriter pw = null;
        try
        {
            pis = new PipedInputStream();
            pw = new PrintWriter(new PipedOutputStream(pis));
        }
        catch (Exception e)
        {
            System.err.println("Failed at constructing streams");
        }

        VisualShell vs = new VisualShell(pw);
        new Thread(vs).start();
        try
        {
            Thread.sleep(1100);
        }
        catch (Exception e)
        {
        }
        OutputStream os = vs.getOutputStream();
        Output OUT = new Output(os, os, System.out);

        if (args.length < 1)
        {
            ConsoleDusty cd = new ConsoleDusty(OUT, pis);
            cd.run();
        }
        else
        {
            String src = "";
            String file = new String(System.getProperty("user.dir") + System.getProperty("file.separator") + "users" + System.getProperty("file.separator") + args[0]);

            try
            {
                BufferedReader br = new BufferedReader(new FileReader(new File(file)));

                String s;
                while ((s = br.readLine()) != null)
                {
                    src = src + s + "\n";
                }
                br.close();
            }
            catch (Exception e)
            {
                System.err.println("Fileread error");
                System.exit(0);
            }

            VarContainer vc = new VarContainer(OUT);
            ActionContainer ac = new ActionContainer(OUT);
            ThingTypeContainer ttc = new ThingTypeContainer(OUT, pis);
            StatementProcessor sp = new StatementProcessor(src, vc, ac, ttc, OUT, pis);

            // Iterate through remaining arguments.
            for (int i = 1; i < args.length; ++i)
            {
                String currArg = args[i];
                if (currArg.equalsIgnoreCase("-quiet"))
                {
                    sp.setUserDebug(false);
                }
                else if (currArg.equalsIgnoreCase("-benchmark"))
                {
                    StatementProcessor.BENCHMARK = true;
                }
            }

            sp.run();
        }

        OUT.println("Please close the frame now..\n\n.. and thank you for using Dustyscript!");
        System.err.println("Dustyscript is finished, thank you.");
    }
}

/*
class ThreadPrinter extends Thread{
	
	private String mssg; private PrintWriter pwrite;
	ThreadPrinter(String m, PrintWriter p)
	{
		pwrite=p; mssg=m;
	}
	
	public void run()
	{	try {sleep(150);} catch(Exception e){}
		pwrite.println(mssg); pwrite.flush();
	}
	
}
*/

 