package org.squirrel_lang.sqdev;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class SquirrelPerspective implements IPerspectiveFactory {

	/* (non-Javadoc) 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();
		
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
	    layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		layout.addNewWizardShortcut("org.squirrel_lang.sqdev.ui.wizards.NewProjectWizard");
		


		// Top left: Resource Navigator view and Bookmarks view placeholder
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,
			editorArea);
		
		IFolderLayout outputArea = layout.createFolder("outputArea", IPageLayout.BOTTOM, (float)0.75, editorArea);
		outputArea.addView(IPageLayout.ID_PROBLEM_VIEW);
					
		topLeft.addView(IPageLayout.ID_RES_NAV);
		//topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);

		//debugger stuff
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		
		// Bottom left: Outline view and Property Sheet view
		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f,"topLeft");
		bottomLeft.addView(IPageLayout.ID_OUTLINE);
		
		

		// Bottom right: Task List view
		layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.BOTTOM, 0.66f, editorArea);
		
		


	}
}
