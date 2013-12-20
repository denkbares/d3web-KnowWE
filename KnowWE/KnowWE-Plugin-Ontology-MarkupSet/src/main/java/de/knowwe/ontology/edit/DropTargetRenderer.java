package de.knowwe.ontology.edit;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.SurroundingRenderer;


public class DropTargetRenderer implements SurroundingRenderer {

	@Override
	public void renderPre(Section<?> section, UserContext user, RenderResult string) {
		if (section.getText().trim().length() > 3) {
			string.appendHtml("<div style='display:inline;' dragdropid='" + section.getID()
					+ "' class='dropTargetMarkup'>");

		}
	}

	@Override
	public void renderPost(Section<?> section, UserContext user, RenderResult string) {
		if (section.getText().trim().length() > 3) {
			string.appendHtml("</div>");
		}
	}

}
