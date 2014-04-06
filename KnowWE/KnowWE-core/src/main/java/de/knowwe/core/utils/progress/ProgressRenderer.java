package de.knowwe.core.utils.progress;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

public class ProgressRenderer implements Renderer {

	private static final ProgressRenderer INSTANCE = new ProgressRenderer();

	public static ProgressRenderer getInstance() {
		return INSTANCE;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		if (!LongOperationUtils.getLongOperations(section).isEmpty()) {
			String id = section.getID();
			result.appendHtml("<div id='progress_" + id + "' sectionId='" + id + "' class='progress-container'></div>");
			result.appendHtml("<script>\n");
			result.appendHtml("KNOWWE.core.plugin.progress.updateProgressBar('" + id + "');\n");
			result.appendHtml("</script>");
		}
	}
}
