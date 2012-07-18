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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.squirrel_lang.sqdev.SQDevPlugin;

/**
 * Java specific text editor.
 */
public class SquirrelEditor extends TextEditor {

	private class DefineFoldingRegionAction extends TextEditorAction {

		public DefineFoldingRegionAction(ResourceBundle bundle, String prefix,
				ITextEditor editor) {
			super(bundle, prefix, editor);
		}

		private IAnnotationModel getAnnotationModel(ITextEditor editor) {
			return (IAnnotationModel) editor
					.getAdapter(ProjectionAnnotationModel.class);
		}

		/*
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			ITextEditor editor = getTextEditor();
			ISelection selection = editor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				if (!textSelection.isEmpty()) {
					IAnnotationModel model = getAnnotationModel(editor);
					if (model != null) {

						int start = textSelection.getStartLine();
						int end = textSelection.getEndLine();

						try {
							IDocument document = editor.getDocumentProvider()
									.getDocument(editor.getEditorInput());
							int offset = document.getLineOffset(start);
							int endOffset = document.getLineOffset(end + 1);
							Position position = new Position(offset, endOffset
									- offset);
							model.addAnnotation(new ProjectionAnnotation(),
									position);
						} catch (BadLocationException x) {
							// ignore
						}
					}
				}
			}
		}
	}

	/** The outline page */
	private SquirrelContentOutlinePage fOutlinePage;

	/** The projection support */
	private ProjectionSupport fProjectionSupport;

	/**
	 * Default constructor.
	 */
	public SquirrelEditor() {
		super();
	}

	/**
	 * The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method extend the actions to add those
	 * specific to the receiver
	 */
	protected void createActions() {
		super.createActions();

		IAction a = new TextOperationAction(
				SquirrelEditorMessages.getResourceBundle(),
				"ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
		a
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a); //$NON-NLS-1$

		a = new TextOperationAction(
				SquirrelEditorMessages.getResourceBundle(),
				"ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		a
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a); //$NON-NLS-1$

		a = new DefineFoldingRegionAction(SquirrelEditorMessages
				.getResourceBundle(), "DefineFoldingRegion.", this); //$NON-NLS-1$
		setAction("DefineFoldingRegion", a); //$NON-NLS-1$
	}

	/**
	 * The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra disposal
	 * actions required by the java editor.
	 */
	public void dispose() {
		if (fOutlinePage != null)
			fOutlinePage.setInput(null);
		super.dispose();
	}

	/**
	 * The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra revert
	 * behavior required by the java editor.
	 */
	public void doRevertToSaved() {
		super.doRevertToSaved();
		UpdateViews();
	}

	/**
	 * The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra save behavior
	 * required by the java editor.
	 * 
	 * @param monitor
	 *            the progress monitor
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		UpdateViews();

	}

	/**
	 * The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra save as
	 * behavior required by the java editor.
	 */
	public void doSaveAs() {
		super.doSaveAs();
		UpdateViews();
	}

	/**
	 * The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs sets the input of the
	 * outline page after AbstractTextEditor has set input.
	 * 
	 * @param input
	 *            the editor input
	 * @throws CoreException
	 *             in case the input can not be set
	 */
	public void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fOutlinePage != null)
			fOutlinePage.setInput(input);
	}

	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "ContentAssistProposal"); //$NON-NLS-1$
		addAction(menu, "ContentAssistTip"); //$NON-NLS-1$
		addAction(menu, "DefineFoldingRegion"); //$NON-NLS-1$
	}

	/**
	 * The <code>JavaEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs gets the java content
	 * outline page if request is for a an outline page.
	 * 
	 * @param required
	 *            the required type
	 * @return an adapter for the required type or <code>null</code>
	 */
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage = new SquirrelContentOutlinePage(
						getDocumentProvider(), this);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
		}

		if (fProjectionSupport != null) {
			Object adapter = fProjectionSupport.getAdapter(getSourceViewer(),
					required);
			if (adapter != null)
				return adapter;
		}

		return super.getAdapter(required);
	}

	/*
	 * (non-Javadoc) Method declared on AbstractTextEditor
	 */
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new SquirrelSourceViewerConfiguration());
	}

	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {

		fAnnotationAccess = createAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
		fProjectionSupport = new ProjectionSupport(viewer,
				getAnnotationAccess(), getSharedColors());
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int,
	 *      int)
	 */
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer = getSourceViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}

	public final static String COMPILER_ERROR_REX = "[^\\(]+ line = \\((\\d+)\\) column = \\((\\d+)\\) : error ([^\\n]+)";

	private final Pattern errorPattern = Pattern.compile(COMPILER_ERROR_REX);

	protected void CompileFile(IFile file, String fullpath) {
		try {
			String compiler = SQDevPlugin.doGetPreferenceStore().getString(
					SQDevPlugin.EDITOR_COMPILER);
			if (compiler.length() == 0)
				return;
			file.deleteMarkers(SQDevPlugin.SQUIRREL_PROBLEM_MARKER, false,
					IResource.DEPTH_ZERO);
			// RUN COMPILER

			SQDevPlugin.logInfo("Compiling: " + fullpath);

			Process process = Runtime.getRuntime().exec(
					new String[]{compiler,"-c",fullpath});

			InputStream is = process.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String lineStr = null;

			while ((lineStr = in.readLine()) != null) {
				Matcher matcher = errorPattern.matcher(lineStr);
				boolean found = matcher.find();
				if (found && matcher.groupCount() == 3) {
					String sline = matcher.group(1);
					String scol = matcher.group(2);
					int line = Integer.parseInt(sline);
					int col = Integer.parseInt(scol);
					String err = matcher.group(3);
					//
					HashMap<String,Object> map = new HashMap<String,Object>();
					map.put(IMarker.MESSAGE, err);
					map.put(IMarker.LINE_NUMBER, new Integer(line));
					try {
						IDocument document = getDocumentProvider().getDocument(
								getEditorInput());
						IRegion region = document.getLineInformation(line - 1);

						int start = region.getOffset()
								+ Math.min(col - 2, region.getLength());
						map.put(IMarker.CHAR_START, new Integer(start));
						map.put(IMarker.CHAR_END, new Integer(start + 1));
					} catch (BadLocationException e) {
					}

					map.put(IMarker.SEVERITY, new Integer(1));
					MarkerUtilities.createMarker(file, map,
							SQDevPlugin.SQUIRREL_PROBLEM_MARKER);
				}
			}

			try {
				process.waitFor();
			} catch (InterruptedException e) {
			}
		} catch (CoreException e) {
		} catch (IOException e) {
			SQDevPlugin.logError("Error compiling: " + e, e);
		}
	}

	protected void UpdateViews() {
		if (fOutlinePage != null)
			fOutlinePage.update();
		IEditorInput ei = getEditorInput();
		boolean syntaxchekicng = SQDevPlugin.doGetPreferenceStore().getBoolean(
				SQDevPlugin.EDITOR_SYNTAX_CHECKING);
		if (syntaxchekicng) {
			if (ei instanceof IFileEditorInput) {
				IFileEditorInput fei = (IFileEditorInput) ei;
				IFile file = fei.getFile();
				IPath fullpath = file.getLocation();
				String spath = fullpath.toString();
				CompileFile(file, spath);

			}
		}
	}

}
