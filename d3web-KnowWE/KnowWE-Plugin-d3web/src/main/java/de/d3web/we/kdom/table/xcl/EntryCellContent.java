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

package de.d3web.we.kdom.table.xcl;

import java.util.Collection;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.kernel.verbalizer.ConditionVerbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author jochen
 * 
 *         cell content type for xclrelation-content cells
 * 
 */
public class EntryCellContent extends TableCellContent {

	/**
	 * <p>
	 * Returns the renderer for the TableCellContent.
	 * </p>
	 * 
	 * @return {@link KnowWEDomRenderer}
	 * @see TableCellContentRenderer
	 */
	@Override
	public KnowWEDomRenderer getRenderer() {
		return CoveringTableEntryRenderer.getInstance();
	}

	public void init() {
		this.addReviseSubtreeHandler(new EntryCellContentSubtreeHandler());
	}

}

class CoveringTableEntryRenderer extends TableCellContentRenderer {

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for
	 * the table
	 */
	protected String wrappContent(String sectionText, Section sec,
			KnowWEUserContext user) {

		String title = "relation_not_found";

		String kbrelId = (String) KnowWEUtils.getStoredObject(sec.getArticle()
				.getWeb(), sec.getTitle(), sec.getId(),
				EntryCellContentSubtreeHandler.XCLRELATION_ID);
		if (kbrelId != null) {
			// Get the KnowledgeSlices from KB and find the XCLRelation to be
			// rendered
			KnowledgeBaseManagement mgn = D3webModule.getInstance()
					.getKnowledgeRepresentationHandler().getKBM(sec.getArticle(), sec);

			Collection<KnowledgeSlice> models = mgn.getKnowledgeBase()
					.getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice knowledgeSlice : models) {
				if (knowledgeSlice instanceof XCLModel) {

					// Check if model contains the relation
					if (((XCLModel) knowledgeSlice).findRelation(kbrelId) != null) {

						XCLRelation rel = ((XCLModel) knowledgeSlice)
								.findRelation(kbrelId);

						title = ((XCLModel) knowledgeSlice).getSolution().getText()+": "+ new ConditionVerbalizer().verbalize( rel.getConditionedFinding(),RenderingFormat.PLAIN_TEXT,null);
						break;
						// // eval the Relation to find the right Rendering
						// try {
						// boolean b =
						// ((XCLModel)knowledgeSlice).findRelation(kbrelId
						// ).eval(c);
						// // Highlight Relation
						// return this.renderRelationChildren(sec, user, true,
						// b);
						// } catch (Exception e) {
						// // Call the XCLRelationMarkerHighlightingRenderer
						// // without any additional info
						// return this.renderRelationChildren(sec, user, false,
						// false);
						// }
					}
				}
			}

		}else {
			Object o = KnowWEUtils.getStoredObject(sec, EntryCellContentSubtreeHandler.KEY_REPORT);
			if(o != null && o instanceof Message) {
				title = ((Message)o).getMessageText();
			}
		}

		String sectionID = sec.getId();
		StringBuilder html = new StringBuilder();
		if (sectionText.trim().length() > 0) {
			if(kbrelId != null) {
			html
					.append("<td style='color: green; background-color:#DDDDDD;text-align:center;'>   ");
			html.append("<span id='" + sec.getId()
					+ "' class = 'XCLRelationInTable'>");
			html.append("<span id=''>");
			html.append("<span title='" + title + "'>");

			generateContent(sectionText, sec, user, sectionID, html);
			html.append("</span>");
			html.append("</span>");
			html.append("</span>");
			html.append("</td>");
			}else {
				html.append("<td title='" + title + "' style='background-color:red;text-align:center;'>   ");
				generateContent(sectionText, sec, user, sectionID, html);
				html.append("</td>");
			}

		} else {
			html.append("<td style='background-color:#BBBBBB;'>   ");
			html.append("</td>");
		}
		return KnowWEEnvironment.maskHTML(html.toString());
	}

	private static CoveringTableEntryRenderer instance = null;

	public static CoveringTableEntryRenderer getInstance() {
		if (instance == null) {
			instance = new CoveringTableEntryRenderer();
		}
		return instance;
	}

}