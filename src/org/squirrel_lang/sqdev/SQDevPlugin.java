package org.squirrel_lang.sqdev;

import org.eclipse.ui.plugin.*;

import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.osgi.framework.Bundle;
import org.squirrel_lang.sqdev.ui.editor.SquirrelPartitionScanner;
import org.squirrel_lang.sqdev.ui.editor.squirrel.SquirreCodeScanner;
//import org.squirrel_lang.sqdev.ui.editor.squirreldoc.SquirreDocScanner;
import org.squirrel_lang.sqdev.ui.editor.util.SquirreColorProvider;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

import java.util.*;

import java.io.File;
import java.io.FileReader;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * The main plugin class to be used in the desktop.
 */
public class SQDevPlugin extends AbstractUIPlugin {

	// The shared instance.
	private static SQDevPlugin plugin;

	public final static String SQDEV_PLUGIN_ID = "org.squirrel_lang.sqdev";

	public final static String SQDEV_NATURE_ID = SQDEV_PLUGIN_ID
			+ ".squirrelnature";

	public final static String SQUIRREL_PARTITIONING = "__squirrel_partitioning"; //$NON-NLS-1$

	public static final String SQUIRREL_PROBLEM_MARKER = "org.squirrel_lang.sqdev.squirrelproblemmarker";

	// preferences
	public static final String EDITOR_SYNTAX_CHECKING = "EDITOR_SYNTAX_CHECKING";

	public static final String EDITOR_COMPLETION_DB1 = "EDITOR_COMPLETION_DB1";

	public static final String EDITOR_COMPLETION_DB2 = "EDITOR_COMPLETION_DB2";

	public static final String EDITOR_COMPLETION_DB3 = "EDITOR_COMPLETION_DB3";

	public static final boolean EDITOR_SYNTAX_CHECKING_DEFAULT = false;

	public static final String EDITOR_COMPILER = "EDITOR_COMPILER";

	public static final String EDITOR_COMPILER_DEFAULT = "";

	public static final String EDITOR_COMPLETION_DB1_DEFAULT = "";

	public static final String EDITOR_COMPLETION_DB2_DEFAULT = "";

	public static final String EDITOR_COMPLETION_DB3_DEFAULT = "";

	private SquirrelPartitionScanner fPartitionScanner;

	private SquirreColorProvider fColorProvider;

	private SquirreCodeScanner fCodeScanner;

	// private SquirreDocScanner fDocScanner;

	private ResourceBundle resourceBundle;

	/**
	 * Creates a new plug-in instance.
	 */
	public SQDevPlugin() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("sqdev.sqdevPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

	}

	/**
	 * Log an INFO message to the Eclipse logging framework. Convenience wrapper
	 * to the logging system.
	 * 
	 * @param msg
	 *            The text to append to the log.
	 */
	public static void logInfo(String msg) {
		logInfo(msg, null);
	}

	/**
	 * Log an INFO message to the Eclipse logging framework. Convenience wrapper
	 * to the logging system.
	 * 
	 * @param msg
	 *            The text to append to the log.
	 * 
	 * @param e
	 *            An exception that is the cause of the message (or
	 *            <code>null</code> if none).
	 */
	public static void logInfo(String msg, Exception e) {
		plugin.getLog().log(
				new Status(Status.INFO, SQDEV_PLUGIN_ID, Status.OK, msg, e));
	}

	/**
	 * Log an ERROR message to the Eclipse logging framework. Convenience
	 * wrapper for the logging system.
	 * 
	 * @param msg
	 *            The text to append to the log.
	 */
	public static void logError(String msg) {
		logError(msg, null);
	}

	/**
	 * Log an ERROR message to the Eclipse logging framework. Convenience
	 * wrapper for the logging system.
	 * 
	 * @param msg
	 *            The text to append to the log.
	 * @param e
	 *            The "cause" exception, if any (may be <code>null</code>).
	 */
	public static void logError(String msg, Exception e) {
		plugin.getLog().log(
				new Status(Status.ERROR, SQDEV_PLUGIN_ID, Status.OK, msg, e));
	}

