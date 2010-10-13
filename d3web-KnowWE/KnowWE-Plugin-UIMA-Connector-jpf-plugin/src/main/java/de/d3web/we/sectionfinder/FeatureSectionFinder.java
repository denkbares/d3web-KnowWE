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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.tcas.Annotation;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.knowweobjecttypes.FeatureImplementation;
import de.d3web.we.uimaconnector.UIMAConnector;
import de.d3web.we.utils.DefaultIntervalCalculator;
import de.d3web.we.utils.IIntervalCalculator;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Used to find a Feature. It uses the father section to find the right Type who
 * has this Feature. You also need to have a KnowWEObjectType in the
 * FatherKnowWEObjectType that implements FeatureImplementation Interface.
 * 
 * @see lookForSections()
 * @see FeatureImplementation
 * 
 * @author Johannes Dienst
 * 
 */
public class FeatureSectionFinder implements ISectionFinder {

	/**
	 * Used in <code>lookforSections()</code>
	 */
	private String clazzName = "";
	private String featKey = "";
	private IIntervalCalculator intervalCalculator = DefaultIntervalCalculator.getInstance();

	/**
	 * Classname of the given class is used.
	 * 
	 * @param c
	 */
	public FeatureSectionFinder(Class<?> c) {
		clazzName = c.getName().replaceAll("knowwetypes", "typesystem");
	}

	/**
	 * The clazzName should look like this: type:featName It is splitted with
	 * the ":".
	 * 
	 * @param clazzName2
	 */
	public FeatureSectionFinder(String clazzName, boolean replace) {
		String[] typeFeatArray = clazzName.split(":");
		this.clazzName = typeFeatArray[0];
		this.featKey = typeFeatArray[1];
		if (replace) this.clazzName = clazzName.replaceAll("knowwetypes", "typesystem");
	}

	/**
	 * This is not really a SectionFinder method. This method gets a feature by
	 * using the father section. It calculates the absolute starting position of
	 * the father in the UIMASection and then selects the right annotation.
	 */
	@Override
	public List<SectionFinderResult> lookForSections(
			String text, Section<?> father, KnowWEObjectType type) {

		// calculate the absolute father position and find
		// the Annotation of the father
		int[] aF = this.intervalCalculator.calculateAbsoluteFather(father);

		List<Annotation> annos =
				UIMAConnector.getInstance().findAllTypes(this.clazzName);
		Annotation a = null;
		for (Iterator<Annotation> it = annos.iterator(); ((a == null) && (it.hasNext()));) {
			a = it.next();
			if ((a.getBegin() == aF[0]) && (a.getEnd() == aF[1])) break;
			a = null;
		}

		// Get the feature and store it in SectionStore
		Feature feature = a.getType().getFeatureByBaseName(this.featKey);
		if (feature.getRange().isPrimitive()) {
			String range = feature.getRange().getName();
			if (range.equals("uima.cas.Integer")) {
				int value = a.getIntValue(feature);
				this.storePrimitiveFeature(father, value);
			}
			else if (range.equals("uima.cas.Double")) {
				double value = a.getDoubleValue(feature);
				this.storePrimitiveFeature(father, value);
			}
			else if (range.equals("uima.cas.Boolean")) {
				boolean value = a.getBooleanValue(feature);
				this.storePrimitiveFeature(father, value);
			}
			else if (range.equals("uima.cas.String")) {
				String value = a.getFeatureValueAsString(feature);
				this.storePrimitiveFeature(father, value);
			}
		}
		else {
			FeatureStructure featVal =
					a.getFeatureValue(feature);
			this.storeFeatureStructure(father, featVal);
		}

		// always empty
		return new ArrayList<SectionFinderResult>();
	}

	/**
	 * Stores a primitive FeatureValue in the KnowWESectionStore.
	 * 
	 * @param father
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	private void storePrimitiveFeature(Section<?> father, Object featVal) {
		Object featList = KnowWEUtils.getStoredObject(father.getArticle(), father, father.getID());
		if (featList != null) ((HashMap<String, Object>) featList).put(this.featKey, featVal);
		else {
			HashMap<String, Object> b = new HashMap<String, Object>();
			b.put(this.featKey, featVal);
			KnowWEUtils.storeObject(father.getArticle(), father, father.getID(), b);
		}
	}

	/**
	 * Stores a FeatureStructure in the KnowWESectionStore.
	 * 
	 * @param father
	 * @param featVal
	 */
	@SuppressWarnings("unchecked")
	private void storeFeatureStructure(Section<?> father, FeatureStructure featVal) {
		Object featList = KnowWEUtils.getStoredObject(father.getArticle(), father, father.getID());
		if (featList != null) ((HashMap<String, Object>) featList).put(this.featKey, featVal);
		else {
			HashMap<String, Object> b = new HashMap<String, Object>();
			b.put(this.featKey, featVal);
			KnowWEUtils.storeObject(father.getArticle(), father, father.getID(), b);
		}
	}

	/**
	 * Sets the IntervalCalculator
	 * 
	 * @param ic
	 */
	public void setIntervalCalculator(IIntervalCalculator ic) {
		this.intervalCalculator = ic;
	}

}
