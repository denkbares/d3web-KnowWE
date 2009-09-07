package de.d3web.we.kdom;

import de.d3web.we.knowRep.KnowledgeRepresentationManager;

/**
 * Interface for a ReviseSubtreeHandler. This handler has to be registered at some type and then
 * after each time a section of this type is created this handler is called with that section and 
 * the subtree can be processed (e.g. translated to a target representation)
 * 
 * @author Jochen
 *
 */
public interface ReviseSubTreeHandler {
	
	public void reviseSubtree(Section s, KnowledgeRepresentationManager  kbm,
			String web, KnowWEDomParseReport report);

}
