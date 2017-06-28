package bcu.transformer.processors;

import org.apache.commons.lang3.StringEscapeUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

import java.util.Arrays;

public class ParamProcessor extends AbstractProcessor<CtInvocation<?>> {

	@Override
	public void process(CtInvocation<?> arg0) {
		// we only consider external calls
		if (arg0.getExecutable().getDeclaration()!=null) {
			return;
		}

		for (int i=0; i<arg0.getArguments().size(); i++) {
			CtExpression param = arg0.getArguments().get(i);
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ExternalCallManager"));
			execref.setSimpleName("exorcise");
			execref.setStatic(true);

			CtTypeReference tmp = param.getType();

			CtExpression arg = null;
			CtFieldReference ctfe = new CtFieldReferenceImpl();
			ctfe.setSimpleName("class");
			ctfe.setDeclaringType(tmp.box());
			arg = getFactory().Core().createFieldRead();
			((CtFieldAccessImpl) arg).setVariable(ctfe);


			CtLiteral location = getFactory().Core().createLiteral();
			location.setValue(""+ StringEscapeUtils.escapeJava(param.toString()+ " "+Helpers.nicePositionString(param.getPosition()))+"");
			location.setType(getFactory().Type().createReference(String.class));

			CtTypeReference tmpref = getFactory().Core().clone(tmp);

			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{param,arg,location}));
			execref.setActualTypeArguments(Arrays.asList(new CtTypeReference<?>[]{tmpref}));

			invoc.setParent(arg0);
			// fail
			// param.replace(invoc);

			// succeeds
			arg0.getArguments().set(i, invoc);
		}
	}


}
