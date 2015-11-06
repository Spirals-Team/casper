package bcu.transformer.processors;

import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;

import bcu.utils.NameResolver;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

@SuppressWarnings({"unchecked","rawtypes"})
public class TargetModifier extends AbstractProcessor<CtTargetedExpression>{

	private int i=0;
	private int j=0;
	@Override
	public void processingDone() {
		System.out.println("0-->"+i +" (failed:"+j+")");
	}
	@Override
	public void process(CtTargetedExpression element) {
		if(element.getTarget()==null)
			return;
		if(element.getTarget() instanceof CtThisAccess 
				|| element.getTarget() instanceof CtSuperAccess)
			return;
		String sign=NameResolver.getName(element.getTarget());
		try{
			i++;
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.CallChecker"));
			execref.setSimpleName("isCalled");
			execref.setStatic(true);
			
			CtTypeReference tmp = element.getTarget().getType();
			if(sign.equals("class")){
				tmp = getFactory().Type().createReference(Class.class);
			}
			if(element.getTarget().getTypeCasts()!=null && element.getTarget().getTypeCasts().size()>0){
				tmp = (CtTypeReference) element.getTarget().getTypeCasts().get(0);
			}
			
			CtExpression arg = null;
			if(tmp.isAnonymous() || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
				arg = getFactory().Core().createLiteral();
				arg.setType(getFactory().Type().nullType());
			}else{
				CtFieldReference ctfe = new CtFieldReferenceImpl();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(tmp.box());
				
				arg = getFactory().Core().createVariableAccess();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}
			
			CtLiteral location = getFactory().Core().createLiteral();
			location.setValue("\""+StringEscapeUtils.escapeJava(sign)+"\"");
			location.setType(getFactory().Type().createReference(String.class));
			
			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{element.getTarget(),arg,location}));
			
			CtTypeReference tmpref = getFactory().Core().clone(tmp);
			if(!(tmpref instanceof CtArrayTypeReference)){
				tmpref = tmpref.box();
			}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
				((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(null);
			}
//			tmpref.setActualTypeArguments(null);
			
			execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));
			
			element.setTarget((CtExpression)invoc);
		}catch(Throwable t){
			j++;
//			System.err.println("cannot resolve an assign");
		}
	}

}
