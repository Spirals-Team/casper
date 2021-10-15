package sacha.asm.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnnotationRemapper;
import org.objectweb.asm.commons.FieldRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

@SuppressWarnings("deprecation")
public class NullClassCreator extends ClassVisitor implements Opcodes{

    protected Remapper remapper;

    protected String oldClassName;
    
    private String from;

    private String to;
    
    private boolean isInterface;

	private boolean initAlreadyExists = false;

    public NullClassCreator(final ClassVisitor cv,
            final String from, final String to, boolean isInterface) {
        super(Opcodes.ASM4, cv);
        this.from = from;
        this.to = to;
        this.isInterface = isInterface;
    }

    @Override
    public void visit(int version, int access, final String name, String signature,
            final String superName, String[] interfaces) {
    	initAlreadyExists = false;
        this.remapper = new Remapper() {
        	@Override
        	public String map(String arg) {
        		if(arg.equals(from))
	        		return to;
	    		return arg;
        	}};
        this.oldClassName = name;
	      if((access & Opcodes.ACC_ABSTRACT) != 0)
	    	  access &= ~Opcodes.ACC_ABSTRACT;
	      if((access & Opcodes.ACC_INTERFACE) == 0)
	        super.visit(version, access, remapper.mapType(name), remapper
	                .mapSignature(signature, false), name,
	                new String[]{"bcornu/nullmode/NullGhost"});
	      else{
	    	  access &= ~Opcodes.ACC_INTERFACE;
		        super.visit(version, access, remapper.mapType(name), remapper
		                .mapSignature(signature, false), "java/lang/Object",
		                new String[]{"bcornu/nullmode/NullGhost",name});
	      }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor av;
        av = super.visitAnnotation(remapper.mapDesc(desc), visible);
        return av == null ? null : createRemappingAnnotationAdapter(av);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
//	    if((access & Opcodes.ACC_STATIC) != 0){
//	    	return super.visitField(access, name, desc, signature, value);
//	    }
        FieldVisitor fv = super.visitField(access,
        		remapper.mapFieldName(oldClassName, name, desc),
        		remapper.mapDesc(desc), remapper.mapSignature(signature, true),
        		remapper.mapValue(value));
        return fv == null ? null : createRemappingFieldAdapter(fv);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
    	MethodVisitor mv;
	    if((access & Opcodes.ACC_ABSTRACT) != 0)
	    	access &= ~Opcodes.ACC_ABSTRACT;
	    if((access & Opcodes.ACC_INTERFACE) != 0)
	    	access &= ~Opcodes.ACC_INTERFACE;
    	if(name.equals("<clinit>")){
    		mv = super.visitMethod(access, remapper.mapMethodName(
                oldClassName, name, desc), remapper.mapMethodDesc(desc), remapper.mapSignature(
                signature, false),
                exceptions == null ? null : remapper.mapTypes(exceptions));
    		return createRemappingMethodAdapter(access, remapper.mapMethodDesc(desc), mv);
		}else if(name.equals("<init>")){
			if(initAlreadyExists && desc.equals("()V")){
				return null;
			}
    		mv = cv.visitMethod(ACC_PUBLIC, "<init>", desc, null, null);
    		mv.visitCode();
    		mv.visitVarInsn(ALOAD, 0);

    		Type[] args = Type.getArgumentTypes(desc);
    		for (Type type : args) {
    			switch (type.getSort()) {
//				case Type.BOOLEAN:
//					break;
//				case Type.BYTE:
//					break;
//				case Type.CHAR:
//					break;
//				case Type.SHORT:
//				break;
				case Type.DOUBLE:
					mv.visitInsn(DCONST_0);
					break;
				case Type.FLOAT:
					mv.visitInsn(FCONST_0);
					break;
				case Type.INT:
					mv.visitInsn(ICONST_0);
					break;
				case Type.LONG:
					mv.visitInsn(LCONST_0);
					break;
				default:
					mv.visitInsn(ACONST_NULL);
					break;
				}
			}
			mv.visitMethodInsn(INVOKESPECIAL, oldClassName , "<init>", desc);

			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "bcornu/nullmode/DebugInfo");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "bcornu/nullmode/DebugInfo", "<init>", "()V");
			mv.visitFieldInsn(PUTFIELD, remapper.mapType(oldClassName), "DNPE_data", "Lbcornu/nullmode/DebugInfo;");
			
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, remapper.mapType(oldClassName), "DNPE_data", "Lbcornu/nullmode/DebugInfo;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "bcornu/nullmode/DebugInfo", "creation", "()V");
			
			mv.visitInsn(RETURN);
    		mv.visitMaxs(0,0);
    		mv.visitEnd();
    		
			initAlreadyExists =true;
    		
