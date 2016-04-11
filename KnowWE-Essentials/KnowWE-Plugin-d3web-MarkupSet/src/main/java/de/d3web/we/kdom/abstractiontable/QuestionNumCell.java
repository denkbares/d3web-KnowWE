package de.d3web.we.kdom.abstractiontable;

import java.util.regex.Pattern;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;

public class QuestionNumCell extends AbstractType {

	private static Pattern INTERVAL_PATTERN = Pattern.compile("(\\[|\\]|\\() *(\\d+\\.?\\d*) +(\\d+\\.?\\d*) *(\\[|\\]|\\))");

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
