/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.taghandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.empiricalTesting.RatedSolution;
import de.d3web.empiricalTesting.RatedTestCase;
import de.d3web.empiricalTesting.SequentialTestCase;
import de.d3web.empiricalTesting.TestSuite;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.Kopic;
import de.d3web.we.kdom.kopic.KopicContent;
import de.d3web.we.testsuite.TestsuiteSection;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TestsuiteTagHandler extends AbstractTagHandler {
	
	private Map<String, Section> testsuites = new HashMap<String, Section>();
	private DecimalFormat formatter = new DecimalFormat("0.00");
	private String topic;
	private String web;
	private String article;
	private ResourceBundle rb;
	
	public TestsuiteTagHandler() {
		super("testsuite");
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.Testsuite.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		
		this.topic = topic;
		this.web = web;
		this.rb = D3webModule.getKwikiBundle_d3web(user);
		
		Map<String, String> urlParameterMap = user.getUrlParameterMap();
		String article = urlParameterMap.get("article");
				
		if (article != null	&& testsuites.containsKey(article)) {
			
			this.article = article;
			
			TestSuite t = loadTestSuite(article);
			
			if (t != null) { 
				
				return renderRunTestSuite(t);
			}
			
			return renderNoTestSuiteFound();
			
		}
		
		return renderFindTestSuites();

	}
		

	private String renderRunTestSuite(TestSuite t) {
		
		StringBuilder html = new StringBuilder();
		
		//HTML-Code
		html.append("<div id='testsuite-panel' class='panel'>");
		html.append("<h3>");
		html.append(rb.getString("KnowWE.Testsuite.headline"));
		html.append("</h3>");
		html.append(renderTestsuiteResult(t));
		html.append("</div>");
		
		return html.toString();	
	}
	
	private String renderNoTestSuiteFound() {
		
		StringBuilder html = new StringBuilder();
		
		//HTML-Code
		html.append("<div id='testsuite-panel' class='panel'>");
		html.append("<h3>");
		html.append(rb.getString("KnowWE.Testsuite.headline"));
		html.append("</h3>");
		html.append("<fieldset>");
		html.append("<div class='left'>");
		html.append("<p>");
		html.append(rb.getString("KnowWE.Testsuite.notestsuitefound"));
		html.append("</p>");
		html.append("</div>");
		html.append("</fieldset>");
		html.append("</div>");
		
		return html.toString();	
		
	}
	
	private String renderFindTestSuites() {
		
		loadArticlesContainingTestSuites();
		StringBuilder html = new StringBuilder();
		
		// HTML-Code
		html.append("<div id='testsuite-panel' class='panel'>");
		html.append("<h3>");
		html.append(rb.getString("KnowWE.Testsuite.headline"));
		html.append("</h3>");
		
		if (testsuites.size() > 0) {
			html.append(renderHowManyTestSuitesFound());
		} else {
			return renderNoTestSuiteFound();			
		}
		
		return html.toString();		
	}

	private String renderHowManyTestSuitesFound() {

		StringBuilder html = new StringBuilder();
		
		// How many TestSuites are found?
		html.append("<fieldset>");
		html.append("<div class='left'>");
		html.append("<p>");
		html.append(rb.getString("KnowWE.Testsuite.testsuitesfound1"));
		html.append("<strong>");
		html.append(testsuites.size());
		html.append("</strong>");
		html.append(" ");
		html.append(rb.getString("KnowWE.Testsuite.testsuitesfound2"));
		html.append("</p>");
		html.append("</div>");
		
		// Form
		html.append(renderSelectTestsuites());
		html.append("</div>");
		
		return html.toString();
		
	}

	private String renderTestsuiteResult(TestSuite t) {
		
		if (t.totalPrecision() == 1.0 && t.totalRecall() == 1.0) {
			return renderTestsuitePassed(t);
			
		} else if (!t.isConsistent()) {
			return renderTestsuiteNotConsistent(t);
			
		}
		
		return renderTestsuiteFailed(t);
		
	}
	
	private String renderTestsuitePassed(TestSuite t) {
		StringBuilder html = new StringBuilder();
		
		html.append("<fieldset>");
				
		// TestSuite passed text and green bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/green_bulb.gif' width='16' height='16' />");
		html.append("<strong>");
		html.append(rb.getString("KnowWE.Testsuite.passed1"));
		html.append(t.getRepository().size());
		html.append(" ");
		html.append(rb.getString("KnowWE.Testsuite.passed2"));
		html.append("</strong>");
		html.append("</p>");

		
		// Clear floating
		html.append("<div style='clear:both'></div>");
		
		// TestSuite Result Detais
		html.append("<div style='margin-left:22px'>");
		html.append("<p>");
		html.append("Precision: ");
		html.append(t.totalPrecision());
		html.append("<br />");
		html.append("Recall: ");
		html.append(t.totalRecall());
		html.append("<br /><br />");
		html.append("</p>");
		html.append("</div>");
		
		// DOT Download Button
		html.append(renderDOTDownload(t));
		
		// PDF Download Button
		html.append(renderPDFDownload(t));
		
		// Run more TestSuites
		html.append(renderExtend());
		
		// Close Tags
		html.append("</fieldset>");
		
		return html.toString();
	}
	
	private String renderExtend() {
		
		StringBuilder html = new StringBuilder();
		
		// Clear Floating
		html.append("<div style='clear:both'></div>");
		
		// Pointer and Text
		html.append("<p id='testsuite-show-extend' class='show-extend pointer extend-panel-down'>");
		html.append(rb.getString("KnowWE.Testsuite.extend"));
		html.append("</p>");
				
		// Dropdown Listbox
		html.append("<div id='testsuite-extend-panel' class='hidden'>");
		html.append(renderSelectTestsuites());
		html.append("</div>");
		
		return html.toString();
	}

	private String renderTestsuiteNotConsistent(TestSuite t) {
		StringBuilder html = new StringBuilder();
		
		html.append("<fieldset>");
				
		// TestSuite failed text and red bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/red_bulb.gif' width='16' height='16' />");
		html.append("<strong>");
		html.append(rb.getString("KnowWE.Testsuite.failed1"));
		html.append(t.getRepository().size());
		html.append(" ");
		html.append(rb.getString("KnowWE.Testsuite.failed2"));
		html.append("</strong>");
		html.append("</p>");
		
		// Clear floating
		html.append("<div style='clear:both'></div>");
		
		// TestSuite Result Detais
		html.append("<div style='margin-left:22px'>");
		html.append("<p>");
		html.append("Precision: ");
		html.append(formatter.format(t.totalPrecision()));
		html.append("<br />");
		html.append("Recall: ");
		html.append(formatter.format(t.totalRecall()));
		html.append("<br /><br />");
		html.append(rb.getString("KnowWE.Testsuite.notconsistent"));
		html.append("<br /><br />");
		html.append("</p>");
		html.append("</div>");
		
		html.append(renderNotConsistentDetails(t));
		
		// Run more TestSuites
		html.append(renderExtend());
		
		// Close Tags
		html.append("</fieldset>");
		
		return html.toString();
	}
	
	private String renderNotConsistentDetails(TestSuite t) {
		StringBuilder html = new StringBuilder();
		
		// Clear Floating
		html.append("<div style='clear:both'></div>");
		
		// Pointer and Text
		html.append("<p id='testsuite2-show-extend' class='show-extend pointer extend-panel-down'>");
		html.append(rb.getString("KnowWE.Testsuite.detail"));
		html.append("</p>");
		
		// Div containing details
		html.append("<div id='testsuite-detail-panel' class='hidden'>");
		html.append("<p style='margin-left:22px'>");
		html.append(findInconsistentRTC(t));
		html.append("</p>");
		html.append("</div>");
		
		return html.toString();
	}

	private String renderTestsuiteFailed(TestSuite t) {
		StringBuilder html = new StringBuilder();
		
		html.append("<fieldset>");
				
		// TestSuite failed text and red bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/red_bulb.gif' width='16' height='16' />");
		html.append("<strong>");
		html.append(rb.getString("KnowWE.Testsuite.failed1"));
		html.append(t.getRepository().size());
		html.append(" ");
		html.append(rb.getString("KnowWE.Testsuite.failed2"));
		html.append("</strong>");
		html.append("</p>");
		
		// Clear floating
		html.append("<div style='clear:both'></div>");
		
		// TestSuite Result Detais
		html.append("<div style='margin-left:22px'>");
		html.append("<p>");
		html.append("Precision: ");
		html.append(formatter.format(t.totalPrecision()));
		html.append("<br />");
		html.append("Recall: ");
		html.append(formatter.format(t.totalRecall()));
		html.append("<br /><br />");
		html.append("</p>");
		html.append("</div>");
		
		// PDF Download Button
		html.append(renderDOTDownload(t));
		
		// DOT Download Button
		html.append(renderPDFDownload(t));
		
		html.append(renderDifferenceDetails(t));
		
		// Run more TestSuites
		html.append(renderExtend());
		
		// Close Tags
		html.append("</fieldset>");
		
		return html.toString();
	}

	private String renderDifferenceDetails(TestSuite t) {

		StringBuilder html = new StringBuilder();
		
		// Clear Floating
		html.append("<div style='clear:both'></div>");
		
		// Pointer and Text
		html.append("<p id='testsuite2-show-extend' class='show-extend pointer extend-panel-down'>");
		html.append(rb.getString("KnowWE.Testsuite.detail"));
		html.append("</p>");
		
		// Table containing details
		html.append("<div id='testsuite-detail-panel' class='hidden'>");
		html.append(renderDetailResultTable(t));
		html.append("</div>");
		
		return html.toString();
	}

	private String renderDetailResultTable(TestSuite t) {
		
		StringBuilder html = new StringBuilder();
		StringBuilder temp;
		
		//HTML-Code
		for (SequentialTestCase stc : t.getRepository()) {
			temp = new StringBuilder();
			for (RatedTestCase rtc : stc.getCases()) {
				if (!rtc.isCorrect()) {
					temp.append("<tr>");
					temp.append("<td colspan='2' class='centered'>");
					temp.append("Rated-Test-Case ");
					temp.append(stc.getCases().indexOf(rtc) + 1);
					temp.append("</td>");
					temp.append("</tr>");
					temp.append("<tr>");
					temp.append("<td class='centered'>");
					temp.append(rb.getString("KnowWE.Testsuite.expected"));
					temp.append("</td>");
					temp.append("<td class='centered'>");
					temp.append(rb.getString("KnowWE.Testsuite.derived"));
					temp.append("</td>");
					temp.append("</tr>");
					temp.append("<tr>");
					temp.append("<td");
					temp.append("<ul>");
					Collections.sort(rtc.getExpectedSolutions(), 
							new RatedSolution.RatingComparatorByName());
					for (RatedSolution rs : rtc.getExpectedSolutions()) {
						temp.append("<li>");
						temp.append(rs.toString());
						temp.append("</li>");
					}
					temp.append("</ul>");
					temp.append("</td>");
					temp.append("<td>");
					temp.append("<ul>");
					Collections.sort(rtc.getDerivedSolutions(), 
							new RatedSolution.RatingComparatorByName());
					for (RatedSolution rs : rtc.getDerivedSolutions()) {
						temp.append("<li>");
						temp.append(rs.toString());
						temp.append("</li>");
					}
					temp.append("</ul>");
					temp.append("</td>");
					temp.append("</tr>");
				}
			}
			
			if (temp.length() > 0) {
				temp.insert(0, "</tr>");
				temp.insert(0, "</td>");
				temp.insert(0, "</strong>");
				temp.insert(0, stc.getName());
				temp.insert(0, "<strong>");
				temp.insert(0, "Sequential-Test-Case ");
				temp.insert(0, "<td colspan='2' class='centered'>");
				temp.insert(0, "<tr>");
				temp.insert(0, "<table cellspacing='0' width='80%'>");
				temp.append("</table>");
				temp.append("<br />");
				html.append(temp);
			}
		}
		
		return html.toString();
	}

	private String renderSelectTestsuites() {
		
		StringBuilder html = new StringBuilder();
				
		// Clear floating
		html.append("<div style='clear:both'></div>");
		
		// Dropdownlistbox
		html.append("<form action='' method='get'>");
		html.append("<fieldset>");
		html.append("<div class='left'>");
		html.append("<label for='article'>Testsuite</label>");
		html.append("<select id='article' name='article' value='' tabindex='1' class='field' title=''/>");
		for (String a : testsuites.keySet()) {
			html.append("<option value='");
			html.append(a);
			html.append("'>");
			html.append(a);
			html.append("</option>");
		}
		html.append("</select>");
		html.append("</div>");
		
		// Run Button
		html.append("<div>");
		html.append("<input type=\"hidden\" name=\"page\" value=\"");
		html.append(urlencode(topic));
		html.append("\" />");
		html.append("<input type='submit' value='");
		html.append(rb.getString("KnowWE.Testsuite.runbutton")); 
		html.append("' class='button' />");		
		html.append("</div>");
				
		// Close Tags
		html.append("</fieldset>");
		html.append("</form>");
		
		return html.toString();
	}

	private TestSuite loadTestSuite(String article) {
		Section s = testsuites.get(article);
		return (TestSuite) KnowWEUtils.getStoredObject(web, article, s.getId(), TestsuiteSection.TESTSUITEKEY);
	}
	
	private void loadArticlesContainingTestSuites() {
		Iterator<KnowWEArticle> iterator = 
			KnowWEEnvironment.getInstance().getArticleManager(web).getArticleIterator();
		
		while (iterator.hasNext()) {
			
			KnowWEArticle article = iterator.next();
			// TODO: HOTFIX!! I don't think this is the proper way to get the TestsuiteSection...
			Section s = article.getSection().getChildren().get(0).findChildOfType(TestsuiteSection.class);
			
			// Check for TestSuite-Section outside of Kopic-Section
			if (s != null) {
				testsuites.put(article.getTitle(), s);
			} else {
				// Check for Testsuite-Section inside of Kopic-Section
				// TODO: HOTFIX!! I don't think this is the proper way to get the TestsuiteSection...
				s = article.getSection().getChildren().get(0).findChildOfType(Kopic.class);
				if (s != null) {
					s = s.findChildOfType(KopicContent.class);
					if (s != null) {
						s = s.findChildOfType(TestsuiteSection.class);
						if (s != null) {
							testsuites.put(article.getTitle(), s);
						}
					}
				}
			}			
		}
		
	}

	private String findInconsistentRTC(TestSuite t) {
		
		StringBuilder message = new StringBuilder();
		
		for (SequentialTestCase stc1 : t.getRepository())
			for (SequentialTestCase stc2 : t.getRepository())
				for(int i = 0; i<stc1.getCases().size() && i<stc2.getCases().size(); i++){
					RatedTestCase rtc1 = stc1.getCases().get(i);
					RatedTestCase rtc2 = stc2.getCases().get(i);
					
					//when the findings are equal...
					if (rtc1.getFindings().equals(rtc2.getFindings())){
						//...but not the solutions...
						if (!rtc1.getExpectedSolutions().equals(
								rtc2.getExpectedSolutions())){
							//...the TestSuite is not consistent!
							message.append("Rated-Test-Case ");
							message.append(stc1.getCases().indexOf(rtc1));
							message.append(" in ");
							message.append(stc1.getName());
							message.append(" ");
							message.append(rb.getString("KnowWE.Testsuite.and"));
							message.append(" ");
							message.append("Rated-Test-Case ");
							message.append(stc2.getCases().indexOf(rtc2));
							message.append(" in ");
							message.append(stc2.getName());
							message.append(" ");
							message.append(rb.getString("KnowWE.Testsuite.havesamefindings"));
							message.append("<br />");
							
						}
					}else 
						break;
				}
		
		// Not very nice but prevents double listing of RTCs
		return message.substring(0, message.length() / 2).toString();
	}
	
	private String renderPDFDownload(TestSuite t) {
		
//		long time = System.nanoTime();
//		
//		// Create PDF
//		StringBuilder path = new StringBuilder();
//		path.append(KnowWEEnvironment.getInstance().getContext().getRealPath(""));
//		path.append("/KnowWEExtension/tmp/");
//		path.append(article);
//		path.append("_testsuite_");
//		path.append(time);
//		path.append(".pdf");
//		JUNGCaseVisualizer.getInstance().writeToFile(t, path.toString());
//		
//		// Render Download Button
//		StringBuilder html = new StringBuilder();
//		html.append("<div>");
//		html.append("<img src='KnowWEExtension/images/arrow_right.png' width='9' height='15' style='margin-left:2px' />");
//		html.append("<a style='margin-left:5px' href='");
//		html.append(KnowWEEnvironment.getInstance().getContext().getContextPath());
//		html.append("/KnowWEExtension/tmp/");
//		html.append(article);
//		html.append("_testsuite_");
//		html.append(time);
//		html.append(".pdf");
//		html.append("'>");
//		html.append(rb.getString("KnowWE.Testsuite.downloadpdf")); 
//		html.append("</a>");
//		html.append("</div>");
		
		// Render Download Button
		StringBuilder html = new StringBuilder();
		html.append("<div>");
		html.append("<img src='KnowWEExtension/images/arrow_right.png' width='9' height='15' style='margin-left:2px' />");
		html.append("<a style='margin-left:5px' href='testsuitedownload?KWiki_Topic=");
		html.append(article);
		html.append("&web=");
		html.append(web);
		html.append("&nodeID=");
		html.append(testsuites.get(article).getId());
		html.append("&filename=");
		html.append(article);
		html.append("_Visualization.pdf");
		html.append("'>");
		html.append(rb.getString("KnowWE.Testsuite.downloadpdf")); 
		html.append("</a>");
		html.append("</div>");
		
		return html.toString();
		
		
		
	}
	
	private String renderDOTDownload(TestSuite t) {
		
//		long time = System.nanoTime();
//		
//		// Create DOT
//		StringBuilder path = new StringBuilder();
//		path.append(KnowWEEnvironment.getInstance().getContext().getRealPath(""));
//		path.append("/KnowWEExtension/tmp/");
//		path.append(article);
//		path.append("_testsuite_");
//		path.append(time);
//		path.append(".dot");
//		DDBuilder.getInstance().writeToFile(t, path.toString());
//		
//		// Render Download Button
//		StringBuilder html = new StringBuilder();
//		html.append("<div>");
//		html.append("<img src='KnowWEExtension/images/arrow_right.png' width='9' height='15' style='margin-left:2px' />");
//		html.append("<a style='margin-left:5px' href='");
//		html.append(KnowWEEnvironment.getInstance().getContext().getContextPath());
//		html.append("/KnowWEExtension/tmp/");
//		html.append(article);
//		html.append("_testsuite_");
//		html.append(time);
//		html.append(".dot");
//		html.append("'>");
//		html.append(rb.getString("KnowWE.Testsuite.downloaddot")); 
//		html.append("</a>");
//		html.append("</div>");
		
		StringBuilder html = new StringBuilder();
		html.append("<div>");
		html.append("<img src='KnowWEExtension/images/arrow_right.png' width='9' height='15' style='margin-left:2px' />");
		html.append("<a style='margin-left:5px' href='TestSuiteDownload.jsp?KWiki_Topic=");
		html.append(article);
		html.append("&web=");
		html.append(web);
		html.append("&nodeID=");
		html.append(testsuites.get(article).getId());
		html.append("&filename=");
		html.append(article);
		html.append("_Visualization.dot");
		html.append("'>");
		html.append(rb.getString("KnowWE.Testsuite.downloaddot")); 
		html.append("</a>");
		html.append("</div>");
		
		return html.toString();
		
	}
	
	private String urlencode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(text);
		}
	}
	
}
