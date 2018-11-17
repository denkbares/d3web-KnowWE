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

package de.knowwe.core.kdom;

import java.util.Collection;
import java.util.List;

import de.knowwe.core.kdom.parsing.Parser;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.MessageRenderer;

/**
 * @author Jochen
 * 
 *         This interface is the foundation of the KnowWE2 Knowledge-DOM
 *         type-system. To every node in this dom tree exactly one Type is
 *         associated.
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
public interface Type {

	/**
	 * Initializes the type instance with a specific path to the root. Each type
	 * is initialized only once. If the type is available in multiple path the
	 * specified path is the breaths-first-path which can be used to access the
	 * type. Especially for recursive types this is also the most shallow path
	 * available. This method is called directly after the object has been
	 * initialized through the available decorating plugins, e.g. child types,
	 * subtree handlers, etc.
	 * 
	 * @created 28.10.2013
	 * @param path the path to this type
	 */
	void init(Type[] path);

	/*
	 * Methods related to parsing
	 */

	/**
	 * Returns the parser that can be used to parse the textual markup of this
	 * type into a section of this type. The parser is responsible to build the
	 * full kdom tree for the specified text.
	 * 
	 * @created 12.03.2011
	 * @return the parser to be used to parse textual markup of this type
	 */
	Parser getParser();

	/**
	 * Adds the type as a child of the current type with the given priority
	 * value. If there are already child types for the given priority it is
	 * appended at the end of the list (lower priority).
	 * 
	 * @created 27.08.2013
	 * @param priority the priority with which the type is added
	 * @param type the type to add
	 */
	void addChildType(double priority, Type type);

	/**
	 * Adds the type as the (currently) last child type with the default
	 * priority (which is 5).
	 * 
	 * @created 27.08.2013
	 * @param type the type to add
	 */
	void addChildType(Type type);

	/**
	 * Replaces the first type with the passed class where the type is instance
	 * of the passed class, if such is existing.
	 * 
	 * @created 27.08.2013
	 * @param typeToBeReplace class to determine what type should be replaced
	 * @param newType type to be inserted
	 * @return the replaced type or null, if the type to replace was not found
	 */
	Type replaceChildType(Class<? extends Type> typeToBeReplace, Type newType);

	/**
	 * Clears the list of children for this type.
	 * 
	 * @created 27.08.2013
	 */
	void clearChildrenTypes();

	/**
	 * @return name of this type
	 */
	String getName();

	/**
	 * A (priority-ordered) list of the types, which are allowed as children of
	 * nodes of this type
	 */
	List<Type> getChildrenTypes();

	/*
	 * Management of Renderers
	 */

	/**
	 * When KnowWE renders the article this renderer is used to render this
	 * node. In most cases rendering should be delegated to children types.
	 * 
	 * @created 27.08.2013
	 * @return
	 */
	Renderer getRenderer();

	/**
	 * Returns the message renderer for the specified message type. The method
	 * may return null if the messages shall not be rendered at all.
	 * 
	 * @created 29.10.2013
	 * @param messageType the message type to be rendered
	 * @return the message renderer to be used
	 */
	MessageRenderer getMessageRenderer(Message.Type messageType);

	/**
	 * An unordered collection of the types, which are the parents of this type.
	 * 
	 * @return the Collection of parent types
	 */
	Collection<Type> getParentTypes();
}
