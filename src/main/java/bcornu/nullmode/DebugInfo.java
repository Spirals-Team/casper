package bcornu.nullmode;

import java.util.ArrayList;
import java.util.List;

public class DebugInfo {
	
	private String initLocation;
	private String excLocation;
	private StackTraceElement[] creationTrace;
	private List<String> events = new ArrayList<>();
	private List<StackTraceElement[]> stacks = new ArrayList<>();
	
	
	private StackTraceElement[] getTrace(){
		boolean ready = false;
		boolean go = false;
		List<StackTraceElement> stes = new ArrayList<>();
		for (StackTraceElement ste :new Exception().getStackTrace()) {
			if(go){
				if(ste.getClassName().startsWith("bcornu.nullmode")){
					stes.clear();
					go=false;
				}else{
					stes.add(ste);
				}
			}else{
				if(ste.getClassName().startsWith("bcornu.nullmode")){
					ready=true;
				}else if(ready){
					stes.add(ste);
					go=true;
				}
			}
		}
		return stes.toArray(new StackTraceElement[0]);
	}
	
	public void creation() {
		this.creationTrace = getTrace();
	}

	public void used(String s) {
		events.add(s);
		stacks.add(getTrace());
	}

	public StackTraceElement[] getCreation() {
		return creationTrace;
	}

	public String getFirst() {
		if(events.size()==0)
			return null;
		return events.get(events.size()-1);
	}

	public String getLast() {
		return events.get(0);
	}

	public int nbSteps() {
		return events.size();
	}

	public String get(int i) {
		return events.get(i);
	}

	public StackTraceElement[] getStack(int i) {
		return stacks.get(i);
	}
	
	
	
	

}
