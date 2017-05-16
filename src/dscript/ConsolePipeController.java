package dscript;

import java.io.*;

public class ConsolePipeController extends Thread{
	
	private BufferedReader BR=null;
	private PrintWriter OUT = null;
	private long pause = 0L;
	private PipedInputStream PIS = null;
	
	public ConsolePipeController(InputStream IN) throws Exception	
	{
		BR = new BufferedReader(new InputStreamReader(IN));
		PIS = new PipedInputStream();
		OUT = new PrintWriter(new PipedOutputStream(PIS));
	}
	
	public ConsolePipeController(InputStream IN, long p) throws Exception
	{
		this(IN);
		pause = p;
	}
	
	public InputStream getInputStream() 
	{
		return PIS;
	}
	
	public void run()
	{
		try {
			String s;
			while ((s=BR.readLine())!=null)
			{
				try{
					sleep(pause);
				}
				catch(Exception e)
				{}
				OUT.println(s);
				OUT.flush();
			}
			BR.close();
		}
		catch(Exception ez) {if (StatementProcessor.DEBUG){ez.printStackTrace();}}
	}
}

