package dscript.connect;
import java.awt.Panel;
import java.awt.event.*;
import java.awt.Component;
import dscript.Var;
import javax.swing.Icon;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/*by subclassing Dustyable, and over-riding certain methods, you can provide Java-Connector
extensions to the Dustyscript language.  Once you have written your sub-class, you can
provide a Dustyscript file that wraps your Dustyable in a Thing... the entire use of
widgets in Dustyscript is accomplished this way.*/

public class Dustyable implements Dustializable, ActionListener, MouseListener, KeyListener{
	HashMap hash=new HashMap();
	static Class AL;
	static {
		try {AL=Class.forName("java.awt.event.ActionListener");}
		catch(ClassNotFoundException cnfe) {AL=null;}
	};
	static Class[] ACT_L=new Class[1];
	
private Dustyable parent;

	
/*call your JavaConnector, if you'd like, with the public getJavaConnector() method.*/
private JavaConnector jc;
private Dustializable dust=null;
private Icon icon=null;

/*this is a default panel for getComponent(), in case Dustyscript calls for a component
for your Dustyable, and it is not one*/
public final static Panel p=new Panel();

/*this is a default Var.  For certain Dustyables, receiving a Var object from Dustyscript,
and therefore, potentially returning it back, will be useful.  This Var instance will
be returned when your particular Dustyable does not need 
or support being passed a Var (or having one requested of it)
*/
public final static Var NOT_THERE = new Var("VAR_NOT_PRESENT","_NOT_THERE_");


/*originally debated an abstract class.  But initializing a javaconnector var uses a
dscript.connect.Dustyable as its Dustyable, until connected to a different one.
Having it this way keeps NullPointers from popping up unexpectedly*/
public Dustyable(){}

public Dustyable(Dustializable d)
{
dust=d;
dust.setDustyable(this);
}	

/*a Dustyable is only a container when it is a java Frame or something like that.
This keeps another Dustyable from trying to add it as a Panel
If your Dustyable uses a Frame or Frame-like Java class as its backbone,
over-ride this to return true.
*/
public boolean isOnlyContainer(){
if (dust != null) {return dust.isOnlyContainer();}
return false;}

/*
originally...all of the java-connector implementations supplied w/ Dustyscript were
'components' ((widgets...visuals)) If the dustyable is not a graphic (AWT/Swing), it is
not a component
If your Dustyable is some type of widget (AWT/Swing) over-ride this to return true
*/
public boolean isComponent(){
if (dust != null) {return dust.isComponent();}
return false;}


/*
over-ride this and return your java.awt.Component subclass here, assuming you have one.
This returns an empty, sizeless panel by default
*/
public Component getAsComponent() {
if (dust != null) {return dust.getAsComponent();}
return p;}


/*this really shouldn't be touched :-) Dustyscript sets your JavaConnector for you*/
public void setJavaConnector(JavaConnector j)
{
 jc =j;
}

/*What you can do with one of these:
Really here for Dustyscript to call your particular JavaConnector back, but:

public Output getOutput()
	get an Output object.  Use getJavaConnector().getOutput().println("My message", int) to send
	messages via Dustyscript's i/o.  
	0 is standard out, 1 is error out, 2 is debug

public void sendActionMessage(String)
	To send a message to (or through) the particular action 
	that has been hooked to your Dustyable. do:
	getJavaConnector().sendActionMessage("I am calling you!");
	
Read JavaConnector's source for more ideas
*/
public JavaConnector getJavaConnector()
{
return jc;
}


/*over-ride any or all of the following methods to change how your Dustyable subclass
interracts with the Dustyscript interpreter*/

/*receive a Dustyable from Dustyscript in your Dustyable subclass, without any args*/


public boolean processDustyable(Dustyable d) {
if (dust != null) {return dust.processDustyable(d);}

return false;}

/*receive a Dustyable from Dustyscript in your Dustyable subclass, with args*/
public boolean processDustyable(Dustyable d, String[] args) {
if (dust != null) {return dust.processDustyable(d,args);}
return false;}

/*receive a single string message from Dustyscript, no args*/
public boolean processCommand(String command) {
if (dust != null) {return dust.processCommand(command);}
return false;}

/*receive a single string message/command from Dustyscript, with args*/
public boolean processCommand(String command, String[] args) {
if (dust != null) {return dust.processCommand(command,args);}

return false;}

/*receive a Var (dscript.Var) from Dustyscript, no args*/
public boolean processVar(Var v) {
if (dust != null) {return dust.processVar(v);}
return false;}

/*receive a Var (dscript.Var) from Dustyscript, with args*/
public boolean processVar(Var v, String[] args) {
if (dust != null) {return dust.processVar(v,args);}
return false;}

/*send (return to) Dustyscript a dscript.Var, require no args*/
public Var getVar() {
if (dust != null) {return dust.getVar();}
return NOT_THERE;}

/*send (return to) Dustyscript a dscript.Var, with args passed in*/
public Var getVar(String[] args) {
if (dust != null) {return dust.getVar(args);}
return NOT_THERE;}

public Icon getIcon()
{
	return icon;
}

/*'icon' for menus and tabs and such-- child classes would provide Dustyscript handlers for "seticon"..there will be a widget.swing.DIcon class and a Thing
type Icon... send someicon to somejc using "seticon"
*/
public void setIcon(Icon i)
{
	icon=i;
}

//implements Dustializable
public void setDustyable(Dustyable d)
{
	if (parent != this) {parent=d;}
}

public void message(String s)
{
	getJavaConnector().sendActionMessage(s);
}
public void actionPerformed(ActionEvent ae)
{
	message("action_performed");
}

public void mouse_message(String s, MouseEvent me)
{
	message(s+me.getButton()+":"+me.getX()+":"+me.getY());
}

public void mouseClicked(MouseEvent me)
{
	mouse_message("mouse_clicked:",me);
}

public void mouseEntered(MouseEvent me)
{
	mouse_message("mouse_entered:",me);
}

public void mouseExited(MouseEvent me)
{
	mouse_message("mouse_exited:",me);
}

public void mousePressed(MouseEvent me)
{
	mouse_message("mouse_pressed:",me);
}

public void mouseReleased(MouseEvent me)
{
	mouse_message("mouse_released:",me);
}

public void key_message(String s, KeyEvent ke)
{
	int kc=ke.getKeyCode(); int km=ke.getModifiersEx();
	//message(s+KeyEvent.getKeyText(kc)+":"+KeyEvent.getKeyModifiersText(km)); //change!!!
	message(s+KeyEvent.getKeyText(kc));
}

public void keyTyped(KeyEvent ke)
{
	//key_message("key_typed:",ke);
}

public void keyPressed(KeyEvent ke)
{
	//key_message("key_pressed:",ke);
}

public void keyReleased(KeyEvent ke)
{
	key_message("key_",ke);
}

public void putLabel(Component cc, String label)
{
	hash.put(cc,label);
}

public String getLabel(Component cc)
{
	return ((String)hash.get(cc));
}
/*
send JAVACONNECTOR to JAVACONNECTOR using IDENTIFIER-STRING
have processDustyable(d,args) call:
Dustyable.addListener(d,this,CHAR);
*/

public static boolean removeListener(Dustyable listener, Dustyable target, char type)
{
	if (!target.isComponent()) {return false;}
	Component c=target.getAsComponent();
	
	switch(type)
	{
	case 'k': 
		{
			c.removeKeyListener(listener);
			break;
		}
	case 'm':
		{
			c.removeMouseListener(listener);
			break;
		}
	case 'a':
		{
			if (ACT_L[0]==null) {ACT_L[0]=AL;}
			
			Class cls=c.getClass();
			try 
			{
				Method m=cls.getDeclaredMethod("removeActionListener",ACT_L);
				m.invoke(c,new Object[]{listener});
			}
			catch(NoSuchMethodException nsme) {return false;}
			catch(IllegalAccessException iae) {return false;}
			catch(InvocationTargetException ite) {return false;}
			break;
		}
		default: {return false;}
	}
	return true;
}
	
		

public static boolean addListener(Dustyable listener,Dustyable target, char type)
{
	if (!target.isComponent()) {return false;}
	Component c=target.getAsComponent();
	
	switch(type)
	{
	case 'a': 
		{
			if (ACT_L[0]==null) {ACT_L[0]=AL;}
			
			Class cls=c.getClass();
			try 
			{
				Method m=cls.getDeclaredMethod("addActionListener",ACT_L);
				m.invoke(c,new Object[]{listener});
			}
			catch(NoSuchMethodException nsme) {return false;}
			catch(IllegalAccessException iae) {return false;}
			catch(InvocationTargetException ite) {return false;}
			break;
		}
	case 'k':
		{
			c.addKeyListener(listener);
			break;
		}
	case 'm':
		{
			c.addMouseListener(listener);
			break;
		}
	default:
		{
			return false;
		}
	}
	return true;
}
/*If you require any help creating a Dustyscript extension, email 
dustyscript-devel@lists.sourceforge.net
OR
xiarcel@users.sourceforge.net
*/

}
