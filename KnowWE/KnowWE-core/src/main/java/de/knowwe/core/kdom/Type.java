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

import java.util.List;
import java.util.TreeMap;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.parsing.Parser;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
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
	 * Returns whether this type is a leaf type, i.e., true if it has no
	 * children types.
	 * 
	 * @created 27.08.2013
	 * @return
	 */
	boolean isLeafType();

	/**
	 * Returns the path from this type up to the root type as an array. First
	 * element is the root type, the last element is this.
	 * 
	 * @created 27.08.2013
	 * @return
	 */
	Type[] getPathToRoot();

	/**
	 * Set the the path from this type to the root type (for caching)
	 * 
	 * @created 27.08.2013
	 * @param path
	 */
	void setPathToRoot(Type[] path);

	/**
	 * Returns whether this.getClass() equals the given class.
	 * 
	 * @created 27.08.2013
	 * @param clazz
	 * @return
	 */
	boolean isType(Class<? extends Type> clazz);

	/**
	 * Returns whether the passed class isAssignableFrom the class of this
	 * instance.
	 * 
	 * @created 27.08.2013
	 * @param clazz
	 * @return
	 */
	boolean isAssignableFromType(Class<? extends Type> clazz);

	/**
	 * Returns the decorated flag, i.e. of this instance of the type already has
	 * been considered for decoration (add child types, renderers, handlers...)
	 * by the plugin framework.
	 * 
	 * @created 27.08.2013
	 * @return
	 */
	boolean isDecorated();

	/**
	 * Sets the decorated flag at the end of type hierarchy initialization by
	 * the plugin framework.
	 * 
	 * @created 27.08.2013
	 */
	void setDecorated();

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

	boolean isNotRecyclable();

	void setNotRecyclable(boolean notRecyclable);

	/*
	 * Management of children types
	 */
	/**
	 * Adds the type a child of this type with the given priority value. If
	 * there are already child types for the given priority it is appended at
	 * the end of the list (lower priority).
	 * 
	 * @created 27.08.2013
	 * @param priority
	 * @param t
	 */
	void addChildType(double i, Type t);

	/**
	 * Adds the type as a child of this type at the specified position in the
	 * priority chain.
	 * 
	 * NOTE: This position may change if other types are inserted into the chain
	 * afterwards. It is recommended to work with priorities, therefore use
	 * {@link Type#addChildType(double, Type)}
	 * 
	 * @created 27.08.2013
	 * @param pos
	 * @param t
	 */
	void addChildTypeAtPosition(int pos, Type t);

	/**
	 * Adds the type a child for this type with the default priority
	 * (considering all existing type with default priority - if existing - it
	 * is appended at the end, i.e., lower priority.)
	 * 
	 * 
	 * @created 27.08.2013
	 * @param t
	 */
	void addChildType(Type t);

	/**
	 * Replaces the first type with the passed type where the type is instance
	 * of the passed class, if such is existing.
	 * 
	 * @created 27.08.2013
	 * @param newType type to be inserted
	 * @param classToBeReplaced class to determine what type should be replaced
	 * @throws InvalidKDOMSchemaModificationOperation
	 * @return true if a replacement has been made
	 */
	boolean replaceChildType(Type type, Class<? extends Type> c) throws InvalidKDOMSchemaModificationOperation;

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
	 * 
	 * @return
	 */
	List<Type> getChildrenTypes();

	/**
	 * Returns the children types attached to this type. Use for plugin
	 * framework initialization only!
	 * 
	 * final!
	 * 
	 * @return
	 */
	List<Type> getChildrenTypesInit();

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

	void setRenderer(Renderer renderer);

	MessageRenderer getErrorRenderer();

	MessageRenderer getNoticeRenderer();

	MessageRenderer getWarningRenderer();

	/*
	 * Methods related to compilation
	 */
	boolean isOrderSensitive();

	boolean isIgnoringPackageCompile();

	void setIgnorePackageCompile(boolean ignorePackageCompile);

	void setOrderSensitive(boolean orderSensitive);

	/*
	 * Management of SubtreeHandlers
	 */
	TreeMap<Priority, List<SubtreeHandler<? extends Type>>> getSubtreeHandlers();

	List<SubtreeHandler<? extends Type>> getSubtreeHandlers(Priority p);

	void addSubtreeHandler(Priority p, SubtreeHandler<? extends Type> handler);

}
