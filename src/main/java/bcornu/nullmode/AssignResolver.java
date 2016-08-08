package bcornu.nullmode;


@SuppressWarnings({"unchecked","rawtypes"})
public class AssignResolver {

	static boolean inside = false;

	public static <T> T setAssigned(Object initialValue, Class clazz, String location) {
		return InceptionManager.createGhost(initialValue, clazz, location,"assigned null to ");
	}

}
