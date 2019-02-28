package de.d3web.we.solutionpanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.we.object.ValueTooltipRenderer;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.tools.CompositeEditToolProvider;
import de.knowwe.util.Color;
import de.knowwe.util.Icon;

public class SolutionPanelUtils {

	public static boolean isShownObject(String[] allowedParents, String[] excludedParents, TerminologyObject to) {
		return (allowedParents.length == 0 || isOrHasParent(allowedParents, to))
				&& (excludedParents.length == 0 || !isOrHasParent(excludedParents, to));
	}

	public static boolean isOrHasParent(String[] allowedParents, TerminologyObject object) {
		if (arrayIgnoreCaseContains(allowedParents, object.getName())) return true;
		for (TerminologyObject parent : object.getParents()) {
			if (arrayIgnoreCaseContains(allowedParents, parent.getName())) return true;
			if (isOrHasParent(allowedParents, parent)) return true;
		}
		return false;
	}

	private static boolean arrayIgnoreCaseContains(String[] allowedParents, String name) {
		for (String string : allowedParents) {
			if (name.equalsIgnoreCase(string)) {
				return true;
			}
		}
		return false;
	}

	public static void renderSolution(Solution solution, Session session, boolean endUser, Locale lang, RenderResult content) {

		// fetch derivation state icon
		Rating solutionRating = D3webUtils.getRatingNonBlocking(session, solution);
		appendImage(solutionRating, content);

		String[] link = MMInfo.getLinks(solution, lang);
		String prompt = MMInfo.getPrompt(solution, lang);
		String description = MMInfo.getDescription(solution, lang);

		StringBuilder tooltip = new StringBuilder();
		if (description != null) {
			tooltip.append(description);
		}

		if (solutionRating != null) {
			if (tooltip.length() > 0) {
				tooltip.append("<p/>");
			}
			ValueTooltipRenderer.appendCurrentValue(solution, solutionRating, tooltip);
			ValueTooltipRenderer.appendSourceFactsExplanation(solution, session, tooltip);
		}

		content.appendHtmlTag("span", "title", tooltip.toString(), "class", "SOLUTION-" + solutionRating + " tooltipster");
		if (endUser) {
			// show solution in end user mode
			if (link.length > 0) {
				content.appendHtmlElement("a", prompt, "href", link[0]);
			}
			else {
				content.append(prompt);
			}
		}
		else {
			content.appendHtmlTag("a", false, "onclick",
					CompositeEditToolProvider.createCompositeEditModeAction(new Identifier(solution.getName())));
			content.append(prompt);
			content.appendHtmlTag("/a");
		}
		content.appendHtmlTag("/span");
		content.appendHtml("<br/>");
	}

	public static void appendImage(Rating solutionRating, RenderResult content) {
		if (solutionRating == null) {
			appendImage(Icon.CALCULATING,
					"value in calculation, please reload later", content);
		}
		else if (solutionRating.hasState(State.ESTABLISHED)) {
			appendImage(Icon.ESTABLISHED.addColor(Color.OK), "Established", content);
		}
		else if (solutionRating.hasState(State.SUGGESTED)) {
			appendImage(Icon.SUGGESTED.addColor(Color.YELLOW), "Suggested", content);
		}
		else if (solutionRating.hasState(State.EXCLUDED)) {
			appendImage(Icon.EXCLUDED.addColor(Color.RED), "Excluded", content);
		}
	}

	private static void appendImage(Icon icon, String title, RenderResult content) {

		content.appendHtml(icon.addTitle(title).addId("sstate-update").toHtml() + " ");
	}

	public static void renderAbstraction(Question question, Session session, Locale lang, RenderResult result) {
		appendImage(Icon.ABSTRACT, "Abstraction", result);
		Value value = D3webUtils.getValueNonBlocking(session, question);

		String description = question.getInfoStore().getValue(MMInfo.DESCRIPTION);
		StringBuilder tooltip = new StringBuilder();
		if (description != null) {
			tooltip.append(description);
		}
		if (value != null) {
			if (tooltip.length() > 0) {
				tooltip.append("<p/>");
			}
			ValueTooltipRenderer.appendSourceFactsExplanation(question, session, tooltip);
		}

		List<String> attributes = new ArrayList<>();
		attributes.add("class");
		attributes.add("ABSTRACTION tooltipster");
		if (tooltip.length() > 0) {
			attributes.add("title");
			attributes.add(tooltip.toString());
		}

		result.appendHtmlTag("span", attributes.toArray(new String[0]));
		// render the abstraction question with value
		if (value == null) {
			result.appendHtml("<i style='color:grey'>value in calculation, please reload later</i>");
		}
		else {
			result.append(question.getName() + " = " + ValueUtils.getVerbalization(question, value, lang));
		}

		// add the unit name for num question, if available
		String unit = question.getInfoStore().getValue(MMInfo.UNIT);
		if (unit != null) {
			result.append(" " + unit);
		}

		result.appendHtml("</span>" + "\n");
	}
}
