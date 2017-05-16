package dscript;

class Pause{

public static void spause(long seconds)
{
 long slp = seconds * 1000;
 try{Thread.sleep(slp);}catch(Exception e){}
}

public static void spause(double seconds)
{
 long slp = (long)(seconds * 1000.00);
 try {Thread.sleep(slp);}catch(Exception e){}
}

public static void mpause(long minutes)
{
 long slp = minutes * 60000;
 try{Thread.sleep(slp);}catch(Exception e){}
}

public static void mpause(double minutes)
{
 long slp = (long)(minutes*60000.000);
 try {Thread.sleep(slp);}catch(Exception e){}
}


}

 