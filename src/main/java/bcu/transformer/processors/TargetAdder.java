package bcu.transformer.processors;

import bcornu.nullmode.ComparizonOperator;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Created by martin on 11/19/16.
 */
public class TargetAdder extends AbstractProcessor<CtInvocation> {
	@Override
	public boolean isToBeProcessed(CtInvocation candidate) {
		return super.isToBeProcessed(candidate) && candidate.getTarget()==null;
	}

	@Override
	public void process(CtInvocation ctInvocation) {
		ctInvocation.setTarget(getFactory().Code().createTypeAccess(ctInvocation.getExecutable().getDeclaringType()));
	}
}
