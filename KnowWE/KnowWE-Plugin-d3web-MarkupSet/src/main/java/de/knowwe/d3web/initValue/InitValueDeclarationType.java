package de.knowwe.d3web.initValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import de.d3web.core.inference.PSMethodInit;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.constraint.AtMostOneFindingConstraint;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.report.SimpleMessageNotice;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.MessageUtils;
import de.d3web.we.utils.Patterns;

public class InitValueDeclarationType extends AbstractType {

	public static final String QUOTED_NAME = Patterns.quoted;
	public static final String UNQUOTED_NAME = "[^\".=#\\n\\r]+";
	public static final String NAME = "\\s*(" + QUOTED_NAME + "|" + UNQUOTED_NAME + ")\\s*";

	public InitValueDeclarationType() {
		this.setSectionFinder(new RegexSectionFinder(Pattern.compile("^.+?$", Pattern.MULTILINE)));
		QuestionReference questionReference = new QuestionReference();
		questionReference.setSectionFinder(new ConstraintSectionFinder(
				new RegexSectionFinder(Pattern.compile(NAME), 1),
				AtMostOneFindingConstraint.getInstance()));
		this.addChildType(questionReference);
		this.addChildType(new EqualsTextType());
		this.addChildType(new ValueType());
		this.addSubtreeHandler(new InitValueHandler());
	}

	private static class InitValueHandler extends D3webSubtreeHandler<InitValueDeclarationType> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<InitValueDeclarationType> section) {

			Section<EqualsTextType> equalsTextSection = Sections.findChildOfType(section,
					EqualsTextType.class);
			if (equalsTextSection == null) {
				return MessageUtils.syntaxErrorAsList("The following construct is expected: 'Question = InitValue'");
			}

			Section<QuestionReference> questionRefSection = Sections.findChildOfType(section,
					QuestionReference.class);
			if (questionRefSection == null) {
				return MessageUtils.syntaxErrorAsList("No Question found.");
			}
			Question question = questionRefSection.get().getTermObject(article, questionRefSection);
			if (question == null) {
				// error message will already be added by the QuestionReference
				return new ArrayList<KDOMReportMessage>(0);
			}

			Section<ValueType> valueSection = Sections.findChildOfType(section, ValueType.class);
			if (valueSection == null) {
				return MessageUtils.syntaxErrorAsList("No value found.");
			}
			String valueString = KnowWEUtils.trimQuotes(valueSection.getText());
			try {
				PSMethodInit.getValue(question, valueString);
			}
			catch (Exception e) {
				return MessageUtils.asList(new SimpleMessageError(e.getMessage()));
			}

			question.getInfoStore().addValue(BasicProperties.INIT, valueString);
			return MessageUtils.asList(new SimpleMessageNotice("Init value '" + valueString
					+ "' successfully set."));
		}
	}

	private static class EqualsTextType extends AbstractType {

		public EqualsTextType() {
			this.setSectionFinder(new RegexSectionFinder("\\s*=\\s*"));
		}
	}

	private static class ValueType extends AbstractType {

		public ValueType() {
			this.setSectionFinder(new AllTextFinderTrimmed());
			this.setCustomRenderer(StyleRenderer.CHOICE);
		}
	}

}