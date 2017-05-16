package widget.swing;
import dscript.connect.*;
import widget.*;
import javax.swing.*;
import java.awt.Component;

public class DJTabbedPane extends Dustyable {
	
	
	private JTabbedPane tabbed=new JTabbedPane();
	
	public DJTabbedPane(){}
	
	
	
	public boolean isComponent() {return true;}
	public Component getAsComponent() { return tabbed;}
	
	public boolean processCommand(String command)
	{
		boolean returnval=false;
		if (command.equals("opaque")) {
			tabbed.setOpaque(true);
			returnval=true;
		}
		else if (command.equals("~opaque")) {
			tabbed.setOpaque(false);
			returnval=true;
		}
		return returnval;
	}
	
	public boolean processDustyable(Dustyable d, String[] args)
	{
		//System.err.println("args.length=="+args.length);
		/*
		for (int i=0; i<args.length;i++)
		{
			System.err.println("\targs["+i+"]=="+args[i]);
		}
		*/
		
		/*
		System.err.println("d.isComponent():"+d.isComponent());
		System.err.println("d.getClass():"+d.getClass());
		*/
		if (!d.isComponent() || (args.length < 1)) {return false;}
		
		if (args[0].equals("add"))
		{
			Component c=d.getAsComponent();
			if (d.getIcon() != null) {
				tabbed.addTab(args[1],d.getIcon(),c);
				
			}
			else
			{
				tabbed.addTab(args[1],c);
			}
		
		return true;
		}
		
		if (args[0].equals("remove"))
		{
			Component c=d.getAsComponent();
			tabbed.remove(c);
			return true;
		}
		
	return false;
	}
	
}

