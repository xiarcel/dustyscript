package dscript;

import java.util.Vector;


class MathParser {
	
	public static final char[] OPS={'^','*','/','+','-'};
	public static final String[] OP={"^","*","/","+","-"};
	public static final boolean DEBUG=false;
	private String data;
	private VarContainer vc;
	private final static int[] no_args={-1};
	
	
	public MathParser (String d,VarContainer v)
	{
		data=d;
		//System.err.println(data);
		vc=v;
	}
	
	private int operatorCount(String s)
	{
		int count=0;
		for (int i=0; i<s.length();i++)
		{
			if (isOp(s.charAt(i)))
			{
				count++;
			}
		}
		return count;
	}
	
	private String trimEnds(String s)
	{
		if (s.startsWith("(")) {s=s.substring(1,s.length());}
		if (s.endsWith(")")) {s=s.substring(0,s.length()-1);}
		return s;
	}
	
	
	private boolean isOp(char c)
	{
		for (int i=0; i<OPS.length;i++)
		{
			if (c == OPS[i]) {return true;}
		}
		
		return false;
	}
	
	private boolean isOp(String s)
	{
		for (int i=0; i<OP.length; i++)
		{
			if (s.equals(OP[i])) {return true;}
		}
		return false;
	}

	private boolean onlyOperator(String s)
	{
		for (int i=0; i<OP.length;i++)
		{
			if (s.equals(OP[i])) {return true;}
		}
		return false;
	}

	public String atom(String data)
	{
		StringBuffer left=new StringBuffer();
		StringBuffer right=new StringBuffer();
		char oppy='\0';
		boolean oppy_found=false;
		for (int i=0; i<data.length(); i++)
		{
			char c=data.charAt(i);
			//if ((i==0) && (c=='-'))
			//{left.append(c);continue;}
				
			if ((i==0) && (c=='-'))
			{
				left.append(c);
				continue;
			}
		        if (isOp(c) && !oppy_found)
			{
				oppy=c;
				oppy_found=true;
			}
			else if (oppy_found) {right.append(c);}
			else {left.append(c);}
		}
		if (left.length()==0 && oppy=='-') {left.append('0');}
		String leftstring=left.toString();
		String rightstring=right.toString();
		leftstring=literalCheck(leftstring);
		rightstring=literalCheck(rightstring);
		//System.err.println("right:"+rightstring+"\nleft:"+leftstring);
		return atom(Double.parseDouble(leftstring),oppy,Double.parseDouble(rightstring));
	}
				
	private String literalCheck(String s)
	{
		if (vc ==null){return s;}
		String negate="";
		if (s.startsWith("-")) {negate="-";
					s=s.substring(1,s.length()-1);
					}
		if (vc.is_in(s))
		{
			Var v=vc.get(s);
			if (v.getType()==Var.THING)
			{
				ActionContainer act=v.getThing().getActionContainer();
				
				if (act.is_in("toDecimal",no_args,2))
				{
					s=negate+v.getDecimal();
				}
				else if (act.is_in("toInteger",no_args,1))
				{
					s=negate+v.getInteger();
				}
				else
				{
					s=negate+v.asString();
				}
			}
			//else if ( v.getType()==Var.ACTION ) {::process ActionVar::}
			else{
			s=negate+v.asString();
			}
		}
		else
		{s=negate+s;}
	if (DEBUG){System.err.println("literalCheck:"+s);}
	return s;
	}
	
		
	
	
	public String atom(double left, char op, double right)
	{
		switch(op)
		{
			
			case '^':
			{
				int rt=(int)right;
				if (rt == 0) {return "1.0";}
				if (rt < 0)
				{
					rt=(-1)*rt;double answer=1.0D;
					for (int i=0; i<rt;i++)
					{
						
						answer=(double)(answer/left);
					}
					return ""+answer;
				}
				double answer=left;
				for (int i=1; i<rt;i++)
				{
					
					answer=(double)(answer*left);
				}
				return ""+answer;
			}
			
						
			case '/':
			{
				return ""+(double)(left/right);
			}
			case '*':
			{
				return ""+(double)(left*right);
			}
			case '+':
			{
				return ""+(double)((1.000000)*(left+right));
			}
			case '-':
			{
				return ""+(double)(left-right);
			}
			default:{break;}
		}
		return "NaN";
	}

	public String evaluate()
	{
		return evaluate(data);
	}
	
