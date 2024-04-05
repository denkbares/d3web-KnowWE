package de.d3web.we.kdom.questionTree;

import java.util.Arrays;
import java.util.Collections;

import com.denkbares.strings.Strings;
import de.d3web.we.kdom.questionTree.QuestionLine.QuestionTypeChecker;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.object.QuestionDefinition.QuestionType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.StringEnumChecker;

/**
 * A Type for the question-type declaration keys "[oc],[mc],[num],..."
 * 
 * @author Jochen
 */
public class QuestionTypeDeclaration extends
		AbstractType {

	public static QuestionType getQuestionType(Section<QuestionTypeDeclaration> typeSection) {

		if (typeSection == null) return null;
		String embracedContent = typeSection.getText();
		if (embracedContent.startsWith("[")) {
			embracedContent = embracedContent.substring(1);
		}
		if (embracedContent.endsWith("]")) {
			embracedContent = embracedContent.substring(0,
					embracedContent.length() - 1);
		}
		String questionTypeDeclaration = embracedContent.trim();

		if (questionTypeDeclaration.equalsIgnoreCase("oc")) {
			return QuestionType.OC;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("mc")) {
			return QuestionType.MC;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("num")) {
			return QuestionType.NUM;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("jn")
				|| questionTypeDeclaration.equalsIgnoreCase("yn")) {
			return QuestionType.YN;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("date")) {
			return QuestionType.DATE;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("info")) {
			return QuestionType.INFO;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("text")) {
			return QuestionType.TEXT;
		}
		else {
			return null;
		}

	}

	public static final String[] QUESTION_DECLARATIONS = {
			"oc", "mc",
			"yn", "jn", "num", "date", "text", "info" };

	public QuestionTypeDeclaration() {
		SectionFinder typeFinder = (text, father, type) -> {

			int start = Strings.indexOfUnquoted(text, "[");
			int end = Strings.indexOfUnquoted(text, "]");
			if (start == -1 || end == -1) {
				return Collections.emptyList();
			}
			return SectionFinderResult
					.singleItemList(new SectionFinderResult(
							start,
							end + 1));
		};
		this.setSectionFinder(typeFinder);
		this.setRenderer(StyleRenderer.OPERATOR.withMaskMode(StyleRenderer.MaskMode.htmlEntities));
		String allowedTypes = Arrays.asList(QUESTION_DECLARATIONS).toString();
		allowedTypes = allowedTypes.substring(1, allowedTypes.length() - 1);
		Message errorMsg = Messages.error(D3webUtils.getD3webBundle()
				.getString("KnowWE.questiontree.allowingonly")
				+ allowedTypes);
		this.addCompileScript(new StringEnumChecker<D3webCompiler, QuestionTypeDeclaration>(
				D3webCompiler.class, QUESTION_DECLARATIONS, errorMsg, 1, 1));
		this.addCompileScript(new QuestionTypeChecker());
	}

	public Section<QuestionDefinition> getQuestionDefinition(Section<QuestionTypeDeclaration> typeDeclaration) {
		return Sections.successor(typeDeclaration.getParent(),
				QuestionDefinition.class);
	}

}