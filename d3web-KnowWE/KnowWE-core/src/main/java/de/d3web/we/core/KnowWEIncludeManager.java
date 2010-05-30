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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.RootType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionID;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.IncludeAddress;
import de.d3web.we.kdom.include.IncludeErrorSection;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;

/**
 * This class manages all Includes.
 * Keeps tack and links them to their target Sections. 
 * 
 * @author astriffler
 *
 */
public class KnowWEIncludeManager {
	
	private final String web;

	/**
	 * This map stores for every Include the Section they are including. Key is
	 * the Include Section, value the included Section.
	 */
	private final Map<Section<Include>, List<Section<? extends KnowWEObjectType>>> src2targets
			= new HashMap<Section<Include>, List<Section<? extends KnowWEObjectType>>>();

	/**
	 * This map stores for every Include the last Section they were including,
	 * if the target of the Include changes. Key is the Include Section, value
	 * the last included Section.
	 */
	private final Map<Section<Include>, List<Section<? extends KnowWEObjectType>>> src2lastTargets = new HashMap<Section<Include>, List<Section<? extends KnowWEObjectType>>>();

	/**
	 * This map stores for every title a set of Includes that include Sections
	 * from the article with the given title.
	 */
	private final Map<String, Set<Section<Include>>> targetArticle2src = new HashMap<String, Set<Section<Include>>>();
	
	/**
	 * List that keeps track of all articles that are sectionizing at the moment.
	 */
	private final Set<String> sectionizingArticles = new HashSet<String>();
	
	public String getWeb() {
		return web;
	}
	
	public KnowWEIncludeManager(String web) {
		this.web = web;
	}
	
