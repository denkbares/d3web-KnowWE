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

package de.d3web.we.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionID;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.IncludeAddress;
import de.d3web.we.kdom.include.IncludeErrorSection;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;

/**
 * This class manages all Includes (not the old TextIncludes!).
 * Keeps tack and links them to their target Sections. 
 * 
 * @author astriffler
 *
 */
public class KnowWEIncludeManager {
	
	private String web;
	
	/**
	 * This map stores for every Include Section the Section they are including.
	 * Key is the Include Section, value the included Section.
	 */
	private Map<Section, Section> src2target = new HashMap<Section, Section>();
	
	/**
	 * This map stores for every title a set of Include Sections that include a
	 * Section from the article with the given title.
	 */
	private Map<String, Set<Section>> target2src = new HashMap<String, Set<Section>>();
	
	/**
	 * List that keeps track of all articles that are initializing at the moment.
	 */
	private List<String> initializingArticles = new ArrayList<String>();
	
	public String getWeb() {
		return web;
	}
	
	public KnowWEIncludeManager(String web) {
		this.web = web;
	}
	
	/**
	 * Registers the Include Section to the IncludeManager and links them to their target.
	 * 
	 * @returns whether the target Section has changed since the given Section 
 	 *  	got registered last time
	 */
	public boolean registerInclude(Section src) {
		
		if (src.getObjectType() instanceof Include) {
			
			IncludeAddress address = src.getIncludeAddress();
			
			// this is the Section the Include Section wants to include
			Section target;
			
			// check for include loops
			// (This algorithm later initializes articles, if they are not
			// yet build but the Include Sections wants to include from
			// it. If these initializing Articles directly or indirectly include
			// this article, which isn't completely build itself, we got a loop.)
			if (initializingArticles.contains(address.getTargetArticle())) {
				
				target = new IncludeErrorSection("Error: Expand loop dedected", 
						src, src.getArticle());
				
			} else {
				// no loops found then, get the targeted article
				KnowWEArticle art = KnowWEEnvironment.getInstance()
						.getArticle(KnowWEEnvironment.DEFAULT_WEB,
								address.getTargetArticle());

				if (art == null) {
					// check if the targeted article exists but is not yet build
					if (KnowWEEnvironment.getInstance().getWikiConnector()
							.doesPageExist(address.getTargetArticle())) {
						String artSrc = KnowWEEnvironment.getInstance()
								.getWikiConnector().getArticleSource(
										address.getTargetArticle());
						if (artSrc != null) {
							// build the targeted article
							art = new KnowWEArticle(artSrc, address.getTargetArticle(), 
									KnowWEEnvironment.getInstance().getRootTypes(), web);
							KnowWEEnvironment.getInstance()
									.getArticleManager(web)
									.saveUpdatedArticle(art);
						}
					}
				}
		
				if (art != null) {
					// search node in Article
					List<Section> allNodes = art.getAllNodesPreOrder();
					List<Section> matchingObjectTypeName = new ArrayList<Section>();
					List<Section> matchingIdEnd = new ArrayList<Section>();
					Section matchingID = null;
					
					String typeName = address.isContentSectionTarget() ? address.getTargetSection().substring(0, 
							address.getTargetSection().length() - SectionID.CONTENT_SUFFIX.length()) 
							: address.getTargetSection();
					
					for (Section node : allNodes) {
						// if the complete ID is given
						if (node.getId().equalsIgnoreCase(address.getOriginalAddress())) {
							matchingID = node;
							break;
						}
						// if only the last part of the ID is given
						if ((node.getId().length() > address.getTargetSection().length() 
								&& node.getId().substring(node.getId().length() - address.getTargetSection().length())
									.equalsIgnoreCase(address.getTargetSection()))) {
							matchingIdEnd.add(node);
							if (matchingIdEnd.size() > 1) {
								break;
							}
						}
						// or the ObjectType
						if (node.getObjectType().getClass().getSimpleName()
									.compareToIgnoreCase(typeName) == 0) {
							matchingObjectTypeName.add(node);
							if (matchingObjectTypeName.size() > 1) {
								break;
							}
						}
					}
					
					// check the Lists if matching Sections were found
					if (matchingID != null) {
						target = matchingID;
					} else if (matchingObjectTypeName.size() == 1) {
						Section locatedNode = matchingObjectTypeName.get(0);
						// get XMLContent if necessary
						if (address.isContentSectionTarget() 
								&& !(locatedNode.getObjectType() instanceof XMLContent)) {
							if (locatedNode.getObjectType() instanceof AbstractXMLObjectType
									&& locatedNode.findChildOfType(XMLContent.class) != null) {
								target = locatedNode.findChildOfType(XMLContent.class);
							} else {
								target = new IncludeErrorSection("Error: No content Section found for Include '"
										+ address.getOriginalAddress() + "'.", src, src.getArticle());
							}
						} else {
							target = locatedNode;
						}
					} else if (matchingIdEnd.size() == 1) {
						target = matchingIdEnd.get(0);
					} else if (matchingObjectTypeName.size() > 1 || matchingIdEnd.size() > 1) {
						target = new IncludeErrorSection("Error: Include '"
								+ address.getOriginalAddress() + "' is not unique. Try IDs.", 
								src, src.getArticle());
					} else {
						target = new IncludeErrorSection("Error: Include '"
								+ address.getOriginalAddress() + "' not found.", src, src.getArticle());
					}
					
				} else {
					target = new IncludeErrorSection("Error: Article '" + address.getTargetArticle()
							+ "' not found.", src, src.getArticle());
				}
			}
			
			// add Include Section and target Section to their maps
			getIncludingSectionsForArticle(address.getTargetArticle()).add(src);
			Section oldTarget = src2target.get(src);
			if (oldTarget == target) {
				return false;
			} else {
				src2target.put(src, target);
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * Updates Includes to the article with the given <code>title</title>.
	 * This method needs to get called after an article has changed.
	 */
	public void updateIncludesToArticle(String title) {
		Map<String, KnowWEArticle> reviseArticles = new HashMap<String, KnowWEArticle>();
		Set<Section> includes = new HashSet<Section>(getIncludingSectionsForArticle(title));
		for (Section inc:includes) {
			// check if the target of the Include Section has changed
			if (registerInclude(inc)) {
				reviseArticles.put(inc.getTitle(), inc.getArticle());
				Section target = src2target.get(inc);
				target.getArticle().getChangedSections().put(target.getId(), target);
				// since an included Section can get included by another Include
				// this needs to be recursive
				updateIncludesToArticle(inc.getTitle());
			}
		}
		
		// rebuild the article 
		// there will be no changes to the KDOM, but maybe 
		// changes to the Knowledge... the update mechanism will take care of that
		for (KnowWEArticle article:reviseArticles.values()) {
			KnowWEArticle newArt = new KnowWEArticle(article.getSection().getOriginalText(), article.getTitle(), 
					KnowWEEnvironment.getInstance().getRootTypes(), web);
			KnowWEEnvironment.getInstance().getArticleManager(web).saveUpdatedArticle(newArt);
		}
	}
	
	/**
	 * @returns the children respectively the target of the Include Section
	 */
	public List<Section> getChildrenForSection(Section src) {
		List<Section> children = new ArrayList<Section>();
		Section target = src2target.get(src);
		if (target != null) {
			children.add(target);
		} else {
			children.add(new IncludeErrorSection("Section " + src.toString() 
					+ " not registered as an including Section", src, src.getArticle()));
		}
		return children;
	}
	
	/**
	 * Gets all Sections that include from the given Article
	 */
	private Set<Section> getIncludingSectionsForArticle(String title) {
		Set<Section> includingSections = target2src.get(title);
		if (includingSections == null) {
			includingSections = new HashSet<Section>();
			target2src.put(title, includingSections);
		}
		return includingSections;
	}
	
	public List<String> getInitializingArticles() {
		return initializingArticles;
	}

	public void addInitializingArticle(String article) {
		this.initializingArticles.add(article);
	}

	/**
	 * Cleans the maps from Include Sections that are not longer
	 * in present in the article
	 */
	public void removeInactiveIncludesForArticle(String title) {
		// get all Includes that are in the article now
		Set<Section> activeIncludes = KnowWEEnvironment.getInstance().getArticle(web, title).getIncludeSections();
		// get all registered Includes (from all articles)
		List<Section> allIncludes = new ArrayList<Section>(src2target.keySet());
		
		for(Section inc:allIncludes) {
			// if an Include is from the article with the given title but not in 
			// the active Includes of this article, it is out of use
			if (inc.getTitle().equals(title) && !activeIncludes.contains(inc)) {
				String targetTitle = src2target.get(inc).getTitle();
				src2target.remove(inc);
				// also delete the Include from the set of Includes of
				// the target article
				getIncludingSectionsForArticle(targetTitle).remove(inc);
			}
		}
		
	}

}
