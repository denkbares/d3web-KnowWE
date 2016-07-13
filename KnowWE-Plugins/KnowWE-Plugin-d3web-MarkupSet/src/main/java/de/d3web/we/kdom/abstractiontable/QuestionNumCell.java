package de.d3web.we.kdom.abstractiontable;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;

public class QuestionNumCell extends AbstractType {


	public QuestionNumCell() {
		StyleRenderer renderer = new StyleRenderer("color:rgb(125, 80, 102)") {

			@Override
			protected void renderContent(Section<?> section, UserContext user, RenderResult string) {
				string.appendJSPWikiMarkup(Strings.encodeHtml(section.getText().replace("~", "")));
			}

		};
		this.setRenderer(renderer);
	}



}
