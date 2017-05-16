package dscript.connect;

import java.io.*;

public class DFile extends Dustyable {

private File file;

public DFile()
{}

public boolean processCommand(String command, String[] args)
{

if (command.equals("write"))
	{
	try {
		
		FileWriter fw=new FileWriter(file);
		fw.write(args[0]);
		fw.close();
		
	}
		catch (Exception e){return false;}
	return true;
	}
if (command.equals("setfile"))
{
	try{
		file=new File(args[0]);
	}
	catch (Exception e){return false;}
	return true;
}

return false;
}

public boolean processCommand(String command)
{
	if (command.equals("read"))
	{
		try {
			StringBuffer sb=new StringBuffer();
			BufferedReader br=new BufferedReader(new FileReader(file));
			String s;
			while ((s=br.readLine())!=null)
			{
				sb.append(s).append('\n');
			}
			
		getJavaConnector().sendActionMessage(sb.toString());
		return true;
		}
		catch (Exception e){return false;}
	}
	return false;
}

}

