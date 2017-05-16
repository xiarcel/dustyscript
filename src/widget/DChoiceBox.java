package widget;

import java.awt.*;
import dscript.connect.*;

public class DChoiceBox extends Dustyable{

private final Choice chc =new Choice();

public DChoiceBox()
{}

public boolean isComponent() {return true;}
public Component getAsComponent() {return chc;}

public boolean processCommand(String command)
{

	if (command.equals("getchoice"))
	{
	 
	 String s = chc.getSelectedItem();
	 if (s==null) {s="NULL";}
	 getJavaConnector().sendActionMessage(s);
	 return true;
 
	}
	return false;
}


public boolean processCommand(String command, String[] args)
{
 if (args.length < 1) {return false;}
 
 if (command.equals("add")) {chc.add(args[0]);return true;}
 if (command.equals("remove")) {chc.remove(args[0]);return true;}
 if (command.equals("select")) {
 chc.select(args[0]);return true;}
 if (command.equals("selectint")) 
 {
	 try 
	 {
		 int i = Integer.parseInt(args[0])-1;
 		 chc.select(i);				
		 return true;
	 }
	 catch(Exception e){}
	 
 	return false;
 }	 
 
 return false;
}

}
