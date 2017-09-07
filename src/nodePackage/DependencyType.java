package nodePackage;

public class DependencyType {
	
	private static int mandatory = 64; // 1000000
	private static int optional = 32;  // 0100000
	private static int possible = 16;  // 0010000
	private static int and = 8;        // 0001000
	private static int or = 4;         // 0000100
	private static int not = 2;        // 0000010
	private static int known = 1;      // 0000001
	
	public static int getMandatory()
	{
		return mandatory;
	}
	
	public static int getOptional()
	{
		return optional;
	}
	
	public static int getPossible()
	{
		return possible;
	}
	
	public static int getAnd()
	{
		return and;
	}
	
	public static int getOr()
	{
		return or;
	}
	
	public static int getNot()
	{
		return not;
	}
	
	public static int getKnown()
	{
		return known;
	}
	

}
