package bcornu.nullmode;


@SuppressWarnings({"unchecked","rawtypes"})
public class AssignResolver {
	
	static boolean inside = false;

	public static <T> T setAssigned(Object initialValue, Class clazz, String location) {

		if(clazz!=null && clazz.equals(Long.class)){
			if(initialValue instanceof Integer){
				return (T) ((Long)((Integer) initialValue).longValue());
			}
		}
		if(initialValue!=null){
			try{
				if (initialValue instanceof NullGhost){
//					try{
					((NullGhost)initialValue).addData("assigned null to "+ location);
//					}catch(NullPointerException npe){
//						rteu
//					}
				}
				return (T)initialValue;
			}catch(ClassCastException cce){
				System.err.println("cast problem, cannot continue");
				cce.printStackTrace();
				System.exit(-1);
			}
		}
		if(location!=null && location.contains("user")){
			System.out.println("here");
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
			((NullGhost)res).addData("assigned null to "+location);
		}
		return res;
		
	}

}
