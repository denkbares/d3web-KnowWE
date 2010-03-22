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
package de.d3web.we.kdom.xcl;

import java.util.logging.Level;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.logging.Logging;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

/**
 * A section for storing DCProperties in a MMInfo.
 * The storing could be generic, but then where to get the NamedObject
 * from, to store the info in?!?!
 *  
 * ATM this class is creating the diagnosis, due to the execution order 
 * of subtreehandlers. So take care the right SolutionContext is set in 
 * the subtreehandler of XCLHead.
 * 
 * @author Reinhard Hatko 
 * Created on: 03.12.2009
 */
public class DCPropertySubtreeHandler extends D3webReviseSubTreeHandler {

	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

		KnowledgeBaseManagement kbm = getKBM(article, s);

		if (kbm == null)
			return null;

		NamedObject obj = getNamedObject(s, kbm);

		if (obj == null)
			return null;
		storeMMInfo(s, obj);
		
		return null;

	}

	/** 
	 * Stores the content of the section into the NamedObjects  MMInfoStore
	 * 
	 */
	private void storeMMInfo(Section s, NamedObject obj) {

		MMInfoStorage mminfo = (MMInfoStorage) obj.getProperties().getProperty(
				Property.MMINFO);
		
		if (mminfo == null) {
			mminfo = new MMInfoStorage();
			obj.getProperties().setProperty(Property.MMINFO, mminfo);
		}
		String subject = s.findChildOfType(DCPropertyNameType.class).getOriginalText().toLowerCase();

		DCMarkup markup = new DCMarkup();
//		markup.setContent(DCElement.TITLE, "Info"); //TODO set title to something?
		markup.setContent(DCElement.SUBJECT, subject);
		markup.setContent(DCElement.SOURCE, obj.getId());
		
		
		String content = s.findChildOfType(DCPropertyContentType.class).getOriginalText();
		
		mminfo.addMMInfo(new MMInfoObject(markup, content));
	}

	/**
	 * Looks for the NamedObject.
	 * ATM this is tailored to work in XCLs.
	 * this is the part which would have to be adapted to other scenarios 
	 *
	 */
	private NamedObject getNamedObject(Section s, KnowledgeBaseManagement kbm) {
		Section father = s.getFather();
		DefaultSubjectContext diagnosis = (DefaultSubjectContext) ContextManager
				.getInstance().getContext(father, DefaultSubjectContext.CID);

		if (diagnosis == null) {
			Logging.getInstance().log(Level.WARNING, "No context set for: " + father.getOriginalText());
			return null;
		}

		NamedObject d = kbm.findDiagnosis(diagnosis.getSubject());

		if (d == null) { //atm diag is not created before, due to sequence of subtreehandlers
			d = kbm.createDiagnosis(diagnosis.getSubject());
		}
		return d;
	}

}
