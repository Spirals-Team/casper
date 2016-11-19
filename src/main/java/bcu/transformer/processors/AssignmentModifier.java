package bcu.transformer.processors;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;
import bcu.utils.NameResolver;

@SuppressWarnings("rawtypes")
public class AssignmentModifier extends AbstractProcessor<CtAssignment>{
	private int i=0;
	@Override
	public void processingDone() {
		System.out.println("2-->"+i);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void process(CtAssignment element) {
		try{
		if(element.getAssigned().getType().isPrimitive() && element.getAssignment()==null)
			return;
		if(element.getAssignment() instanceof CtLiteral)
			if(!(((CtLiteral)element.getAssignment()).getValue() == null))
				return;

		// already processed for null literal
		if (element.getAssignment().toString().startsWith("bcornu")) return;

		}catch(NullPointerException npe){
//			System.out.println("cannot get element type?");
			return;
		}
		i++;
		String sign="???";
		try {
			sign = element.getAssigned().toString();
		} catch (Exception ignore) {}
		try{
			NameResolver.getName(element.getAssigned());
		}catch(NullPointerException npe){
			System.err.println("cannot get signature");
		}
		try{
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.AssignResolver"));
			execref.setSimpleName("setAssigned");
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

				arg = getFactory().Core().createFieldRead();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}

			CtLiteral location = getFactory().Core().createLiteral();
			location.setValue(""+StringEscapeUtils.escapeJava(sign+ " "+Helpers.nicePositionString(element.getPosition()))+"");
			location.setType(getFactory().Type().createReference(String.class));

			//remove generic
			CtTypeReference tmpref = getFactory().Core().clone(element.getAssigned().getType());
			if(!(tmpref instanceof CtArrayTypeReference)){
				tmpref = tmpref.box();
			}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
				((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(new ArrayList<CtTypeReference<?>>());
			}
			tmpref.setActualTypeArguments(new ArrayList<CtTypeReference<?>>());

			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{element.getAssignment(), arg, location}));
			execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));

			element.setAssignment(invoc);
		}catch(Throwable t){
			System.err.println("cannot resolve an assign");
		}
	}

}
