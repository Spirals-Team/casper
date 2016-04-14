package bcu.transformer.processors;

import spoon.reflect.cu.SourcePosition;

public class Helpers {
	static String nicePositionString(SourcePosition i) {
		if (i == null)
			return "";
		return "(" + i.getFile().getName() + ":" + i.getLine() + ")";
	}
}
