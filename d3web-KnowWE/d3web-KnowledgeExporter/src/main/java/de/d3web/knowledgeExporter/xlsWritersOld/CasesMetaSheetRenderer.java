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

package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.text.DateFormat;
//import java.util.Date;
//import java.util.ResourceBundle;
//
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//
//import de.d3web.caserepository.CaseObject;
//import de.d3web.caserepository.addons.train.AdditionalTrainData;
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.supportknowledge.DCElement;
//import de.d3web.kernel.supportknowledge.DCMarkup;
//import de.d3web.kernel.supportknowledge.Property;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//
//public class CasesMetaSheetRenderer extends AbstractFormatableSheetRenderer {
//	
//	private Iterable<CaseObject> cases;
//	private ResourceBundle resourceBundle;
//
//	public CasesMetaSheetRenderer(KnowledgeBase kb, HSSFSheet s, KnowledgeManager m, Iterable<CaseObject> cases) {
//		super(kb, s, m);
//		this.cases = cases;
//		resourceBundle = manager.getResourceBundle();
//	}
//
//	@Override
//	public void renderSheet() {
//		renderHead();
//		
//		int row = 1;
//		
//		for (CaseObject co : cases) {
//			int column = 0;
//			
//			//copied this from MetaInfoModel, doesn't seem to be right
//	        if (co.getProperties().getProperty(
//	                Property.CASE_KNOWLEDGEBASE_DESCRIPTOR) == null) {
//	            co.getProperties().setProperty(
//	                    Property.CASE_KNOWLEDGEBASE_DESCRIPTOR, new DCMarkup());
//	        }
//	        
//			DCMarkup markup = co.getDCMarkup();
//			AdditionalTrainData atd = (AdditionalTrainData)co.getAdditionalTrainData();
//			if (atd == null) atd = new AdditionalTrainData();
//			
//			//TITLE
//			setCellValue(column++, row, markup.getContent(DCElement.TITLE), STYLE_SMALLBOLD);
//			
//			//rest of DC
//			setCellValue(column++, row, markup.getContent(DCElement.IDENTIFIER));
//			setCellValue(column++, row, markup.getContent(DCElement.CREATOR));
//			//setCellValue(column++, row, markup.getContent(DCElement.DATE));
//			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
//		            DateFormat.SHORT);
//            Date d = DCElement.string2date(co.getDCMarkup()
//                    .getContent(DCElement.DATE));
//            //if (d == null) d = new Date(); <-- TODO: so machen wie Plugin-CaseManagement?
//            if (d != null) setCellValue(column, row, df.format(d));
//            column++;
//
//            //TODO: the stuff beow doesn't work, not even in CaseManagement
//            
//			//setCellValue(column++, row, markup.getContent(DCElement.SUBJECT));
//			setCellValue(column++, row, ((DCMarkup)co.getProperties().getProperty(
//                    Property.CASE_KNOWLEDGEBASE_DESCRIPTOR))
//                    .getContent(DCElement.SUBJECT));
//			//setCellValue(column++, row, markup.getContent(DCElement.PUBLISHER));
//			setCellValue(column++, row, ((DCMarkup) co.getProperties().getProperty(
//                    Property.CASE_KNOWLEDGEBASE_DESCRIPTOR))
//                    .getContent(DCElement.PUBLISHER));
//			//setCellValue(column++, row, markup.getContent(DCElement.CONTRIBUTOR));
//			setCellValue(column++, row, ((DCMarkup) co.getProperties().getProperty(
//                    Property.CASE_KNOWLEDGEBASE_DESCRIPTOR))
//                    .getContent(DCElement.CONTRIBUTOR));
//			//setCellValue(column++, row, markup.getContent(DCElement.SOURCE));
//			setCellValue(column++, row, ((DCMarkup) co.getProperties().getProperty(
//                    Property.CASE_KNOWLEDGEBASE_DESCRIPTOR))
//                    .getContent(DCElement.SOURCE));
//			//setCellValue(column++, row, markup.getContent(DCElement.LANGUAGE));
//			setCellValue(column++, row, ((DCMarkup) co.getProperties().getProperty(
//                    Property.CASE_KNOWLEDGEBASE_DESCRIPTOR))
//                    .getContent(DCElement.LANGUAGE));
//			
//			//OTHER
//			Object comment = co.getProperties().getProperty(Property.CASE_COMMENT);
//			
//			if (comment == null) {
//				//setCellValue(column++, row, "");
//				column++;
//			} else {
//				setCellValue(column++, row, comment);
//			}
//			
//			if (atd.getComplexity() == null) {
//				setCellValue(column++, row, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo."+
//						AdditionalTrainData.Complexity.MEDIUM.getName()));
//			} else {
//				setCellValue(column++, row, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo."+
//						atd.getComplexity().getName()));
//			}
//			
//			if (atd.getDuration() == null) {
//				setCellValue(column++, row, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo."+
//						AdditionalTrainData.Duration.UNDER_THIRTY.getName()));
//			} else {
//				setCellValue(column++, row, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo."+
//						atd.getDuration().getName()));
//			}
//			
//			row++;
//		}
//	}
//	
//	private void renderHead() {
//		int column = 1;
//		
//		//DC
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.id"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.author"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.date"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.subject"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.publisher"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.contributor"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.source"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.language"), STYLE_SMALLBOLD);
//		
//		//OTHER
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.description"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.complex"), STYLE_SMALLBOLD);
//		setCellValue(column++, 0, resourceBundle.getString("d3web.KnowME.CaseManagement.MetaInfo.workingTime"), STYLE_SMALLBOLD);
//	}
//}
