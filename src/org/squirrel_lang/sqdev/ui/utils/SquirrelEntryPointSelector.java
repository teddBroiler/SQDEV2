/*
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.squirrel_lang.sqdev.ui.utils;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.squirrel_lang.sqdev.ui.dialogs.ElementListSelectionDialog;
/**
 * @author Alberto
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SquirrelEntryPointSelector extends ResourceSelector {

	protected ProjectSelector projectSelector;

	public SquirrelEntryPointSelector(Composite parent, ProjectSelector projectSelector) {
		super(parent);
		this.projectSelector = projectSelector;
		
		browseDialogTitle = "File Selection";
	}

	protected Object[] getSquirrelFiles() {
		IProject squirrelProject = projectSelector.getSelection();
		if (squirrelProject == null)
			return new Object[0];

		SquirrelElementVisitor visitor = new SquirrelElementVisitor();
		try {
			squirrelProject.accept(visitor);
		} catch(CoreException e) {
			System.out.println(e);
		}
		return visitor.getCollectedSquirrelFiles();
	}

	public IFile getSelection() {
		String fileName = getSelectionText();
		if (fileName != null && !fileName.equals("")) {
			IPath filePath = new Path(fileName);
			IProject project = projectSelector.getSelection();
			if (project != null && project.exists(filePath))
				return project.getFile(filePath);
		}
			
		return null;
	}

	protected void handleBrowseSelected() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new WorkbenchLabelProvider());
		dialog.setTitle(browseDialogTitle);
		dialog.setMessage(browseDialogMessage);
		dialog.setElements(getSquirrelFiles());

		if (dialog.open() == Dialog.OK) {
			textField.setText(((IResource) dialog.getFirstResult()).getProjectRelativePath().toString());
		}
	}

	protected String validateResourceSelection() {
		IFile selection = getSelection();
		return selection == null ? EMPTY_STRING : selection.getProjectRelativePath().toString();
	}
}
