package de.d3web.we.questionTreeNew;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.decisionTree.QuestionDef;
import de.d3web.we.kdom.error.SimpleMessageError;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.sectionFinder.StringEnumChecker;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuestionLine extends DefaultAbstractKnowWEObjectType{

	private static final String QUESTIONTYPE_DECLARATION = "TypeDeclaration";
	
	public static final String [] QUESTION_DECLARATIONS = { "oc", "mc", "jn", "yn", "num", "date", "text" };

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {
			
			@Override
			protected boolean condition(String text, Section father) {
				return SplitUtility.containsUnquoted(text, "[" ) && SplitUtility.containsUnquoted(text, "]" ) && !text.startsWith("[");
			}
		};
		
		//this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
		
		
		
		AnonymousType typeDeclarationType = createTypeDeclarationType();
		this.childrenTypes.add(typeDeclarationType);
		this.childrenTypes.add(createQuestionDefTypeBefore(typeDeclarationType));
	}

	private KnowWEObjectType createQuestionDefTypeBefore(KnowWEObjectType typeAfter) {
		QuestionDef qid = new QuestionDef();
		qid.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		return qid;
	}

	private AnonymousType createTypeDeclarationType() {
		AnonymousType typeDef = new AnonymousType(QUESTIONTYPE_DECLARATION);
		SectionFinder typeFinder = new SectionFinder() {
			
			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father) {
				
				return SectionFinderResult.createSingleItemList(new SectionFinderResult(SplitUtility.indexOfUnquoted(text, "["), SplitUtility.indexOfUnquoted(text, "]")+1));
			}
		};
		typeDef.setSectionFinder(typeFinder);
		typeDef.setCustomRenderer(new TypeDeclarationRenderer());
		typeDef.addReviseSubtreeHandler(new StringEnumChecker(QUESTION_DECLARATIONS, new SimpleMessageError("Invalid Question type - allowing only: "+QUESTIONTYPE_DECLARATION.toString())));
		
		return typeDef;
	}
	
	class TypeDeclarationRenderer extends KnowWEDomRenderer{

		@Override
		public void render(KnowWEArticle article, Section sec,
				KnowWEUserContext user, StringBuilder string) {
			String embracedContent = sec.getOriginalText();
			if(embracedContent.contains("oc")) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/images/questionChoice.gif'>"));
			} else if(embracedContent.contains("mc")) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/images/questionMC.gif'>"));
			} else if(embracedContent.contains("num")) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/images/questionNum.gif'>"));
			} else if(embracedContent.contains("jn") || embracedContent.contains("yn")) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/images/questionYesNo.gif'>"));
			}  else if(embracedContent.contains("date")) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/images/questionDate.gif'>"));
			} else if(embracedContent.contains("text")) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/images/questionText.gif'>"));
			} else {
				string.append(sec.getOriginalText());
			}
			
		}
		
	}

}
