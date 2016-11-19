package bcu.transformer.processors;

import java.util.Arrays;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings({"unchecked","rawtypes"})
public class ComparizonModifier extends AbstractProcessor<CtBinaryOperator<?>>{

	private int i=0;
	@Override
	public void processingDone() {
		System.out.println("5-->"+i);
	}
	@Override
	public void process(CtBinaryOperator<?> element) {
		i++;
		switch (element.getKind()) {
		case EQ:
			CtExpression<?> right = element.getRightHandOperand();
			CtExpression<?> left = element.getLeftHandOperand();
			if(left instanceof CtLiteral && (((CtLiteral)left).getValue()==null)){
				CtExecutableReference execref = getFactory().Core().createExecutableReference();
				execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ComparizonOperator"));
				execref.setSimpleName("isNull");
				execref.setStatic(true);
				CtInvocation invoc = getFactory().Core().createInvocation();
				invoc.setExecutable(execref);
				invoc.setArguments(Arrays.asList(new CtExpression[]{right}));
				element.replace(invoc);
				return;
			}
			if(right instanceof CtLiteral && (((CtLiteral)right).getValue()==null)){
				CtExecutableReference execref = getFactory().Core().createExecutableReference();
				execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ComparizonOperator"));
				execref.setSimpleName("isNull");
				execref.setStatic(true);
				CtInvocation invoc = getFactory().Core().createInvocation();
				invoc.setExecutable(execref);
				invoc.setArguments(Arrays.asList(new CtExpression[]{left}));
				element.replace(invoc);
				return;
			}
			break;
		case NE:
			right = element.getRightHandOperand();
			left = element.getLeftHandOperand();
			if(left instanceof CtLiteral && (((CtLiteral)left).getValue()==null)){
				CtExecutableReference execref = getFactory().Core().createExecutableReference();
				execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ComparizonOperator"));
				execref.setSimpleName("isNotNull");
				execref.setStatic(true);
				CtInvocation invoc = getFactory().Core().createInvocation();
				invoc.setExecutable(execref);
				invoc.setArguments(Arrays.asList(new CtExpression[]{right}));
				element.replace(invoc);
				return;
			}
			if(right instanceof CtLiteral && (((CtLiteral)right).getValue()==null)){
				CtExecutableReference execref = getFactory().Core().createExecutableReference();
				execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ComparizonOperator"));
				execref.setSimpleName("isNotNull");
				execref.setStatic(true);
				CtInvocation invoc = getFactory().Core().createInvocation();
				invoc.setExecutable(execref);
				invoc.setArguments(Arrays.asList(new CtExpression[]{left}));
				element.replace(invoc);
				return;
			}
			break;
		case INSTANCEOF:
			right = element.getRightHandOperand();
			left = element.getLeftHandOperand();
			
			CtTypeReference argValue = (CtTypeReference) ((CtLiteral)right).getValue();
			
			CtFieldReference ref = getFactory().Core().createFieldReference();
			ref.setDeclaringType(argValue);
//			ref.setType(argValue);
			ref.setSimpleName("class");
			
			CtFieldAccess arg= getFactory().Core().createFieldRead();
//			arg.setType(argValue);
			arg.setVariable(ref);
			
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.nullmode.ComparizonOperator"));
			execref.setSimpleName("instanceOf");
			execref.setStatic(true);
			CtInvocation invoc = getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{left,arg}));
			element.replace(invoc);
			return;
		default:;
		}
	}

}
