package widget.swing;

import dscript.connect.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionListener;


public class DJMenuItem extends Dustyable  implements ActionListener{

	private JMenuItem jmi;
	private String name;
	private String mnemonic;
	private String id;
	
	public DJMenuItem()
	{
		jmi=new JMenuItem();
	name="";
	mnemonic="";
	}
	
	public boolean isComponent() {return true;}
	public Component getAsComponent() {return jmi;}
	
	public boolean processCommand(String command)
	{
		if (command.equals("listen") && (jmi != null)) {
		
			jmi.removeActionListener(this);
			jmi.addActionListener(this);
			return true;
		}
		
		if (command.equals("!listen") && (jmi != null)) 
		{
			jmi.removeActionListener(this);
			return true;
		}
		return false;
	}
	
	
	public boolean processCommand (String command, String[] args)
	{
	if (args.length<1) {return false;}
	if (command.equals("setactionid")) {
		id=args[0];
		return true;
	}
	
	if (command.equals("setlabel")) 
	{
		name=args[0];
		jmi.setText(name);
		return true;
	}
	
	if (command.equals("setmnemonic"))
	{
		/**/
		return true;
	}
	
	return false;
}
public void actionPerformed(java.awt.event.ActionEvent ae)
	
	{
		
		getJavaConnector().sendActionMessage((id==null)?"selected":id);
	}
	
}

