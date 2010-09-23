package de.d3web.we.tools;


public class DefaultTool implements Tool {

	private final String iconPath;
	private final String title;
	private final String description;
	private final String jsAction;

	public DefaultTool(String iconPath, String title, String description, String jsAction) {
		this.iconPath = iconPath;
		this.title = title;
		this.description = description;
		this.jsAction = jsAction;
	}

	public String getIconPath() {
		return iconPath;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String getJSAction() {
		return jsAction;
	}


}
