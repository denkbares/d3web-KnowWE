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

package de.d3web.we.kdom;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import de.d3web.we.kdom.report.MessageRenderer;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * @author Jochen
 * 
 *         This interface is the foundation of the KnowWE2 Knowledge-DOM
 *         type-system. To every node in this dom tree exactly one
 *         KnowWEObjectType is associated.
 * 
 *         A type defines itself by its SectionFinder, which allocates text
 *         parts to this type.
 * @see getSectioner
 * 
 *      Further it defines what subtypes it allows.
 * @see getAllowedChildrenTypes
 * 
 *      For user presentation it provides a renderer.
 * @see getRenderer
 * 
 */
public interface KnowWEObjectType extends KnowWEType {

	/**
	 * On tree creation this SectionFinder is used to create node of this type
	 * 
	 * @return
	 */
	public abstract ISectionFinder getSectioner();

	/**
	 * @return name of this type
	 */
	public abstract String getName();

	/**
	 * A (priority-ordered) list of the types, which are allowed as children of
	 * nodes of this type
	 * 
	 * @return
	 */
	public abstract List<KnowWEObjectType> getAllowedChildrenTypes();

	/**
	 * This method offers the possibility for a type to revise its subtree when
	 * its completed. Not necessary in most cases.
	 * 
	 * @param section
	 * @param kbm
	 */
	// public void reviseSubtree(KnowWEArticle article, Section<? extends
	// KnowWEObjectType> section, Priority p);

	// public <T extends KnowWEObjectType> void reviseSubtree(KnowWEArticle
	// article, Section<T> section, SubtreeHandler<T> h);

	public Collection<Section> getAllSectionsOfType();

	public abstract void findTypeInstances(Class clazz,
			List<KnowWEObjectType> instances);

	public void deactivateType();

	public void activateType();

	public boolean getActivationStatus();

	public void cleanStoredInfos(String articleName);

	public MessageRenderer getErrorRenderer();

	public MessageRenderer getNoticeRenderer();

	public MessageRenderer getWarningRenderer();

	public boolean isLeafType();

	public boolean isNotRecyclable();

	public boolean allowesGlobalTypes();

	public void setNotRecyclable(boolean notRecyclable);

	public boolean isOrderSensitive();

	public void setOrderSensitive(boolean orderSensitive);

	public TreeMap<Priority, List<SubtreeHandler<? extends KnowWEObjectType>>> getSubtreeHandlers();

	public List<SubtreeHandler<? extends KnowWEObjectType>> getSubtreeHandlers(Priority p);
}
