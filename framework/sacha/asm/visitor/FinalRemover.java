package sacha.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class FinalRemover extends ClassVisitor implements Opcodes{

	public FinalRemover(ClassVisitor cv) {
		super(ASM4, cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
    	if( (access & Opcodes.ACC_FINAL) >0){
//    		System.out.println("removing final for a class");
    	}
		super.visit(version, access & ~ACC_FINAL, name, signature, superName, interfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv=super.visitMethod(access & ~ACC_FINAL, name, desc, signature, exceptions);
		return mv;
	}
}
