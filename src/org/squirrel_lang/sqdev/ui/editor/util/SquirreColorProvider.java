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
package org.squirrel_lang.sqdev.ui.editor.util;


import java.util.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manager for colors used in the Java editor
 */
public class SquirreColorProvider {

	public static final RGB MULTI_LINE_COMMENT = new RGB(0, 128, 0);
	public static final RGB SINGLE_LINE_COMMENT = new RGB(0, 128, 0);
	public static final RGB KEYWORD = new RGB(0, 0, 255);
	public static final RGB TYPE= new RGB(0, 0, 128);
	public static final RGB STRING = new RGB(255, 0, 0);
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	public static final RGB MULTI_LINE_STRING = new RGB(255, 0, 0);
	public static final RGB CLASS_ATTRIBUTE = new RGB(128, 128, 128);
	//public static final RGB JAVADOC_LINK= new RGB(128, 128, 128);
	//public static final RGB JAVADOC_DEFAULT= new RGB(0, 128, 128);

	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);

	/**
	 * Release all of the color resources held onto by the receiver.
	 */	
	public void dispose() {
		Iterator<Color> e= fColorTable.values().iterator();
		while (e.hasNext())
			 e.next().dispose();
	}
	
	/**
	 * Return the color that is stored in the color table under the given RGB
	 * value.
	 * 
	 * @param rgb the RGB value
	 * @return the color stored in the color table for the given RGB value
	 */
	public Color getColor(RGB rgb) {
		Color color= fColorTable.get(rgb);
		if (color == null) {
			color= new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}
