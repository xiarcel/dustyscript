package network;
import dscript.connect.*;
import java.net.*;
import java.io.*;

public class DSocket extends Dustyable {
	
	private BufferedReader br=null;
	private Socket sock = null;
	private boolean open = false;
	private boolean ignore_breaks = false;
	private String fail="!FAIL!";
	private String address="";
	private int port = 0;
	private PrintWriter pw=null;
	
	
	public DSocket()
	{}
	
	public boolean processCommand(String command)
	{
		if (command.equals("init"))
		{
			try{
				sock = new Socket(address,port);
				pw = new PrintWriter(sock.getOutputStream());
				br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				open=true;
			}
			catch (Exception e){
				
				kill();fail();
				System.err.println(e.toString());
				getJavaConnector().sendActionMessage(e.toString());
				getJavaConnector().sendActionMessage("using port:"+port);
				return false;
			}
			return true;
		}
		
		if (command.equals("read"))
		{
			if ((sock==null)||!open)
			{
			kill();fail();
			return false;
			}
			
			try{
			String s = br.readLine();
			if (s == null) 
			{
				kill();
				fail();
				getJavaConnector().sendActionMessage(fail);
				return false;
			}
			
			getJavaConnector().sendActionMessage(ignore_breaks?s:(s+'\n'));
			return true;
			}
			catch(Exception arg) 
			{
			kill();
			fail();
			}
			return false;
		}
		if (command.equals("readall"))
		{
			try{
				String s;
				StringBuffer cn=new StringBuffer(250);
				while ((s=br.readLine())!=null)
				{
					cn.append(s);
					if (!ignore_breaks){cn.append('\n');}
				}
				kill();
				getJavaConnector().sendActionMessage(cn.toString());
				return true;
				
			}
			catch(Exception e){kill();fail();}
			return false;
		}
		if (command.equals("ignore"))
		{
			ignore_breaks=true;
			return true;
		}
		if (command.equals("!ignore"))
		{
			ignore_breaks=false;
			return true;
		}
		if (command.equals("close"))
		{
			kill();
			return true;
		}
		
		return false;
	}
	
	public boolean processCommand(String command, String[] args)
	{
		if (args.length < 1) {return false;}
		if (args[0].startsWith("\"")) {args[0]=args[0].substring(1,args[0].length());
			//getJavaConnector().getOutput().println("stripped leading quote",1);
		}
		if (args[0].endsWith("\"")) {args[0]=args[0].substring(0,args[0].length()-1);
			//getJavaConnector().getOutput().println("stripped ending quote",1);
		}
		
		if (command.equals("setaddress")) {address=args[0];}
		else if (command.equals("setport")) {
			try{port=Integer.parseInt(args[0]);} catch (Exception nfe){port=0;}
		}
		else if (command.equals("setfail")){
			fail = args[0];
		}
		else if (command.equals("write"))
		{
			try{
			pw.print(ignore_breaks?args[0]:(args[0]+'\n'));
			pw.flush();
			}
			catch (Exception e){return false;}
			return true;
		}
		else {return false;}
		return true;
	}
	
		
	private void kill()
	{
	try{br.close();} catch(Exception e){}
	br=null; sock=null;
	open = false;
	}
	
	private void fail()
	{
		getJavaConnector().sendActionMessage(fail);
	}
	
}

