package widget.swing;

import widget.*;
import dscript.connect.*;
import javax.swing.*;
import java.awt.Component;


public class DJMenuBar extends Dustyable {

private JMenuBar jmenu;

public DJMenuBar()
{
jmenu=new JMenuBar();
}

public boolean processCommand(String command)
{
	boolean returnval=false;
	
	if (command.equals("opaque")) { jmenu.setOpaque(true); returnval=true;}
	else if (command.equals("~opaque")) {jmenu.setOpaque(false); returnval=true;}
	
	return returnval;
}

					

public Component getAsComponent()
{
/*	
	JFrame jf=new JFrame("TEST");
	jf.setJMenuBar(jmenu);
	jf.pack();
	jf.setVisible(true);
*/	
return jmenu;
}

public boolean  isComponent()
{
	return true;
}
public boolean processDustyable(Dustyable d)
{
	//System.err.println("d.isComponent():"+d.isComponent());
	if (!d.isComponent())
	{
	return false;
	}
	
	Component c = d.getAsComponent();
	//System.out.println(c.getClass());
	if (c instanceof JMenu)
	{
		//System.err.println("Adding menu to menubar");
		jmenu.add((JMenu)c);
		return true;
	}
	return false;
}

}


