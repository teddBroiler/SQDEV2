/*
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.squirrel_lang.sqdev.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Alberto
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SquirrelElementVisitor implements IResourceVisitor {

	protected List<IFile> nutFiles = new ArrayList<IFile>();

	public SquirrelElementVisitor() {
		super();
	}

	public boolean visit(IResource resource) throws CoreException {
		switch (resource.getType()) {
			case IResource.PROJECT :
				return true;

			case IResource.FOLDER :
				return true;

			case IResource.FILE :
				IFile fileResource = (IFile) resource;
				if ( "nut".equals(fileResource.getFileExtension()) || "aq".equals(fileResource.getFileExtension()) ) {
					nutFiles.add(fileResource);
					return true;
				}

			default :
				return false;
		}
	}
	
	public Object[] getCollectedSquirrelFiles() {
		return nutFiles.toArray();
	}

}
