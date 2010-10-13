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
package de.d3web.we.uimaconnector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.XMLInputSource;

import de.d3web.we.logging.Logging;

/**
 * Connecter to UIMA components. Offers all needed functions for the
 * SectionFinders IUIMADomains. Implemented as singleton.
 * 
 * @see IUIMADomain
 * @see de.d3web.we.sectionfinder
 * 
 * @author Johannes Dienst
 * 
 */
public class UIMAConnector {

	/**
	 * Singleton instance.
	 */
	private static UIMAConnector instance = null;

	/**
	 * List of registered UIMADomains.
	 */
	private final List<IUIMADomain> doms = new ArrayList<IUIMADomain>();

	/**
	 * Singleton
	 */
	public static UIMAConnector getInstance() {
		if (instance == null) instance = new UIMAConnector();
		return instance;
	}

	/**
	 * The actual CAS.
	 */
	private CAS actCas;

	/**
	 * Builds an analysis engine from the descriptor.xml
	 * 
	 * @param descriptor
	 * @return
	 */
	public AnalysisEngine getAnalysisEngine(File descriptor) {

		AnalysisEngine ae = null;
		try {
			XMLInputSource in = new XMLInputSource(descriptor);
			ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);
			ae = UIMAFramework.produceAnalysisEngine(specifier);
			in.close();
		}
		catch (Exception e) {
			System.out.println(e.getStackTrace());
		}

		return ae;
	}

	/**
	 * Sets the actual CAS.
	 * 
	 * @param cas
	 */
	public void setActualCAS(CAS cas) {
		this.actCas = cas;
	}

	/**
	 * Returns the CAS which is actually worked on.
	 * 
	 * @return
	 */
	public CAS getActualCAS() {
		return this.actCas;
	}

	/**
	 * @param text
	 * @param clazzName
	 */
	public List<Annotation> findAllTypes(String clazzName) {

		List<Annotation> annos = new ArrayList<Annotation>();
		try {
			Type type = this.actCas.getTypeSystem().getType(clazzName);
			FSIterator iterator = this.actCas.getAnnotationIndex(type).iterator();
			while (iterator.isValid()) {
				FeatureStructure fs = iterator.get();
				annos.add((Annotation) fs);
				iterator.moveToNext();
			}
		}
		catch (Exception e) {
			Logging.getInstance().log(Level.FINER,
					"Something went wrong in findAllTypes()");
		}

		return annos;
	}

	/**
	 * Finds all Features of a type. Note: This collects all types and then gets
	 * the Features.
	 * 
	 * See {@link NestedTypeSectionfinder} how this is used.
	 * 
	 * @param clazzName
	 */
	public List<FeatureStructure> findFeatureAnnotations(String clazzName) {
		List<FeatureStructure> annos = new ArrayList<FeatureStructure>();
		try {
			String typName = clazzName.substring(0, clazzName.lastIndexOf(":"));
			String featName = clazzName.substring(clazzName.lastIndexOf(":") + 1);
			Type type = this.actCas.getTypeSystem().getType(typName);
			FSIterator iterator = this.actCas.getAnnotationIndex(type).iterator();
			while (iterator.isValid()) {
				FeatureStructure fs = iterator.get();
				Feature f = fs.getType().getFeatureByBaseName(featName);
				annos.add(fs.getFeatureValue(f));
				iterator.moveToNext();
			}
		}
		catch (Exception e) {
			Logging.getInstance().log(Level.FINER,
					"Something went wrong in findFeatures()");
		}

		return annos;
	}

	/**
	 * Every Domain registers itself here when it is initialized.
	 * 
	 * @param domain
	 * @return
	 */
	public boolean registerUIMADomain(IUIMADomain domain) {
		return this.doms.add(domain);
	}

	/**
	 * Gets the IUIMA Domain corresponding to its name
	 * 
	 * @param domain
	 */
	public IUIMADomain getUIMADomain(String domain) {
		for (IUIMADomain d : doms)
			if (d.getDomainName().equals(domain)) return d;
		return null;
	}
}
