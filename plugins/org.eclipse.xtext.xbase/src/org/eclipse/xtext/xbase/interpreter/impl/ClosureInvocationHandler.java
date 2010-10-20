/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.interpreter.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.interpreter.IEvaluationContext;
import org.eclipse.xtext.xbase.interpreter.IEvaluationResult;
import org.eclipse.xtext.xbase.interpreter.IExpressionInterpreter;
import org.eclipse.xtext.xbase.lib.Functions.FunctionX;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class ClosureInvocationHandler implements InvocationHandler {

	private final IExpressionInterpreter interpreter;

	private final IEvaluationContext context;
	
	private final XClosure closure;
	
	public ClosureInvocationHandler(XClosure closure, IEvaluationContext context, IExpressionInterpreter interpreter) {
		this.closure = closure;
		this.context = context;
		this.interpreter = interpreter;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (isEquals(method)) {
			return proxy == args[0];
		}
		if (isHashCode(method)) {
			return System.identityHashCode(proxy);
		}
		if (isToString(method)) {
			Class<?> interfaceType = proxy.getClass().getInterfaces()[0];
			return "Proxy for " + interfaceType.getName() + ": "+ closure.toString();
		}
		IEvaluationContext forkedContext = context.fork();
		if (args != null) {
			if (!FunctionX.class.equals(method.getDeclaringClass())) {
				initializeClosureParameters(forkedContext, args);
			} else {
				initializeClosureParameters(forkedContext, (Object[])args[0]);
			}
		}
		IEvaluationResult result = interpreter.evaluate(closure.getExpression(), forkedContext);
		if (result.getException() != null)
			throw result.getException();
		return result.getResult();
	}

	protected void initializeClosureParameters(IEvaluationContext context, Object[] args) {
		if (args.length != closure.getFormalParameters().size())
			throw new IllegalStateException("Number of arguments did not match. Expected: " + 
					closure.getFormalParameters().size() + " but was: " + args.length);
		int i = 0;
		for(JvmFormalParameter param: closure.getFormalParameters()) {
			context.newValue(QualifiedName.create(param.getName()), args[i]);
			i++;
		}
	}

	protected boolean isHashCode(Method method) {
		return "hashCode".equals(method.getName()) 
			&& method.getParameterTypes().length == 0;
	}
	
	protected boolean isToString(Method method) {
		return "toString".equals(method.getName()) 
			&& method.getParameterTypes().length == 0;
	}

	protected boolean isEquals(Method method) {
		return "equals".equals(method.getName())
			&& method.getParameterTypes().length == 1
			&& Object.class.equals(method.getParameterTypes()[0]);
	}

}
