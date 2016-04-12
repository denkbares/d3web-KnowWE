package de.d3web.we.kdom.questionTree;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.strings.Strings;
import de.d3web.we.object.D3webTermDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.MatchUntilEndFinder;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * A type to allow for the definition of (extended) question-text for a question
 * leaded by '~'
 * <p/>
 * the subtreehandler creates the corresponding DCMarkup using
 * MMInfoSubject.PROMPT for the question object
 *
 * @author Jochen
 */
public class ObjectDescription extends AbstractType {

	private static final String QTEXT_START_SYMBOL = "~";

	public ObjectDescription(final Property<?> prop) {
		this.setSectionFinder(new MatchUntilEndFinder(new StringSectionFinderUnquoted(
				QTEXT_START_SYMBOL)));

		this.setRenderer((section, user, result) -> StyleRenderer.PROMPT.renderText(QTEXT_START_SYMBOL + section.getText(), user, result));
		this.addCompileScript((D3webHandler<ObjectDescription>) (compiler, section) -> {

			@SuppressWarnings("rawtypes")
			Section<D3webTermDefinition> qDef = Sections.successor(
					section.getParent(), D3webTermDefinition.class);

			if (qDef != null) {

				// get the object the information should be stored for
				@SuppressWarnings("unchecked")
				Object ob = qDef.get().getTermObject(compiler, qDef);
				TerminologyObject object = null;
				if (ob instanceof TerminologyObject) {
					object = (TerminologyObject) ob;
				}

				if (object != null) {
					// if its MMINFO then it a question, so set text as
					// prompt
					String objectDescriptionText = ObjectDescription.getObjectDescriptionText(section);
					if (prop.equals(MMInfo.PROMPT)) {
						object.getInfoStore().addValue(MMInfo.PROMPT, objectDescriptionText);
						return Messages.noMessage();
					} // for any other properties (than MMINFO) set
					// information normally
					else {
						object.getInfoStore().addValue(prop, objectDescriptionText);
						return Messages.noMessage();
					}
				}
			}
			return Messages.asList(Messages.objectCreationError(
					D3webUtils.getD3webBundle()
							.getString("KnowWE.questiontree.questiontext")));
		});
	}

	public static String getObjectDescriptionText(Section<ObjectDescription> s) {
		String text = s.getText();
		if (text.startsWith(QTEXT_START_SYMBOL)) {
			text = text.substring(1).trim();
		}

		return Strings.unquote(text);
	}
}
