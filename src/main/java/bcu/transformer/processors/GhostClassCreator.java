package bcu.transformer.processors;

import java.util.ArrayList;
import java.util.List;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import bcornu.nullmode.DebugInfo;
import bcornu.nullmode.NullGhost;

public class GhostClassCreator extends AbstractProcessor<CtClass> {

	public static final String FIELD_NAME = "DNPE_data";

	@Override
	public void process(CtClass arg0) {
		System.out.println(arg0.getSimpleName());
		// removing final
		arg0.getModifiers().remove(ModifierKind.FINAL);
		for (Object m : arg0.getAllMethods()) {
			CtMethod meth = (CtMethod)m; 
			meth.getModifiers().remove(ModifierKind.FINAL);
		}
		
		// cloning
		CtClass ghostClass = getFactory().Core().clone(arg0);

		ghostClass.setSimpleName(arg0.getSimpleName()+"Nullified");
		ghostClass.setSuperclass(arg0.getReference());
		// add marker interface
		ghostClass.addSuperInterface(getFactory().Type().createReference(NullGhost.class));		
		for (Object m : ghostClass.getAllMethods()) {
			CtMethod meth = (CtMethod)m; 
			// we don't override static methods
			if (meth.getModifiers().contains(ModifierKind.STATIC)) continue;
			CtCodeSnippetStatement stmt = getFactory().Core().createCodeSnippetStatement();
			stmt.setValue("throw new bcornu.nullmode.DeluxeNPE("+FIELD_NAME+")");
			List<CtStatement> l = new ArrayList<>();
			l.add(stmt);
			meth.getBody().setStatements(l);
		}
		addAddDataMethod(ghostClass);
		
		addDebugInfoField(ghostClass);
		
		ghostClass.setParent(arg0.getPackage());
		CtPackage p = getFactory().Package().getOrCreate(ghostClass.getPackage().getQualifiedName());
		p.addType(ghostClass);

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
