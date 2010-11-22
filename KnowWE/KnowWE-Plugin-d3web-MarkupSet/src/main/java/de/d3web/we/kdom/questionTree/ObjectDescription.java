package de.d3web.we.kdom.questionTree;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.MatchUntilEndFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.SplitUtility;
import de.knowwe.core.renderer.FontColorRenderer;

/**
 * A type to allow for the definition of (extended) question-text for a question
 * leaded by '~'
 * 
 * the subtreehandler creates the corresponding DCMarkup using
 * MMInfoSubject.PROMPT for the question object
 * 
 * @author Jochen
 * 
 */
public class ObjectDescription extends DefaultAbstractKnowWEObjectType {

	private static final String QTEXT_START_SYMBOL = "~";

	public ObjectDescription(final Property<?> prop) {
		this.sectionFinder = new MatchUntilEndFinder(new StringSectionFinderUnquoted(
					QTEXT_START_SYMBOL));

		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR8));
		this.addSubtreeHandler(new SubtreeHandler<ObjectDescription>() {

			@Override
			public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ObjectDescription> sec) {

				Section<TermDefinition> qDef = sec.getFather().findSuccessor(
							TermDefinition.class);

				if (qDef != null) {

					// get the object the information should be stored for
					Object ob = qDef.get().getTermObject(article, qDef);
					NamedObject object = null;
					if (ob instanceof NamedObject) {
						object = (NamedObject) ob;
					}

					if (object != null) {
						// if its MMINFO then it a question, so set text as
						// prompt
						String objectDescriptionText = ObjectDescription.getObjectDescriptionText(sec);
						if (prop.equals(MMInfo.PROMPT)) {
							object.getInfoStore().addValue(MMInfo.PROMPT, objectDescriptionText);
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									D3webModule.getKwikiBundle_d3web()
											.getString(
													"KnowWE.questiontree.questiontextcreated")
											+ " " + objectDescriptionText));
						} // for any other properties (than MMINFO) set
							// information normally
						else {
							object.getInfoStore().addValue(
									prop,
									objectDescriptionText);
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									"Explanation set: " + objectDescriptionText));
						}
					}
				}
				return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
							D3webModule.getKwikiBundle_d3web()
									.getString("KnowWE.questiontree.questiontext"),
							this.getClass()));
			}
		});
	}

	public static String getObjectDescriptionText(Section<ObjectDescription> s) {
		String text = s.getOriginalText();
		if (text.startsWith(QTEXT_START_SYMBOL)) {
			text = text.substring(1).trim();
		}

		return SplitUtility.unquote(text);
	}
}
