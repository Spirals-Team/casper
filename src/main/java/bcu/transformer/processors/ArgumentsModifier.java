package bcu.transformer.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.declaration.CtParameterImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

	@SuppressWarnings({ "unchecked", "rawtypes" })
public class ArgumentsModifier extends AbstractProcessor<CtMethod>{

	static int i=0;
	
//	private void test() {
		// TODO Auto-generated method stub
//		getFactory().Type().createReference("").getDeclaration().getNestedType(name);
//	}
		
	@Override
	public void process(CtMethod element) {
		if(element.getParameters() == null || element.getParameters().size()==0 || element.getBody()==null){
			return;
		}else{
			i++;
			List<CtParameterImpl> args = element.getParameters();
			for (CtParameterImpl current : args) {
				
				String sign = "parameter "+current.getSimpleName() + " is null in "+element.getSimpleName()+ " at "+Helpers.nicePositionString(element.getPosition());
				
				if(current.hasModifier(ModifierKind.FINAL)){
					
					CtLocalVariable var = getFactory().Core().createLocalVariable();
					var.setSimpleName(current.getSimpleName());
					var.setType(current.getType());
					var.addModifier(ModifierKind.FINAL);

					current.removeModifier(ModifierKind.FINAL);
					current.setSimpleName(current.getSimpleName()+"_s");
					
					CtVariableAccess variable = getFactory().Core().createVariableAccess();
					variable.setVariable(current.getReference());
					variable.setType(current.getReference().getType());
					
					CtExecutableReference execref = getFactory().Core().createExecutableReference();
					execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ArgumentResolver"));
					execref.setSimpleName("setPassedArg");
					execref.setStatic(true);
					
					CtTypeReference tmp = variable.getType();
					
					CtExpression arg = null;
					if(tmp.isAnonymous() || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
						arg = getFactory().Core().createLiteral();
						arg.setType(getFactory().Type().nullType());
					}else{
						CtFieldReference ctfe = new CtFieldReferenceImpl();
						ctfe.setSimpleName("class");
						ctfe.setDeclaringType(tmp.box());
						
						arg = new CtFieldAccessImpl();
						((CtFieldAccessImpl) arg).setVariable(ctfe);
					}
					
					CtLiteral location = getFactory().Core().createLiteral();
					location.setValue(StringEscapeUtils.escapeJava(sign));
					location.setType(getFactory().Type().createReference(String.class));
					
					CtInvocation invoc = getFactory().Core().createInvocation();
					invoc.setExecutable(execref);
					invoc.setArguments(Arrays.asList(new CtExpression[]{variable,arg,location}));
					
					CtTypeReference tmpref = getFactory().Core().clone(tmp);
					if(!(tmpref instanceof CtArrayTypeReference)){
						tmpref = tmpref.box();
					}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
						((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(new ArrayList<CtTypeReference<?>>());
					}
					tmpref.setActualTypeArguments(new ArrayList<CtTypeReference<?>>());
					
					execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));
					
					var.setDefaultExpression(invoc);
					
					element.getBody().insertBegin(var);
					
					
				}else{
					CtVariableAccess variable = getFactory().Core().createVariableAccess();
					variable.setVariable(current.getReference());
					variable.setType(current.getReference().getType());
					
					CtExecutableReference execref = getFactory().Core().createExecutableReference();
					execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ArgumentResolver"));
					execref.setSimpleName("setPassedArg");
					execref.setStatic(true);
					
					CtTypeReference tmp = variable.getType();
					
					CtExpression arg = null;
					if(tmp.isAnonymous() || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
						arg = getFactory().Core().createLiteral();
						arg.setType(getFactory().Type().nullType());
					}else{
						CtFieldReference ctfe = new CtFieldReferenceImpl();
						ctfe.setSimpleName("class");
						ctfe.setDeclaringType(tmp.box());
						
						arg = new CtFieldAccessImpl();
						((CtFieldAccessImpl) arg).setVariable(ctfe);
					}
					
					CtLiteral location = getFactory().Core().createLiteral();
					location.setValue(StringEscapeUtils.escapeJava(sign));
					location.setType(getFactory().Type().createReference(String.class));
					
					CtInvocation invoc = getFactory().Core().createInvocation();
					invoc.setExecutable(execref);
					invoc.setArguments(Arrays.asList(new CtExpression[]{variable,arg,location}));
					
					CtTypeReference tmpref = getFactory().Core().clone(tmp);
					if(!(tmpref instanceof CtArrayTypeReference)){
						tmpref = tmpref.box();
					}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
						((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(new ArrayList<CtTypeReference<?>>());
					}
					tmpref.setActualTypeArguments(new ArrayList<CtTypeReference<?>>());
					
					execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));
					
					CtAssignment assignment = getFactory().Core().createAssignment();
					assignment.setAssigned(variable);
					assignment.setAssignment(invoc);
					
					element.getBody().insertBegin(assignment);
				}
			}
		}
	}
	
	@Override
		public void processingDone() {
			System.out.println("3-->"+i);
		}
}