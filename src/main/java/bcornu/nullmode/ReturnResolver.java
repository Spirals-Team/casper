package bcornu.nullmode;

@SuppressWarnings({"unchecked","rawtypes"})
public class ReturnResolver {

	public static <T> T setReturn(Object initialValue, Class clazz, String location) {
 		return InceptionManager.createGhost(initialValue, clazz, location, "returned null in method ");
	}

}
