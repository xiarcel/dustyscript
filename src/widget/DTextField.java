package widget;
import java.awt.*;
import java.awt.event.*;
import dscript.connect.*;

public class DTextField extends Dustyable implements ActionListener{

private TextField tf;
private boolean listening=false;

public DTextField()
	{
	tf=new TextField("");
	}

public boolean isComponent(){return true;}
public Component getAsComponent() {return tf;}

public void actionPerformed(ActionEvent ae)
	{
	getJavaConnector().sendActionMessage(tf.getText());
	}

public boolean processCommand(String command)
	{
	if (command.equalsIgnoreCase("clear")){tf.setText("");return true;}
	if (command.equalsIgnoreCase("sendtext"))
		{getJavaConnector().sendActionMessage(tf.getText()); return true;}

	if (command.equalsIgnoreCase("send_on_enter"))
		{if (!listening){tf.addActionListener(this);listening = true;} return true;}
	if (command.equalsIgnoreCase("is_editable")){tf.setEditable(true);}
	if (command.equalsIgnoreCase("not_editable")){tf.setEditable(false);}
	if (command.equalsIgnoreCase("no_send_on_enter"))
		{
		if (listening){tf.removeActionListener(this); listening = false;}
		return true;
		}
	return false;
	}

public boolean processCommand(String command, String[] args)
	{
	if (args.length < 1){return false;}
	if (command.equalsIgnoreCase("setlength"))
		{
		try{
		   tf=new TextField(Integer.parseInt(args[0]));
		   listening=false;
		   return true;
		   }
		   catch(Exception e){return false;}
		}
	if (command.equalsIgnoreCase("settext"))
		{
		tf.setText(args[0]); return true;
		}
	if (command.equalsIgnoreCase("tf_with_value"))
		{
		tf = new TextField(args[0]);
		listening=false;
		return true;
		}
	return false;
	}
	
public boolean processDustyable(Dustyable d)
{
	if (d instanceof DKeyListener) 
	{
		
	tf.addKeyListener((KeyListener)d);
	return true;
	}
	return false;
}

/*finis*/
}
