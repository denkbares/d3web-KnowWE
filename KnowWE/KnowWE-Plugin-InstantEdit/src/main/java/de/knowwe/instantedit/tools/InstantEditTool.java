package de.knowwe.instantedit.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;

public class InstantEditTool extends DefaultTool {

	public static final String DEFAULTJSNAMESPACE = "KNOWWE.plugin.defaultEditTool";

	private final String jsNameSpace;

	public InstantEditTool(String iconPath, String title, String description, Section<?> section) {
		this(iconPath, title, description, section, DEFAULTJSNAMESPACE);
	}

	public InstantEditTool(String iconPath, String title, String description, Section<?> section, String jsNameSpace) {
		this(iconPath, title, description, section, jsNameSpace, "");
	}

	public InstantEditTool(String iconPath, String title, String description, Section<?> section, String jsNameSpace, String additionalJSAction) {
		super(iconPath, title, description, getJSAction(section.getID(), jsNameSpace,
				additionalJSAction), Tool.CATEGORY_EDIT);
		if (jsNameSpace == null) {
			throw new NullPointerException("jsNameSpace needs to be specified for InstantEditTools");
		}
		this.jsNameSpace = jsNameSpace;
	}

	private static String getJSAction(String sectionID, String jsNameSpace, String additionalJSAction) {
		return additionalJSAction
				+ "KNOWWE.plugin.instantEdit.enable("
				+ "'" + sectionID + "', "
				+ jsNameSpace + ");";
	}

	public String getJSNameSpace() {
		return this.jsNameSpace;
	}

}
