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

package de.d3web.we.ci4ke.groovy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import groovy.lang.Script;
import de.d3web.we.ci4ke.handling.CIConfiguration;
import de.d3web.we.ci4ke.handling.CITest;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xcl.XCLRelation;
import de.d3web.we.kdom.xcl.XCList;

/**
 * This abstract class serves as base class for implementing a Groovy CITest 
 * directly in the wiki. There, you have to override the execute(CIConfiguration) method.
 *  
 * @author Marc-Oliver Ochlast
 */
public abstract class AbstractCITestScript extends Script implements CITest {

	public Collection<KnowWEArticle> getAllArticles(CIConfiguration config){
		return KnowWEEnvironment.getInstance().
				getArticleManager(config.get(CIConfiguration.WEB_KEY)).getArticles();
	}
	
	//just for documentation purposes
	public KnowWEArticle getArticle(CIConfiguration config){
		return config.getMonitoredArticle();
	}
	
	public static List<String> findXCListsWithLessThenXRelations(KnowWEArticle article, int limitRelations){
		List<String> sectionIDs = new ArrayList<String>();
		
		List<Section<XCList>> found = new ArrayList<Section<XCList>>();
		article.getSection().findSuccessorsOfType(XCList.class, found);
		
		for(Section<XCList> xclSection : found){
			List<Section<XCLRelation>> relations = new ArrayList<Section<XCLRelation>>();
			xclSection.findSuccessorsOfType(XCLRelation.class, relations);
			if(relations.size() < limitRelations)
				sectionIDs.add(xclSection.getId());
		}		
		return sectionIDs;
	}
}
