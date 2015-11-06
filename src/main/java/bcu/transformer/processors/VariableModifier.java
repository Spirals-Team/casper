package bcu.transformer.processors;

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
public class VariableModifier extends AbstractProcessor<CtLocalVariable>{

	private int i=0;
	@Override
	public void processingDone() {
		System.out.println("2b-->"+i);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void process(CtLocalVariable element) {
		if(element.getParent() instanceof CtCatch 
				|| element.getParent() instanceof CtFor 
				|| element.getParent() instanceof CtForEach)
			return;
		if(element.getType().isPrimitive() && element.getDefaultExpression()==null)
			return;
		if(element.getDefaultExpression() instanceof CtLiteral)
			if(!(((CtLiteral)element.getDefaultExpression()).getValue()==null))
				return;
		String sign="???";
		i++;
		try{
			sign = element.getSimpleName();
		}catch(Throwable npe){
			System.err.println("cannot get signature");
		}
		if(element.getDefaultExpression()==null){
			if(element.hasModifier(ModifierKind.FINAL))
				return;
			else{
				CtLiteral tmp = getFactory().Core().createLiteral();
				tmp.setValue(null);
				element.setDefaultExpression(tmp);
			}
		}
		try{
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.AssignResolver"));
			execref.setSimpleName("setAssigned");
			execref.setStatic(true);
			
			CtTypeReference tmp = element.getType();
			
			CtExpression arg = null;
			if(tmp.isAnonymous() || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
				arg = getFactory().Core().createLiteral();
				arg.setType(getFactory().Type().nullType());
			}else{
				CtFieldReference ctfe = new CtFieldReferenceImpl();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(element.getType().box());
				
				arg = new CtFieldAccessImpl();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}

			CtLiteral location = getFactory().Core().createLiteral();
			location.setValue("\""+StringEscapeUtils.escapeJava(sign)+"\"");
			location.setType(getFactory().Type().createReference(String.class));

			CtExpression assignment = element.getDefaultExpression();

			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{assignment,arg,location}));

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
