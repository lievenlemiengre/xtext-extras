/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.expression;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XUnaryOperation;
import org.eclipse.xtext.xbase.tests.interpreter.AbstractXbaseInterpreterTest;

/**
 * @author Sebastian Benz - Initial contribution and API
 */
public class CustomExpressionTest extends AbstractXbaseInterpreterTest {

	public void testBinaryExpressionsShouldReturnArgumentsInInternalEList() throws Exception {
		assertResolvingCrossReferencesThrowsNoException("1 == 1");
	}
	
	public void testAssignmentShouldReturnArgumentsInInternalEList() throws Exception {
		assertResolvingCrossReferencesThrowsNoException("{var x = 'literal' x = 'newValue'}");
	} 

	public void testFeatureCallShouldReturnArgumentsInInternalEList() throws Exception {
		assertResolvingCrossReferencesThrowsNoException("'literal'.toUpperCase()");
	}
	
	public void testMemberFeatureCallShouldReturnArgumentsInInternalEList() throws Exception {
		assertResolvingCrossReferencesThrowsNoException("'literal'.toUpperCase()");
	}
	
	public void testUnaryOperationShouldReturnArgumentsInInternalEList() throws Exception {
		//unary operations are not implemented yet
		XUnaryOperation expression = (XUnaryOperation) expression("!true", false);
		assertTrue(expression.getArguments() instanceof InternalEList);
	}
	
	public void testNullValuesShouldBeIgnored() throws Exception {
		//unary operations are not implemented yet
		String input = "!";
		XUnaryOperation expression = (XUnaryOperation) incompleteExpression(input);
		expression.getArguments(); // throws exception if null value is added to result list
	}

	private XExpression incompleteExpression(String input) throws IOException {
		Resource resource = newResource(input);
		return (XExpression) resource.getContents().get(0);
	}
	
	private void assertResolvingCrossReferencesThrowsNoException(String input) throws Exception {
		XExpression expression = expression(input);
		// throws CCE otherwise
		((LazyLinkingResource) expression.eResource()).resolveLazyCrossReferences(CancelIndicator.NullImpl);
	}
}