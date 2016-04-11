package de.knowwe.instantedit.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

/**
 * Class to render tool menu for wiki tables.
 * 
 * @author volker_belli
 * @created 16.03.2012
 */
public final class TableRenderer implements Renderer {

	private class IconRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult out) {
			out.appendHtml("<img src='KnowWEExtension/images/table/table-menu-icon.png' />");
		}
	}

	ToolMenuDecoratingRenderer popupRenderer = new ToolMenuDecoratingRenderer(new IconRenderer());

	@Override
	public void render(Section<?> section, UserContext user, RenderResult out) {
		out.appendHtml("<div>");
		out.appendHtml("<div class='tablePopupParent toolMenuParent' id='");
		out.append(section.getID());
		out.appendHtml("'>");
		out.append("\n");

		for (Section<?> child : section.getChildren()) {
			out.append(child, user);
		}

		out.appendHtml("<div class='tablePopupIcon'>");
		popupRenderer.render(section, user, out);
		out.appendHtml("</div>");
		out.appendHtml("</div>");
		out.appendHtml("</div>");
	}

}