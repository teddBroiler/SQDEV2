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

import org.eclipse.jface.text.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.squirrel_lang.sqdev.SQDevPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A content outline page which always represents the content of the
 * connected editor in 10 segments.
 */
public class SquirrelContentOutlinePage extends ContentOutlinePage {

	/**
	 * A segment element.
	 */
	protected static class Segment {
		static final int CLASS = 0;
		static final int FUNCTION = 1;
		public String name;
		public Position position;
		public int type;

		public Segment(String name, Position position,int type) {
			this.name= name;
			this.position= position;
			this.type = type;
		}

		public String toString() {
			return name;
		}
	};

	protected class SquirrelLabelProvider implements ILabelProvider {
		public Image getImage(Object element)
		{
			if(element instanceof Segment) {
				switch(((Segment)element).type) {
				case Segment.CLASS:
					return SQDevPlugin.getImage("class");
				case Segment.FUNCTION:
					return SQDevPlugin.getImage("function");
				}
			}
			return null;
		}
		public String getText(Object element)
		{
			if(element instanceof Segment) {
				return ((Segment)element).name;
			}
			return null;
		}
		public void addListener(ILabelProviderListener listener){} 
		public void dispose(){} 
		public boolean isLabelProperty(Object element, String property){return false;}
		public void removeListener(ILabelProviderListener listener){} 

	}
	/**
	 * Divides the editor's document into ten segments and provides elements for them.
	 */
	protected class ContentProvider implements ITreeContentProvider {

		protected final static String SEGMENTS= "__squirrel_segments"; //$NON-NLS-1$
		protected IPositionUpdater fPositionUpdater= new DefaultPositionUpdater(SEGMENTS);
		protected List<Segment> fContent= new ArrayList<Segment>();
		public final static String FUNC_REX = "\\s*(function\\s\\w?((::\\w)*).+)|(constructor.+)";
		public final static String GLOB_FUNC_CLEAN_REX = "^\\s*function\\s+(\\w+\\s*\\([\\w,\\s\\.]*\\))";
		public final static String MEMBER_FUNC_CLEAN_REX = "^\\s*function\\s+(\\w+?(?:(?:::\\w+)*)\\s*\\([\\w,\\s\\.]*\\))";
		public final static String CONSTRUCTOR_FUNC_CLEAN_REX = "^\\s*(constructor\\s*\\([\\w,\\s\\.]*\\))";
		//public final static String CLASS_CLEAN_REX = "^\\s*class\\s+(\\w+)";
		public final static String CLASS_CLEAN_REX = "^\\s*class\\s+([0-9A-Za-z_.]+)";
		public final Pattern globFuncPattern = Pattern.compile(GLOB_FUNC_CLEAN_REX);
		public final Pattern memberFuncPattern = Pattern.compile(MEMBER_FUNC_CLEAN_REX);
		public final Pattern constructorFuncPattern = Pattern.compile(CONSTRUCTOR_FUNC_CLEAN_REX);
		public final Pattern classPattern = Pattern.compile(CLASS_CLEAN_REX); 

		protected void parse(IDocument document) {

			int lines= document.getNumberOfLines();
			//int increment= Math.max(Math.round((float) (lines / 10)), 10);
			
			for (int line= 0; line < lines; line += 1) {
				
				
				
			/*	int length= increment;
				if (line + increment > lines)
					length= lines - line;*/

				try {
					int offset=0;
					int length=0;
					int type = Segment.FUNCTION;
					IRegion region=document.getLineInformation(line);
					offset=region.getOffset();
					length=region.getLength();
					String sline=document.get(offset,length);
					Matcher matcher = globFuncPattern.matcher(sline);
					
					if(!matcher.find()) {
						matcher = memberFuncPattern.matcher(sline);
						if(!matcher.find()) {
														
							matcher = constructorFuncPattern.matcher(sline);
							if(!matcher.find()) {
									
								matcher = classPattern.matcher(sline);
								if(!matcher.find()) {
									continue;
								}
								type = Segment.CLASS;
							}
						}
					}
					
					{
						Position p= new Position(offset, length);
						document.addPosition(SEGMENTS, p);
						sline = sline.substring(matcher.start(1),matcher.end(1));
						sline.trim();
						fContent.add(new Segment(sline, p, type)); //$NON-NLS-1$
					}
					/*int offset= document.getLineOffset(line);
					int end= document.getLineOffset(line + length);
					length= end - offset;*/
					
					

				} catch (BadPositionCategoryException x) {
				} catch (BadLocationException x) {
				}
			}
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null) {
				IDocument document= fDocumentProvider.getDocument(oldInput);
				if (document != null) {
					try {
						document.removePositionCategory(SEGMENTS);
					} catch (BadPositionCategoryException x) {
					}
					document.removePositionUpdater(fPositionUpdater);
				}
			}

			fContent.clear();

			if (newInput != null) {
				IDocument document= fDocumentProvider.getDocument(newInput);
				if (document != null) {
					document.addPositionCategory(SEGMENTS);
					document.addPositionUpdater(fPositionUpdater);

					parse(document);
				}
			}
		}

		/*
		 * @see IContentProvider#dispose
		 */
		public void dispose() {
			if (fContent != null) {
				fContent.clear();
				fContent= null;
			}
		}

		/*
		 * @see IContentProvider#isDeleted(Object)
		 */
		public boolean isDeleted(Object element) {
			return false;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object element) {
			return fContent.toArray();
		}

		/*
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			return element == fInput;
		}

		/*
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof Segment)
				return fInput;
			return null;
		}

		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object element) {
			if (element == fInput)
				return fContent.toArray();
			return new Object[0];
		}
	};

	protected Object fInput;
	protected IDocumentProvider fDocumentProvider;
	protected ITextEditor fTextEditor;

	/**
	 * Creates a content outline page using the given provider and the given editor.
	 */
	public SquirrelContentOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		super();
		fDocumentProvider= provider;
		fTextEditor= editor;
	}
	
	/* (non-Squirreldoc)
	 * Method declared on ContentOutlinePage
	 */
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer= getTreeViewer();
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new SquirrelLabelProvider());
		viewer.addSelectionChangedListener(this);

		if (fInput != null)
			viewer.setInput(fInput);
	}
	
	/* (non-Squirreldoc)
	 * Method declared on ContentOutlinePage
	 */
	public void selectionChanged(SelectionChangedEvent event) {

		super.selectionChanged(event);

		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			fTextEditor.resetHighlightRange();
		else {
			Segment segment= (Segment) ((IStructuredSelection) selection).getFirstElement();
			int start= segment.position.getOffset();
			int length= segment.position.getLength();
			try {
				fTextEditor.setHighlightRange(start, length, true);
			} catch (IllegalArgumentException x) {
				fTextEditor.resetHighlightRange();
			}
		}
	}
	
	/**
	 * Sets the input of the outline page
	 */
	public void setInput(Object input) {
		fInput= input;
		update();
	}
	
	/**
	 * Updates the outline page.
	 */
	public void update() {
		TreeViewer viewer= getTreeViewer();

		if (viewer != null) {
			Control control= viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(fInput);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}

}
