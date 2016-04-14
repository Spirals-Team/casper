package bcu.utils;

import spoon.reflect.code.CtAnnotationFieldAccess;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtTargetedAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;

@SuppressWarnings("rawtypes")
public class NameResolver {

	public static String getName(CtExpression e) {
		try{
			if (e instanceof CtVariableAccess) {
				return ((CtVariableAccess)e).getVariable().getSimpleName();
			} else if (e instanceof CtFieldAccess) {
				return ((CtFieldAccess)e).getVariable().getSimpleName();
			} else if (e instanceof CtAssignment) {
				return getName(((CtAssignment)e).getAssigned())+"="+getName(((CtAssignment)e).getAssignment());
			} else if (e instanceof CtLiteral) {
				return ((CtLiteral) e).getValue().toString();
			} else if (e instanceof CtTargetedExpression) {
				if (e instanceof CtArrayAccess) {
					return getName(((CtTargetedExpression)e).getTarget())+"["+getName(((CtArrayAccess)e).getIndexExpression())+"]";
				} else if (e instanceof CtInvocation) {
					return getName(((CtTargetedExpression)e).getTarget())+"."+((CtInvocation)e).getExecutable().getSimpleName()+"()";
				} else{
					return "call on" +getName(((CtTargetedExpression)e).getTarget());
				}
			} else if (e instanceof CtThisAccess) {
				return "this";
			} else if (e instanceof CtVariableAccess) {
				return ((CtVariableAccess)e).getVariable().getSimpleName();
			} 
			
			return e.toString();
			
		} catch (Throwable t) {
			return "cannot resolve";
		}
	}

}
