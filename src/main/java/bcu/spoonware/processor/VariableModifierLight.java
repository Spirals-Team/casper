package bcu.spoonware.processor;


import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

@SuppressWarnings("rawtypes")
public class VariableModifierLight extends AbstractProcessor<CtLocalVariable>{

	@SuppressWarnings("unchecked")
	@Override
	public void process(CtLocalVariable element) {
		if(element.getParent() instanceof CtCatch 
				|| element.getParent() instanceof CtFor 
				|| element.getParent() instanceof CtForEach)
			return;
		if(!element.getType().isPrimitive())
			return;
		if(element.getDefaultExpression()==null)
			return;
		if(element.getDefaultExpression() instanceof CtLiteral)
			if(!(((CtLiteral)element.getDefaultExpression()).getValue()==null))
				return;
		try{
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(IConstants.fullyQualifiedClassName));
			execref.setSimpleName(IConstants.methodName);
			execref.setStatic(true);
			
			CtExpression assignment = element.getDefaultExpression();

			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{assignment}));

			CtTypeReference tmpref = getFactory().Core().clone(element.getType());
			if(!(tmpref instanceof CtArrayTypeReference)){
				tmpref = tmpref.box();
			}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
				((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(null);
			}
			tmpref.setActualTypeArguments(null);
			
			execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));
			element.setDefaultExpression(invoc);
		}catch(Throwable t){
			System.err.println("cannot resolve an assign");
		}
	}

}
