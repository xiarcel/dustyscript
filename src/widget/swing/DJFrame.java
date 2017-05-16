package widget.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import dscript.connect.*;
import widget.*;

public class DJFrame extends DFrame
{
	private JFrame jf;
	
	public DJFrame()
	{
		jf=new JFrame();
	}
	
	public boolean processCommand(String command)
	{
		boolean ret=true;
		if (command.equalsIgnoreCase("show"))
		{
			jf.pack(); jf.setVisible(true);
		}
		else if (command.equalsIgnoreCase("hide")) 
		{
			jf.setVisible(false);
		}
		else if (command.equalsIgnoreCase("enable_close"))
		{
			jf.addWindowListener(getWindowListener());
		}
		else if (command.equalsIgnoreCase("disable_close"))
		{
			jf.removeWindowListener(getWindowListener());
		}
		else {
			ret=false;
		}
		return ret;
	}
	
	public boolean processCommand(String command, String[] args)
	{
		if (args.length < 1) {return false;}
		if (command.equalsIgnoreCase("setlabel"))
		{
			jf.setTitle(args[0]);
			return true;
		}
		return false;
	}
	
	public boolean processDustyable(Dustyable d)
	{
		if (d instanceof DKeyListener)
		{
			jf.addKeyListener((KeyListener)d);
			return true;
		}
		//System.err.println("DJFrame pD (d.isComponent):"+d.isComponent());
		
		if (!d.isComponent()) {
		//System.err.println("DJFrame pD (d.getClass()):"+d.getClass());
		return false;}
		
		Component c=d.getAsComponent();
		//System.err.println("DJFrame pD (c.getClass()):"+c.getClass());
		if (c instanceof JMenuBar)
		{
			//System.err.println("c instanceof JMenuBar");
			//if (c == null) {System.err.println("c==null in DJFrame.processD");}
			c.setVisible(true);
			jf.setJMenuBar((JMenuBar)c);
			return true;
		}
		
		//JPanel jp=new JPanel();
		//jp.add(c);
		jf.setSize(c.getPreferredSize());
		jf.setContentPane((Container)c);
		System.err.println("c=="+c.getClass());
		return true;
	}
	
	/*finis*/
}

