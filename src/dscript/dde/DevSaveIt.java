package dscript.dde;

import java.awt.*;
import java.awt.event.*;
import java.io.*;


class DevSaveIt implements ActionListener{

static String filesep = System.getProperty("file.separator");
static String basedir = System.getProperty("user.dir")+filesep+"users";

private String savefile;
private Frame f=new Frame("Save a Dustyscript file");
private Button abort=new Button("Abort this save");
private Button proceed=new Button("Proceed with this save");
private Checkbox overwrite=new Checkbox("Overwrite");
private Label mssg=new Label("---------??File-name??----------");
private TextField fname = new TextField(20);
private String dsource;
private Frame dde;
private boolean ok_to_close=true;

public DevSaveIt(String source)
{
dsource=source;

}
public static void setLibrary()
{
basedir=System.getProperty("user.dir")+filesep+"libraries";
}

	
public void closeFrame()
{
if (ok_to_close && (f !=null))
{f.setVisible(false);
f=null;
}
ok_to_close=false;

}

public void run()
{

dsource=sanitize(dsource);

abort.addActionListener(this); proceed.addActionListener(this);

Panel butts =new Panel(); butts.setLayout(new GridLayout(3,1));
 butts.add(overwrite); butts.add(proceed); butts.add(abort);
Panel entry =new Panel(); entry.setLayout(new BorderLayout());
fname.addActionListener(this);

entry.add(fname,BorderLayout.SOUTH);
entry.add(mssg,BorderLayout.NORTH);
f.setLayout(new BorderLayout());
f.add(entry,BorderLayout.NORTH);
f.add(butts,BorderLayout.SOUTH);
f.addWindowListener(new WindowAdapter()
				{
				public void windowClosing(WindowEvent we)
					{
					closeFrame();
					}
				});

f.pack();
f.setVisible(true);
}

public void actionPerformed(ActionEvent ae)
{
Object o = ae.getSource();
	if (o == abort) {closeFrame();}
	if (o == proceed) {save_file();}
	if (o == fname) {save_file();}

}
 
public void save_file()
	{
	
	try{
		File file = new File(basedir+filesep+fname.getText());
		if (file.exists() & !overwrite.getState())
			{
			mssg.setText("Check overwrite, then proceed");
			return;
			}
		FileWriter fw = new FileWriter(file);
		fw.write(dsource);
		fw.close();
		mssg.setText("Saved file!");
		try{Thread.sleep(2000);}
			catch(Exception e){}
		closeFrame();
		}
		catch(Exception ez)
			{
			mssg.setText("Save failed");
			return;
			}
	}

private String sanitize(String src)
	{
	String tosave="";
	for (int i=0; i<src.length();i++)
	{
	char c= src.charAt(i);
	if (c=='\n')
	   {if (i==0){tosave=tosave+c;continue;}
	    if (src.charAt((i-1)) != '\r')
		{tosave=tosave+"\r\n";}
	    else {tosave=tosave+"\n";}
	   continue;
	   }
	tosave=tosave+c;
	}
	return tosave;
	}

}
