package widget;
import java.util.HashMap;
import dscript.connect.Dustyable;

public class DEventListener extends Dustyable {
	
	
	
	public DEventListener()
	{
		//System.err.println("DEventListener()");
	}
	
	public boolean processDustyable(Dustyable d, String[] args)
	{
		/*
		System.err.println("procD:"+args.length);
		for (int j=0;j<args.length;j++)
		{
		System.err.println("args["+j+"]:"+args[j]);
		}
		*/
		if (!d.isComponent()) {return false;}
		if (args.length < 2 ) {return false;}
		if (args[1].length()<1){return false;}
	
		if (args[0].equals("listen"))
		{
			//System.err.println("procD--listen\nargs[0]"+args[0]+"\nargs[1]"+args[1]);
			char ch = args[1].charAt(0);
			return addListener(this,d,ch);
		
		}
		else if (args[0].equals("unlisten"))
		{
			char ch = args[1].charAt(0);
			return removeListener(this,d,ch);
		}
		
		return false;
	}
}

