package bcornu.nullmode;

public class ComparizonOperator {
	
	public static boolean isNull(Object o){
		if(o==null)
			return true;
		if(o.getClass().getName().endsWith("Nullified"))
			return true;
		return false;
	}	
	public static boolean isNotNull(Object o){
		return ! isNull(o);
	}
	
	public static boolean instanceOf(Object o, Class<?> c){
		if(isNull(o))
			return false;
		else
			return c.isAssignableFrom(o.getClass());
	}
}
