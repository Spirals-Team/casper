package bcornu.nullmode;

import java.io.PrintWriter;


@SuppressWarnings("serial")
public class DeluxeNPE extends NullPointerException {

	public DebugInfo data;

	final private StackTraceElement lastStackTraceElement;

	public DeluxeNPE(DebugInfo data) {
		this.data = data;

		StackTraceElement[] old = super.getStackTrace();
		lastStackTraceElement= old[1];
		StackTraceElement[] res = new StackTraceElement[old.length-1];
		for (int i =1;i<old.length;i++) {
			res[i-1]=old[i];
		}
		setStackTrace(res);

		this.data.used(lastEvent());
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
	}

	@Override
	public String toString() {
		if (data == null) {
			return super.toString();
		}
		String s="";
		for (int i = 0;i<data.nbSteps();i++) {
			s+=""+data.get(i)+"\n";
		}

		return s;
		//		try{
		//return super.toString()+" from "+ data.getFirst()+"\n"+NullInstanceManager.printNb();
//		}catch(Throwable t){
//			t.printStackTrace(System.out);
//			return "cannot provide data";
//		}
	}

	private String lastEvent() {
		return "throws NPE "+"at "+lastStackTraceElement.toString();
	}

	public static <T> T setPassedArg(Object initialValue, Class clazz, String location) {
		return InceptionManager.createGhost(initialValue, clazz, location, "parameter ");
	}

//	@Override
//	public synchronized Throwable getCause() {
//		Null e = null;
//		for (int i = 0;i<data.nbSteps();i++) {
//			e =new Null("from "+data.get(i),e);
//			try{
//				e.setStackTrace(data.getStack(i));
//			}catch(NullPointerException npe){
//				System.err.println("cannot set the creation trace");
//			}
//		}
//		return e;
//	}


}
