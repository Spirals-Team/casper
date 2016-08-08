package bcornu.nullmode;

import org.apache.commons.lang3.reflect.FieldUtils;

public class CallChecker {

	public static <T> T isCalled(T initialValue, Class clazz, String location) {
		// case 1
		if(initialValue instanceof NullGhost){
			((NullGhost)initialValue).addData("field access on null at "+location);
			try {
				throw new DeluxeNPE((DebugInfo)FieldUtils.readField(initialValue, "DNPE_data"));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		//case 2
		if(initialValue!=null)
			return (T)initialValue;

		// in the worst case, we can still do better than a NPE
		// we can give the location in the line
		throw new LocatedNPE(location);
	}

}

