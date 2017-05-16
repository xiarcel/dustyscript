package dscript;

import java.io.*;

class FileOp{
 static String fs =System.getProperty("file.separator");
 static String bdir = System.getProperty("user.dir")+fs+"users"+fs+"data"+fs;
 static String LS =System.getProperty("line.separator");


 private static String read(String fname, Output OUT)
	{
	if(StatementProcessor.DEBUG){OUT.println("base dir:\n"+bdir,2);}
	try{
	    BufferedReader br = new BufferedReader(new FileReader(new File(bdir+fname)));
	    String contents="";
	    String s;
	    while ((s=br.readLine())!=null)
		{
		contents=contents+s+LS;
		}
          br.close();
	    return contents;
	    }
          catch(Exception e){}
	return "~";
	}

private static boolean write(String fname, String cont)
	{
	try{
	FileWriter fw =new FileWriter(new File(bdir+fname));
	fw.write(cont);
	fw.close();
	return true;
	}
	catch(Exception e){}
	return false;
	}

public static boolean readFile(Statement s, VarContainer vc)
	{
	String[] parts = s.getParts();
	Output OUT = vc.getOutput();
	if (parts.length < 4)
	{OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"This 'read' statement is too short",s),
			vc,s.getAttempting());
	return false;}
		
	parts[3] = StringFunct.pullStringLiteral(parts[3],s,vc);

	String contents = read(parts[3],OUT);
	if (contents.equals("~")){return false;}
	Var v=new Var(contents,parts[1]);
	if (vc.is_in(parts[1]))
		{
		
		vc.replace(parts[1],v);
		
		}
		else{vc.add(v);}
	return true;
	}

public static boolean writeFile(Statement s, VarContainer vc)
	{
	String[] parts = s.getParts();
	Output OUT = vc.getOutput();
	if (parts.length<4){
	OUT.println(new DSOut(DSOut.ERR_OUT,-1,"This write statement is too short",s),
				vc,s.getAttempting());return false;}
 	parts[1]=StringFunct.pullStringLiteral(parts[1],s,vc);
	parts[3]=StringFunct.pullStringLiteral(parts[3],s,vc);
	return write(parts[3],parts[1]);
	}

}
