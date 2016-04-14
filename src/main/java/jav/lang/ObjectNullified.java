package jav.lang;

import bcornu.nullmode.DebugInfo;
import bcornu.nullmode.DeluxeNPE;
import bcornu.nullmode.NullGhost;

public class ObjectNullified implements NullGhost {


	private DebugInfo data = new DebugInfo();
	
	public ObjectNullified() {
		data.creation();
	
	}
	public void addData(String s){
		data.used(s);
	}
	
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new DeluxeNPE(data);
	}
	
	@Override
	public boolean equals(Object obj) {
		throw new DeluxeNPE(data);
	}
	
	@Override
	protected void finalize() throws Throwable {
		throw new DeluxeNPE(data);
	}
	
	@Override
	public int hashCode() {
		throw new DeluxeNPE(data);
	}
	
	@Override
	public String toString() {
		throw new DeluxeNPE(data);
	}
}
