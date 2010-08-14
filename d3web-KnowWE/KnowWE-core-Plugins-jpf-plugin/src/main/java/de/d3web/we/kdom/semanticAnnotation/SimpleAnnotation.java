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

/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.openrdf.model.URI;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author kazamatzuri
 * 
 */
public class SimpleAnnotation extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new AllTextFinderTrimmed();
		this.addSubtreeHandler(new SimpleAnnotationSubTreeHandler());
	}

	private class SimpleAnnotationSubTreeHandler extends
			SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			IntermediateOwlObject io = new IntermediateOwlObject();
			UpperOntology uo = UpperOntology.getInstance();

			String annos = "";
			try {
				annos = URLEncoder.encode(s.getOriginalText().trim(), "UTF-8");
			}
			catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			URI anno = null;
			if (annos.contains(":")) {
				String[] list = annos.split(":");
				String ns = list[0];
				String ens = SemanticCoreDelegator.getInstance().expandNamespace(ns);
				if (ns.equals(ens)) {
					io.setValidPropFlag(false);
					io.setBadAttribute(ns + " is no valid namespace");
				}
				try {
					anno = uo.getHelper().createURI(ens, list[1]);
				}
				catch (IllegalArgumentException e) {
					io.setValidPropFlag(false);
					io.setBadAttribute(ns);
				}
			}
			else {
				anno = uo.getHelper().createlocalURI(annos);
			}
			if (anno != null) {
				io.addLiteral(anno);
			}
			KnowWEUtils.storeSectionInfo(s, OwlHelper.IOO, io);
			return null;
		}

	}

}
