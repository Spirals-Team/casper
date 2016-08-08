package bcornu.nullmode;


@SuppressWarnings({"unchecked","rawtypes"})
public class ArgumentResolver {

	static boolean inside = false;

	public static <T> T setPassedArg(Object initialValue, Class clazz, String location) {
		return InceptionManager.createGhost(initialValue, clazz, location, "parameter ");
	}

}
