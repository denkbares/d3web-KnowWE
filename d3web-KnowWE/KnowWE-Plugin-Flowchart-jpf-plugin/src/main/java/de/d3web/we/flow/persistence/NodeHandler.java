package de.d3web.we.flow.persistence;

import java.util.List;

import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.diaFlux.flow.INode;
import de.d3web.report.Message;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * 
 * @author Reinhard Hatko
 *
 */
public interface NodeHandler {
	
	/**
	 * Checks if this NodeHandler can create a node from the supplied section.
	 * 
	 * @param article the article which contains the section
	 * @param kbm the KBM of the article
	 * @param nodeSection the section of the node
	 * @return true, if this nodehandler can create a node
	 */
	boolean canCreateNode(KnowWEArticle article, KnowledgeBaseManagement kbm, Section nodeSection);
	
	/**
	 * Creates a node from the supplied section.
	 *  
	 * @param article
	 * @param kbm
	 * @param nodeSection
	 * @param flowSection 
	 * @param id id of the node to create
	 * @param errors 
	 * @return
	 */
	INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm, Section nodeSection, Section flowSection, String id, List<Message> errors);
	
	/**
	 * Returns the ObjectType of the NodeModel this handler handles.
	 * 
	 * @return 
	 */
	KnowWEObjectType getObjectType(); 
	

}
