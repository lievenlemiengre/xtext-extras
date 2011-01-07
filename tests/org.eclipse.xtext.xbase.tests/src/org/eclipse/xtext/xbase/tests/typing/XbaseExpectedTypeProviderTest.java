/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.typing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XCasePart;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XCatchClause;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XDoWhileExpression;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XTryCatchFinallyExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XWhileExpression;
import org.eclipse.xtext.xbase.tests.AbstractXbaseTestCase;
import org.eclipse.xtext.xbase.typing.XbaseExpectedTypeProvider;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class XbaseExpectedTypeProviderTest extends AbstractXbaseTestCase {

	public void testAssignment() throws Exception {
		XAssignment assi = (XAssignment) ((XBlockExpression) expression("{ " + "  var  x = 'hello'" + "  x = null"
				+ "}")).getExpressions().get(1);
		assertExpected("java.lang.String", assi.getValue());
	}

	public void testMemberFeatureCall() throws Exception {
		XMemberFeatureCall fc = (XMemberFeatureCall) expression("'foo'.charAt(null)");
		assertExpected("int", fc.getMemberCallArguments().get(0));
		assertExpected(null, fc.getMemberCallTarget());
	}

	public void testBinaryOperationCall() throws Exception {
		XBinaryOperation fc = (XBinaryOperation) expression("new java.util.ArrayList<java.lang.String>() += null");
		assertExpected("java.lang.String", fc.getRightOperand());
	}

	public void testVariableDeclaration_0() throws Exception {
		XVariableDeclaration decl = (XVariableDeclaration) ((XBlockExpression) expression("{ " + "  var  x = 'hello'"
				+ "  null" + "}")).getExpressions().get(0);
		assertExpected("java.lang.Object", decl.getRight());
	}

	public void testVariableDeclaration_1() throws Exception {
		XVariableDeclaration decl = (XVariableDeclaration) ((XBlockExpression) expression("{ "
				+ "  var  java.lang.String x = null" + "  null" + "}")).getExpressions().get(0);
		assertExpected("java.lang.String", decl.getRight());
	}

	public void testConstructorCall() throws Exception {
		XConstructorCall decl = (XConstructorCall) expression("new java.lang.String('foo')");
		assertExpected("java.lang.String", decl.getArguments().get(0));
	}

	public void testBlockExpression() throws Exception {
		XCastedExpression cast = (XCastedExpression) expression("(java.lang.Boolean){ true false null}");
		XBlockExpression target = (XBlockExpression) cast.getTarget();

		assertExpected(null, target.getExpressions().get(0));
		assertExpected(null, target.getExpressions().get(1));
		assertExpected("java.lang.Boolean", target.getExpressions().get(2));
	}

	public void testIfExpression() throws Exception {
		XCastedExpression cast = (XCastedExpression) expression("(java.lang.String)if (null) null else null");
		XIfExpression target = (XIfExpression) cast.getTarget();

		assertExpected("java.lang.Boolean", target.getIf());
		assertExpected("java.lang.String", target.getThen());
		assertExpected("java.lang.String", target.getElse());
	}

	public void testForLoopExpression_0() throws Exception {
		XForLoopExpression loop = (XForLoopExpression) expression("for (java.lang.String x : null) null");

		assertExpected("java.lang.Iterable<? extends java.lang.String>", loop.getForExpression());
		assertExpected(null, loop.getEachExpression());
		assertExpected(null, loop.getDeclaredParam());
	}

	public void testForLoopExpression_1() throws Exception {
		XForLoopExpression loop = (XForLoopExpression) expression("for (x : null) null");

		// expect raw type
		assertExpected("java.lang.Iterable", loop.getForExpression());
		assertExpected(null, loop.getEachExpression());
		assertExpected(null, loop.getDeclaredParam());
	}

	public void testWhileExpression_0() throws Exception {
		XWhileExpression exp = (XWhileExpression) expression("while (null) null");

		assertExpected("java.lang.Boolean", exp.getPredicate());
		assertExpected(null, exp.getBody());
	}

	public void testWhileExpression_1() throws Exception {
		XDoWhileExpression exp = (XDoWhileExpression) expression("do null while (null)");

		assertExpected("java.lang.Boolean", exp.getPredicate());
		assertExpected(null, exp.getBody());
	}

	public void testTryCatchExpression() throws Exception {
		XTryCatchFinallyExpression exp = (XTryCatchFinallyExpression) ((XCastedExpression) 
				expression("(java.lang.String)try null catch (java.lang.Throwable t) null finally null"))
				.getTarget();

		assertExpected("java.lang.String", exp.getExpression());
		for (XCatchClause cc : exp.getCatchClauses()) {
			assertExpected(null, cc.getExpression());
			assertExpected("java.lang.Throwable", cc.getDeclaredParam());
		}
		assertExpected(null, exp.getFinallyExpression());
	}
	
	public void testThrowExpression() throws Exception {
		XThrowExpression exp = (XThrowExpression) expression("throw null");
		assertExpected("java.lang.Throwable", exp.getExpression());
	}
	
	public void testSwitchExpression_0() throws Exception {
		XSwitchExpression exp = (XSwitchExpression) ((XCastedExpression)expression(
				"(java.lang.String) switch null {" +
				"  java.lang.Boolean case null : null;" +
				"  default : null;" +
				"}")).getTarget();
		assertExpected(null,exp.getSwitch());
		for (XCasePart cp : exp.getCases()) {
			assertExpected("java.lang.Class", cp.getTypeGuard());
			assertExpected(null, cp.getCase());
			assertExpected("java.lang.String", cp.getThen());
		}
		assertExpected("java.lang.String", exp.getDefault());
	}
	
	public void testSwitchExpression_1() throws Exception {
		XSwitchExpression exp = (XSwitchExpression) ((XCastedExpression)expression(
				"(java.lang.String) switch {" +
				"  java.lang.Boolean case null : null;" +
				"  default : null;" +
		"}")).getTarget();
		for (XCasePart cp : exp.getCases()) {
			assertExpected("java.lang.Class", cp.getTypeGuard());
			assertExpected("java.lang.Boolean", cp.getCase());
			assertExpected("java.lang.String", cp.getThen());
		}
		assertExpected("java.lang.String", exp.getDefault());
	}
	
	public void testCastedExpression() throws Exception {
		XCastedExpression expression = (XCastedExpression) expression("(java.lang.Class<? extends java.lang.CharSequence>)null");
		assertExpected("java.lang.Class<? extends java.lang.CharSequence>", expression.getTarget());
	}

	protected void assertExpected(String expectedExpectedType, EObject obj) {
		JvmTypeReference reference = get(XbaseExpectedTypeProvider.class).getExpectedType(obj);
		if (reference == null)
			assertNull("expected " + expectedExpectedType + " but was null", expectedExpectedType);
		else
			assertEquals(expectedExpectedType, reference.getCanonicalName());
	}
}