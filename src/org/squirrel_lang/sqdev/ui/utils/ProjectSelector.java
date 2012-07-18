/*
 * Created on Nov 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.squirrel_lang.sqdev.ui.utils;

import org.squirrel_lang.sqdev.SQDevPlugin;
import org.squirrel_lang.sqdev.ui.dialogs.ElementListSelectionDialog;

import org.eclipse.core.resources.IProject; 
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;
/**
 * @author Alberto
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ProjectSelector extends ResourceSelector {

	public ProjectSelector(Composite parent) {
		super(parent);
		
		browseDialogTitle = "Project Selection";
	}

	public IProject getSelection() { 
		String projectName = getSelectionText();
		if (projectName != null && !projectName.equals(""))
			return SQDevPlugin.getWorkspace().getRoot().getProject(projectName);
			
		return null;
	}

	protected void handleBrowseSelected() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new WorkbenchLabelProvider());
		dialog.setTitle(browseDialogTitle);
		dialog.setMessage(browseDialogMessage);
		dialog.setElements(SQDevPlugin.getSquirrelProjects());

		if (dialog.open() == Dialog.OK) {
			textField.setText(((IProject) dialog.getFirstResult()).getName());
		}
	}

	protected String validateResourceSelection() {
		IProject project = getSelection();
		return project == null ? EMPTY_STRING : project.getName();
	}

}