	public String evaluate(String data)
	{
		if(DEBUG){System.err.println("operating on:"+data);}
		if (operatorCount(data) == 0) {return trimEnds(data);}
		String[] parts=tokenize(data);
		//while (parts.length>1) 
		//{
			for (int i=0; i<parts.length;i++) 
			{
				if ((operatorCount(parts[i]) == 1) && !onlyOperator(parts[i]))
				{
					parts[i]=atom(parts[i]);
				}
				else if (operatorCount(parts[i]) > 1)
				{
				parts[i]=evaluate(parts[i]);
				}
			}
			
		//	parts=retokenize(parts);
		//}
		
		dump(parts,"evaluated");
		return untokenize(parts); //wrap in paren
	}
	
	private String[] strip(String[] s)
	{
		Vector v=new Vector();
		for (int i=0; i<s.length;i++)
		{
			String m=s[i].trim();
			if (m.equals("")) {continue;}
			v.addElement(m);
		}
		Object[] o = v.toArray();
		s=new String[o.length];
		System.arraycopy(o,0,s,0,o.length);
		return s;
	}
	
	public String untokenize (String[] parts)
	{
		parts=strip(parts);
		Vector tokens=new Vector();
		for (int i=0; i<OP.length;i++)
		{
			String op=OP[i];
			dump(parts,"pre-"+i+":");
			for (int j=0; j<parts.length;j++)
			{
				
				if (DEBUG){System.err.println("["+i+"]:evaluating:"+parts[j]);}
				if (parts[j].equals(op) && (j+1)<parts.length && isOK(parts[j+1]) && isOK(parts[j-1]))
				{
					String lastpart=(String)tokens.elementAt(tokens.size()-1);
					String eval=lastpart+parts[j]+parts[j+1];
					if(DEBUG){System.err.println("Processing equation:"+eval);}
					
					parts[j]=atom(eval);
					if(DEBUG){System.err.println("Removing:"+(String)tokens.elementAt(tokens.size()-1));}
					tokens.removeElementAt(tokens.size()-1);
					tokens.addElement(parts[j]);
					if(DEBUG){System.err.println("added:"+parts[j]);}
					j++;
				}
				else {tokens.addElement(parts[j]);}
				
			}
			Object[] o = tokens.toArray();
			tokens.removeAllElements();
			parts=new String[o.length];
			System.arraycopy(o,0,parts,0,o.length);
			
		}
		return merge(parts);
	}
	
		
	public boolean isOK(String part)
	{
		if (operatorCount(part)==0) {return true;}
		if (operatorCount(part)==1 && part.indexOf("-") > -1) {return true;}
		return false;
	}
	
	
	public String[] tokenize()
	{
		return tokenize(data);
	}
	
	
	public String[] tokenize(String s)
	{
		Vector tokens=new Vector();
		StringBuffer sb=new StringBuffer();
		int paren=0;
		
		for (int i=0; i<s.length(); i++)
		{
			char c= s.charAt(i);
			if (isOp(c) && paren == 0)
			{
				if (sb.length()>0){
					tokens.addElement(sb.toString());
				}

				tokens.addElement(""+c);
				sb.setLength(0);
			}
			else if (c == '(') 
			{
				paren++;
				if (sb.length() != 0 && paren <= 1)
				{
					tokens.addElement(sb.toString());
					sb.setLength(0);
				}	
				if (paren > 1){sb.append(c);}
			}
			else if (c == ')')
			{
				paren--;
				if (paren == 0 && sb.length() != 0)
				{
					tokens.addElement(sb.toString());
					sb.setLength(0);
				}
				if (paren > 0) {
				sb.append(c);}
			}
			else if ( !isWSP(c) )
			{
				if (sb.length()==0 && c=='-') {sb.append('0');}
				sb.append(c);
			}
			
		}
	if (sb.length() > 0 ) {tokens.addElement(sb.toString());}
	Object[] o = tokens.toArray();
	String[] ret= new String[o.length];
	System.arraycopy(o,0,ret,0,o.length);
	dump(ret,"tokenization_complete");
	return ret;
	}
	
	private boolean isWSP(char c)
	{
		if ( c == ' ' || c == '\t' || c == '\r' || c=='\n' ) {return true;}
		return false;
	}
	
	private static String merge(String[] a)
	{
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<a.length; i++)
		{
			sb.append(a[i]);
		}
		return sb.toString();
	}
	
	public static void dump(String[] tokens)
	{
		dump(tokens,"token");
	}
	
	
	public static void dump(String[] tokens, String label)
	{
		if (!DEBUG) {return;}
		for (int i=0; i<tokens.length; i++)
		{
			System.out.println(label+"["+i+"]:"+tokens[i]);
		}
	}
	
	
				
	public static void main(String[] args)
	{
		MathParser mp=new MathParser(merge(args),null);
		System.out.println(mp.evaluate());
	}
	
}
			
