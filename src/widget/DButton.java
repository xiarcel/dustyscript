
package widget;
import dscript.connect.*;
import java.awt.*;
import java.awt.event.*;

public class DButton extends Dustyable implements ActionListener{

private Button dbutton;
private String actionName = "pressed";

public DButton()
	{
	dbutton =new Button(); dbutton.addActionListener(this); //dbutton.addKeyListener(this);
	}

public boolean isComponent() {return true;}
public Component getAsComponent() {return dbutton;}

public void actionPerformed(ActionEvent ae)
	{
	processCommand("press");
	}

public boolean processCommand(String command)
	{
	if (command.equalsIgnoreCase("enable"))
		{
		dbutton.setEnabled(true);getJavaConnector().sendActionMessage("enabled");
		return true;
		}
	if (command.equalsIgnoreCase("disable"))
		{
		dbutton.setEnabled(false);getJavaConnector().sendActionMessage("disabled");
		return true;
		}
	if (command.equalsIgnoreCase("press"))
		{
		getJavaConnector().sendActionMessage(actionName);
		return true;
		}
	return false;
	}

public boolean processCommand(String command, String[] args)
	{
	if (args.length < 1) {return false;}
	if (command.equalsIgnoreCase("setlabel"))
		{
		dbutton.setLabel(args[0]);
		return true;
		}
	if (command.equalsIgnoreCase("setactionname"))
		{
		actionName=""+args[0];
		return true;
		}
	return false;
	}

/*finis*/
}
