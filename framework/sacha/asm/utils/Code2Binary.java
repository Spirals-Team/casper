package sacha.asm.utils;

	@SuppressWarnings("rawtypes")
public class Code2Binary {
		
	public static Class getCorrespondingClass(String tmp) throws ClassNotFoundException {
			switch (tmp) {
				case "void":
					return null;
				case "boolean":
					return Boolean.TYPE;
				case "char":
					return Character.TYPE;
				case "byte":
					return Byte.TYPE;
				case "short":
					return Short.TYPE;
				case "int":
					return Integer.TYPE;
				case "float":
					return Float.TYPE;
				case "long":
					return Long.TYPE;
				case "double":
					return Double.TYPE;
			}
			tmp = tmp.substring(1).replaceAll("/", ".");
			return Class.forName(tmp);
		}
}

