package bcu.transformer.processors;

import java.util.ArrayList;
import java.util.List;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import bcornu.nullmode.DebugInfo;
import bcornu.nullmode.NullGhost;

public class GhostClassCreator extends AbstractProcessor<CtClass> {

	public static final String FIELD_NAME = "DNPE_data";

	
	@Override
	public boolean isToBeProcessed(CtClass candidate) {
		return candidate.isTopLevel() && !candidate.getModifiers().contains(ModifierKind.ABSTRACT);
	}
	
	@Override
	public void process(CtClass arg0) {
		System.out.println(arg0.getSimpleName());
		// removing final
		arg0.getModifiers().remove(ModifierKind.FINAL);
		for (Object m : arg0.getAllMethods()) {
			CtMethod meth = (CtMethod)m; 
			meth.getModifiers().remove(ModifierKind.FINAL);
		}
		
		// public netsetdtypes
		for (Object m : arg0.getNestedTypes()) {
			CtType nested = (CtType)m; 
			nested.getModifiers().remove(ModifierKind.PRIVATE);
			nested.getModifiers().remove(ModifierKind.PROTECTED);
			nested.addModifier(ModifierKind.PUBLIC);
		}
		
		
		// cloning
		CtClass ghostClass = getFactory().Core().createClass();

		ghostClass.setSimpleName(arg0.getSimpleName()+"Nullified");
		ghostClass.setSuperclass(arg0.getReference());
		
		if (arg0.getConstructors().size()>0) {
			// handling Constructor
			CtConstructor constructor = getFactory().Core().createConstructor();
			constructor.addModifier(ModifierKind.PUBLIC);
			ghostClass.addConstructor(constructor);
			class SmallConstructorFound extends RuntimeException{};
			CtConstructor parent = null; 
			try {
			for (int i=0;i<10;i++) {
				for (Object o:arg0.getConstructors()) {
					CtConstructor x = (CtConstructor)o;
					if (x.getParameters().size()==i) {
						parent = x;
						throw new SmallConstructorFound();
					}
			}}
			}
			catch (SmallConstructorFound ignore) {}	
			parent.getModifiers().remove(ModifierKind.PRIVATE);
			parent.getModifiers().remove(ModifierKind.PROTECTED);
			parent.addModifier(ModifierKind.PUBLIC);
			CtCodeSnippetStatement stmt = getFactory().Core().createCodeSnippetStatement();
			String params = "";
			for (int j=0;j< parent.getParameters().size();j++) {
				String typeRef = ((CtParameter)parent.getParameters().get(j)).getType().getQualifiedName();
				if ("int".equals(typeRef)) {
					params+="0";
				} else	if ("boolean".equals(typeRef)) {
					params+="true";
				} else	if ("char".equals(typeRef)) {
					params+="(char)0";
				} else	if ("double".equals(typeRef)) {
					params+="0f";
				} else	if ("float".equals(typeRef)) {
					params+="0f";
				} else {
					params+="("+typeRef+")null";					
				}
				if (j<parent.getParameters().size()-1) {
					params+=", ";
				}				
			}
			stmt.setValue("super("+params+")");
			List<CtStatement> l = new ArrayList<>();
			l.add(stmt);
			constructor.setBody(getFactory().Core().createBlock());
			constructor.getBody().setStatements(l);
		}
		// add marker interface
		ghostClass.addSuperInterface(getFactory().Type().createReference(NullGhost.class));		
		for (Object m : arg0.getAllMethods()) {
			CtMethod meth = (CtMethod)getFactory().Core().clone(m); 
			// we don't override static methods
			if (meth.getModifiers().contains(ModifierKind.STATIC)) continue;
			if (meth.getModifiers().contains(ModifierKind.ABSTRACT)) continue;
			if (meth.getModifiers().contains(ModifierKind.FINAL)) continue;
			// no interface method
			if (meth.getBody()==null) continue;

			CtCodeSnippetStatement stmt = getFactory().Core().createCodeSnippetStatement();
			stmt.setValue("throw new bcornu.nullmode.DeluxeNPE("+FIELD_NAME+")");
			List<CtStatement> l = new ArrayList<>();
			l.add(stmt);
			meth.getBody().setStatements(l);
			ghostClass.addMethod(meth);
		}
		
		addAddToStringMethod(ghostClass);
		
		addAddDataMethod(ghostClass);
		
		addDebugInfoField(ghostClass);
		
		ghostClass.setParent(arg0.getPackage());
		String qualifiedName = ghostClass.getPackage().getQualifiedName();
		CtPackage p = getFactory().Package().getOrCreate(qualifiedName);
		p.addType(ghostClass);

	}


	private void addAddToStringMethod(CtClass ghostClass) {
		CtMethod m = getFactory().Core().createMethod();
		m.setSimpleName("toString");
		m.setType(getFactory().Type().STRING);
		m.addModifier(ModifierKind.PUBLIC);
		m.setBody(getFactory().Core().createBlock());
		CtCodeSnippetStatement stmt = getFactory().Core().createCodeSnippetStatement();
		
		System.out.println(Thread.currentThread().getStackTrace()[0]);
		
		
		//stmt.setValue("System.out.println(\"++\"+Thread.currentThread().getStackTrace()[4]);return null");
		
		stmt.setValue("throw new bcornu.nullmode.DeluxeNPE("+FIELD_NAME+")");

		List<CtStatement> l = new ArrayList<>();
		l.add(stmt);
		m.getBody().setStatements(l);
		ghostClass.addMethod(m);
	}

	
	private void addAddDataMethod(CtClass ghostClass) {
		CtMethod m = getFactory().Core().createMethod();
		m.setSimpleName("addData");
		m.setType(getFactory().Type().VOID_PRIMITIVE);
		m.addModifier(ModifierKind.PUBLIC);
		CtParameter<String> param = getFactory().Core().createParameter();
		param.setSimpleName("info");
		param.setType(getFactory().Type().createReference(String.class));
		m.addParameter(param);
		m.setBody(getFactory().Core().createBlock());
		CtCodeSnippetStatement stmt = getFactory().Core().createCodeSnippetStatement();
		stmt.setValue("System.out.println(info);");
		CtCodeSnippetStatement stmt2 = getFactory().Core().createCodeSnippetStatement();
		stmt2.setValue(FIELD_NAME+".used(info);");
		List<CtStatement> l = new ArrayList<>();
//		l.add(stmt);
		l.add(stmt2);
		m.getBody().setStatements(l);
		ghostClass.addMethod(m);
	}

	private void addDebugInfoField(CtClass ghostClass) {
		CtField f = getFactory().Core().createField();
		f.setSimpleName(FIELD_NAME);
		f.addModifier(ModifierKind.PUBLIC);
		f.setType(getFactory().Type().createReference(DebugInfo.class));
		CtCodeSnippetExpression exp = getFactory().Core().createCodeSnippetExpression();
		exp.setValue("new bcornu.nullmode.DebugInfo()");
		f.setDefaultExpression(exp);
		ghostClass.addField(f);
	}

}
