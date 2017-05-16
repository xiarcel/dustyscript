package dscript;

/*this can be used to re-direct stdout/stderr to a Frame w/ a textarea..
Debugging..*/


   import java.io.*;
   import java.awt.*;
   import java.awt.event.*;

    public class TextAreaConsole extends OutputStream{
   
      private TextArea ta;
      private Panel p;
   	
      public TextAreaConsole()
      {
	ta = new TextArea("",20,60);
	p=new Panel();p.setSize(ta.getPreferredSize());
	p.add(ta);
      }

	public TextAreaConsole(int c, int r)
	{
	ta= new TextArea("",c,r,TextArea.SCROLLBARS_VERTICAL_ONLY);
	p=new Panel(); p.setSize(ta.getPreferredSize());
	p.add(ta);
	}

   
      public Panel asPanel()
      {
         return p;
      }
   
     public Frame asFrame()
     {
     return asFrame("");
     }

     public Frame asFrame(String lab)
     {
	Frame f=new Frame(lab);
     f.setSize(p.getPreferredSize());
     f.add(p);
     return f;
     }

      public void println(String s)
      {
         ta.append(s);
      }
   
      public void write(int i)
      {
         char c = (char)i;
         print(""+c);
      }
      public void write(float f)
      {
         print(new Float(f));
      }
      public void write(long l)
      {
         print(new Long(l));
      }
      public void write(char[] c)
      {
         print(""+c);
      }
      public void write(char c)
      {
         print(new Character(c));
      }
      public void write(String s)
      {
         print(s);
      }
   
      public void write (double d)
      {
         print(new Double(d));
      }
   
      public void print(Object s)
      {	
      ta.append(s.toString());
      }
   
      public void clear()
      {
         ta.setText("");
      }
   	
	public TextArea getTextArea(){return ta;}

   /*end*/
   }


