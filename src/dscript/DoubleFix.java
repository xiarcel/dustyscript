package dscript;

class DoubleFix{

public static double fix(double d, int point)
{
 if (point <= 0){return d;}
 String lft=""; String rt="";
 String doub = ""+d;

 boolean ol=true;
 for (int i=0; i<doub.length();i++)
	{
	char c= doub.charAt(i);
	if (c=='.'){ol=false;continue;}
	if (ol) {lft=lft+c;}
	else{rt=rt+c;}
	}
 if (point > (rt.length()-1))
	{
	return d;}
 String nwright="";
 for (int j=0; j<(point-1);j++)
 {nwright=nwright+rt.charAt(j);}
 int rtmost = Integer.parseInt(""+rt.charAt((point-1)));
 int comp = Integer.parseInt(""+rt.charAt(point));
 if (comp >= 5){rtmost++;}

 nwright=nwright+rtmost;
 String nwdouble = lft+"."+nwright;
 return Double.parseDouble(nwdouble);
}

public static int getDecLength(String s)
{
int i = s.indexOf(".");
if (i<0){return i;}

String sub = new String(s.substring(i,s.length()));

return (sub.length()-1);
}

public static int getDecLength(double d)
{
return getDecLength(""+d);
}


public static double matchLength(double d)
{
/*returns a double equal to 1.<length_in_zeros>*/
int i = getDecLength(""+d);
String newdoub="1.";
for (int j=0;j<i;j++)
{newdoub=newdoub+"0";}
return Double.parseDouble(newdoub);
}


}