	/**
	 * Registers the Include Section to the IncludeManager and links them to their target.
	 */
	public void registerInclude(Section<Include> src) {
		
		if (src.getObjectType() instanceof Include) {
			
			IncludeAddress address = src.getIncludeAddress();
			
			// this is the Section the Include Section wants to include
			List<Section<? extends KnowWEObjectType>> target = 
					new ArrayList<Section<? extends KnowWEObjectType>>();
			
			if (address == null) {
				target.add(getNoValidAddressErrorSection(src));
				
			} else if (sectionizingArticles.contains(address.getTargetArticle())) {
				// check for include loops
				// (This algorithm later initializes articles, if they are not
				// yet build but the Include Sections wants to include from
				// it. If these initializing Articles directly or indirectly include
				// this article, which isn't completely build itself, we got a loop.)
				target.add(getIncludeLoopErrorSection(src));
				
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
									KnowWEEnvironment.getInstance().getRootType(), web);
							KnowWEEnvironment.getInstance()
									.getArticleManager(web)
									.saveUpdatedArticle(art);
						}
					}
				}
		
				if (art != null) {
					target = findTargets(art, src);
				} else {
					target.add(new IncludeErrorSection("Error: Article '" + address.getTargetArticle()
							+ "' not found.", src, src.getArticle()));
				}
			}
			
			// add Include Section and target Section to their maps
			if (address != null) {
				getIncludingSectionsForArticle(address.getTargetArticle()).add(src);
			}
			src2targets.put(src, target);
		}
	}
	
	/**
	 * Finds the target of the Include in the given Article
	 */
	private List<Section<? extends KnowWEObjectType>> findTargets(KnowWEArticle art, Section<Include> includeSec) {
		
		IncludeAddress address = includeSec.getIncludeAddress();
		List<Section<? extends KnowWEObjectType>> targets = new ArrayList<Section<? extends KnowWEObjectType>>();
		
		if (address == null) {
			targets.add(getNoValidAddressErrorSection(includeSec));
			return targets;
		}
		
		// search node in Article
		
		List<Section<? extends KnowWEObjectType>> matchingObjectTypeNameSections = new ArrayList<Section<? extends KnowWEObjectType>>();
		List<Section<? extends KnowWEObjectType>> matchingIdEndSections = new ArrayList<Section<? extends KnowWEObjectType>>();
		Section<? extends KnowWEObjectType> matchingIDSection = null;
		
		String typeName = address.isContentSectionTarget() ? address.getTargetSection().substring(0, 
				address.getTargetSection().indexOf(SectionID.CONTENT_SUFFIX.length())) 
				: address.getTargetSection();
		
		if (address.getTargetSection() != null) {
			for (Section<?> node : art.getAllNodesPreOrder()) {
				// if the complete ID is given
				if (node.getId().equalsIgnoreCase(address.getOriginalAddress())) {
					matchingIDSection = node;
					break;
				}
				// if only the last part of the ID is given
				if ((node.getId().length() > address.getTargetSection().length() 
						&& node.getId().substring(node.getId().length() - address.getTargetSection().length())
							.equalsIgnoreCase(address.getTargetSection()))) {
					matchingIdEndSections.add(node);
					if (!address.isWildcardSectionTarget() && matchingIdEndSections.size() > 1) {
						break;
					}
				}
				// or the ObjectType
				if (node.getObjectType().getClass().getSimpleName()
							.compareToIgnoreCase(typeName) == 0) {
					matchingObjectTypeNameSections.add(node);
					if (!address.isWildcardSectionTarget() && matchingObjectTypeNameSections.size() > 1) {
						break;
					}
				}
			}
		}
		
		// check the Lists if matching Sections were found
		if (matchingIDSection != null) {
			targets.add(matchingIDSection);
			
		}  else if (!address.isWildcardSectionTarget() 
				&& (matchingObjectTypeNameSections.size() > 1 || matchingIdEndSections.size() > 1)) {
			targets.add(new IncludeErrorSection("Error: Include '"
					+ address.getOriginalAddress() + "' is ambiguous. Try IDs.", 
					includeSec, includeSec.getArticle()));
			
		} else if (!matchingObjectTypeNameSections.isEmpty()) {
			
			for (Section<? extends KnowWEObjectType> locatedNode:matchingObjectTypeNameSections) {
				// get XMLContent if necessary
				if (address.isContentSectionTarget() 
						&& !(locatedNode.getObjectType() instanceof XMLContent)) {
					if (locatedNode.getObjectType() instanceof AbstractXMLObjectType
							&& locatedNode.findChildOfType(XMLContent.class) != null) {
						targets.add(locatedNode.findChildOfType(XMLContent.class));
					} else {
						targets.add(new IncludeErrorSection("Error: No content Section found for Include '"
								+ address.getOriginalAddress() + "'.", includeSec, includeSec.getArticle()));
					}
				} else {
					targets.add(locatedNode);
				}
			}
			
		} else if (!matchingIdEndSections.isEmpty()) {
			targets.addAll(matchingIdEndSections);
			
		} else if (address.getTargetSection() == null) {
			Section<? extends RootType> root = art.getSection().findChildOfType(RootType.class);
			if (root != null) {
				List<Section<? extends KnowWEObjectType>> children = root.getChildren();
				if (!children.isEmpty()) {
					
					// Checks the given List of children if it contains Sections that
					// were already included in the article in another place.
					Set<Section<? extends KnowWEObjectType>> candidates 
						= new HashSet<Section<? extends KnowWEObjectType>>(children);

					List<Section<? extends KnowWEObjectType>> potentialDuplicates =
						new ArrayList<Section<? extends KnowWEObjectType>>();
				
					for (Section<Include> inc:includeSec.getArticle().getIncludeSections()) {
						if (inc != includeSec) {
							inc.getAllNodesPreOrderToDepth(potentialDuplicates, 2);
						}
					}
					
					for (Section<? extends KnowWEObjectType> pd:potentialDuplicates) {
						// Found duplicates get removed.
						candidates.remove(pd);
					}

					if (candidates.isEmpty()) {
						targets.add(new IncludeErrorSection("Error: All Sections at the address '" 
								+ address.getOriginalAddress() + "' are already added in other Includes.",
								includeSec, includeSec.getArticle()));
					} else {
						// restore order
						for (Section<? extends KnowWEObjectType> sec:children) {
							if (candidates.contains(sec)) {
								targets.add(sec);
							}
						}
					}
				}
			}
			
		} else {
			targets.add(new IncludeErrorSection("Error: Include '" + includeSec.getOriginalText() 
					+ "' not found.", includeSec, includeSec.getArticle()));
		}
		// check if the included Section originates from the requesting article,
		// but isn't directly included from it -> causes update loops
		// (auto includes are allowed, but not via other articles)
		boolean loop = false;
		for (Section<? extends KnowWEObjectType> tar:targets) {
			if (!(tar instanceof IncludeErrorSection)
					&& !address.getTargetArticle().equals(includeSec.getTitle())
					&& tar.getTitle().equals(includeSec.getTitle())) {
				loop = true;
				break;
			}
		}
		if (loop) {
			targets.clear();
			targets.add(getIncludeLoopErrorSection(includeSec));
		}
		return targets;
	}

	private IncludeErrorSection getNoValidAddressErrorSection(Section<Include> src) {
		return new IncludeErrorSection("Error: No valid address found in '" + 
				src.getOriginalText().trim() + "'.", src, src.getArticle());
	}
	
	private IncludeErrorSection getIncludeLoopErrorSection(Section<Include> src) {
		return new IncludeErrorSection("Error: Include loop detected!", 
				src, src.getArticle());
	}
	
	/**
	 * Updates Includes to the given <tt>article</tt>.
	 * This method needs to get called after an article has changed.
	 */
	public void updateIncludesToArticle(KnowWEArticle article) {
		Set<KnowWEArticle> reviseArticles = new HashSet<KnowWEArticle>();
		Set<Section<Include>> includes = new HashSet<Section<Include>>(getIncludingSectionsForArticle(article.getTitle()));
		for (Section<Include> inc:includes) {
			// check if the target of the Include Section has changed
			List<Section<? extends KnowWEObjectType>> targets = findTargets(article, inc);
			List<Section<? extends KnowWEObjectType>> lastTargets = src2targets.get(inc);
			
			// if the target of an Include changes, the article needs to be
			// rebuild
			// if the target stays the same but contains an Include,
			// it is possible, that the target of that Include has changed and
			// therefore the article also needs to be rebuild
			boolean includeSuccessor = false;
			for (Section<? extends KnowWEObjectType> tar : targets) {
				if (tar.findSuccessor(Include.class) != null) {
					includeSuccessor = true;
					break;
				}
			}
			if (!targets.equals(lastTargets) || includeSuccessor) {
				if (lastTargets != null) {
					// since the target has changed, the including article doesn't 
					// reuse the last target
					for (Section<? extends KnowWEObjectType> lastTar:lastTargets) {
						lastTar.setReusedStateRecursively(inc.getTitle(), false);
					}
				}
				// overwrite the last target
				src2targets.put(inc, targets);
				// put the last target in the according map to make it available
				// for destruction of the stuff produced by its SubtreeHandlers
				src2lastTargets.put(inc, lastTargets);
				if (inc.getIncludeAddress() != null) {
					getIncludingSectionsForArticle(inc.getIncludeAddress().getTargetArticle()).add(inc);
				}
				// don't revise the article that is currently revised again
				// and don't revise if the originalText hasn't changed
				if (!inc.getTitle().equals(article.getTitle())) {
					if (targets.size() == lastTargets.size()) {
						for (int i = 0; i < targets.size(); i++) {
							if (!targets.get(i).getOriginalText().equals(lastTargets.get(i).getOriginalText())) {
								reviseArticles.add(inc.getArticle());
								break;
							}
						}
					} else {
						reviseArticles.add(inc.getArticle());
					}
				}
			}
		}
		// rebuild the articles
		// there will be no changes to the KDOM, but maybe 
		// changes to the Knowledge... the update mechanism will take care of that
		KnowWEEnvironment env = KnowWEEnvironment.getInstance();
		
		String updatingArticle;
		if (article.isUpdatingIncludes()) {
			updatingArticle = article.getArticleUpdatingIncludesOfThisArticle();
		}
		else {
			updatingArticle = article.getTitle();
		}
		for (KnowWEArticle ra : reviseArticles) {
			KnowWEArticle newArt = new KnowWEArticle(ra.getSection().getOriginalText(), ra.getTitle(), 
					env.getRootType(), web, updatingArticle, false);
			env.getArticleManager(web).saveUpdatedArticle(newArt);
		}
	}

	/**
	 * @returns the children respectively the target of the Include
	 */
	public List<Section<? extends KnowWEObjectType>> getChildrenForSection(Section<Include> src) {
		List<Section<? extends KnowWEObjectType>> children = src2targets.get(src);
		if (children == null) {
			children = new ArrayList<Section<? extends KnowWEObjectType>>();
		}
		if (children.isEmpty()) {
			children.add(new IncludeErrorSection("Section " + src.toString() 
					+ " is not registered as an including Section", src, src.getArticle()));
		}
		return children;
	}

	/**
	 * @returns the last children respectively the last target of the Include
	 */
	public List<Section<? extends KnowWEObjectType>> getLastChildrenForSection(Section<Include> src) {
		List<Section<? extends KnowWEObjectType>> children = src2lastTargets.get(src);
		if (children == null) {
			children = new ArrayList<Section<? extends KnowWEObjectType>>();
		}
		if (children.isEmpty()) {
			children.add(new IncludeErrorSection("Section " + src.toString()
					+ " is not registered as an including Section", src, src.getArticle()));
		}
		return children;
	}

	/**
	 * Gets all Sections that include from the given Article
	 */
	private Set<Section<Include>> getIncludingSectionsForArticle(String title) {
		Set<Section<Include>> includingSections = targetArticle2src.get(title);
		if (includingSections == null) {
			includingSections = new HashSet<Section<Include>>();
			targetArticle2src.put(title, includingSections);
		}
		return includingSections;
	}
	
	public void removeSectionizingArticles(String title) {
		this.sectionizingArticles.remove(title);
	}

	public void addSectionizingArticle(String title) {
		this.sectionizingArticles.add(title);
	}

	/**
	 * Returns all inactive Includes for the article with the given title. The
	 * calling of this method only makes sense after building the new KDOM but
	 * before removing the inactive Includes.
	 * 
	 * @created 29.05.2010
	 * @param title is the title of the article you want the inactive Includes
	 *        from.
	 * @param activeIncludes are the active includes of the article you want the
	 *        inactive Includes from. They are needed to calculate, which
	 *        Includes are inactive.
	 * @return a List with all inactive Includes for the article with the given
	 *         title. Returns an empty list, if there are no inactive Includes
	 *         still registered to the KnowWEIncludeManager.
	 */
	public List<Section<Include>> getInactiveIncludesForArticle(String title,
			Set<Section<Include>> activeIncludes) {
		List<Section<Include>> inactiveIncludes = new ArrayList<Section<Include>>();
		// get all registered Includes (from all articles)
		List<Section<Include>> allIncludes = new ArrayList<Section<Include>>(src2targets.keySet());

		for(Section<Include> inc:allIncludes) {
			// if an Include is from the article with the given title but not in 
			// the active Includes of this article, it is out of use and inactive
			if (inc.getTitle().equals(title) && !activeIncludes.contains(inc)) {
				inactiveIncludes.add(inc);
			}
		}
		return inactiveIncludes;
	}

	// /**
	// * Cleans the maps from Include Sections that are not longer in present in
	// * the article
	// */
	// public void removeInactiveIncludesForArticle(String title,
	// List<Section<Include>> activeIncludes) {
	// // get all registered Includes (from all articles)
	// List<Section> allIncludes = new ArrayList<Section>(src2target.keySet());
	//
	// for (Section inc : allIncludes) {
	// // if an Include is from the article with the given title but not in
	// // the active Includes of this article, it is out of use
	// if (inc.getTitle().equals(title) && !activeIncludes.contains(inc)) {
	// List<Section<? extends KnowWEObjectType>> targetSections =
	// src2target.get(inc);
	// // since the target has changed, the including article doesn't
	// // reuse the last target
	// for (Section<? extends KnowWEObjectType> tar : targetSections) {
	// tar.setReusedStateRecursively(inc.getTitle(), false);
	// }
	// // remove from map...
	// src2target.remove(inc);
	// // also delete the Include from the set of Includes of
	// // the target article
	// if (inc.getIncludeAddress() != null) {
	// getIncludingSectionsForArticle(inc.getIncludeAddress().getTargetArticle()).remove(
	// inc);
	// }
	// }
	// }
	// }

	/**
	 * Sets the reused states of Sections that were included before, but are not
	 * included any longer in the article with the given title to false. Of
	 * course both lists need to contain the inactive, respectively the active
	 * Includes of one and the same article, the article specified by the
	 * parameter <tt>title</tt>.
	 * 
	 * @created 29.05.2010
	 * @param title is the title of the article you want to set the reused
	 *        states of the inactive Includes false for.
	 * @param inactiveIncludes are the currently inactive Includes (you get them
	 *        with <tt>getInactiveIncludesForArticle(...)</tt> of the same
	 *        article you need the active Includes from.
	 * @param activeIncludes are the active Includes
	 */
	public void resetReusedStateOfInactiveIncludeTargets(String title,
			Collection<Section<Include>> inactiveIncludes,
			Collection<Section<Include>> activeIncludes) {

		Set<Section<?>> inactiveIncludeTargets = new HashSet<Section<?>>();
		Set<Section<?>> activeIncludeTargets = new HashSet<Section<?>>();

		for (Section<Include> inaInc : inactiveIncludes) {
			if (inaInc.getTitle().equals(title)) {
				inactiveIncludeTargets.addAll(src2targets.get(inaInc));
			}
		}
		for (Section<Include> aInc: activeIncludes) {
			if (aInc.getTitle().equals(title)) {
				activeIncludeTargets.addAll(src2targets.get(aInc));
			}
		}
		
		inactiveIncludeTargets.removeAll(activeIncludeTargets);
		
		for (Section<?> inaIncTar:inactiveIncludeTargets) {
			inaIncTar.setReusedStateRecursively(title, false);
		}
	}

	/**
	 * Unregisters, respectively removes, the given Includes from the
	 * KnowWEIncludeManager.
	 * 
	 * @created 29.05.2010
	 * @param includes are the Includes you want to unregister/remove.
	 */
	public void unregisterIncludes(Collection<Section<Include>> includes) {

		for (Section<Include> inc : includes) {
			// remove from maps...
			src2targets.remove(inc);
			src2lastTargets.remove(inc);
			// also delete the Include from the set of Includes of
			// the target article
			if (inc.getIncludeAddress() != null) {
				getIncludingSectionsForArticle(inc.getIncludeAddress().getTargetArticle()).remove(
						inc);
			}
		}

	}

}
