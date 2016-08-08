package bcu.transformer.processors;

import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

public class NullLiteralProcessor extends AbstractProcessor<CtLiteral<?>>{
	@Override
	public boolean isToBeProcessed(CtLiteral<?> candidate) {
		return candidate.getValue()==null
				&& (
					candidate.getParent() instanceof CtReturn
					|| candidate.getParent() instanceof CtLocalVariable
					|| candidate.getParent() instanceof CtAssignment
					|| candidate.getParent() instanceof CtField
				);
	}

	@Override
	public void process(CtLiteral<?> arg0) {

		CtElement parent = arg0.getParent();

		CtExecutableReference execref = getFactory().Core().createExecutableReference();
		execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.NullLiteralResolver"));
		execref.setSimpleName("createNullGhost");
		execref.setStatic(true);

		CtTypeReference tmp = getType(arg0);

		CtExpression arg = null;
		CtFieldReference ctfe = new CtFieldReferenceImpl();
		ctfe.setSimpleName("class");
		ctfe.setDeclaringType(tmp.box());
		arg = new CtFieldAccessImpl();
		((CtFieldAccessImpl) arg).setVariable(ctfe);


		CtLiteral location = getFactory().Core().createLiteral();
		location.setValue(""+StringEscapeUtils.escapeJava(arg0.toString()+ " "+Helpers.nicePositionString(arg0.getPosition()))+"");
		location.setType(getFactory().Type().createReference(String.class));

		CtTypeReference tmpref = getFactory().Core().clone(tmp);

		CtInvocation invoc = getFactory().Core().createInvocation();
		invoc.setType(tmp);
		invoc.setExecutable(execref);
		invoc.setArguments(Arrays.asList(new CtExpression[]{arg0,arg,location}));
		execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));

		arg0.setParent(parent);
		arg0.replace(invoc);
	}

	private CtTypeReference<?> getType(CtLiteral<?> arg0) {
		if (arg0.getParent() instanceof CtReturn) {
			return arg0.getParent(CtMethod.class).getType();
		}
		if (arg0.getParent() instanceof CtLocalVariable) {
			return arg0.getParent(CtLocalVariable.class).getType();
		}
		if (arg0.getParent() instanceof CtAssignment) {
			return arg0.getParent(CtAssignment.class).getAssigned().getType();
		}
		if (arg0.getParent() instanceof CtField) {
			return arg0.getParent(CtField.class).getDefaultExpression().getType();
		}
		return arg0.getType();
	}


}
