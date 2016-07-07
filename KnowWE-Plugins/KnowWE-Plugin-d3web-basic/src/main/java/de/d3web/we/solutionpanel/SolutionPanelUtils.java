package de.d3web.we.solutionpanel;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.strings.Identifier;
import de.d3web.we.object.ValueTooltipRenderer;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.tools.CompositeEditToolProvider;
import de.knowwe.util.Icon;
import de.knowwe.util.IconColor;

public class SolutionPanelUtils {

	public static boolean isShownObject(String[] allowedParents, String[] excludedParents, TerminologyObject to) {
		return (allowedParents.length == 0 || isOrHasParent(allowedParents, to))
				&& (excludedParents.length == 0 || !isOrHasParent(excludedParents, to));
	}

	public static boolean isOrHasParent(String[] allowedParents, TerminologyObject object) {

		if (object.getParents() == null) return false;
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

	public static void renderSolution(Solution solution, Session session, boolean endUser, RenderResult content) {

		// fetch derivation state icon
		Rating solutionRating = D3webUtils.getRatingNonBlocking(session, solution);
		appendImage(solutionRating, content);

		String link = solution.getInfoStore().getValue(MMInfo.LINK);
		String prompt = solution.getInfoStore().getValue(MMInfo.PROMPT);
		String description = solution.getInfoStore().getValue(MMInfo.DESCRIPTION);

		String label;
		if (prompt != null) {
			label = prompt;
		}
		else if (description != null) {
			label = description;
		}
		else {
			label = solution.getName();
		}

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

		content.appendHtmlTag("span", "title", tooltip.toString(), "class", "SOLUTION-" + String.valueOf(solutionRating) + " tooltipster");
		if (endUser) {
			// show solution in end user mode
			if (link != null) {
				content.appendHtmlElement("a", label, "href", link);
			}
			else {
				content.append(label);
			}
		}
		else {
			content.appendHtmlElement("a", label, "onclick",
					CompositeEditToolProvider.createCompositeEditModeAction(new Identifier(solution.getName())));
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
			appendImage(Icon.ESTABLISHED.addColor(IconColor.OK), "Established", content);
		}
		else if (solutionRating.hasState(State.SUGGESTED)) {
			appendImage(Icon.SUGGESTED.addColor(IconColor.YELLOW), "Suggested", content);
		}
		else if (solutionRating.hasState(State.EXCLUDED)) {
			appendImage(Icon.EXCLUDED.addColor(IconColor.RED), "Excluded", content);
		}
	}

	private static void appendImage(Icon icon, String title, RenderResult content) {

		content.appendHtml(icon.addTitle(title).addId("sstate-update").toHtml() + " ");
	}

	public static void renderAbstraction(Question question, Session session, int digits, RenderResult result) {
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
			result.append(question.getName()
					+ " = "
					+ ValueTooltipRenderer.formatValue(question, value, digits));
		}

		// add the unit name for num question, if available
		String unit = question.getInfoStore().getValue(MMInfo.UNIT);
		if (unit != null) {
			result.append(" " + unit);
		}

		result.appendHtml("</span>" + "\n");
	}

}
