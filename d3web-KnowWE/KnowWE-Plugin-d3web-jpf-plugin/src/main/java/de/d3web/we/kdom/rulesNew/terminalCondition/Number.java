package de.d3web.we.kdom.rulesNew.terminalCondition;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class Number extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.addSubtreeHandler(new NumberChecker());
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR7));
	}

	public static Double getNumber(Section<Number> s) {
		try {
			return Double.parseDouble(s.getOriginalText().trim());
		}
		catch (Exception e) {

		}

		return null;
	}

	class NumberChecker extends SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
			try {
				Double.parseDouble(s.getOriginalText().trim());
			}
			catch (Exception e) {
				msgs.add(new InvalidNumberError(s.getOriginalText().trim()));
			}
			return msgs;
		}

	}

}
