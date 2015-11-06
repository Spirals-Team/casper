package bcu.spoonware.processor;


import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;

import bcu.utils.NameResolver;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

@SuppressWarnings("rawtypes")
public class AssignmentModifierLight extends AbstractProcessor<CtAssignment>{
	@SuppressWarnings("unchecked")
	@Override
	public void process(CtAssignment element) {
		try{
		if(element.getAssigned().getType().isPrimitive() && element.getAssignment()==null)
			return;
		if(element.getAssignment() instanceof CtLiteral)
			if(!(((CtLiteral)element.getAssignment()).getValue() == null))
				return;
		}catch(NullPointerException npe){
			return;
		}
		try{
			NameResolver.getName(element.getAssigned());
		}catch(NullPointerException npe){
			System.err.println("cannot get signature");
		}
		try{
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(IConstants.fullyQualifiedClassName));
			execref.setSimpleName(IConstants.methodName);
			execref.setStatic(true);
			
			CtTypeReference tmp = element.getAssigned().getType();
			
			CtExpression arg = null;
			if(tmp.isAnonymous() || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
				arg = getFactory().Core().createLiteral();
				arg.setType(getFactory().Type().nullType());
			}else{
				CtFieldReference ctfe = new CtFieldReferenceImpl();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(element.getAssigned().getType().box());
				
				arg = new CtFieldAccessImpl();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}
			
			//remove generic
			CtTypeReference tmpref = getFactory().Core().clone(element.getAssigned().getType());
			if(!(tmpref instanceof CtArrayTypeReference)){
				tmpref = tmpref.box();
			}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
				((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(null);
			}
			tmpref.setActualTypeArguments(null);
			
			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{element.getAssignment()}));
			invoc.setGenericTypes(Arrays.asList(new CtTypeReference[]{tmpref}));
			
			element.setAssignment(invoc);
		}catch(Throwable t){
			System.err.println("cannot resolve an assign");
		}
	}

}
