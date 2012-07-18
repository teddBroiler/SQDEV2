package org.squirrel_lang.sqdev.ui.dialogs;

import org.eclipse.core.runtime.IStatus;

public interface ISelectionValidator {

	IStatus validate(Object[] selection);

}