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

package de.d3web.we.ci4ke.handling;

import groovy.lang.Script;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.we.ci4ke.groovy.GroovyCITestReviseSubtreeHandler;
import de.d3web.we.ci4ke.groovy.GroovyCITestType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Immutable configuration class for Continuous Integration Testing
 * @author Marc
 * 
 */
public class CIConfiguration {
	
	//PUBLIC KEY CONSTANTS
	public static final String WEB_KEY 					= "web";
	public static final String MONITORED_ARTICLE_KEY 	= "article";
	public static final String TESTS_KEY 				= "tests";

	//*** PRIVATE MEMBERS ***
	
	/**
	 * The configuration parameters which get passed to every CI test
	 */
	private Map<String, String> parameters;
	
	/**
	 * A List of tests, which have to be executed for a new build
	 */
	private List<Class<? extends CITest>> testsToExecute;
	
	public List<Class<? extends CITest>> getTestsToExecute() {
		return testsToExecute;
	}

	private static final Map<String, String> defaultParameters = createDefaultParameters();
	
	private static Map<String, String> createDefaultParameters(){
		Map<String, String> defMap = new HashMap<String, String>();
		defMap.put(WEB_KEY, KnowWEEnvironment.DEFAULT_WEB);
		return Collections.unmodifiableMap(defMap);
	}
	
	/**
	 * Constructs a new CI Configuration, containing the given parameters.
	 * @param parameters
	 * @param dashboardArticleTitle The title of the article on which this CI Dashboard resides.
	 */
	public CIConfiguration(Map<String, String> parameters, String dashboardArticleTitle){
		this.parameters = new HashMap<String, String>();
		this.testsToExecute = new LinkedList<Class<? extends CITest>>();

		//Add all default configuration parameters...
		this.parameters.putAll(defaultParameters);
		//...and amend them (and eventually overwrite them!) with the given parameters 
		for(String key : parameters.keySet()){
			if(key.equals(TESTS_KEY))//the tests need special treatment
				this.testsToExecute = parseTestClasses(parameters.get(key));
			else
				this.parameters.put(key, parameters.get(key));
		}
		//if no monitored Article was set: The article on which the Dashboard 
		//resides will be monitored 
		if(!this.parameters.containsKey(MONITORED_ARTICLE_KEY))
			this.parameters.put(MONITORED_ARTICLE_KEY, dashboardArticleTitle);
	}
	
	/**
	 * @see java.util.Map#get(Object)
	 */
	public String get(String key){ return parameters.get(key); }
	
	/**
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet(){ return parameters.keySet(); }
	
	/**
	 * Convenience Accessor for the monitored article
	 * @return the monitored article
	 */
	public KnowWEArticle getMonitoredArticle(){
		//return parameters.get(MONITORED_ARTICLE_KEY);
		return KnowWEEnvironment.getInstance().getArticle(this.get(WEB_KEY), this.get(MONITORED_ARTICLE_KEY));
	}
	
	public String getWeb(){
		return this.get(WEB_KEY);
	}

	private List<Class<? extends CITest>> parseTestClasses(String testClassNames){
		//get all Sections containing a GroovyCITest
		Map<String,Section<GroovyCITestType>> groovyTestSections = getAllGroovyCITestSections();
		//our return list
		List<Class<? extends CITest>> classesList = new LinkedList<Class<? extends CITest>>();
		//the test class names are separeted by colons... lets split() them!
		String[] classes = testClassNames.split(":");
		//the package prefix to find the
		String packagePrefix = "de.d3web.we.ci4ke.testmodules.";

		for(String c : classes){
			
			//a test can either be statically defined in a java class
			//or dynamically defined in a grovvy test section
			if(groovyTestSections.containsKey(c)){
				//the current class name matches the name of a (groovy) test section
				Section<GroovyCITestType> sec = groovyTestSections.get(c);
				
				Script script = GroovyCITestReviseSubtreeHandler.parseGroovyCITestSection(sec);
				
				@SuppressWarnings("unchecked") Class<? extends CITest> testClass =
					(Class<? extends CITest>) script.getClass();
				
				classesList.add(testClass);
				
			} else {
				
				//Try to load the class
				Class<?> clazz = null;
				try {
					clazz = Class.forName(packagePrefix+c);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					
				}
				//If our new class implements the ITest-interface...
				if(CITest.class.isAssignableFrom(clazz)){
					//this cast is legit due to the type-checking beforehand
					@SuppressWarnings("unchecked") Class<? extends CITest> testClass =
						(Class<? extends CITest>) clazz;
					classesList.add(testClass);		
				}					
			}
		}
		return classesList;
	}
	
	private Map<String,Section<GroovyCITestType>> getAllGroovyCITestSections(){
		//return map
		Map<String,Section<GroovyCITestType>> sectionsMap = new HashMap<String,Section<GroovyCITestType>>();
		//a collection containing all wiki-articles
		Collection<KnowWEArticle> allWikiArticles = KnowWEEnvironment.getInstance().
						getArticleManager(this.get(WEB_KEY)).getArticles();
		//iterate over all articles
		for(KnowWEArticle article : allWikiArticles){
			List<Section<GroovyCITestType>> sectionsList = new LinkedList<Section<GroovyCITestType>>();
			//find all GroovyCITestType sections on this article...
			article.getSection().findSuccessorsOfType(GroovyCITestType.class, sectionsList);
			//...and add them to our Map
			for(Section<GroovyCITestType> section : sectionsList){
				//a GroovyCITest is uniquely identified by its name-annotation
				String testName = DefaultMarkupType.getAnnotation(section, "name");
				sectionsMap.put(testName, section);
			}
		}
		return sectionsMap;
	}
}
