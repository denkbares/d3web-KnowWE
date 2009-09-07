package de.d3web.we.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.report.Message;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.webapp.preparser.KWikiReport;

public class KnowWEParseRenderer {

	private static ResourceBundle kwikiBundle = ResourceBundle
			.getBundle("KnowWE_messages");
	private Map<String, String> headers = new HashMap<String, String>();

	private String note = null;
	private String warning = null;
	private String error = null;

	private KnowWEParseRenderer() {
		headers = KnowWEParseRenderer.initializeHeaderNames();
		note = kwikiBundle.getString("KnowWE.note");
		warning = kwikiBundle.getString("KnowWE.warning");
		error = kwikiBundle.getString("KnowWE.error");
	}

	public static KnowWEParseRenderer getInstance() {
		return new KnowWEParseRenderer();
	}

	public String renderReport(KnowWEDomParseReport domRep) {
		
		return "TODO: reimplement ME!!!";
		
//		StringBuffer totalBuffer = new StringBuffer();
//		StringBuffer totalBufferHead = new StringBuffer();
//		if (domRep == null) {
//			return "report is null";
//		}
//		if ((domRep.getKopicParseResult() == null))
//			return "no KopicParseResult in report";
//		if (((KopicParseResult) (domRep.getKopicParseResult())).getKb() == null) {
//			return "no KB";
//		}
//		String kbid1 = ((KopicParseResult) (domRep.getKopicParseResult()))
//				.getKb().getId();
//		if (kbid1 == null) {
//			kbid1 = "kbid_not_found_in_report..anyID";
//		}
//		String topic = ((KopicParseResult) (domRep.getKopicParseResult()))
//				.getTopic();
//		if (topic == null) {
//			topic = kbid1.substring(0, kbid1.indexOf(".."));
//		}
//		totalBufferHead
//				.append("<div style=\"border-style:solid;border-width:1px 0pt 1pt;line-height:1.5em;padding: 20px;background-color:#FBF7E8;border-color:#E2DCC8;color:#666666;\">");
//		totalBufferHead.append("<center><h1>Syntax Check Report " + topic
//				+ "</h1></center>");
//
//		ResourceBundle kwikiBundle = ResourceBundle
//				.getBundle("KnowWE_messages");
//		KnowWEParseResult knowWEParseResult = domRep.getKopicParseResult();
//
//		KopicParseResult result = (KopicParseResult) knowWEParseResult;
//		KnowledgeBaseManagement kbm = result.getKbm();
//
//		// /String kbid = result.getKb().getId();
//		totalBufferHead.append("<span \"><a href=\"#" + kbid1 + "\">" + kbid1
//				+ "</a></span><br>");
//		Map reportMap = result.getReportMap();
//
//		StringBuffer buffi = new StringBuffer();
//		StringBuffer statusBuffi = new StringBuffer();
//		StringBuffer headBuffi = new StringBuffer();
//		headBuffi
//				.append("<div style=\"border-width:1px;padding-left:20px;padding-right:20px;background-color:#FFFFFF;padding-top:20px;border-style:solid\">");
//		headBuffi.append("<a name=\"" + kbid1 + "\">&nbsp;</a><h1>"
//				+ kwikiBundle.getString("report_report_for") + " " + kbid1
//				+ "</h1>");
//
//		// append User who created the report and the date it was created;
//		if (knowWEParseResult.getUser() != null) {
//			Date d = new Date();
//			headBuffi.append("Erstellt von: " + knowWEParseResult.getUser()
//					+ " am " + d.toString());
//		} else {
//			Date d = new Date();
//			headBuffi.append("Erstellt von: unbekannter User" + " am "
//					+ d.toString());
//		}
//
//		Integer errors = 0;
//		Integer warnings = 0;
//		if (reportMap != null) {
//			Iterator iter = reportMap.keySet().iterator();
//
//			for (Iterator iterator = iter; iterator.hasNext();) {
//				String element = (String) iterator.next();
//				String headline = getHeadline(element);
//				buffi
//						.append("<div style=\" color:#a00; font-family:Arial\"><h2>"
//								+ headline
//								+ ": </h2></div><ul style=\"list-style-type:none;\">");
//				Report report1 = (Report) reportMap.get(element);
//				if (report1 instanceof KWikiReport) {
//					addGeneratedItemsMessages((KWikiReport) report1, result
//							.getGeneratedItems(), kbm);
//				}
//				boolean hasErrors = false;
//				buffi.append("<!--Begin id=\"" + kbid1 + "\" " + element
//						+ "-->");
//				for (Iterator iterator2 = report1.getAllMessages().iterator(); iterator2
//						.hasNext();) {
//					Message messi = (Message) iterator2.next();
//					if (messi == null)
//						return "Message ist null!";
//					if (element.equals(KBTextInterpreter.QU_DEC_TREE)) {
//						if (messi.getMessageText().contains(
//								KopicParser.DUMMY_QCLASS)) {
//							messi.setMessageText(kwikiBundle
//									.getString("report.qTree.autogenerated"));
//						}
//					}
//					String color = "";
//					String img = "KnowWEExtension/images/icon_answermc_small.gif\" width=\"16\" height=\"20\" >";
//
//					if (messi.getMessageType().equals(Message.ERROR)) {
//						buffi.append("<!--BeginError-->");
//						hasErrors = true;
//						errors++;
//						color = "red";
//						img = "de/buttons/delete.gif\" width=\"20\" height=\"20\" >";
//					}
//					if (messi.getMessageType().equals(Message.WARNING)) {
//						warnings++;
//						color = "orange";
//						img = "KnowWEExtension/images/lo.gif\" width=\"20\" height=\"20\" >";
//					}
//					String imgPath = "";
//					String icon = "<img style=\"vertical-align:bottom\" src=\""
//							+ imgPath + img;
//					int zeile = messi.getLineNo();
//					buffi
//							.append("<li><div align=\"top\" style=\" font-family:Arial\"><table border=\"0\"><tr><td nowrap style=\"vertical-align:top\">"
//									+ icon
//									+ "<b><span style=\" color:"
//									+ color
//									+ ";vertical-align:top\">"
//									+ " "
//									+ "<u><span onclick=\"errorLink('"
//									+ element
//									+ "',"
//									+ messi.getLineNo()
//									+ ","
//									+ messi.getColumnNo()
//									+ ",'"
//									+ kbid1.substring(kbid1.indexOf("..") + 2)
//									+ "')\" align=\"top\" style=\"cursor:pointer;font-family:Arial\">"
//									+ verbalizeType(messi.getMessageType())
//									+ "</span></u>"
//									+ ": </span></b></td><td style=\"white-space:normal\">"
//									+ messi.getMessageText());
//					if (zeile > 0) {
//						buffi.append(" - "
//								+ kwikiBundle.getString("report_line") + ": "
//								+ zeile);
//					}
//					String line = messi.getLine();
//					if (line != null && line.length() > 0) {
//						buffi.append(" " + kwikiBundle.getString("report_at")
//								+ " \"" + line.substring(0, line.length() - 1)
//								+ "\"");
//					}
//					if (messi.getMessageType().equals(Message.ERROR)) {
//						buffi.append("<!--EndError-->");
//					}
//					buffi.append("</th></td></table></div></li>");
//
//				}
//				if (hasErrors) {
//					buffi.append("<!--" + element + " Error id=\"" + kbid1
//							+ "\"-->");
//				}
//				buffi.append("</ul>");
//				buffi.append("<!--End " + element + "-->");
//			}
//			buffi.append("</div>");
//		} else {
//			buffi = new StringBuffer("Report ist null!");
//		}
//		statusBuffi.append("<div style=\"font-size:70%; font-family:Arial\">");
//		statusBuffi.append("<!--openStatus id=\"" + kbid1 + "\"--> "
//				+ kwikiBundle.getString("report_status") + ": ");
//		if (errors > 0) {
//			statusBuffi.append("<font color=\"red\">"
//					+ kwikiBundle.getString("report_errors") + "</font>");
//		} else {
//			statusBuffi.append("<font color=\"green\">"
//					+ kwikiBundle.getString("report_ok") + "</font>");
//
//		}
//		statusBuffi.append("<!--closeStatus--><br><!--openResult id=\"" + kbid1
//				+ "\"-->" + kwikiBundle.getString("report_result") + ": ");
//		if (errors > 0) {
//			statusBuffi.append("<font color=\"red\">Errors: " + errors
//					+ "</font>");
//		} else {
//			statusBuffi.append("Errors: " + errors);
//		}
//		statusBuffi.append(", Warnings: " + warnings
//				+ "<!--closeResult--></div>");
//
//		totalBuffer.append(headBuffi.toString() + statusBuffi.toString()
//				+ buffi.toString());
//
//		totalBuffer.append("</div>");
//
//		String resultString = KnowWEUtils.convertUmlaut(totalBufferHead
//				.toString()
//				+ "<br>" + totalBuffer);
//		return resultString;
	}

