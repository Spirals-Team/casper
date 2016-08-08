package bcornu.nullmode;


@SuppressWarnings({"unchecked","rawtypes"})
public class NullLiteralResolver {

	static boolean inside = false;

	public static <T> T createNullGhost(Object initialValue, Class clazz, String location) {
		return InceptionManager.createGhost(initialValue, clazz, location, "null initialized ");
	}

}
