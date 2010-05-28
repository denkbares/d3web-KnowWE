package de.d3web.we.kdom.rulesNew.ruleAction;

import de.d3web.abstraction.ActionAddValue;
import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.values.Choice;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.objects.AnswerRef;
import de.d3web.we.kdom.objects.AnswerRefImpl;
import de.d3web.we.kdom.objects.QuestionRef;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.utils.SplitUtility;

public class AddValueAction extends DefaultAbstractKnowWEObjectType {

	public static final String ADD_VALUE_SIGN = "+=";

	public AddValueAction() {
		this.addChildType(new NumericalAddValueAction());
		this.addChildType(new ChoiceAddValueAction());
		this.setSectionFinder(new ConditionalAllTextFinder() {
			@Override
			protected boolean condition(String text, Section father) {
				return SplitUtility.containsUnquoted(text, ADD_VALUE_SIGN);
			}
		});
	}


	class NumericalAddValueAction extends D3webRuleAction<NumericalAddValueAction> {

		public NumericalAddValueAction() {
			AnonymousType equals = new AnonymousType("plus-equal");
			equals.setSectionFinder(new StringSectionFinderUnquoted(ADD_VALUE_SIGN));

			QuestionRef qr = new QuestionRef();
			qr.setSectionFinder(new AllBeforeTypeSectionFinder(equals));
			this.childrenTypes.add(equals);
			this.childrenTypes.add(qr);

			de.d3web.we.kdom.rulesNew.terminalCondition.Number a = new de.d3web.we.kdom.rulesNew.terminalCondition.Number();
			a.setSectionFinder(new AllTextFinderTrimmed());
			this.childrenTypes.add(a);

			this.setSectionFinder(new ConditionalAllTextFinder() {
				@Override
				protected boolean condition(String text, Section father) {
					int index = SplitUtility.indexOfUnquoted(text, ADD_VALUE_SIGN);

					String value = text.substring(index + 2).trim();
					try {
						Double d = Double.parseDouble(value);
						return true;
					}
					catch (Exception e) {
						return false;
					}
				}
			});
		}

		@Override
		public PSAction getAction(Section<NumericalAddValueAction> s) {
			Section<QuestionRef> qref = s.findSuccessor(QuestionRef.class);
			Question q = qref.get().getObject(qref);
			Section<de.d3web.we.kdom.rulesNew.terminalCondition.Number> aref = s.findSuccessor(de.d3web.we.kdom.rulesNew.terminalCondition.Number.class);

			Double d = aref.get().getNumber(aref);
			if (q != null && d != null) {
				ActionAddValue a = new ActionAddValue();
				a.setQuestion(q);
				a.setValue(d);
				return a;
			}
			return null;
		}

	}

	class ChoiceAddValueAction extends D3webRuleAction<NumericalAddValueAction> {
		public ChoiceAddValueAction() {
			AnonymousType equals = new AnonymousType("plus-equal");
			equals.setSectionFinder(new StringSectionFinderUnquoted(ADD_VALUE_SIGN));

			QuestionRef qr = new QuestionRef();
			qr.setSectionFinder(new AllBeforeTypeSectionFinder(equals));
			this.childrenTypes.add(equals);
			this.childrenTypes.add(qr);

			AnswerRef a = new AnswerRefImpl();
			a.setSectionFinder(new AllTextFinderTrimmed());
			this.childrenTypes.add(a);
			this.sectionFinder = new AllTextFinderTrimmed();
		}

		@Override
		public PSAction getAction(Section<NumericalAddValueAction> s) {
			Section<QuestionRef> qref = s.findSuccessor(QuestionRef.class);
			Question q = qref.get().getObject(qref);
			Section<AnswerRef> aref = s.findSuccessor(AnswerRef.class);
			Choice c = aref.get().getObject(aref);

			if (q != null && c != null) {
				ActionAddValue a = new ActionAddValue();
				a.setQuestion(q);
				a.setValue(c);
				return a;
			}
			return null;
		}
	}
}