	public static IPreferenceStore doGetPreferenceStore() {

		return SQDevPlugin.getDefault().getPreferenceStore();
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Initialize the default preferences. This is called when the preferences
	 * dialog is opened for the first time.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(EDITOR_COMPILER, EDITOR_COMPILER_DEFAULT);
		store.setDefault(EDITOR_SYNTAX_CHECKING, EDITOR_SYNTAX_CHECKING_DEFAULT);
		store.setDefault(EDITOR_COMPLETION_DB1, EDITOR_COMPLETION_DB1_DEFAULT);
		store.setDefault(EDITOR_COMPLETION_DB2, EDITOR_COMPLETION_DB2_DEFAULT);
		store.setDefault(EDITOR_COMPLETION_DB3, EDITOR_COMPLETION_DB3_DEFAULT);

	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = SQDevPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Search the current workspace for any and all Squirrel projects.
	 * 
	 * @return A (possibly empty) array of Squirrel project references.
	 */
	public static IProject[] getSquirrelProjects() {
		List<IProject> squirrelProjectsList = new ArrayList<IProject>();
		IProject[] workspaceProjects = SQDevPlugin.getWorkspace().getRoot()
				.getProjects();

		for (int i = 0; i < workspaceProjects.length; i++) {
			IProject iProject = workspaceProjects[i];
			if (isSquirrelProject(iProject))
				squirrelProjectsList.add(iProject);
		}

		IProject[] squirrelProjects = new IProject[squirrelProjectsList.size()];
		return (IProject[]) squirrelProjectsList.toArray(squirrelProjects);
	}

	/**
	 * Test if a project is a Squirrel project. This is indicated by a project
	 * having the Squirrel nature attached to it. (Usually done at project
	 * creation time.)
	 * 
	 * @param aProject
	 *            The project to test.
	 * @return True if the squirrel nature was found for this project, or false
	 *         otherwise.
	 */
	public static boolean isSquirrelProject(IProject aProject) {
		try {
			return aProject.hasNature(SQDevPlugin.SQDEV_NATURE_ID);
		} catch (CoreException e) {
		}
		return false;
	}

	/**
	 * Returns the default plug-in instance.
	 * 
	 * @return the default plug-in instance
	 */
	public static SQDevPlugin getDefault() {
		return plugin;
	}

	/**
	 * Return a scanner for creating Java partitions.
	 * 
	 * @return a scanner for creating Java partitions
	 */
	public SquirrelPartitionScanner getSquirrelPartitionScanner() {
		if (fPartitionScanner == null)
			fPartitionScanner = new SquirrelPartitionScanner();
		return fPartitionScanner;
	}

	/**
	 * Returns the singleton Squirrel code scanner.
	 * 
	 * @return the singleton Squirrel code scanner
	 */
	public RuleBasedScanner getSquirrelCodeScanner() {
		if (fCodeScanner == null)
			fCodeScanner = new SquirreCodeScanner(getSquirrelColorProvider());
		return fCodeScanner;
	}

	/**
	 * Returns the singleton Squirrel color provider.
	 * 
	 * @return the singleton Squirrel color provider
	 */
	public SquirreColorProvider getSquirrelColorProvider() {
		if (fColorProvider == null)
			fColorProvider = new SquirreColorProvider();
		return fColorProvider;
	}

	/**
	 * Returns the singleton Squirrel scanner.
	 * 
	 * @return the singleton Squirrel scanner
	 */
	/*
	 * public RuleBasedScanner getJavaDocScanner() { if (fDocScanner == null)
	 * fDocScanner= new SquirreDocScanner(fColorProvider); return fDocScanner; }
	 */

	/* custom functions */
	public static void addSquirrelNature(IProject project,
			IProgressMonitor monitor) throws CoreException {
		if (!project.hasNature(SQDevPlugin.SQDEV_NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = SQDevPlugin.SQDEV_NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		}
	}

	// IMAGES RELATED STUFF

	@SuppressWarnings("deprecation")
	protected void RegisterIcon(ImageRegistry reg, String name, String path) {

		ImageDescriptor id;
		String iconPath = "$nl$/" + path;
		Bundle b = Platform.getBundle(SQDEV_PLUGIN_ID);
		// Leaving in the Eclipse 3.1 way for compatibility.
 		//URL url = Platform.find( b, new Path(iconPath) );
 		// The Eclipse 3.2 way:
		URL url = FileLocator.find(b, new Path(iconPath), null);
		id = ImageDescriptor.createFromURL(url);
		reg.put(name + ".desc", id);
		Image img = id.createImage();
		reg.put(name, img);
	}

	protected void RegisterStandardIcon(ImageRegistry reg, String name) {
		ImageDescriptor id = DebugUITools.getImageDescriptor(name);
		Image img = id.createImage();
		reg.put(name, img);
	}

	protected void initializeImageRegistry(ImageRegistry reg) {
		RegisterIcon(reg, "array", "icons/types/array.png");
		RegisterIcon(reg, "bool", "icons/types/bool.png");
		RegisterIcon(reg, "class", "icons/types/class.png");
		RegisterIcon(reg, "float", "icons/types/float.png");
		RegisterIcon(reg, "function", "icons/types/function.png");
		RegisterIcon(reg, "generator", "icons/types/generator.png");
		RegisterIcon(reg, "instance", "icons/types/instance.png");
		RegisterIcon(reg, "integer", "icons/types/integer.png");
		RegisterIcon(reg, "null", "icons/types/null.png");
		RegisterIcon(reg, "table", "icons/types/table.png");
		RegisterIcon(reg, "thread", "icons/types/thread.png");
		RegisterIcon(reg, "string", "icons/types/string.png");
		RegisterIcon(reg, "weakref", "icons/types/weakref.png");
		RegisterIcon(reg, "roottable", "icons/types/roottable.png");
		RegisterStandardIcon(reg, IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
		RegisterStandardIcon(reg, IDebugUIConstants.IMG_OBJS_BREAKPOINT);
		RegisterStandardIcon(reg, IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED);
		RegisterStandardIcon(reg, IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
	}

	public static URL newURL(String url_name) {
		try {
			return new URL(url_name);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed URL " + url_name, e);
		}
	}

	public static Image getImage(String name) {
		return getDefault().getImageRegistry().get(name);

	}

	private static void ParseCompletionDesc(CompletionNode parent,
			org.w3c.dom.Node node) {
		NodeList nodes = node.getChildNodes();

		int len = nodes.getLength();
		for (int n = 0; n < len; n++) {
			org.w3c.dom.Node child = (org.w3c.dom.Node) nodes.item(n);
			if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				org.w3c.dom.Element e = (org.w3c.dom.Element) child;
				CompletionNode cn = new CompletionNode();
				String name = e.getAttribute("name");
				cn.name = name.toLowerCase();
				cn.display = name;
				cn.type = e.getAttribute("type");
				cn.icon = getImage(cn.type);
				if (cn.type.equals("function")) {
					cn.signature = e.getAttribute("signature");
				}
				parent.addChildren(cn);
				ParseCompletionDesc(cn, e);
			}
		}
	}

	public static CompletionNode completionDatabase;

	private static void LoadSymbolFile(CompletionNode db, String filename) {
		try {
			DocumentBuilderFactory docfactory = DocumentBuilderFactory
					.newInstance();
			docfactory.setValidating(false);

			DocumentBuilder docbuilder = docfactory.newDocumentBuilder();

			File f = new File(filename);
			Document doc = docbuilder.parse(new InputSource(new FileReader(f)));

			NodeList cc = doc.getElementsByTagName("symbols");
			ParseCompletionDesc(completionDatabase, cc.item(0));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void resetCompletionDatabase() {
		completionDatabase = null;
	}

	public static CompletionNode getCompletionDatabase() {

		if (completionDatabase == null) {
			completionDatabase = new CompletionNode();
			completionDatabase.name = "@root@";
			try {
				/*
				 * DocumentBuilderFactory docfactory =
				 * DocumentBuilderFactory.newInstance();
				 * docfactory.setValidating(false);
				 * 
				 * DocumentBuilder docbuilder = docfactory.newDocumentBuilder();
				 * 
				 * File f = new File("D:\\Nerv\\MasterCD\\completion.xml");
				 * Document doc = docbuilder.parse( new InputSource(new
				 * FileReader(f)));
				 * 
				 * 
				 * 
				 * NodeList cc = doc.getElementsByTagName("symbols");
				 * ParseCompletionDesc(completionDatabase,cc.item(0));
				 */
				String f1 = doGetPreferenceStore().getString(
						EDITOR_COMPLETION_DB1);
				if (f1.length() > 0)
					LoadSymbolFile(completionDatabase, f1);
				String f2 = doGetPreferenceStore().getString(
						EDITOR_COMPLETION_DB2);
				if (f2.length() > 0)
					LoadSymbolFile(completionDatabase, f2);
				String f3 = doGetPreferenceStore().getString(
						EDITOR_COMPLETION_DB3);
				if (f3.length() > 0)
					LoadSymbolFile(completionDatabase, f3);
				// LoadSymbolFile()
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return completionDatabase;
	}

}
