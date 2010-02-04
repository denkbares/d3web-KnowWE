package de.d3web.we.questionTreeNew;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.decisionTree.QuestionDef;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.questionTreeNew.QuestionLine.TypeDeclarationRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SetValueLine extends DefaultAbstractKnowWEObjectType{
	
	private static final String SETVALUE_ARGUMENT = "SetValueArgument";

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {
			
			@Override
			protected boolean condition(String text, Section father) {
				return SplitUtility.containsUnquoted(text, "(" ) && SplitUtility.containsUnquoted(text, ")" );
				
			}
		};
		
		
		
		AnonymousType argumentType = createArgumentType();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));
	}
	
	private KnowWEObjectType createObjectRefTypeBefore(KnowWEObjectType typeAfter) {
		ObjectRef qid = new ObjectRef();
		qid.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR1));
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		return qid;
	}

	private AnonymousType createArgumentType() {
		AnonymousType typeDef = new AnonymousType(SETVALUE_ARGUMENT);
		SectionFinder typeFinder = new SectionFinder() {
			
			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father) {
				
				return SectionFinderResult.createSingleItemList(new SectionFinderResult(SplitUtility.indexOfUnquoted(text, "("), SplitUtility.indexOfUnquoted(text, ")")+1));
			}
		};
		typeDef.setSectionFinder(typeFinder);
		typeDef.setCustomRenderer(new ArgumentRenderer());
		return typeDef;
	}

	class ArgumentRenderer extends KnowWEDomRenderer{

		@Override
		public void render(KnowWEArticle article, Section sec,
				KnowWEUserContext user, StringBuilder string) {
			String embracedContent = sec.getOriginalText().substring(1, sec.getOriginalText().length()-1);
			string.append(KnowWEUtils.maskHTML(" <img height='10' src='KnowWEExtension/images/arrow_right_s.png'>"));
			string.append(KnowWEUtils.maskHTML("<b>"+embracedContent+"</b>"));
			
		}
		
	}
}
