/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.squirrel_lang.sqdev.ui.editor.squirrel;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.squirrel_lang.sqdev.ui.editor.util.*;

/**
 * A Java code scanner.
 */
public class SquirreCodeScanner extends RuleBasedScanner {

	private static String[] fgKeywords= { 
		"break", "case", "catch", "class", "clone", "continue", 
		"default", "delegate", "delete", "do", "else", "extends", "for", 
		"function", "if", "in", "local", "resume",
		"return", "switch", "this", "throw", "try", "typeof",
		"while","parent", "yield", "constructor", "vargc", "vargv", 
		"instanceof" ,"foreach", "static"
 
		}; 

	//private static String[] fgTypes= { "void", "boolean", "char", "byte", "short", "int", "long", "float", "double" }; //$NON-NLS-1$ //$NON-NLS-5$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-2$

	private static String[] fgConstants= { "false", "null", "true" }; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	/**
	 * Creates a Java code scanner with the given color provider.
	 * 
	 * @param provider the color provider
	 */
	public SquirreCodeScanner(SquirreColorProvider provider) {

		IToken keyword= new Token(new TextAttribute(provider.getColor(SquirreColorProvider.KEYWORD)));
		IToken type= new Token(new TextAttribute(provider.getColor(SquirreColorProvider.TYPE)));
		IToken string= new Token(new TextAttribute(provider.getColor(SquirreColorProvider.STRING)));
		IToken comment= new Token(new TextAttribute(provider.getColor(SquirreColorProvider.SINGLE_LINE_COMMENT)));
		IToken other= new Token(new TextAttribute(provider.getColor(SquirreColorProvider.DEFAULT)));

		List<IRule> rules= new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new SquirreWhitespaceDetector()));

		// Add word rule for keywords, types, and constants.
		WordRule wordRule= new WordRule(new SquirreWordDetector(), other);
		for (int i= 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], keyword);
		for (int i= 0; i < fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], type);
		rules.add(wordRule);

		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
