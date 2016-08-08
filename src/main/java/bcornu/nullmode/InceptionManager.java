package bcornu.nullmode;

import java.util.Stack;


@SuppressWarnings({"unchecked","rawtypes"})
public class InceptionManager {

	static Stack<String> inside = new Stack<>();

	public static <T> T createGhost(Object initialValue, Class clazz, String location, String type) {
		final String data = type+location;
		if(initialValue!=null){
			try{
				if (initialValue instanceof NullGhost){
					((NullGhost)initialValue).addData(data);
				}
				return (T)initialValue;
			}catch(ClassCastException cce){
				System.err.println("omfg");
				System.exit(-1);
			}
		}
		if(inside.size()>0)
			return (T)initialValue;
		if(clazz  == null){
			System.err.println("assign generic clazz?");
			return null;
		}
		inside.push(location);
		T res=(T) NullInstanceManager.getNullInstance(clazz);
		inside.pop();
		if (res instanceof NullGhost){
			((NullGhost)res).addData("inception: "+data);
		}
		return res;

	}

}
