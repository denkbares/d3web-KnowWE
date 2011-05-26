package de.d3web.we.kdom.questionTree.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.NothingRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.rules.RuleContentType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.sectionFinder.UnquotedExpressionFinder;
import de.d3web.we.kdom.type.AnonymousType;
import de.d3web.we.object.QASetDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;

/**
 * This type allows to define within the definition of a question, when this
 * question will be asked This is an alternative method to dashTree-indents
 * 
 * @author Jochen
 * @created 23.12.2010
 */
public class InlineContraIndicationCondition extends AbstractType {

	private static final String START_KEY = "&\\s*?(Nur falls|Only if):?";
	private static final String END_KEY = "&";

	public InlineContraIndicationCondition() {
		this.addSubtreeHandler(new CreateIndicationRulesHandler());
		this.setSectionFinder(new InlineIndiFinder());

		// TODO find better way to crop open and closing signs
		AnonymousType open = new AnonymousType(START_KEY);
		open.setSectionFinder(new RegexSectionFinder(
				START_KEY, Pattern.CASE_INSENSITIVE));
		open.setCustomRenderer(new KnowWEDomRenderer<Type>() {

			@Override
			public void render(KnowWEArticle article, Section<Type> sec, UserContext user, StringBuilder string) {
				string.append(KnowWEUtils.maskHTML("<b>"));
				string.append(sec.getOriginalText().substring(1).trim());
				string.append(KnowWEUtils.maskHTML("</b>"));
			}
		});
		this.addChildType(open);

		AnonymousType close = new AnonymousType(END_KEY);
		close.setSectionFinder(new UnquotedExpressionFinder(
				END_KEY));
		close.setCustomRenderer(NothingRenderer.getInstance());
		this.addChildType(close);

		this.addChildType(new Finding());

	}

	private class InlineIndiFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			Pattern pattern = Pattern.compile(START_KEY, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				int start = matcher.start();
				int end = SplitUtility.lastIndexOfUnquoted(text, END_KEY);
				return SectionFinderResult.createSingleItemList(new SectionFinderResult(start,
						end + 1));
			}
			return null;
		}

	}

	static class CreateIndicationRulesHandler extends D3webSubtreeHandler<InlineContraIndicationCondition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<InlineContraIndicationCondition> s) {
			Section<Finding> finding = Sections.findSuccessor(s, Finding.class);

			@SuppressWarnings("rawtypes")
			Section<QASetDefinition> qDef = Sections.findSuccessor(s.getFather(),
					QASetDefinition.class);
			Collection<KDOMReportMessage> msgs = new HashSet<KDOMReportMessage>();
			if (finding != null && qDef != null) {

				@SuppressWarnings("unchecked")
				QASet qaset = (QASet) qDef.get().getTermObject(article, qDef);

				// create an contraIndication-rule that always fires at
				// beginning
				List<QASet> obsContra = new ArrayList<QASet>();
				obsContra.add(qaset);
				Condition condition = finding.get().getCondition(article, finding);
				if (condition == null) {
					msgs.add(new SimpleMessageError("Could not create condition"));
				}
				else {
					msgs = createRule(article, s, obsContra, new CondNot(
							condition));
				}
			}

			return msgs;
		}

		private Collection<KDOMReportMessage> createRule(KnowWEArticle article, Section<InlineContraIndicationCondition> s, List<QASet> d3action, Condition d3Cond) {
			if (s.hasErrorInSubtree(article)) {
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed("Rule"));
			}

			if (d3action != null && d3Cond != null) {
				Rule r = RuleFactory.createContraIndicationRule(d3action, d3Cond);
				if (r != null) {
					KnowWEUtils.storeObject(article, s, RuleContentType.ruleStoreKey, r);
					return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage("Rule"));
				}

			}

			// should not happen
			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
					D3webModule.getKwikiBundle_d3web().
							getString("KnowWE.rulesNew.notcreated")
					));
		}

	}
}
