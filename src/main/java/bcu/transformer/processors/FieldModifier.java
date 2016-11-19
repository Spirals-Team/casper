package bcu.transformer.processors;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

@SuppressWarnings("rawtypes")
public class FieldModifier extends AbstractProcessor<CtField>{
	private int i = 0;
	@Override
	public void processingDone() {
		System.out.println("1-->"+i );
	}
	@SuppressWarnings("unchecked")
	@Override
	public void process(CtField element) {
		if(element.getParent() instanceof CtAnnotationType){
			return;
		}
		if(element.getType()==null || element.getType().isPrimitive() && element.getDefaultExpression()==null)
			return;

		i++;
		String sign="???";
		try{
			sign = "field "+element.getSimpleName()+ " "+ Helpers.nicePositionString(element.getPosition());
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
		if(element.getDefaultExpression() instanceof CtLiteral){
			if(!(((CtLiteral)element.getDefaultExpression()).getValue()==null))
				return;
			if(element.getType().isPrimitive())
				return;
		}
		if(element.getDefaultExpression() instanceof CtUnaryOperator && (((CtUnaryOperator)element.getDefaultExpression()).getOperand() instanceof CtLiteral)){
			if(!(((CtLiteral)((CtUnaryOperator)element.getDefaultExpression()).getOperand()).getValue()==null))
				return;
			if(element.getType().isPrimitive())
				return;
		}
		try{
			System.out.println(element.getDefaultExpression().toString());
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.AssignResolver"));
			execref.setSimpleName("setAssigned");
			if((((CtLiteral)element.getDefaultExpression()).getValue()==null)) {
				execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.NullLiteralResolver"));
				execref.setSimpleName("createNullGhost");
			}

			execref.setStatic(true);

			System.out.println(execref);
			CtTypeReference tmp = element.getType();

			CtExpression arg = null;
			if(tmp.isAnonymous() || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
				arg = getFactory().Core().createLiteral();
				arg.setType(getFactory().Type().nullType());
			}else{
				CtFieldReference ctfe = new CtFieldReferenceImpl();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(element.getType().box());

				arg = getFactory().Core().createFieldRead();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}

			CtLiteral location = getFactory().Core().createLiteral();
			location.setValue(""+StringEscapeUtils.escapeJava(sign)+"");
			location.setType(getFactory().Type().createReference(String.class));

			CtExpression assignment = element.getDefaultExpression();

			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{assignment,arg,location}));

			CtTypeReference tmpref = getFactory().Core().clone(element.getType());
			if(!(tmpref instanceof CtArrayTypeReference)){
				tmpref = tmpref.box();
			}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
				((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(new ArrayList<CtTypeReference<?>>());
			}
			tmpref.setActualTypeArguments(new ArrayList<CtTypeReference<?>>());

			execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));
			element.setDefaultExpression(invoc);
		}catch(Throwable t){
			System.err.println("cannot resolve an assign");
		}
	}

}
