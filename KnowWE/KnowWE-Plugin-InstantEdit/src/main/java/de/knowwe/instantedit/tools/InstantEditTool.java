package de.knowwe.instantedit.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.util.Icon;

public class InstantEditTool extends DefaultTool {

	public static final String DEFAULTJSNAMESPACE = "KNOWWE.plugin.defaultEditTool";

	private final String jsNameSpace;

	public InstantEditTool(Icon icon, String title, String description, Section<?> section) {
		this(icon, title, description, section, DEFAULTJSNAMESPACE);
	}

	public InstantEditTool(Icon icon, String title, String description, Section<?> section, String jsNameSpace) {
		this(icon, title, description, section, jsNameSpace, "");
	}

	public InstantEditTool(Icon icon, String title, String description, Section<?> section, String jsNameSpace, String additionalJSAction) {
		super(icon, title, description, getJSAction(section.getID(), jsNameSpace,
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
