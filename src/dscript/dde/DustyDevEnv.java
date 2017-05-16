package dscript.dde;

import dscript.*;
import dscript.connect.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

public class DustyDevEnv extends Thread implements ActionListener{

private static boolean SPECIAL = false;
private PipedInputStream pis_d,pis_e,pis_r;
private PipedOutputStream pos_d,pos_e,pos_r;

private TextAreaConsole devel;
private VisualShell error_shell;
private VisualShell run_shell;

private Killable current;

private TextArea devel_ta =new TextArea("",25,55);

private Button save,load,go,as_inter;
private Checkbox cb =new Checkbox("Re-direct errors to error-shell");
private Checkbox qu =new Checkbox("Check here to run without 'helpful comments'");

private TextAreaConsole runit,error;
private Frame frame =new Frame ("The Dustyscript Development Environment");
private Button clr_run =new Button("Clear");
private Button clr_err =new Button("Clear");
private Button clr_dev =new Button("Clear");
private Button kill=new Button("Stop running program/shell");


public DustyDevEnv () throws Exception
	{
	

	go=new Button("Run your source");

	save =new Button("Save to file");
	load =new Button("Load from file");
	as_inter=new Button("Run as interractive shell");

	pos_d=new PipedOutputStream();
	pis_d= new PipedInputStream(pos_d);
	
	pos_e =new PipedOutputStream();
	pis_e =new PipedInputStream(pos_e);

	pos_r =new PipedOutputStream();
	pis_r =new PipedInputStream(pos_r);


	devel = new TextAreaConsole(30,45);
	runit = new TextAreaConsole(20,30);
	error = new TextAreaConsole(10,30);
	

	run_shell =new VisualShell(new PrintWriter(pos_r),runit,true);
	error_shell =new VisualShell(new PrintWriter(pos_e),error,true);

	
	}

public void run()
{

	new Thread(run_shell).start(); new Thread(error_shell).start();
	/*we sleep to give the shells a chance to be ready*/
	try{Thread.sleep(2000);}catch(Exception e){}
	
	/*add all the listeners..*/
	go.addActionListener(this);
	clr_run.addActionListener(this);
	clr_err.addActionListener(this);
	save.addActionListener(this);
	load.addActionListener(this);
	as_inter.addActionListener(this);
	clr_dev.addActionListener(this);
	kill.addActionListener(this);


	frame.setLayout(new BorderLayout());
	Panel p =new Panel();
	p.setLayout(new BorderLayout());
	
	Panel last =new Panel(); last.setLayout(new GridLayout());
	last.add(new Label("Type your source in the field below:"),BorderLayout.WEST); 
	last.add(clr_dev,BorderLayout.EAST);

	Panel dpan = new Panel();dpan.setLayout(new BorderLayout());
	dpan.add(last,BorderLayout.NORTH);
	dpan.add(devel_ta,BorderLayout.SOUTH);

	p.add(dpan,BorderLayout.SOUTH);
	Panel pp =new Panel(); pp.setLayout(new GridLayout(6,1));

	 pp.add(save); pp.add(load); pp.add(go);pp.add(as_inter); pp.add(kill);pp.add(qu);
	p.add(pp,BorderLayout.NORTH);
	Panel ppp =new Panel(); ppp.setLayout(new BorderLayout());

	Panel ri =new Panel(); ri.setLayout(new BorderLayout());
	Panel rii =new Panel(); rii.setLayout(new BorderLayout());
	rii.add(new Label("Your program will run here"),BorderLayout.WEST);
	rii.add(clr_run,BorderLayout.EAST);
	ri.add(rii,BorderLayout.NORTH);

	Panel rta =new Panel();
	try{rta=run_shell.getPanel();} catch(Exception ee){rta=new Panel();}

	ri.add(rta,BorderLayout.SOUTH); 	
	Panel er =new Panel(); er.setLayout(new BorderLayout());
	Panel eer =new Panel(); eer.setLayout(new BorderLayout());

	eer.add(new Label("Error messages from your program:"),BorderLayout.WEST);
	eer.add(clr_err,BorderLayout.EAST);
	er.add(eer,BorderLayout.NORTH);

	Panel eta = new Panel();
		try {eta = error_shell.getPanel();} catch(Exception eee){eta=new Panel();}
	

	er.add(eta,BorderLayout.SOUTH);


	ppp.add(ri,BorderLayout.NORTH);
	ppp.add(er,BorderLayout.SOUTH);

	frame.add(p,BorderLayout.WEST);
	frame.add(ppp,BorderLayout.EAST);

 frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent we)
								{finish(frame);}
							  });

 frame.pack();
 frame.setVisible(true);
 if (DApplet.using_as_applet)
 {
	 load.setEnabled(false);
	 save.setEnabled(false);
 }
}

public void actionPerformed(ActionEvent ae)
{
Object o = ae.getSource();
if (o==save) {new DevSaveIt(devel_ta.getText()).run();}
if (o==load) {new DevLoadIt(devel_ta).run();}
if (o==kill) {if (current != null) {current.kill();}}

/*run source as statement-processor*/

if (o == go)
	{
	Output out = null;
	   try{
		   out =new Output(runit,error,error);
		   }
	         catch(Exception eeez){try{out =new Output();}catch(Exception oz){System.exit(1);}}
			    
	    ThingTypeContainer ttc =new ThingTypeContainer(out,pis_r);
	    ActionContainer act =new ActionContainer(out);
	    VarContainer vc=new VarContainer(out);
	    String src = devel_ta.getText();
		if (SPECIAL) {dump(src);}
		StatementProcessor.reset();
	    StatementProcessor sp =new StatementProcessor(src,vc,act,ttc,out,pis_r);
		if (qu.getState()){sp.setUserDebug(false);}
		current = sp;
	    //set AHI's 'best match' to on
	    ActionHashIterator.USE_BEST_MATCH=true;
	    new Thread(sp).start();
			try{
	   runit.getTextArea().requestFocus();
		}
		catch(Exception rgte){}

	}

/*clear button for run area*/
 if (o == clr_run)
	{
	runit.getTextArea().setText("");
	}

/*clear button for error area*/
 if (o == clr_err)
	{
	error.getTextArea().setText("");
	}

/*clear button for devel-area*/
 if (o == clr_dev)
	{
	devel_ta.setText("");
	}

/*run ConsoleDusty in 'run' area*/
 if (o == as_inter) 
	{
	/*interractive shell here*/
		Output out = null;
	   try{
		   out =new Output(runit,error,error);
		   }
		catch(Exception eeez){try{out =new Output();}catch(Exception oz){System.exit(1);}}

	ConsoleDusty cd =new ConsoleDusty(out,pis_r,false);
	new Thread(cd).start();
	current = cd;
	try{
	   runit.getTextArea().requestFocus();
		}
		catch(Exception rgte){}

	
	}

	
}

private void dump(String source)
{
System.err.println(""); 
 for (int i=0; i<source.length();i++)
	{
	int chr = (int)source.charAt(i);
	System.err.print("\t"+chr);
	}
System.err.println("");
}

public static void finish(Frame f)
{
	if (DApplet.using_as_applet)
	{
		f.setVisible(false);
		f.dispose();
	}
	else
	{
		System.exit(0);
	}
}



public static void main(String[] args)
{
if ( args.length > 0 && args[0].equals("library"))
{
	DevSaveIt.setLibrary();
	DevLoadIt.setLibrary();
}

	try{

	DustyDevEnv dde =new DustyDevEnv();
	new Thread(dde).start();
	}
	catch(Exception e){System.err.println("Failed at starting the DDE");}
}

}

 	
