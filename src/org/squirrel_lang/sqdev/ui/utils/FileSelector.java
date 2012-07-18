/*
 * Created on Nov 14, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.squirrel_lang.sqdev.ui.utils;



import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
/**
 * @author Alberto
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FileSelector extends ResourceSelector {

	public FileSelector(Composite parent) {
			super(parent);
	}
	/* (non-Javadoc)
	 * @see org.squirrel_lang.sqdev.ui.utils.ResourceSelector#handleBrowseSelected()
	 */
	protected void handleBrowseSelected() {
		FileDialog filedlg = new FileDialog(getShell());
		if(extensions.size() > 0) {
			String[] exts = new String[extensions.size()];
			String[] names = new String[extensions_names.size()];
			extensions.toArray(exts);
			extensions_names.toArray(names);
			filedlg.setFilterExtensions(exts);
			filedlg.setFilterNames(names);
		}
		String selectedFile = filedlg.open();
		textField.setText(selectedFile);
	}

	/* (non-Javadoc)
	 * @see org.squirrel_lang.sqdev.ui.utils.ResourceSelector#validateResourceSelection()
	 */
	protected String validateResourceSelection() {
		return textField.getText();
		//return null;
	}
	public void AddExtensionFilter(String name,String ext) {
		extensions_names.add(name);
		extensions.add("*."+ext);
	}
	private ArrayList<String> extensions = new ArrayList<String>();
	private ArrayList<String> extensions_names = new ArrayList<String>();
}
