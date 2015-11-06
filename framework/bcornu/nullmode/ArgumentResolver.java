package bcornu.nullmode;


@SuppressWarnings({"unchecked","rawtypes"})
public class ArgumentResolver {
	
	static boolean inside = false;

	public static <T> T setPassedArg(Object initialValue, Class clazz, String location) {
		if(initialValue!=null){
			try{
				if (initialValue instanceof NullGhost){
					((NullGhost)initialValue).addData(location);
				}
				return (T)initialValue;
			}catch(ClassCastException cce){
				System.err.println("omfg");
				System.exit(-1);
			}
		}
		if(inside)
			return null;
		if(clazz  == null){
			System.err.println("assign generic clazz?");
			return null;
		}
		inside=true;
		T res=(T) NullInstanceManager.getNullInstance(clazz);
		inside=false;
		if (res instanceof NullGhost){
			((NullGhost)res).addData(location);
		}
		return res;
		
	}

}
