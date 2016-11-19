package bcu.transformer.processors;

import java.util.ArrayList;
import java.util.Arrays;

import bcornu.nullmode.AssignResolver;
import bcornu.nullmode.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.reference.CtFieldReferenceImpl;
import bcu.utils.NameResolver;

// handles what is called nullreturn in the paper
@SuppressWarnings({"unchecked","rawtypes"})
public class TargetModifier extends AbstractProcessor<CtTargetedExpression>{

	private int i=0;
	private int j=0;
	@Override
	public void processingDone() {
		System.out.println("TargetModifier -->"+i +" (failed:"+j+")");
	}
	@Override
	public void process(CtTargetedExpression element) {

		// main condition
		if (
				!(element instanceof CtFieldAccess)
				&& !(element instanceof CtArrayAccess)

		) {
			return;
		}

		if(element.getTarget() instanceof CtTypeAccess)
			return;
		if(element.getTarget()==null)
			return;
		if(	element.getTarget() instanceof CtSuperAccess
				|| element.getTarget() instanceof CtThisAccess // impossible that this is null
				 || element instanceof CtInvocation // those are handled by the GhostClass
				)
			return;

		String targetStringRepresentation=NameResolver.getName(element.getTarget());
		try{
			System.out.println(element);

			i++;
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.CallChecker"));
			execref.setSimpleName("isCalled");
			execref.setStatic(true);

			CtTypeReference tmp = element.getTarget().getType();

			// special case of arrays
			if (tmp.getQualifiedName().equals("java.lang.reflect.Array")
					&& element.getParent() instanceof CtAssignment && element.getParent(CtAssignment.class).getAssigned() == element
					) {
				return;
			}

			if(targetStringRepresentation.equals("class")){
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

				arg = getFactory().Core().createFieldRead();
//				arg = new CtFieldAccessImpl();
				((CtFieldAccess) arg).setVariable(ctfe);
			}

			CtLiteral location = getFactory().Core().createLiteral();

			//location.setValue("\""+StringEscapeUtils.escapeJava(sign)+"\"");
			location.setValue(Helpers.nicePositionString(element.getPosition()));


			location.setType(getFactory().Type().createReference(String.class));

			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{element.getTarget(),arg,location}));
			invoc.setTarget(getFactory().Code().createTypeAccess(getFactory().Code().createCtTypeReference(CallChecker.class)));

			CtTypeReference tmpref = getFactory().Core().clone(tmp);
			if(!(tmpref instanceof CtArrayTypeReference)){
				tmpref = tmpref.box();
			}else if(((CtArrayTypeReference)tmpref).getComponentType()!=null){
				((CtArrayTypeReference)tmpref).getComponentType().setActualTypeArguments(new ArrayList<CtTypeReference<?>>());
			}
//			tmpref.setActualTypeArguments(new ArrayList<CtTypeReference<?>>());

			execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));

			element.setTarget((CtExpression)invoc);
		}catch(Throwable t){
			j++;
			System.err.println(t.toString());
		}
	}

}
