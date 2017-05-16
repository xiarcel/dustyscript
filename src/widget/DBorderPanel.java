package widget;

import dscript.connect.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class DBorderPanel extends Dustyable{

private Component nth,sth,est,wst,ctr;
private Panel dbord;
private boolean packed=false;
private Dimension non =new Dimension(0,0);

public DBorderPanel()
{
dbord=new Panel(); 
nth=new Panel();nth.setSize(non);
sth=new Panel();sth.setSize(non);
est=new Panel();est.setSize(non); 
wst=new Panel();wst.setSize(non); 
ctr=new Panel();ctr.setSize(non);
}

public boolean isComponent(){return true;}
public Component getAsComponent() {
		if (!packed){processCommand("pack");}
		return dbord;}

public boolean processCommand(String command)
	{
	if (command.equalsIgnoreCase("pack"))
		{
		dbord=new Panel();dbord.setLayout(new BorderLayout());
		dbord.add(nth,BorderLayout.NORTH);
		dbord.add(sth,BorderLayout.SOUTH);
		dbord.add(est,BorderLayout.EAST);
		dbord.add(wst,BorderLayout.WEST);
		dbord.add(ctr,BorderLayout.CENTER);
		packed=true;
		dbord.setVisible(true);
		return true;
		}
	if (command.equalsIgnoreCase("clear"))
		{
		nth=sth=est=wst=ctr=new Panel();
		dbord=new Panel();
		packed=false;
		return true;
		}
	return false;
	}

public boolean processCommand(String command, String[] args)
	{
	if (command.equals("setcolor"))
		{
		if (args.length == 1)
			{
			if (args[0].equalsIgnoreCase("red"))
				{
				dbord.setBackground(Color.red);
				}
			else if (args[0].equalsIgnoreCase("black"))
				{
				dbord.setBackground(Color.black);
				}
			else if (args[0].equalsIgnoreCase("gray"))
				{
				dbord.setBackground(Color.gray);
				}
			else if (args[0].equalsIgnoreCase("white"))
				{
				dbord.setBackground(Color.white);
				}
			else {return false;}
			return true;
			}
		if (args.length != 3) {return false;}
		try{
		int i = Integer.parseInt(args[0]);
		int j = Integer.parseInt(args[1]);
		int k = Integer.parseInt(args[2]);
		dbord.setBackground(new Color(i,j,k));
		return true;
		}
		catch(Exception e){}
		return false;
		}
	return false;
	}

public boolean processDustyable(Dustyable d, String[] args)
	{
	if (dscript.StatementProcessor.DEBUG){
	System.err.println("Dborder adding::"+d.getClass().getName()+"to "+args[0]);
	}
	
	if (d instanceof DKeyListener)
	{
		dbord.addKeyListener((KeyListener)d);
		return true;
	}
	if (args.length < 1){return false;}
	if (!d.isComponent() | d.isOnlyContainer())
		{
		return false;
		}
	Component c = d.getAsComponent();
	boolean valid = false;
	if (args[0].equalsIgnoreCase("north")) {nth= c;valid=true;}
	else if (args[0].equalsIgnoreCase("south")) {sth= c; valid=true;}
	else if (args[0].equalsIgnoreCase("east"))  {est= c; valid=true;}
	else if (args[0].equalsIgnoreCase("west"))  {wst= c; valid=true;}
	else if (args[0].equalsIgnoreCase("center")) {ctr= c; valid=true;}
	
	return valid;
	}

/*finis*/
}