	private String verbalizeType(String messageType) {

		if (messageType.equals(Message.ERROR)) {
			return this.error;
		}
		if (messageType.equals(Message.NOTE)) {
			return this.note;
		}
		if (messageType.equals(Message.WARNING)) {
			return warning;
		}
		return null;
	}

	public static Map<String, String> initializeHeaderNames() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("KWIKI_PARSER", kwikiBundle
				.getString("KWiki.tagParser"));
		headers.put(KBTextInterpreter.COMPL_RULES, kwikiBundle
				.getString("rules"));
		headers.put(KBTextInterpreter.ATTR_TABLE, kwikiBundle
				.getString("attributeTable"));
		headers.put(KBTextInterpreter.DH_HIERARCHY, kwikiBundle
				.getString("diagnosisHierarchy"));
		headers.put(KBTextInterpreter.INDICATION_TABLE, kwikiBundle
				.getString("indicationTable"));
		headers.put(KBTextInterpreter.QCH_HIERARCHY, kwikiBundle
				.getString("questionClassHierarchy"));
		headers.put(KBTextInterpreter.QU_DEC_TREE, kwikiBundle
				.getString("decisionTree"));
		headers.put(KBTextInterpreter.QU_DIA_TABLE, kwikiBundle
				.getString("diagnosticScores"));
		headers.put(KBTextInterpreter.SET_COVERING_TABLE, kwikiBundle
				.getString("setCoveringTable"));
		headers.put(KBTextInterpreter.QU_RULE_DIA_TABLE, kwikiBundle
				.getString("decisionTable"));
		headers.put(KBTextInterpreter.QU_RULE_DIA_TABLE_NEW, kwikiBundle
				.getString("decisionTableNew"));
		headers.put(KBTextInterpreter.REPLACEMENT_TABLE, kwikiBundle
				.getString("replacementTable"));
		headers.put(KBTextInterpreter.CASES_TABLE, kwikiBundle
				.getString("caseTable"));
		headers.put(KBTextInterpreter.SYM_ABSTRACTION_TABLE, kwikiBundle
				.getString("symptomAbstractionTable"));
		headers.put(KBTextInterpreter.SIMILARITY_TABLE, kwikiBundle
				.getString("similarityTable"));
		headers.put(KBTextInterpreter.KNOWLEDGEBASE_CONFIG, kwikiBundle
				.getString("KBconfig"));
		headers.put(KBTextInterpreter.SET_COVERING_LIST, kwikiBundle
				.getString("setCoveringList"));
		headers.put(KBTextInterpreter.ANNOTATED_TEXT, kwikiBundle
				.getString("annotatedText"));
		headers.put(KBTextInterpreter.CASE_LIST, kwikiBundle
				.getString("caseList"));
		return headers;
	}

	private void addGeneratedItemsMessages(KWikiReport report,
			Collection generatedItems, KnowledgeBaseManagement kb) {

		Message m = renderGeneratedDiagnosis(generatedItems, report
				.isSolutionSafeMode());
		report.add(m);
		report.add(renderGeneratedQuestions(generatedItems, kb, report
				.isSafeMode()));

	}

	private Message renderGeneratedDiagnosis(Collection generatedItems,
			boolean safeModeSolutions) {
		boolean diagHeaderSet = false;
		String diagText = kwikiBundle
				.getString("report.no_diagnosis_generated");
		for (Object object : generatedItems) {

			if (object instanceof Diagnosis) {
				if (!diagHeaderSet) {
					diagHeaderSet = true;
					diagText = kwikiBundle
							.getString("report.generated_diagnoses")
							+ ":<br>";
				}
				String name = ((Diagnosis) object).getText();
				diagText += KnowWEUtils.convertUmlaut(name) + ", ";
			}
		}
		if (diagHeaderSet) {
			diagText = cutLast2Chars(diagText);
		}
		Message m = new Message(Message.NOTE, diagText, null, 0, null);
		if (safeModeSolutions && diagHeaderSet) {
			m = new Message(Message.ERROR, diagText, null, 0, null);
		}

		return m;
	}

	private String cutLast2Chars(String s) {
		return s.substring(0, s.length() - 2);
	}

	private Message renderGeneratedQuestions(Collection generatedItems,
			KnowledgeBaseManagement kbm, boolean safeMode) {
		boolean qHeaderSet = false;
		String qText = kwikiBundle.getString("report.no_questions_generated");
		for (Object object : generatedItems) {

			if (object instanceof Question) {
				if (!qHeaderSet) {
					qHeaderSet = true;
					qText = kwikiBundle.getString("report.generated_questions")
							+ ":<br>";
					;
				}
				String name = ((Question) object).getText();
				Question q = kbm.findQuestion(((Question) object).getText());

				if (object instanceof QuestionChoice) {
					QuestionChoice qoc = (QuestionChoice) q;

					qText += KnowWEUtils.convertUmlaut(name);
					if (qoc instanceof QuestionMC) {
						qText += " [mc] ";
					} else if (qoc instanceof QuestionYN) {
						qText += " [yn] ";

					} else {
						qText += " [oc] ";
					}
					if (!(qoc instanceof QuestionYN)) {

						qText += "{";
						boolean answerB = false;
						for (AnswerChoice answer : qoc.getAllAlternatives()) {
							answerB = true;
							qText += KnowWEUtils
									.convertUmlaut(answer.getText())
									+ "; ";
						}
						if (answerB) {
							qText = cutLast2Chars(qText);
						}
						qText += "}";
					}
					qText += ", ";
				}

				if (object instanceof QuestionNum) {
					qText += name + " [num], ";
				}

			}
		}
		if (qHeaderSet) {
			qText = cutLast2Chars(qText);
		}
		String type = Message.NOTE;
		if (safeMode && qHeaderSet) {
			type = Message.ERROR;
		}
		Message m = new Message(type, qText, null, 0, null);
		return m;
	}

	private String getHeadline(String element) {
		String s = "not found(" + element + ")";
		if (headers.containsKey(element)) {
			s = headers.get(element);
		}
		return KnowWEUtils.convertUmlaut(s);
	}

}
