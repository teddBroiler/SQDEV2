package org.squirrel_lang.sqdev.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.squirrel_lang.sqdev.SQDevPlugin;

import org.eclipse.ui.INewWizard;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
//import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage; 
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class NewProjectWizard extends BasicNewResourceWizard implements INewWizard,IExecutableExtension {
	protected WizardNewProjectCreationPage projectPage;
	protected IConfigurationElement configurationElement;
	protected IProject newProject;
	
	public NewProjectWizard() {
		setWindowTitle("New");
	}

	public boolean performFinish() {
		IRunnableWithProgress projectCreationOperation = new WorkspaceModifyDelegatingOperation(getProjectCreationRunnable());

		try {
			getContainer().run(false, true, projectCreationOperation);
		} catch (Exception e) {
		//	RdtUiPlugin.log(e);
			return false;
		}

		BasicNewProjectResourceWizard.updatePerspective(configurationElement);
		selectAndReveal(newProject);

		return true;
	}

	protected IRunnableWithProgress getProjectCreationRunnable() {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				int remainingWorkUnits = 10;
				monitor.beginTask("Creating new Squirrel Project", remainingWorkUnits);

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				newProject = projectPage.getProjectHandle();
				
				IProjectDescription description = workspace.newProjectDescription(newProject.getName());
				IPath path = Platform.getLocation();
				IPath customPath = projectPage.getLocationPath();
				if (!path.equals(customPath)) {
					path = customPath;
					description.setLocation(path);
				}

				try {
					if (!newProject.exists()) {
						newProject.create(description, new SubProgressMonitor(monitor, 1));
						remainingWorkUnits--;
						monitor.worked(1);
					}
					if (!newProject.isOpen()) {
						newProject.open(new SubProgressMonitor(monitor, 1));
						remainingWorkUnits--;
						monitor.worked(1);
					} 
					SQDevPlugin.addSquirrelNature(newProject, new SubProgressMonitor(monitor, remainingWorkUnits));
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					
					monitor.done();
				}
			}
		};
	}

	public void addPages() {
		super.addPages();

		projectPage = new WizardNewProjectCreationPage("New Squirrel Project");
		projectPage.setTitle("New Squirrel Project");
		projectPage.setDescription("Creates a new Squirrel project");

		addPage(projectPage);
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		configurationElement = config;
	}

}