    		return null;
    	}else if((access & Opcodes.ACC_STATIC) != 0){
    		mv = super.visitMethod(access, remapper.mapMethodName(
                    oldClassName, name, desc), remapper.mapMethodDesc(desc), remapper.mapSignature(
                    signature, false),
                    exceptions == null ? null : remapper.mapTypes(exceptions));
    		return createRemappingMethodAdapter(access, remapper.mapMethodDesc(desc), mv);
	    }else{
	    	mv=cv.visitMethod(access, name, desc, signature, exceptions);
	    	mv.visitCode();
	    	mv.visitTypeInsn(NEW, "bcornu/nullmode/DeluxeNPE");
	    	mv.visitInsn(DUP);
	    	mv.visitVarInsn(ALOAD, 0);
	    	mv.visitFieldInsn(GETFIELD, remapper.mapType(oldClassName), "DNPE_data", "Lbcornu/nullmode/DebugInfo;");
	    	mv.visitMethodInsn(INVOKESPECIAL, "bcornu/nullmode/DeluxeNPE", "<init>", "(Lbcornu/nullmode/DebugInfo;)V");
	    	mv.visitInsn(ATHROW);
	    	mv.visitMaxs(0,0);
	    	mv.visitEnd();
			return null;
    	}
    }
    
    @Override
    public void visitEnd() {
    	FieldVisitor fv;
    	MethodVisitor mv;
    	
    	fv = cv.visitField(ACC_PUBLIC, "DNPE_data", "Lbcornu/nullmode/DebugInfo;", null, null);
    	fv.visitEnd();
    	
    	mv = cv.visitMethod(ACC_PUBLIC, "addData", "(Ljava/lang/String;)V", null, null);
    	mv.visitCode();
    	mv.visitVarInsn(ALOAD, 0);
    	mv.visitFieldInsn(GETFIELD, remapper.mapType(oldClassName), "DNPE_data", "Lbcornu/nullmode/DebugInfo;");
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKEVIRTUAL, "bcornu/nullmode/DebugInfo", "used", "(Ljava/lang/String;)V");
    	mv.visitInsn(RETURN);
    	mv.visitMaxs(2, 2);
    	mv.visitEnd();
    	
    	mv = cv.visitMethod(ACC_PUBLIC, "throwDNPE", "()V", null, null);
    	mv.visitCode();
    	mv.visitTypeInsn(NEW, "bcornu/nullmode/DeluxeNPE");
    	mv.visitInsn(DUP);
    	mv.visitVarInsn(ALOAD, 0);
    	mv.visitFieldInsn(GETFIELD, remapper.mapType(oldClassName), "DNPE_data", "Lbcornu/nullmode/DebugInfo;");
    	mv.visitMethodInsn(INVOKESPECIAL, "bcornu/nullmode/DeluxeNPE", "<init>", "(Lbcornu/nullmode/DebugInfo;)V");
    	mv.visitInsn(ATHROW);
    	mv.visitMaxs(3, 1);
    	mv.visitEnd();
    	
//    	if(!initAlreadyExists){
//	    	mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
//			mv.visitCode();
//			mv.visitVarInsn(ALOAD, 0);
//	
//			mv.visitMethodInsn(INVOKESPECIAL, isInterface?"java/lang/Object":oldClassName , "<init>", "()V");
//
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitTypeInsn(NEW, "bcornu/nullmode/DebugInfo");
//			mv.visitInsn(DUP);
//			mv.visitMethodInsn(INVOKESPECIAL, "bcornu/nullmode/DebugInfo", "<init>", "()V");
//			mv.visitFieldInsn(PUTFIELD, remapper.mapType(oldClassName), "DNPE_data", "Lbcornu/nullmode/DebugInfo;");
//			
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitFieldInsn(GETFIELD, remapper.mapType(oldClassName), "DNPE_data", "Lbcornu/nullmode/DebugInfo;");
//			mv.visitMethodInsn(INVOKEVIRTUAL, "bcornu/nullmode/DebugInfo", "creation", "()V");
//			
//			mv.visitInsn(RETURN);
//    		mv.visitMaxs(0,0);
//    		mv.visitEnd();
//    		
//			initAlreadyExists =true;
//    	}
    	super.visitEnd();
    }

    @Override
    public void visitInnerClass(String name, String outerName,
            String innerName, int access) {
        super.visitInnerClass(remapper.mapType(name), outerName == null ? null
                : remapper.mapType(outerName), innerName, access);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(remapper.mapType(owner), name == null ? null
                : remapper.mapMethodName(owner, name, desc),
                desc == null ? null : remapper.mapMethodDesc(desc));
    }

    protected FieldVisitor createRemappingFieldAdapter(FieldVisitor fv) {
        return new FieldRemapper(fv, remapper);
    }

    protected MethodVisitor createRemappingMethodAdapter(int access,
            String newDesc, MethodVisitor mv) {
        return new MethodRemapper(mv, remapper);
    }

    protected AnnotationVisitor createRemappingAnnotationAdapter(
            AnnotationVisitor av) {
        return new AnnotationRemapper(av, remapper);
    }

//    private static Pattern allParamsPattern = Pattern.compile("(\\(.*?\\))");
//    private static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|(:?L[^;]+;))");
//
//
//    int getMethodParamCount(String methodRefType) {
//        Matcher m = allParamsPattern.matcher(methodRefType);
//        if (!m.find()) {
//            throw new IllegalArgumentException("Method signature does not contain parameters");
//        }
//        String paramsDescriptor = m.group(1);
//        Matcher mParam = paramsPattern.matcher(paramsDescriptor);
//
//        List<Integer> l = new ArrayList<>();
//        int count = 0;
//        while (mParam.find()) {
//        	String param = mParam.group();
//        	switch (param) {
//			case "":
//				
//				break;
//
//			default:
//				l.add(ACONST_NULL);
//				break;
//			}
//        }
//        return count;
//    }
}