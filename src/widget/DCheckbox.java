package widget;

import dscript.connect.*;
import java.awt.*;
import java.awt.event.*;

public class DCheckbox extends Dustyable implements ItemListener{

private Checkbox cb;

public DCheckbox()
	{
	cb=new Checkbox(); cb.addItemListener(this);
	}

public boolean isComponent() {return true;}
public Component getAsComponent() {return cb;}

public boolean processCommand(String command)
	{
	if (command.equals("select"))
		{
		cb.setState(true);
		return true;
		}
	if (command.equals("deselect"))
		{
		cb.setState(false);
		return true;
		}
	return false;
	}

public void itemStateChanged(ItemEvent ie)
	{
	if (cb.getState()) {getJavaConnector().sendActionMessage("selected");}
		else {getJavaConnector().sendActionMessage("deselected");}
	}

public boolean processCommand(String command, String[] args)
	{
	if (args.length < 1) {return false;}
	if (command.equalsIgnoreCase("setlabel"))
		{
		cb.setLabel(args[0]);
		return true;
		}
	if (command.equalsIgnoreCase("setstate"))
		{
		boolean state = false;
		if (args[0].equalsIgnoreCase("yes") | args[0].equalsIgnoreCase("true"))
			{
			state=true;
			}
		cb.setState(state);
		return true;
		}

	return false;
	}


public boolean processDustyable(Dustyable d)
	{
	try{
	   DCheckboxGroup dcbg = (DCheckboxGroup)d;
	   CheckboxGroup cbg= dcbg.getCheckboxGroup();
	   cb=new Checkbox(cb.getLabel(),cbg,cb.getState());
	   cb.addItemListener(this);
	   return true;
	   }
	   catch(Exception e){}
	return false;
	}


public boolean processDustyable(Dustyable d, String[] args)
	{
	if (args.length < 2) 
		{return false;}
	
	CheckboxGroup cbg=null;
	try{
	   DCheckboxGroup dcbg = (DCheckboxGroup)d;		
	   cbg = dcbg.getCheckboxGroup();
	   }
	   catch(Exception e){return false;}
	boolean state=false;
	if (args[1].equalsIgnoreCase("YES") | args[1].equalsIgnoreCase("TRUE"))
		{
		state=true;
		}
	cb =new Checkbox(args[0],cbg,state);
	cb.addItemListener(this);
	return true;
	}

/*finis*/
}
