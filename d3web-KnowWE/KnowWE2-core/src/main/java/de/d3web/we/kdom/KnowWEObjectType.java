package de.d3web.we.kdom;

import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;

/**
 * @author Jochen
 * 
 * This interface is the foundation of the KnowWE2 Knowledge-DOM type-system.
 * To every node in this dom tree exactly one KnowWEObjectType is associated.
 *
 * A type defines itself by its SectionFinder, which allocates text parts to this type.
 * @see getSectioner
 * 
 * Further it defines what subtypes it allows.
 * @see getAllowedChildrenTypes
 * 
 * For user presentation it provides a renderer.
 * @see getRenderer
 *
 */
public interface KnowWEObjectType {
	
	
	/**
	 * On tree creation this SectionFinder is used to create node of this type
	 * @return
	 */
	public abstract SectionFinder getSectioner();
	
	/**
	 * @return name of this type
	 */
	public abstract String getName();
	
	/**
	 * A (priority-ordered) list of the types, 
	 * which are allowed as children of nodes of this type
	 * 
	 * @return
	 */
	public abstract List<? extends KnowWEObjectType> getAllowedChildrenTypes();
	
	/**
	 * When KnowWE renders the article this renderer is used to render this node.
	 * In most cases rendering should be delegated to children types.
	 * @return
	 */
	public KnowWEDomRenderer getRenderer();
	
//	/**
//	 * When KnowWE renders the article this renderer is used to render this node.
//	 * In most cases rendering should be delegated to children types.
//	 * @return
//	 */
//	public KnowWEDomRenderer getRenderer(String user, String  topic);

	/**
	 * This method offers the possibility for a type to revise its subtree
	 * when its completed. Not necessary in most cases.
	 * 
	 * @param section
	 * @param kbm
	 */
	public abstract void reviseSubtree(Section section, KnowledgeRepresentationManager kbm, String web, KnowWEDomParseReport rep);
	
	public Collection<Section> getAllSectionsOfType();
	
	public IntermediateOwlObject getOwl(Section s);

	public abstract void findTypeInstances(Class clazz,
			List<KnowWEObjectType> instances);
	
	public void deactivateType();
	public void activateType();
	public boolean getActivationStatus();
	
	public void cleanStoredInfos(String articleName);
}
