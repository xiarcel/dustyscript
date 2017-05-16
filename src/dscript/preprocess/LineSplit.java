package dscript.preprocess;

/*this class splits tagged documents into string[]s*/
  

 import java.util.Vector;

    public class LineSplit{
   
      boolean debug = false;
      private String log,key,skey,ekey;
      private int offset;
   
      public LineSplit ()
      {
         key ="";
         log ="";
      }		
   
      public void setKey(String k_ey)
      {key = k_ey;
         skey = new String("<"+key+">");
         ekey = new String("</"+key+">");
         offset = skey.length();
      }
   
   
      public void setLog(String l_og)
      {
         log = l_og;
      }
   
      private String[] split_()
      {  String[] result;
         Vector v= new Vector();
         int point = 0;
         int ln = log.length(); 
         int counter =0;
         int s,e;   
         while(point<ln)
         { 
            s=log.indexOf(skey,point);
            if (s==-1){
               break;}
            s=s+offset;
            e=log.indexOf(ekey,s);
         
            try{add(v,s,e);} 
               catch(Exception ez){
                  System.out.println(ez.toString());}
            point = e; 
            e = 0; s = 0;
         }
      
         if (v.size() < 1){result = new String[0];
            return result;}
         result = new String[v.size()];
         for (int i=0; i<result.length;i++)
         {
            try{result[i] = (String)v.elementAt(i);
            }
               catch(ClassCastException cce){
                  result[i] ="";}
         }
         return result;      
      }
      public String ssplit(String k_ey, String l_og)
      {
      /*returns the first occurrance of a 'key' tag's contents*/
         setKey(k_ey);setLog(l_og);
         return single();
      }
   
      public String[] split(String k_ey)
      {
         setKey(k_ey);
         return split_();
      }
   
      public String[] split(String k_ey, String l_og)
      {setLog(l_og); setKey(k_ey); 
         if (debug){System.out.println("k_ey:"+k_ey);}
         return split_();}
   
   private String single()
   {
    String it ="";
    int s_,e_;
    try{
    s_ = log.indexOf(skey,0);
    s_=s_+offset;
    e_ = log.indexOf(ekey,s_);
    it = log.substring(s_,e_);
    }
    catch(Exception e){it="";}
    return it;
   }
   
      public void add(Vector c,int st, int en)
      {
         c.addElement(new String(log.substring(st,en)));}
   
   }


