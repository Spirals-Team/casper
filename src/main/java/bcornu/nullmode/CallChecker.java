package bcornu.nullmode;

public class CallChecker {
	
	public static <T> T isCalled(Object initialValue, Class clazz, String location) {
		if(initialValue instanceof NullGhost){
			((NullGhost)initialValue).throwDNPE();
			throw new LocatedNPE(location);
		}
		if(initialValue!=null)
			return (T)initialValue;
		throw new LocatedNPE(location);
	}

}

