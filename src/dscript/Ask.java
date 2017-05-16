package dscript;

import java.io.*;
/*io*/

public class Ask
{


    static boolean DEBUG=false;

    public static String ask(int entrytype, Output OUT, InputStream IN)
    {


        if (entrytype == Var.STR)
        {
            return Ask.ask(IN);
        }

        boolean do_it=true;
        String s="";

        if (StatementProcessor.DEBUG)
        {
            OUT.println("requesting "+entrytype,2);
        }

        do
        {

            s = Ask.ask(IN);

            if (entrytype == Var.INT)
            {

                try
                {
                    long l=Long.parseLong(s);
                    do_it=false;
                }
                catch(Exception e)
                {
                    do_it=true;
                    OUT.println("Dustyscript requested an integer,\n\t..'"+s+"' is not an integer"); OUT.print("ds<<- ");
                    continue;
                }

            }

            if (entrytype==Var.DEC)
            {

                try
                {
                    double d= Double.parseDouble(s);
                    do_it=false;
                }
                catch(Exception e)
                {
                    do_it=true;
                    OUT.println("Dustyscript requested a decimal,\n\t..'"+s+"' is not a decimal");OUT.print("ds<<- ");
                    continue;
                }

            }

            if (entrytype==Var.YES_NO)
            {
                String vl=s.toLowerCase();

                if (vl.equals("yes")|vl.equals("true")|vl.equals("false")|vl.equals("no"))
                {
                    do_it=false;
                }
                else
                {
                    do_it=true;
                    OUT.println("Dustyscript requested a yes_no,\n\t..'"+s+"' is not a yes_no");OUT.print("ds<<- ");
                    continue;
                }

            }

            if (entrytype==Var.CHR)
            {

                if (s.length() < 1)
                {
                    OUT.println("Dustyscript requested a character\n\t..you put in nothing");OUT.print("ds<<-- ");
                    continue;
                }

                s= "" + s.charAt(0);
                do_it=false;

            }

        }while (do_it);

        return s;

    }


    public static String ask(InputStream IN)
    {

        try
        {

            InputStreamReader isr = new InputStreamReader(IN);
            String s="";

            while (true)
            {

                int c = isr.read();
                char cc = (char)c;

                if ((cc == '\n')|(cc=='\r'))
                {
                    return s;
                }

                s=s+cc;

            }

        }
        catch(Exception e)
        {
            return new String("\"error reading\"");
        }

    }


    public static String ask()
    {
        return ask(System.in);
    }


    public static String ask(int entry)
    {

        try
        {
            Output OUT = new Output();
            return ask(entry,OUT,System.in);
        }
        catch (Exception e)
        {
            return "/error in ask/";
        }


    }


}


