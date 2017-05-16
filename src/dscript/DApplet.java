package dscript;
import java.awt.*;
import java.applet.*;
import java.io.*;

public class DApplet extends Applet {//implements Runnable{

	public static boolean using_as_applet=false;
	private TextAreaConsole tac = null;
	public static String urlpath ="";
	private VisualShell vs=null;
	private PipedInputStream pis=null;
	private PrintWriter pw=null;
	private Output OUT;
	
	public DApplet(){
		//check boolean on file-read--write
		using_as_applet=true;

	}
	public void init(){

		tac=new TextAreaConsole(20,50);
		try {pis = new PipedInputStream();
		     pw=new PrintWriter(new PipedOutputStream(pis));
		}
		catch(Exception e) {System.out.println("error creating streams");}
		
		vs = new VisualShell(pw,tac,true);
	}
	
	/*
	public void start()
	{
		new Thread(this).start();
	}
	*/
	public void start()
	{
		urlpath=getCodeBase().toString();
		ConsoleDusty.urlpath=urlpath;
		Use.urlpath=ConsoleDusty.urlpath;
		new Thread(vs).start();
		//try {Thread.sleep(1200);} catch (Exception e){}
		setLayout(new BorderLayout());
		
		try{
		Panel p=vs.getPanel();
		setSize(p.getPreferredSize());
		add(p, BorderLayout.CENTER);
		//OutputStream os = vs.getOutputStream();
		OUT=new AppletOutput(tac.getTextArea()); //third normally System.out
		//OUT.println("Hmmm...\n\n");
		
		}
		catch(Exception e){System.out.println("Error initializing");
		showStatus(e.toString());
		}
		//pack();
		setVisible(true);
		showStatus("entering cd-run");
		try{Thread.sleep(1000);}catch(Exception m){}
		new ConsoleDusty(OUT,pis).start();
		
	}
}

