package bcu.transformer.processors;

import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

@SuppressWarnings("rawtypes")
public class ReturnModifier extends AbstractProcessor<CtReturn>{
	private int i=0;
	@Override
	public void processingDone() {
		System.out.println("4-->"+i);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void process(CtReturn element) {
		if(element.getReturnedExpression()==null)
			return;
		if(element.getReturnedExpression() instanceof CtLiteral){
			if(!(((CtLiteral)element.getReturnedExpression()).getValue() == null))
				return;
		}
		i++;
		String sign="???";
		try{
			sign = element.getParent(CtMethod.class).getSimpleName();
		}catch(Throwable npe){
			System.err.println("cannot get signature");
		}
		try{
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ReturnResolver"));
			execref.setSimpleName("setReturn");
			execref.setStatic(true);
			
			CtTypeReference tmp = element.getParent(CtMethod.class).getType();
			
			CtExpression arg = null;
			if(tmp.isAnonymous() || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
				arg = getFactory().Core().createLiteral();
				arg.setType(getFactory().Type().nullType());
			}else{
				CtFieldReference ctfe = new CtFieldReferenceImpl();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(element.getParent(CtMethod.class).getType().box());
				
				arg = new CtFieldAccessImpl();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}

			CtLiteral location = getFactory().Core().createLiteral();
			location.setValue("\""+StringEscapeUtils.escapeJava(sign)+"\"");
			location.setType(getFactory().Type().createReference(String.class));
				
			CtTypeReference tmpref = getFactory().Core().clone(element.getParent(CtMethod.class).getType());
			if(!(tmpref instanceof CtArrayTypeReference)){
				tmpref = tmpref.box();
			}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
				((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(null);
			}
			tmpref.setActualTypeArguments(null);
		
			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{element.getReturnedExpression(),arg,location}));
			invoc.setGenericTypes(Arrays.asList(new CtTypeReference[]{tmpref}));
			
			element.setReturnedExpression(invoc);
		}catch(Throwable t){
			System.err.println("cannot resolve a return");
		}
	}

}
