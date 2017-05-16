package dscript.connect;
import dscript.Var;
import java.awt.Component;

public interface Dustializable{
/*over-ride any or all of the following methods to change how your Dustyable subclass
interracts with the Dustyscript interpreter*/

/*set reference to the Dustyable that wraps this Dustializable*/
public void setDustyable(Dustyable d);

public Component getAsComponent();
public boolean isComponent();
public boolean isOnlyContainer();

/*receive a Dustyable from Dustyscript in your Dustyable subclass, without any args*/


public boolean processDustyable(Dustyable d);

/*receive a Dustyable from Dustyscript in your Dustyable subclass, with args*/
public boolean processDustyable(Dustyable d, String[] args); 

/*receive a single string message from Dustyscript, no args*/
public boolean processCommand(String command); 

/*receive a single string message/command from Dustyscript, with args*/
public boolean processCommand(String command, String[] args); 

/*receive a Var (dscript.Var) from Dustyscript, no args*/
public boolean processVar(Var v);

/*receive a Var (dscript.Var) from Dustyscript, with args*/
public boolean processVar(Var v, String[] args); 

/*send (return to) Dustyscript a dscript.Var, require no args*/
public Var getVar();

/*send (return to) Dustyscript a dscript.Var, with args passed in*/
public Var getVar(String[] args); 

}

