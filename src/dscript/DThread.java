package dscript;
import java.io.InputStream;

class DThread {
	
private static long COUNTER=0;
private long count;

private SPThread thread;
private StatementProcessor state;
private String threadcount ="";
private Var mevar;
private boolean hibernating;

public DThread(SPThread thd, StatementProcessor sp, long tc)
{
count =COUNTER; COUNTER++;
state = sp; thread=thd;
threadcount=""+tc;
}

public DThread(SPThread thd, StatementProcessor sp, String tc)
{state=sp; thread=thd; threadcount=tc;}


public DThread()
{
thread=new SPThread();
try {
Output OUT=new Output(); InputStream IN = System.in;
state =new StatementProcessor("",new VarContainer(OUT),new ActionContainer(OUT),
			new ThingTypeContainer(OUT,IN),OUT,IN);
    }
	catch(Exception e){state=null;}
threadcount="-1";
}


public String getHashValue()
{
	return "dthread:"+count;
}

public Thread getThread()
{
return thread;
}

public StatementProcessor getStatementProcessor()
{
return state;
}

public String getThreadCount()
{
return threadcount;
}

public void kill()
{
state.BREAKOUT=true;
/*should terminate out of 'as long as loop'*/
 
}

public void setVarReference(Var v)
{

mevar = v;
if (state != null)
	{state.setThreadVar(mevar);}

}


}
