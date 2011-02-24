/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.compiler;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.util.TypeArgumentContext;
import org.eclipse.xtext.common.types.util.TypeArgumentContextProvider;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XCasePart;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XCatchClause;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XDoWhileExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XInstanceOfExpression;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XTryCatchFinallyExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XWhileExpression;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.typing.FunctionConversion;

import com.google.inject.Inject;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class XbaseCompiler extends FeatureCallCompiler {
	
	@Inject 
	private TypeReferences typeRefs;
	
	protected void openBlock(XExpression xExpression, IAppendable b) {
		if (xExpression instanceof XBlockExpression) {
			return;
		}
		b.append("{").increaseIndentation();
	}

	protected void closeBlock(XExpression xExpression, IAppendable b) {
		if (xExpression instanceof XBlockExpression) {
			return;
		}
		b.decreaseIndentation().append("\n}");
	}

	public void _toJavaStatement(XBlockExpression expr, IAppendable b, boolean isReferenced) {
		if (isReferenced)
			declareLocalVariable(expr, b);
		b.append("\n{").increaseIndentation();
		final EList<XExpression> expressions = expr.getExpressions();
		for (int i = 0; i < expressions.size(); i++) {
			XExpression ex = expressions.get(i);
			if (i < expressions.size() - 1) {
				internalToJavaStatement(ex, b, false);
			} else {
				internalToJavaStatement(ex, b, isReferenced);
				if (isReferenced) {
					b.append("\n").append(getJavaVarName(expr, b)).append(" = (");
					internalToJavaExpression(ex, b);
					b.append(");");
				}
			}
		}
		b.decreaseIndentation().append("\n}");
	}

	public void _toJavaExpression(XBlockExpression expr, IAppendable b) {
		b.append(getJavaVarName(expr, b));
	}

	public void _toJavaStatement(XTryCatchFinallyExpression expr, IAppendable b, boolean isReferenced) {
		if (isReferenced && !isPrimitiveVoid(expr)) {
			declareLocalVariable(expr, b);
		}
		b.append("\ntry {").increaseIndentation();
		internalToJavaStatement(expr.getExpression(), b, true);
		if (isReferenced && !isPrimitiveVoid(expr.getExpression())) {
			b.append("\n").append(getJavaVarName(expr, b)).append(" = ");
			internalToJavaExpression(expr.getExpression(), b);
			b.append(";");
		}
		b.decreaseIndentation().append("\n}");
		appendCatchAndFinally(expr, b, isReferenced);
	}

	protected void appendCatchAndFinally(XTryCatchFinallyExpression expr, IAppendable b, boolean isReferenced) {
		for (XCatchClause catchClause : expr.getCatchClauses()) {
			JvmTypeReference type = catchClause.getDeclaredParam().getParameterType();
			final String name = declareNameInVariableScope(catchClause.getDeclaredParam(), b);
			b.append(" catch (").append(getSerializedForm(type)).append(" ").append(name).append(") { ");
			b.increaseIndentation();
			internalToJavaStatement(catchClause.getExpression(), b, true);
			if (isReferenced && ! isPrimitiveVoid(catchClause.getExpression())) {
				b.append("\n").append(getJavaVarName(expr, b)).append(" = ");
				internalToJavaExpression(catchClause.getExpression(), b);
				b.append(";");
			}
			b.decreaseIndentation();
			b.append("\n}");
		}
		final XExpression finallyExp = expr.getFinallyExpression();
		if (finallyExp != null) {
			b.append(" finally ");
			openBlock(finallyExp, b);
			internalToJavaStatement(finallyExp, b, false);
			closeBlock(finallyExp, b);
		}
	}

	public void _toJavaExpression(XTryCatchFinallyExpression expr, IAppendable b) {
		b.append(getJavaVarName(expr, b));
	}

	public void _toJavaStatement(XThrowExpression expr, IAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getExpression(), b, true);
		b.append("\nthrow ");
		internalToJavaExpression(expr.getExpression(), b);
		b.append(";");
	}

	public void _toJavaExpression(XThrowExpression expr, IAppendable b) {
	}

	public void _toJavaExpression(XInstanceOfExpression expr, IAppendable b) {
		b.append("(");
		internalToJavaExpression(expr.getExpression(), b);
		b.append(" instanceof ").append(expr.getType().getQualifiedName('.')).append(")");
	}

	public void _toJavaStatement(XInstanceOfExpression expr, IAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getExpression(), b, true);
	}

	public void _toJavaExpression(XVariableDeclaration expr, IAppendable b) {
	}

	public void _toJavaStatement(XVariableDeclaration varDeclaration, IAppendable b, boolean isReferenced) {
		internalToJavaStatement(varDeclaration.getRight(), b, true);
		b.append("\n");
		if (!varDeclaration.isWriteable()) {
			b.append("final ");
		}
		if (varDeclaration.getType() != null) {
			final JvmTypeReference type = varDeclaration.getType();
			b.append(getSerializedForm(type, varDeclaration, false, true));
		} else {
			final JvmTypeReference type = getTypeProvider().getType(varDeclaration.getRight());
			b.append(getSerializedForm(type, varDeclaration, false, true));
		}
		b.append(" ");
		b.append(declareNameInVariableScope(varDeclaration, b));
		b.append(" = ");
		internalToJavaExpression(varDeclaration.getRight(), b);
		b.append(";");
	}

	public void _toJavaExpression(XWhileExpression expr, IAppendable b) {
	}

	public void _toJavaStatement(XWhileExpression expr, IAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getPredicate(), b, true);
		b.append("\nBoolean ").append(declareNameInVariableScope(expr, b)).append(" = ");
		internalToJavaExpression(expr.getPredicate(), b);
		b.append(";");
		b.append("\nwhile (");
		b.append(getJavaVarName(expr, b));
		b.append(") { ");
		openBlock(expr.getBody(), b);
		internalToJavaStatement(expr.getBody(), b, false);
		closeBlock(expr.getBody(), b);
		b.append("\n").append(getJavaVarName(expr, b)).append(" = ");
		internalToJavaExpression(expr.getPredicate(), b);
		b.append(";");
		b.append("\n}");
	}

	public void _toJavaStatement(XDoWhileExpression expr, IAppendable b) {
		_toJavaStatement(expr, b);
	}

	public void _toJavaExpression(XDoWhileExpression expr, IAppendable b) {
	}

	public void _toJavaStatement(XDoWhileExpression expr, IAppendable b, boolean isReferenced) {
		b.append("\nBoolean ").append(declareNameInVariableScope(expr, b)).append(";");
		b.append("\ndo {");
		internalToJavaStatement(expr.getBody(), b, false);
		internalToJavaStatement(expr.getPredicate(), b, true);
		b.append("\n").append(getJavaVarName(expr, b)).append(" = ");
		internalToJavaExpression(expr.getPredicate(), b);
		b.append(";");
		b.append("\n} while(");
		b.append(getJavaVarName(expr, b));
		b.append(");");
	}

	public void _toJavaExpression(XForLoopExpression expr, IAppendable b) {
	}

	public void _toJavaStatement(XForLoopExpression expr, IAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getForExpression(), b, true);
		b.append("\nfor (");
		JvmTypeReference paramType = getTypeProvider().getTypeForIdentifiable(expr.getDeclaredParam());
		b.append(paramType.getIdentifier());
		b.append(" ");
		String varName = declareNameInVariableScope(expr.getDeclaredParam(), b);
		b.append(varName);
		b.append(" : ");
		internalToJavaExpression(expr.getForExpression(), b);
		b.append(") ");
		openBlock(expr.getEachExpression(), b);
		internalToJavaStatement(expr.getEachExpression(), b, false);
		closeBlock(expr.getEachExpression(), b);
	}

	public void _toJavaStatement(XConstructorCall expr, IAppendable b, boolean isReferenced) {
		for (XExpression arg : expr.getArguments()) {
			internalToJavaStatement(arg, b, true);
		}
	}

	public void _toJavaExpression(XConstructorCall expr, IAppendable b) {
		b.append("new ");
		b.append(expr.getConstructor().getDeclaringType().getQualifiedName('.'));
		if (!expr.getTypeArguments().isEmpty()) {
			b.append("<");
			for (int i = 0; i < expr.getTypeArguments().size(); i++) {
				JvmTypeReference arg = expr.getTypeArguments().get(i);
				b.append(arg.getQualifiedName('.'));
				if (i + 1 < expr.getTypeArguments().size())
					b.append(", ");
			}
			b.append(">");
		}
		b.append("(");
		appendArguments(expr.getArguments(), b);
		b.append(")");
	}

	public void _toJavaExpression(XCastedExpression expr, IAppendable b) {
		b.append("((");
		b.append(expr.getType().getIdentifier());
		b.append(") ");
		internalToConvertedExpression(expr.getTarget(), b, expr.getType());
		b.append(")");
	}

	public void _toJavaStatement(XCastedExpression expr, IAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getTarget(), b, true);
	}

	public void _toJavaStatement(XIfExpression expr, IAppendable b, boolean isReferenced) {
		if (isReferenced)
			declareLocalVariable(expr, b);
		internalToJavaStatement(expr.getIf(), b, true);
		b.append("\nif (");
		internalToJavaExpression(expr.getIf(), b);
		b.append(") ");
		openBlock(expr.getThen(), b);
		internalToJavaStatement(expr.getThen(), b, isReferenced);
		if (isReferenced && !isPrimitiveVoid(expr.getThen())) {
			b.append("\n");
			b.append(getJavaVarName(expr, b));
			b.append(" = ");
			internalToJavaExpression(expr.getThen(), b);
			b.append(";");
		}
		closeBlock(expr.getThen(), b);
		if (expr.getElse() != null) {
			b.append(" else ");
			openBlock(expr.getElse(), b);
			internalToJavaStatement(expr.getElse(), b, isReferenced);
			if (isReferenced && !isPrimitiveVoid(expr.getElse())) {
				b.append("\n");
				b.append(getJavaVarName(expr, b));
				b.append(" = ");
				internalToJavaExpression(expr.getElse(), b);
				b.append(";");
			}
			closeBlock(expr.getElse(), b);
		}
	}

	public void _toJavaExpression(XIfExpression expr, IAppendable b) {
		b.append(getJavaVarName(expr, b));
	}

	public void _toJavaStatement(XSwitchExpression expr, IAppendable b, boolean isReferenced) {
		// declare variable
		JvmTypeReference type = getTypeProvider().getType(expr);
		String switchResultName = makeJavaIdentifier(b.declareVariable(Tuples.pair(expr,"result"), "switchResult"));
		if (isReferenced) {
			b.append("\n").append(getSerializedForm(type)).append(" ").append(switchResultName).append(" = null;");
		}
		
		internalToJavaStatement(expr.getSwitch(), b, true);

		// declare local var for the switch expression
		String name = getNameProvider().getSimpleName(expr);
		if (name == null) {
			// define synthetic name
			name = "__valOfSwitchOver";
		}
		b.append("\nfinal ").append(getReturnTypeName(expr.getSwitch())).append(" ");
		String variableName = b.declareVariable(expr, name);
		b.append(variableName);
		b.append(" = ");
		internalToJavaExpression(expr.getSwitch(), b);
		b.append(";");

		// declare 'boolean matched' to check whether a case has matched already
		b.append("\nboolean ");
		String matchedVariable = b.declareVariable(Tuples.pair(expr, "matches"), "matched");
		b.append(matchedVariable).append(" = false;");

		for (XCasePart casePart : expr.getCases()) {
			b.append("\nif (!").append(matchedVariable).append(") {");
			b.increaseIndentation();
			if (casePart.getTypeGuard() != null) {
				final String guardType = getSerializedForm(casePart.getTypeGuard());
				b.append("\nif (");
				b.append(variableName);
				b.append(" instanceof ");
				b.append(guardType);
				b.append(") {");
				b.increaseIndentation();

				// declare local var for case
				String simpleName = getNameProvider().getSimpleName(casePart);
				if (simpleName != null) {
					b.append("\nfinal ").append(guardType).append(" ");
					String typeGuardName = b.declareVariable(casePart, simpleName);
					b.append(typeGuardName);
					b.append(" = (").append(guardType).append(") ").append(variableName).append(";");
				}
			}
			if (casePart.getCase() != null) {
				internalToJavaStatement(casePart.getCase(), b, true);
				b.append("\nif (");
				JvmTypeReference convertedType = getTypeProvider().getType(casePart.getCase());
				if (typeRefs.is(convertedType, Boolean.TYPE) || typeRefs.is(convertedType, Boolean.class)) {
					internalToJavaExpression(casePart.getCase(), b);
				} else {
					b.append(ObjectExtensions.class.getCanonicalName()).append(".operator_equals(").append(variableName).append(",");
					internalToJavaExpression(casePart.getCase(), b);
					b.append(")");
				}
				b.append(") {");
				b.increaseIndentation();
			}
			// set matched to true
			b.append("\n").append(matchedVariable).append("=true;");

			// execute then part
			internalToJavaStatement(casePart.getThen(), b, isReferenced);
			if (isReferenced) {
				b.append("\n").append(switchResultName).append(" = ");
				internalToJavaExpression(casePart.getThen(), b);
				b.append(";");
			}

			// close surrounding if statements
			if (casePart.getCase() != null) {
				b.decreaseIndentation().append("\n}");
			}
			if (casePart.getTypeGuard() != null) {
				b.decreaseIndentation().append("\n}");
			}
			b.decreaseIndentation();
			b.append("\n}");
		}
		if (expr.getDefault()!=null) {
			b.append("\nif (!").append(matchedVariable).append(") {");
			b.increaseIndentation();
			internalToJavaStatement(expr.getDefault(), b, isReferenced);
			if (isReferenced) {
				b.append("\n").append(switchResultName).append(" = ");
				internalToJavaExpression(expr.getDefault(), b);
				b.append(";");
			}
			b.decreaseIndentation();
			b.append("\n}");
		}
	}

	public void _toJavaExpression(XSwitchExpression expr, IAppendable b) {
		b.append(getJavaVarName(Tuples.pair(expr,"result"), b));
	}

	@Inject
	private FunctionConversion functionConversion;
	@Inject
	private TypeArgumentContextProvider ctxProvider;
	
	protected void _toJavaStatement(final XClosure call, final IAppendable b, boolean isReferenced) {
		if (!isReferenced)
			throw new IllegalArgumentException("a closure definition does not cause any sideffeccts");
		JvmTypeReference type = getTypeProvider().getType(call);
		TypeArgumentContext context = ctxProvider.getReceiverContext(type);
		final String serializedFormWithConstraints = getSerializedForm(type);
		final String serializedFormWithoutConstraints = getSerializedForm(type, null, true, false);
		b.append("\n").append("final ").append(serializedFormWithConstraints);
		b.append(" ");
		String variableName = makeJavaIdentifier(b.declareVariable(call, "function"));
		b.append(variableName).append(" = ");
		b.append("new ").append(serializedFormWithoutConstraints).append("() {");
		b.increaseIndentation().increaseIndentation();
		JvmOperation operation = functionConversion.findSingleMethod(type);
		final JvmTypeReference returnType = context.resolve(operation.getReturnType());
		b.append("\npublic ").append(getSerializedForm(returnType, null, true, false)).append(" ").append(operation.getSimpleName());
		b.append("(");
		EList<JvmFormalParameter> closureParams = call.getFormalParameters();
		for (Iterator<JvmFormalParameter> iter = closureParams.iterator(); iter.hasNext();) {
			JvmFormalParameter param = iter.next();
			final JvmTypeReference parameterType2 = getTypeProvider().getTypeForIdentifiable(param);
			final JvmTypeReference parameterType = context.resolve(parameterType2);
			b.append(getSerializedForm(parameterType, null, true, false)).append(" ");
			String name = makeJavaIdentifier(b.declareVariable(param, param.getName()));
			b.append(name);
			if (iter.hasNext())
				b.append(" , ");
		}
		b.append(") {");
		b.increaseIndentation();
		internalToJavaStatement(call.getExpression(), b, true);
		b.append("\nreturn ");
		internalToJavaExpression(call.getExpression(), b);
		b.append(";");
		b.decreaseIndentation();
		b.append("\n}");
		b.decreaseIndentation().append("\n};").decreaseIndentation();
	}
	
	protected void _toJavaExpression(final XClosure call, final IAppendable b) {
		b.append(getJavaVarName(call, b));
	}
	
}
