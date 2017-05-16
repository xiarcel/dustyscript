package widget;

import dscript.connect.*;
import java.awt.*;
import java.awt.event.*;

public class DSinglePanel extends Dustyable{

private Component component;
private Panel dbord;
private boolean packed=false;

private Color blk = new Color(0,0,0);
private Color wht = new Color(255,255,255);
private Color gry = new Color(192,192,192);
private Color use = wht;

public DSinglePanel()
{
dbord=new Panel(); 
}

public boolean isComponent(){return true;}
public Component getAsComponent() {if (!packed) {processCommand("pack");}
						
						return dbord;
						}

public boolean processCommand(String command)
	{
	if (command.equals("pack"))
		{
		Panel p =new Panel();
		p.add(component);		
		dbord.setLayout(new BorderLayout());
		dbord.setBackground(use);
		dbord.add(p,BorderLayout.CENTER);
		dbord.setVisible(true);
		packed = true;
		return true;
		}
	return false;
	}

public boolean processCommand(String command, String[] args)
	{
	if (command.equals("force_size"))
		{
		if (args.length != 2) {return false;}
		try{
		   int i = Integer.parseInt(args[0]);
		   int j = Integer.parseInt(args[1]);
		   dbord.setSize(new Dimension(i,j));
		   }
		   catch(Exception e){return false;}
		return true;
		}


	
	if (command.equals("setcolor"))
		{
		if (args.length == 1)
			{
			if (args[0].equalsIgnoreCase("red"))
				{
				use=Color.red;
				}
			else if (args[0].equalsIgnoreCase("black"))
				{

				use=blk;
				}
			else if (args[0].equalsIgnoreCase("gray"))
				{
				use=gry;
				}
			else if (args[0].equalsIgnoreCase("white"))
				{
				use=wht;
				}
			else {return false;}
			return true;
			}
		if (args.length != 3) {return false;}
		try{
		int i = Integer.parseInt(args[0]);
		int j = Integer.parseInt(args[1]);
		int k = Integer.parseInt(args[2]);
		use =new Color (i,j,k);
		return true;
		}
		catch(Exception e){}
		return false;
		}
	return false;
	}

public boolean processDustyable(Dustyable d)
	{
	if (d instanceof DKeyListener)
	{
		dbord.addKeyListener((KeyListener)d);
		return true;
	}
	if (!d.isComponent() | d.isOnlyContainer())
		{
		return false;
		}
	if (dscript.StatementProcessor.DEBUG)
	{
	System.err.println("DSingle setting component to:"+d.getClass().getName());
	}
	component = d.getAsComponent();
	packed = false;
	return true;
	}

/*finis*/
}


