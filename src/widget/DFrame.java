package widget;

import dscript.connect.*;
import java.awt.*;
import java.awt.event.*;

public class DFrame extends Dustyable{

private Frame f;

private WindowAdapter wa =new WindowAdapter(){				
					public void windowClosing(WindowEvent we)
					{
					getJavaConnector().sendActionMessage("closing");
					}
				};

public DFrame()
	{
	f=new Frame();
	}

public WindowListener getWindowListener() 
{
	return wa;
}

public boolean isComponent(){return true;}
public boolean isOnlyContainer(){return true;}

public boolean processCommand(String command)
	{
	if (command.equalsIgnoreCase("show")){f.pack();f.setVisible(true);}
	else if (command.equalsIgnoreCase("hide")){f.setVisible(false);}
	else if (command.equalsIgnoreCase("enable_close"))
			{
			f.addWindowListener(wa);

			}
	else if (command.equalsIgnoreCase("disable_close"))
			{
			f.removeWindowListener(wa);
			}
	else {return false;}
	return true;
	}

public boolean processCommand(String command, String[] args)
	{
	if (args.length <1){return false;}
	if (command.equalsIgnoreCase("setlabel"))
		{
		/*used to do setName..woops!*/
		f.setTitle(args[0]);
		return true;
		}
	return false;
	}

public boolean processDustyable(Dustyable d)
	{
	/*right now, DFrame takes ONE component (should be a panel)*/
	
	if (d instanceof DKeyListener)
	{
		f.addKeyListener((KeyListener)d);
		return true;
	}
	
	if (!d.isComponent()){return false;}
	if (dscript.StatementProcessor.DEBUG)
	{
	System.err.println("DFrame adding:"+d.getClass().getName());
	}
	Component c= d.getAsComponent();f.setSize(c.getPreferredSize());
	f.add(c,BorderLayout.CENTER);
	

	return true;
	}

/*finis*/
}
