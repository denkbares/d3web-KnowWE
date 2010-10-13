/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.we.sectionfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.ContentType;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.logging.Logging;
import de.d3web.we.uimaconnector.IUIMADomain;
import de.d3web.we.uimaconnector.UIMAConnector;
import de.d3web.we.utils.AnnotationStore;

/**
 * Initialises the CAS for a UIMA-Domain. The domain is specified in a field.
 * Every UIMA-Domain has an unique name and an unique ContentType of its
 * DefaultMarkup.
 * 
 * @author Johannes Dienst
 * 
 */
public class DefaultMarkupUIMADomainContentSectionFinder implements ISectionFinder {

	/**
	 * The domain, which is searched in the UIMAConnector for
	 */
	private final String domain;

	/**
	 * Is called in the ContentType of an UIMA-Domain DefaultMarkup.
	 * 
	 * @param domain
	 */
	public DefaultMarkupUIMADomainContentSectionFinder(String domain) {
		this.domain = domain;
	}

	/**
	 * Initialises the Analysis Engine and the CAS.
	 * 
	 */
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {

		if (father.getObjectType() instanceof ContentType) {

			try {
				IUIMADomain d = UIMAConnector.getInstance().getUIMADomain(domain);

				if (d != null) {
					// Reset the AnnotationStore: Because so it will not only
					// function once
					AnnotationStore.getInstance().reset();
					AnalysisEngine ae = UIMAConnector.getInstance().getAnalysisEngine(d
							.getDescriptorXML());

					CAS cas = UIMAConnector.getInstance().getActualCAS();
					if (cas != null) {
						cas.reset();
					}
					if (cas == null) {
						cas = ae.newCAS();
					}
					cas.setDocumentText(text);
					ae.process(cas);
					UIMAConnector.getInstance().setActualCAS(cas);
				}

			}
			catch (Exception e) {
				Logging.getInstance().log(Level.FINER,
						"Something went wrong in UIMA-Processing");
			}
		}
		ArrayList<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
		results.add(new SectionFinderResult(0, text.length()));
		return results;
	}
}
