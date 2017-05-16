package widget;

import dscript.connect.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class DGridPanel extends Dustyable{

private Panel dgrid; private Vector comps; 
private int vert=1; private int horiz=1;
private boolean packed=false;


public DGridPanel()
	{
	dgrid=new Panel();
	comps=new Vector();
	}

public Component getAsComponent() {
				if (!packed) {processCommand("pack");}
				return dgrid;}
public boolean isComponent(){return true;}



public boolean processCommand(String command, String[] args)
	{
	if (command.equalsIgnoreCase("layout_vertical"))
		{
		if (args.length < 1){return false;}
		try{
		   vert = Integer.parseInt(args[0]);
		   }
		   catch(Exception e)
			{vert=-1;}
		if (vert < 0) {vert = 1;return false;}
		return true;
		}

	if (command.equalsIgnoreCase("layout_horizontal"))
		{
		if (args.length < 1){return false;}
		try{
		   horiz = Integer.parseInt(args[0]);
		   }
		   catch(Exception e)
			{horiz=-1;}
		if (horiz < 0) {horiz = 1;return false;}
		return true;
		}
	if (command.equals("setcolor"))
		{
		if (args.length == 1)
			{
			if (args[0].equalsIgnoreCase("red"))
				{
				dgrid.setBackground(Color.red);
				}
			else if (args[0].equalsIgnoreCase("black"))
				{
				dgrid.setBackground(Color.black);
				}
			else if (args[0].equalsIgnoreCase("gray"))
				{
				dgrid.setBackground(Color.gray);
				}
			else if (args[0].equalsIgnoreCase("white"))
				{
				dgrid.setBackground(Color.white);
				}
			else {return false;}
			return true;
			}
		if (args.length != 3) {return false;}
		try{
		int i = Integer.parseInt(args[0]);
		int j = Integer.parseInt(args[1]);
		int k = Integer.parseInt(args[2]);
		dgrid.setBackground(new Color(i,j,k));
		return true;
		}
		catch(Exception e){}
		return false;
		}
	return false;
	}

public boolean processCommand(String command)
	{

	if (command.equalsIgnoreCase("clear"))
		{
		dgrid.setVisible(false);
		comps.removeAllElements();
		packed = false;
		return true;
		}

	if (command.equalsIgnoreCase("pack"))
		{
		dgrid=new Panel(); dgrid.setLayout(new GridLayout(vert,horiz));
		for (int i=0; i<comps.size(); i++)
			{
			Component comp = (Component)comps.elementAt(i);
			if ((i>0) && (i<(comps.size()-1)))
				{
				Component two = (Component)comps.elementAt((i-1));
				if (comp == two && dscript.StatementProcessor.DEBUG) {System.err.println("Component added twice");}
				}
			dgrid.add(comp);
			}
		if (dscript.StatementProcessor.DEBUG)
		{
		System.err.println(comps.size());
		System.err.println("horiz=="+horiz);
		System.err.println("vert=="+vert);
		}
		packed=true;
		return true;
		}
	return false;
	}

public boolean processDustyable(Dustyable d)
	{
	if (d instanceof DKeyListener)
	{
		dgrid.addKeyListener((KeyListener)d);
		return true;
	}
	if (!d.isComponent() && dscript.StatementProcessor.DEBUG){System.err.println("Dustyable not a component:"+d.getClass().getName());}
	if (!d.isComponent() | d.isOnlyContainer())
		{
		return false;
		}
	
	if (dscript.StatementProcessor.DEBUG)
	{System.err.println("DGrid adding:"+d.getClass().getName());}
	comps.addElement(d.getAsComponent());
	return true;
	}


/*finis*/
}
