/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.squirrel_lang.sqdev.ui.config;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.squirrel_lang.sqdev.SQDevPlugin;
import org.squirrel_lang.sqdev.ui.utils.FileSelector;

/**
 * @author Alberto
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SquirrelBasePreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	FileSelector compiler;
	Button syntaxcheking;
	FileSelector completiondb1;
	FileSelector completiondb2;
	FileSelector completiondb3;
	public SquirrelBasePreferencePage() {
		super();
	}

	protected IPreferenceStore doGetPreferenceStore() {
		 return SQDevPlugin.getDefault().getPreferenceStore(); 
	}
	public void init(IWorkbench workbench) {}

	private void UpdateDialog()
	{
		boolean errorcheck = syntaxcheking.getSelection(); 
		compiler.setEnabled(errorcheck);
	}
	protected void LoadConfiguration()
	{
		compiler.setSelectionText(doGetPreferenceStore().getString(SQDevPlugin.EDITOR_COMPILER));
		syntaxcheking.setSelection(doGetPreferenceStore().getBoolean(SQDevPlugin.EDITOR_SYNTAX_CHECKING));
		completiondb1.setSelectionText(doGetPreferenceStore().getString(SQDevPlugin.EDITOR_COMPLETION_DB1));
		completiondb2.setSelectionText(doGetPreferenceStore().getString(SQDevPlugin.EDITOR_COMPLETION_DB2));
		completiondb3.setSelectionText(doGetPreferenceStore().getString(SQDevPlugin.EDITOR_COMPLETION_DB3));
		UpdateDialog();
	}
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		new Label(composite, SWT.NONE).setText("General Properties"); //$NON-NLS-1$
		syntaxcheking = new Button(composite,SWT.CHECK);
		syntaxcheking.setText("Enable syntax checking");
		syntaxcheking.addMouseListener(new MouseAdapter(){
									public void mouseDoubleClick(MouseEvent e)  {
										UpdateDialog();
									}
									public void mouseDown(MouseEvent e)  {
										UpdateDialog();
									}
									public void mouseUp(MouseEvent e)  {
										UpdateDialog();
									}
									});
		new Label(composite, SWT.SEPARATOR|SWT.HORIZONTAL ).setText("Compiler"); //$NON-NLS-1$
		compiler = new FileSelector(composite);
		compiler.AddExtensionFilter("Executables(*.exe)","exe");
		compiler.AddExtensionFilter("All Files(*.*)","*");
		compiler.setBrowseDialogMessage("select the interepreter executable");
		compiler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		compiler.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				UpdateDialog();
			}});
		new Label(composite, SWT.NONE ).setText("Auto completion"); //$NON-NLS-1$
		new Label(composite, SWT.SEPARATOR|SWT.HORIZONTAL ).setText("Compiler"); //$NON-NLS-1$
		new Label(composite, SWT.NONE).setText("Symbols file 1");
		completiondb1 = new FileSelector(composite);
		AddCompletionDBBox(composite,completiondb1);
		new Label(composite, SWT.NONE).setText("Symbols file 2");
		completiondb2 = new FileSelector(composite);
		AddCompletionDBBox(composite,completiondb2);
		new Label(composite, SWT.NONE).setText("Symbols file 3");
		completiondb3 = new FileSelector(composite);
		AddCompletionDBBox(composite,completiondb3);
		
		LoadConfiguration();
		return composite;
	}
	private void AddCompletionDBBox(Composite composite,FileSelector fs)
	{
		fs.AddExtensionFilter("Completion DB(*.xml)","xml");
		fs.AddExtensionFilter("All Files(*.*)","*");
		fs.setBrowseDialogMessage("a symbol file");
		fs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fs.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				UpdateDialog();
			}});
	}
	public boolean performOk()
	{
		doGetPreferenceStore().setValue(SQDevPlugin.EDITOR_SYNTAX_CHECKING,syntaxcheking.getSelection());
		doGetPreferenceStore().setValue(SQDevPlugin.EDITOR_COMPILER,compiler.getSelectionText());
		doGetPreferenceStore().setValue(SQDevPlugin.EDITOR_COMPLETION_DB1,completiondb1.getSelectionText());
		doGetPreferenceStore().setValue(SQDevPlugin.EDITOR_COMPLETION_DB2,completiondb2.getSelectionText());
		doGetPreferenceStore().setValue(SQDevPlugin.EDITOR_COMPLETION_DB3,completiondb3.getSelectionText());
		SQDevPlugin.resetCompletionDatabase();		
		return true;
	}
}
