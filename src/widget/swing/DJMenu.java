package widget.swing;
import dscript.connect.*;
import javax.swing.*;
import java.awt.Component;

public class DJMenu extends Dustyable {
	
	private JMenu jmenu;
	private String name;
	private String mnemonic;
	
	public DJMenu()
	
	{
	name="";
	mnemonic="";
	}
	
	public boolean isComponent()
	{
		return true;
	}
	
	public Component getAsComponent()
	{
		if (jmenu == null) {jmenu=getJMenu();}
		return jmenu;
	}
	
	private JMenu getJMenu()
	{
		JMenu j=new JMenu(name);
		//if (! "".equals(mnemonic)){j.setMnemonic(mnemonic);}
		return j;
	}
	
	public boolean processCommand(String command, String[] args)
	
	{
		if (args.length < 1) { 
			return false;
		}
		
		if (command.equals("setname"))
		{
			//System.err.println("DJMenu.processCommand(String, args) 'setname'");
			name=args[0];
			if (jmenu == null)
			{
				jmenu=new JMenu(name);
			}
			else
			{
				jmenu.setText(name);
			}
			return true;
		}
		
		if (command.equals("setmnemonic"))
		{
			mnemonic=args[0];
			return true;
		}
		
		return false;
	}
	
	public boolean processDustyable(Dustyable d)
	{
		//System.err.println(d.getClass());
		//System.err.println("d.isComponent() in DJMenu pD:"+d.isComponent());
		if (!d.isComponent()) {return false;}
		Component c= d.getAsComponent();
		if (jmenu == null)
		{jmenu=getJMenu();}
	
		jmenu.add(c);
		return true;
	}

	
}

