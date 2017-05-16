package dscript.dde;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class DevLoadIt implements ActionListener{

static String filesep = DevSaveIt.filesep;
static String basedir = DevSaveIt.basedir+filesep;

private TextArea dumpit;
private Button abort = new Button("Abort this load");
private Button proceed = new Button("Load this file");
private TextField fname = new TextField(20);
private Frame f= new Frame("Load a file...");
private Label mssg=new Label("-----:::Load File:::-----");
private boolean ok_to_close=true;

public DevLoadIt(TextArea ta)
{
dumpit=ta;
}
public static void setLibrary()
{
	basedir=DevSaveIt.basedir+filesep;
	System.out.println("Load basedir="+basedir);
}

public void closeFrame()
{
if ((f != null) && ok_to_close){f.setVisible(false);}
f = null;
}


public void run()
{
 f.setLayout(new GridLayout(4,1));
 abort.addActionListener(this);
 proceed.addActionListener(this);
 fname.addActionListener(this);

 f.add(mssg); f.add(fname); f.add(proceed); f.add(abort);

 f.addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent we)
						{closeFrame();}
							});

 f.pack();
 f.setVisible(true);
}

public void actionPerformed(ActionEvent ae)
	{
	Object o = ae.getSource();
	if (o==abort) {closeFrame();return;}
	if ((o==proceed)||(o==fname))
		{
		loadFile();
		}
	}

private void loadFile()
	{
	try{
	File file =new File(basedir+fname.getText());
	BufferedReader br=new BufferedReader(new FileReader(file));
	String contents="";
	String s;
	while ((s=br.readLine())!=null)
		{
		contents=contents+s+"\n";
		}
	br.close();
	dumpit.setText(contents);
	closeFrame();
	   }
	   catch(Exception e){dumpit.setText("---Failed at reading:\n"+fname.getText());}
	}

}
