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
import java.util.StringTokenizer;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.swt.custom.StyledText;
import org.squirrel_lang.sqdev.CompletionNode;
import org.squirrel_lang.sqdev.SQDevPlugin;


/**
 * Example Java completion processor.
 */
public class SquirrelCompletionProcessor implements IContentAssistProcessor {

	/**
	 * Simple content assist tip closer. The tip is valid in a range
	 * of 5 characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

		protected int fInstallOffset;

		/*
		 * @see IContextInformationValidator#isContextInformationValid(int)
		 */
		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		/*
		 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
		 */
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			fInstallOffset= offset;
		}
		
		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int, TextPresentation)
		 */
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	protected final static String[] fgProposals=
		{ "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while" }; //$NON-NLS-48$ //$NON-NLS-47$ //$NON-NLS-46$ //$NON-NLS-45$ //$NON-NLS-44$ //$NON-NLS-43$ //$NON-NLS-42$ //$NON-NLS-41$ //$NON-NLS-40$ //$NON-NLS-39$ //$NON-NLS-38$ //$NON-NLS-37$ //$NON-NLS-36$ //$NON-NLS-35$ //$NON-NLS-34$ //$NON-NLS-33$ //$NON-NLS-32$ //$NON-NLS-31$ //$NON-NLS-30$ //$NON-NLS-29$ //$NON-NLS-28$ //$NON-NLS-27$ //$NON-NLS-26$ //$NON-NLS-25$ //$NON-NLS-24$ //$NON-NLS-23$ //$NON-NLS-22$ //$NON-NLS-21$ //$NON-NLS-20$ //$NON-NLS-19$ //$NON-NLS-18$ //$NON-NLS-17$ //$NON-NLS-16$ //$NON-NLS-15$ //$NON-NLS-14$ //$NON-NLS-13$ //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	protected IContextInformationValidator fValidator= new Validator();

	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	class CompletionTarget {
		public String[] target;
		public int offset;
	}
	private  CompletionTarget GetCompletionTargetByPos(ITextViewer viewer, int documentOffset,boolean funcinfo)
	{
		StyledText st = viewer.getTextWidget();
		int len = 150;
		len = documentOffset < len ?documentOffset:len;
		int start = documentOffset-len;
		String text = st.getText(start,documentOffset-1);
		if(text.length() == 0)return null;
		int n = text.length()-1;
		int offset = 0;
		String ret = "";
		if(funcinfo) {
			while(n >= 0) {
				char c = text.charAt(n);
				if(c == '(') {n--; break;}
				n--;
				offset++;
			}
		}
		while(n >= 0) {
			char c = text.charAt(n);
			if(!Character.isJavaIdentifierPart(c) && c != '.')
				break;
			n--;
			offset++;
			ret = c + ret;
		}
		
		
		
		if(ret.length() == 0) return null;
		
		
		
		StringTokenizer stok = new StringTokenizer(ret.toLowerCase(),".");
		
		String[] s = new String[stok.countTokens()];
		for(int y = 0;y< s.length; y++) {
			s[y] = stok.nextToken();
		}
		CompletionTarget ct = new CompletionTarget();
		ct.target = s;
		ct.offset = offset;
		return ct;
	}
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		try {
			CompletionTarget ct = GetCompletionTargetByPos(viewer,documentOffset,false);
			if(ct == null)return null;
			if(ct.target == null) return null;
			
			int suggestionoffset = documentOffset-ct.offset;
			CompletionNode db = SQDevPlugin.getCompletionDatabase();
			ArrayList<CompletionNode> res = new ArrayList<CompletionNode>();
			int nres = db.getCandidates(res,ct.target);
			if(nres > 0)
			{
				ICompletionProposal[] result = new ICompletionProposal[nres];
				for(int k = 0; k < nres; k++) {
					CompletionNode cn = (CompletionNode)res.get(k);
					String display = cn.display;
					if(cn.signature != null) {
						display += cn.signature;
					}
					result[k] = new CompletionProposal(cn.display,suggestionoffset,ct.offset, cn.display.length(),
							cn.icon,display,null,null);
					
				}
				return result;
				}
			return null;
		}catch(Exception e)
		{
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result= new IContextInformation[1];
		CompletionTarget ct = GetCompletionTargetByPos(viewer,documentOffset,true);
		if(ct == null)return null;
		if(ct.target == null) return null;
				
		CompletionNode db = SQDevPlugin.getCompletionDatabase();
		ArrayList<CompletionNode> res = new ArrayList<CompletionNode>();
		int nres = db.getCandidates(res,ct.target);
		if(nres > 0) {
			result[0] = res.get(0);
			return result;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '(' };
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public String getErrorMessage() {
		return null;
	}
}
