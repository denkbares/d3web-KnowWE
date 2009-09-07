package de.d3web.we.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.we.codeCompletion.AbstractCompletionFinder;
import de.d3web.we.codeCompletion.CompletionFinding;
import de.d3web.we.codeCompletion.DefaultCompletionFinder;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;

public class CodeCompletionRenderer implements KnowWEAction {

	AbstractCompletionFinder defaultFinder = new DefaultCompletionFinder();

	private static List<String> additionalKeyWords = new ArrayList<String>();

	public CodeCompletionRenderer() {
		initKeyWords();
	}

	private void initKeyWords() {
		additionalKeyWords.add("<Kopic>");
		additionalKeyWords.add("</Kopic>");
		additionalKeyWords.add("Kopic");
		additionalKeyWords.add("Questionnaires-section");
		additionalKeyWords.add("<Questionnaires-section>");
		additionalKeyWords.add("</Questionnaires-section>");
		additionalKeyWords.add("Questions-section");
		additionalKeyWords.add("<Questions-section>");
		additionalKeyWords.add("</Questions-section>");
		additionalKeyWords.add("SetCoveringList-section");
		additionalKeyWords.add("<SetCoveringList-section>");
		additionalKeyWords.add("</SetCoveringList-section>");
		additionalKeyWords.add("Rules-section");
		additionalKeyWords.add("<Rules-section>");
		additionalKeyWords.add("</Rules-section>");
	}

	// public void render(Model model) {
	// String data = (String) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.COMPLETION_TEXT, String.class, true);
	//
	//		
	// java.io.PrintWriter out = getHtmlPrintWriter(model);
	// out.print(result);
	// }

	public String perform(KnowWEParameterMap parameterMap) {

		CompletionFinding[] options = calcOptions(parameterMap);
		String result = formatOptions(options);
		return result;
	}

	private String formatOptions(CompletionFinding[] options) {
		if (options.length == 0) {
			return "no completion found";
		}

		String res = "<select id=\"codeCompletion\">";
		res += "<option value=\"\"> </option>";
		for (int i = 0; i < options.length; i++) 
		{
			String selected = ( i == 0 ) ? "selected=\"selected\"" : "";
			
			res += "<option value=\"" + encodeHTMLEntities(options[i].getCompletion()) + "\" " + selected + ">" 
			    + encodeHTMLEntities(options[i].getTermName()) + "</option>";
		}
		return res + "</select>";
	}
	
	private String encodeHTMLEntities(String s) 
	{
		StringBuffer buf = new StringBuffer();
		int len = (s == null ? -1 : s.length());

		for (int i = 0; i < len; i++) 
		{
			char c = s.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') 
			{
				buf.append(c);
			} else 
			{
				buf.append("&#" + (int) c + ";");
			}
		}
		return buf.toString();
	}

	private CompletionFinding[] calcOptions(Map<String, String> parameterMap) {
		Set<CompletionFinding> set = new HashSet<CompletionFinding>();
		String data = parameterMap.get(KnowWEAttributes.COMPLETION_TEXT);
		DPSEnvironment dpse = D3webModule.getDPSE(parameterMap);

		for (GlobalTerminology eachGT : dpse.getTerminologyServer()
				.getGlobalTerminologies()) {
			for (Term eachTerm : eachGT.getAllTerms()) {
				String name = (String) eachTerm.getInfo(TermInfoType.TERM_NAME);
				CompletionFinding finding = checkString(name, data);
				if (finding != null) {
					set.add(finding);
				}
				Object answer = eachTerm.getInfo(TermInfoType.TERM_VALUE);
				if (answer instanceof String) {
					name = (String) answer;
					finding = checkString(name, data);
					if (finding != null) {
						set.add(finding);
					}
				}

			}
		}

		for (String name : additionalKeyWords) {
			CompletionFinding finding = checkString(name, data);
			if (finding != null) {
				set.add(finding);
			}
		}

		List<CompletionFinding> result = new ArrayList<CompletionFinding>(set);

		Collections.sort(result);
		return result.toArray(new CompletionFinding[] {});
	}

	private CompletionFinding checkString(String name, String data) {
		CompletionFinding finding = runFinder(defaultFinder, name, data);

		return finding;
	}

	private CompletionFinding runFinder(AbstractCompletionFinder finder,
			String name, String data) {

		int startIndex = 0;
		CompletionFinding finding = null;
		while (finding == null) {
			String tmp = data.substring(startIndex).trim();
			finding = finder.find(name, tmp);
			startIndex++;
			if (startIndex >= data.length() - 1) {
				break;
			}
		}
		return finding;
	}

}
