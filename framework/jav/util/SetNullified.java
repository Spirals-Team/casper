package jav.util;

import java.util.Collection;
import java.util.Iterator;

import bcornu.nullmode.DebugInfo;
import bcornu.nullmode.DeluxeNPE;
import bcornu.nullmode.NullGhost;

@SuppressWarnings("rawtypes")
public class SetNullified implements java.util.Set, NullGhost{

private DebugInfo data = new DebugInfo();
	
	public SetNullified() {
		data.creation();
	
	}
	public void addData(String s){
		data.used(s);
	}
	
	@Override
	public int size() {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean isEmpty() {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean contains(Object o) {
		throw new DeluxeNPE(data);
	}

	@Override
	public Iterator iterator() {
		throw new DeluxeNPE(data);
	}

	@Override
	public Object[] toArray() {
		throw new DeluxeNPE(data);
	}

	@Override
	public Object[] toArray(Object[] a) {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean add(Object e) {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean remove(Object o) {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean containsAll(Collection c) {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean addAll(Collection c) {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new DeluxeNPE(data);
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new DeluxeNPE(data);
	}

	@Override
	public void clear() {
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
	@Override
	public void throwDNPE() {
		throw new DeluxeNPE(data);
	}
}
