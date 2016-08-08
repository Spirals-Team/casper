package bcornu.nullmode;

public class ExternalCallManager {

	public static <T> T exorcise(T initialValue, Class clazz, String location) {

		if(initialValue instanceof NullGhost){
			return null;
		}

		return initialValue;
	}

}

