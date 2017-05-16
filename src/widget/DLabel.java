package widget;

import java.awt.*;
import dscript.connect.*;

public class DLabel extends Dustyable{

private Label lab;

public DLabel()
	{
	lab=new Label();
	}

public boolean isComponent(){return true;}
public Component getAsComponent() {return lab;}

public boolean processCommand(String command, String[] args)
	{
	if (args.length < 1) {return false;}
	if (command.equalsIgnoreCase("setlabel"))
		{
		lab.setText(args[0]);
		return true;
		}
	return false;
	}

/*finis*/
}
