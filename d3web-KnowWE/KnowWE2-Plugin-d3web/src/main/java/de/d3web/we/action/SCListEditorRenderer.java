package de.d3web.we.action;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class SCListEditorRenderer implements KnowWEAction {

	// private FindingXMLWriter questionWriter;

	public String perform(KnowWEParameterMap map) {
		// String data = map.get(KnowWEAttributes.SCL_DATA);

		StringBuffer htmlBuffi = new StringBuffer();
		// htmlBuffi.append("<html><head></head><body>");
		renderQuestions(htmlBuffi, map);
		// htmlBuffi.append("</body></html>");

		return htmlBuffi.toString();
	}

	private void renderQuestions(StringBuffer htmlBuffi, KnowWEParameterMap map) {
		htmlBuffi.append(getQuestionStrings(map));
	}

	// private Term getTerm(DPSEnvironment dpse, String termName) {
	// Term result = null;
	// for (GlobalTerminology each : dpse.getTerminologyServer().getBroker()
	// .getGlobalTerminologies()) {
	// result = each.getTerm(termName, null);
	// if (result != null) {
	// return result;
	// }
	// }
	// return result;
	// }

	private StringBuffer getQuestionStrings(KnowWEParameterMap map) {

		StringBuffer sb = new StringBuffer();

		DPSEnvironment dpse = D3webModule.getDPSE(map);
		String namespace = map.get(KnowWEAttributes.SEMANO_NAMESPACE);
//		String type = map.get(KnowWEAttributes.SEMANO_TERM_TYPE);
//		String user = map.get(KnowWEAttributes.USER);

		KnowledgeBase base = ((D3webKnowledgeService) (dpse
				.getService(namespace))).getBase();
		if (namespace == null) {
			return null;
		}
		sb.append("<table>");
		QASet qaset = base.getRootQASet();
		int cnt = 0;
		int level = 0;
		renderQASet(qaset, sb, cnt, level);

		sb.append("<div onclick=\"insertSCLData();\">Einfï¿½gen</div>");

		sb.append("</table>");
		return sb;
	}

	private void renderQASet(QASet qaset, StringBuffer sb, int cnt, int level) {
		level++;
		String tab = "";
		for (int i = 0; i < level; i++) {
			tab += "&nbsp;&nbsp;";
		}
		if (qaset instanceof QASet && (!(qaset instanceof Question))) {

			sb.append(qaset.getText() + "<br>");
			for (Object q : qaset.getChildren()) {
				renderQASet((QASet) q, sb, ++cnt, level);

			}
		}

		if (qaset instanceof QuestionChoice) {
			sb
					.append("<span class=\"semLink\">"
							+ tab
							+ "<a onmouseout=\"return nd();\" onmouseover=\"return overlib('&lt;div class=&quot;semContents&quot; &gt;&lt;form action=&quot; javascript:void(); &quot; name=&quot;semanomcQ72&quot; id=&quot;semanomcQ72&quot; &gt;&lt;div align=left &gt;");
			for (AnswerChoice a : ((QuestionChoice) qaset).getAllAlternatives()) {
				sb.append("&lt;input type=\\'checkbox\\' name=\\'f0idmc"
						+ qaset.getId() + "\\' value=\\'" + a.getId()
						+ " \\' &gt;" + replaceHTML(a.getText())
						+ "&lt;br /&gt;");
			}

			sb
					.append("&lt;input type=\\'button\\' name=\\'submit\\' value=\\'ok\\' onclick=\\'javascript:readformSCLEdit(&quot;KnowWE.jsp?KWikiWeb=Sportberatung&action=setFinding&namespace=Schwimmen%2E%2ESchwimmen%20%28im%20Verein%29&ObjectID=Q1&quot;,&quot;mc"
							+ qaset.getId()
							+ "&quot;,&quot;0&quot;)\\' &gt;&lt;/div&gt;&lt;/form &gt;&lt;/div &gt;',STICKY,MOUSEOFF,NOCLOSE,TEXTCOLOR, '#111111',FGCOLOR, '#fbf7e8',BGCOLOR, '#AA3311',CAPTION,'Medium',CAPCOLOR,'#FFFFFF');\" href=\"javascript:void(0);\">"
							+ replaceHTML(qaset.getText())
							+ "</a><div id=\"scledit"
							+ qaset.getId()
							+ "\"></div>");
			sb.append("</span><br>");
		}
		level--;
	}

	private String replaceHTML(String text) {
		text = text.replaceAll("\"", "&quot;");
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");
		return text;
	}
//	FL: commented out, cause it was never used
//	private IdentifiableInstance getII(DPSEnvironment dpse, String namespace,
//			Term term) {
//		IdentifiableInstance ii = null;
//		List<IdentifiableInstance> iis = dpse.getTerminologyServer()
//				.getBroker().getAlignedIdentifiableInstances(term,
//						TerminologyAlignmentLinkFilter.getInstance());
//		if (iis != null && !iis.isEmpty()) {
//			for (IdentifiableInstance instance : iis) {
//				if (instance.getNamespace().equals(namespace)) {
//					ii = instance;
//				}
//			}
//		}
//		return ii;
//	}

}
