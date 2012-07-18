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
package org.squirrel_lang.sqdev.ui.editor;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.*;

/**
 * This scanner recognizes the JavaDoc comments and Java multi line comments.
 */
public class SquirrelPartitionScanner extends RuleBasedPartitionScanner {

	public final static String SQUIRREL_MULTILINE_COMMENT= "__squirrel_multiline_comment"; //$NON-NLS-1$
	public final static String SQUIRREL_CLASS_ATTRIBUTE= "__squirrel_class_attribute"; //$NON-NLS-1$
	public final static String SQUIRREL_STRING= "__squirrel_string"; //$NON-NLS-1$
	public final static String SQUIRREL_MULTILINE_STRING= "__squirrel_multiline_string"; //$NON-NLS-1$
	public final static String[] SQUIRREL_PARTITION_TYPES= new String[] { SQUIRREL_MULTILINE_COMMENT, SQUIRREL_CLASS_ATTRIBUTE, SQUIRREL_MULTILINE_STRING };
		
	/**
	 * Detector for empty comments.
	 */
	static class EmptyCommentDetector implements IWordDetector {

		/* (non-Javadoc)
		* Method declared on IWordDetector
	 	*/
		public boolean isWordStart(char c) {
			return (c == '/');
		}

		/* (non-Javadoc)
		* Method declared on IWordDetector
	 	*/
		public boolean isWordPart(char c) {
			return (c == '*' || c == '/');
		}
	}
	
	/**
	 * 
	 */
	static class WordPredicateRule extends WordRule implements IPredicateRule {
		
		private IToken fSuccessToken;
		
		public WordPredicateRule(IToken successToken) {
			super(new EmptyCommentDetector());
			fSuccessToken= successToken;
			addWord("/**/", fSuccessToken); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(ICharacterScanner, boolean)
		 */
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return super.evaluate(scanner);
		}

		/*
		 * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
		 */
		public IToken getSuccessToken() {
			return fSuccessToken;
		}
	}

	/**
	 * Creates the partitioner and sets up the appropriate rules.
	 */
	public SquirrelPartitionScanner() {
		super();

		IToken classAttributes= new Token(SQUIRREL_CLASS_ATTRIBUTE);
		IToken comment= new Token(SQUIRREL_MULTILINE_COMMENT);
		IToken mlstring = new Token(SQUIRREL_MULTILINE_STRING);
		IToken string= new Token(SQUIRREL_STRING);
		List<IPredicateRule> rules= new ArrayList<IPredicateRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", Token.UNDEFINED)); //$NON-NLS-1$

		// Add rule for strings and character constants.
		
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add special case word rule.
		rules.add(new WordPredicateRule(comment));

		rules.add(new MultiLineRule("@\"", "\"", mlstring, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("</", "/>", classAttributes, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("/*", "*/", comment, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$
		

		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
