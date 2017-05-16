package dscript.connect;

import javax.swing.JPanel;
import java.awt.Component;
import dscript.Var;


public class AdvancedDustyable extends Dustyable {

public final static Component default_comp=new JPanel();

private Object wrapped;

public AdvancedDustyable(Object o)
{
wrapped=o;
}

public Component getAsComponent() {
	if (wrapped instanceof Component) {return (Component)wrapped;}
	return default_comp;
}
	
public boolean processDustyable(Dustyable d)
{
return false;
//we need args
}

public boolean processDustyable(Dustyable d, String[] args)
{
//eval guts need to be written here !!
return true;
}

public boolean processCommand(String command)
{
return false;
//again--since we're a Java Object wrapper, we need args to let us know
//what we're doing
}

public boolean processCommand(String command, String[] args)
{

//guts go here
return true;
}

public boolean processVar(Var v) {

//we need args to do anything
return false;
}


public boolean processVar(Var v, String[] args)
{
//guts go here
return true;
}

public Var getVar()
{
if (wrapped instanceof Var) {return (Var)wrapped;}
return super.getVar();
}

public Var getVar(String[] args)
{
//guts go here
if (wrapped instanceof Var) {return (Var)wrapped;}
return super.getVar();
}

}
