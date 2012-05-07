package de.knowwe.instantedit.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
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
		public void render(Section<?> section, UserContext user, StringBuilder out) {
			out.append(Strings.maskHTML("<img src='KnowWEExtension/images/table/table-menu-icon.png'></img>"));
		}
	}

	ToolMenuDecoratingRenderer popupRenderer = new ToolMenuDecoratingRenderer(new IconRenderer());

	@Override
	public void render(Section<?> section, UserContext user, StringBuilder out) {
		out.append(Strings.maskHTML("<div>"));
		out.append(Strings.maskHTML("<div class='tablePopupParent' id='"));
		out.append(section.getID());
		out.append(Strings.maskHTML("'>"));
		out.append("\n");
		out.append(section.getText());
		out.append(Strings.maskHTML("<div class='tablePopupIcon'>"));
		popupRenderer.render(section, user, out);
		out.append(Strings.maskHTML("</div>"));
		out.append(Strings.maskHTML("</div>"));
		out.append(Strings.maskHTML("</div>"));
	}

}