package jav.util;

import java.util.Comparator;

import bcornu.nullmode.DebugInfo;
import bcornu.nullmode.DeluxeNPE;

public class ComparatorNullified<T> implements Comparator<T> {


	private DebugInfo data = new DebugInfo();
	
	public ComparatorNullified() {
		data.creation();
	
	}
	public void addData(String s){
		data.used(s);
	}
	
	@Override
	public int compare(T o1, T o2) {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean equals(Object obj) {
		throw new DeluxeNPE(data);
	}
	
	@Override
	public int hashCode() {
		throw new DeluxeNPE(data);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new DeluxeNPE(data);
	}
	
	@Override
	public String toString() {
		throw new DeluxeNPE(data);
	}
	
}

