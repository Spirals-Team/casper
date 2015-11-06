package bcornu.nullmode;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import sacha.reflect.classloading.PermissiveClassLoader;

@SuppressWarnings({"all"})
public class NullInstanceManager {

	private static PermissiveClassLoader pClassLoader;
	private static int i = 0;

//	public static void call(){
//		System.out.println("call on null");
//		throw new NullPointerException();
//	}
	

	private static List<Class> notGhostable = new ArrayList<>();
	
	public static Object getNullInstance(Class clazz){
//		if(clazz.getCanonicalName().contains("SubHyperplane")){
//			System.out.println("here");
//		}
		i++;
		try{
			if(notGhostable.contains(clazz)){
				return null;
			}
			Class correspondingNullClass = getClassLoader().loadNullClass(clazz);
			if(correspondingNullClass==null){
				System.err.println(clazz.getCanonicalName()+" cannot be nullInstanciated");
				notGhostable.add(clazz);
				return null;
			}
			try {
				Object o = correspondingNullClass.newInstance();
				return o;
			} catch (Exception e) {
				for (Constructor ctr : correspondingNullClass.getConstructors()) {
					List<Object> args = new ArrayList<>();
					for (Class claz : ctr.getParameterTypes()) {
						if(claz.isPrimitive()){
							if(claz.equals(int.class)){
								args.add(0);
								continue;
							}
							if(claz.equals(double.class)){
								args.add(0d);
								continue;
							}
							if(claz.equals(float.class)){
								args.add(0f);
								continue;
							}
							if(claz.equals(long.class)){
								args.add(0l);
								continue;
							}
							args.add(null);
						}else{
							args.add(null);
						}
					}
					Object[] tmp = args.toArray(new Object[0]);
					try{
						ctr.setAccessible(true);
						return ctr.newInstance(tmp);
					}catch(Throwable t){
//						t.printStackTrace();
					}
				}
				notGhostable.add(clazz);
				return null;
				
			}
			
		}catch(Throwable t){
//			System.err.println("class :"+clazz.getCanonicalName());
//			t.printStackTrace();
			notGhostable.add(clazz);
			return null;
		}
	}
	
	public static String printNb(){
		return ""+i;
	}

	private static PermissiveClassLoader getClassLoader() {
		if(pClassLoader==null)
			pClassLoader = (PermissiveClassLoader) ClassLoader.getSystemClassLoader();
		return pClassLoader;
	}
}
