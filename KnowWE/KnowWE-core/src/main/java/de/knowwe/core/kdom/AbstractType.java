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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.parsing.Parser;
import de.knowwe.core.kdom.parsing.Sectionizable;
import de.knowwe.core.kdom.parsing.Sectionizer;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.DefaultMessageRenderer;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.core.utils.KnowWEUtils;

public abstract class AbstractType implements Type, Sectionizable {

	/**
	 * the children types of the type. Used to serve the getAllowedChildrenTypes
	 * of the Type interface
	 * 
	 * @see Type#getChildrenTypes()
	 * 
	 */
	private final TypePriorityList childrenTypes = new TypePriorityList();

	/**
	 * Manages the subtreeHandlers which are registered to this type
	 * 
	 * @see SubtreeHandler
	 */
	private final TreeMap<Priority, List<SubtreeHandler<? extends Type>>> subtreeHandler = new TreeMap<Priority, List<SubtreeHandler<? extends Type>>>();

	/**
	 * a flag to show, that this ObjectType is sensitive to the order its
	 * Sections appears in the article
	 */
	private boolean isOrderSensitive = false;

	/**
	 * a flag to determine if SubtreeHandlers registered to this type should
	 * ignore package compile
	 */
	private boolean ignorePackageCompile = false;

	/**
	 * determines whether there is a enumeration of siblings defined for this
	 * type (e.g., to add line numbers to line-based sections)
	 */
	private boolean isNumberedType = false;

	/**
	 * allows to set a custom renderer for a type (at initialization) if a
	 * custom renderer is set, it is used to present the type content in the
	 * wiki view
	 */
	private Renderer renderer = DelegateRenderer.getInstance();

	/**
	 * The sectionFinder of this type, used to serve the getSectionFinder-method
	 * of the Type interface
	 * 
	 * @see Type#getSectionFinder()
	 */
	private SectionFinder sectionFinder;

	public boolean isNumberedType() {
		return isNumberedType;
	}

	public void setNumberedType(boolean isNumberedType) {
		this.isNumberedType = isNumberedType;
	}

	/**
	 * Allows to set a specific sectionFinder for this type
	 * 
	 * @param sectionFinder
	 */
	@Override
	public void setSectionFinder(SectionFinder sectionFinder) {
		this.sectionFinder = sectionFinder;
	}

	/**
	 * constructor calling init() which is abstract
	 * 
	 */
	public AbstractType() {
		ResourceBundle resourceBundle = KnowWEUtils.getConfigBundle();
		String ignoreFlag = "packaging.ignorePackages";
		if (resourceBundle.containsKey(ignoreFlag)) {
			if (resourceBundle.getString(ignoreFlag).contains("true")) {
				this.ignorePackageCompile = true;
			}
			if (resourceBundle.getString(ignoreFlag).contains("false")) {
				this.ignorePackageCompile = false;
			}
		}
	}

	public AbstractType(SectionFinder sectionFinder) {
		this();
		this.setSectionFinder(sectionFinder);
	}

	/**
	 * Returns the list of the registered ReviseSubtreeHandlers
	 * 
	 * @return list of handlers
	 */
	@Override
	public final TreeMap<Priority, List<SubtreeHandler<? extends Type>>> getSubtreeHandlers() {
		return subtreeHandler;
	}

	@Override
	public final List<SubtreeHandler<? extends Type>> getSubtreeHandlers(Priority p) {
		List<SubtreeHandler<? extends Type>> handlers = getSubtreeHandlers().get(p);
		if (handlers == null) {
			handlers = new ArrayList<SubtreeHandler<? extends Type>>();
			subtreeHandler.put(p, handlers);
		}
		return handlers;
	}

	/**
	 * Registers the given SubtreeHandlers with the given Priority.
	 */
	@Override
	public void addSubtreeHandler(Priority p, SubtreeHandler<? extends Type> handler) {
		if (p == null) p = Priority.DEFAULT;
		addSubtreeHandler(-1, p, handler);
	}

	/**
	 * Registers the given SubtreeHandlers at position <tt>pos</tt> in the List
	 * of SubtreeHandlers of the given Priority.
	 */
	public final void addSubtreeHandler(int pos, Priority p, SubtreeHandler<? extends Type> handler) {
		if (pos < 0) {
			getSubtreeHandlers(p).add(handler);
		}
		else {
			getSubtreeHandlers(p).add(pos, handler);
		}
	}

	/**
	 * Registers the given SubtreeHandlers with Priority.DEFAULT.
	 */
	public void addSubtreeHandler(SubtreeHandler<? extends Type> handler) {
		addSubtreeHandler(null, handler);
	}

	@Override
	public boolean replaceChildType(Type type, Class<? extends Type> c) {
		return childrenTypes.replaceType(type, c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Sectionizable#getSectioFinder()
	 */
	@Override
	public final SectionFinder getSectionFinder() {
		return sectionFinder;
	}

	@Override
	public Parser getParser() {
		return new Sectionizer(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#getAllowedChildrenTypes()
	 */
	@Override
	public final List<Type> getChildrenTypes() {
		return childrenTypes.getTypes();
	}

	@Override
	public void clearChildrenTypes() {
		this.childrenTypes.clear();
	}

	public void clearSubtreeHandlers() {
		this.subtreeHandler.clear();
	}

	public void removeSubtreeHandler(Class<? extends SubtreeHandler<? extends Type>> clazz) {

		List<Priority> toRemovePriority = new LinkedList<Priority>();

		for (Entry<Priority, List<SubtreeHandler<? extends Type>>> entry : subtreeHandler.entrySet()) {

			List<SubtreeHandler<? extends Type>> toRemove = new LinkedList<SubtreeHandler<? extends Type>>();

			for (SubtreeHandler<? extends Type> subtreeHandler : entry.getValue()) {
				if (subtreeHandler.getClass().isAssignableFrom(clazz)) {
					toRemove.add(subtreeHandler);
				}

			}
			entry.getValue().removeAll(toRemove);
			if (entry.getValue().isEmpty()) {
				toRemovePriority.add(entry.getKey());
			}

		}
		subtreeHandler.keySet().removeAll(toRemovePriority);
	}

	@Override
	public void addChildType(double priority, Type type) {
		childrenTypes.addType(priority, type);
	}

	@Override
	public void addChildType(Type type) {
		this.childrenTypes.addType(type);
	}

	public void removeChildType(Class<? extends Type> typeClass) {
		this.childrenTypes.removeType(typeClass);
	}

	/**
	 * Adds the given type at the end of the (current) children priority chain.
	 * 
	 * @created 27.08.2013
	 * @param type the type to add
	 */
	public void addChildTypeLast(Type type) {
		childrenTypes.addLast(type);
	}

	@Override
	public final Renderer getRenderer() {
		return renderer;
	}

	@Override
	public MessageRenderer getErrorRenderer() {
		return DefaultMessageRenderer.ERROR_RENDERER;
	}

	@Override
	public MessageRenderer getNoticeRenderer() {
		return DefaultMessageRenderer.NOTE_RENDERER;
	}

	@Override
	public MessageRenderer getWarningRenderer() {
		return DefaultMessageRenderer.WARNING_RENDERER;
	}

	/**
	 * Allows to set a renderer for this type
	 */
	@Override
	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void init(Type[] path) {
		// do nothing here for default
	}

	@Override
	public boolean isOrderSensitive() {
		return isOrderSensitive;
	}

	@Override
	public void setOrderSensitive(boolean orderSensitive) {
		this.isOrderSensitive = orderSensitive;
	}

	/**
	 * If a type ignores package compile, all SubtreeHandlers registered to this
	 * type will always compile, but only for the article the section is
	 * directly hooked in.
	 */
	@Override
	public boolean isIgnoringPackageCompile() {
		return ignorePackageCompile;
	}

	/**
	 * If a type ignores package compile, all SubtreeHandlers registered to this
	 * type will always compile, but only for the article the section is
	 * directly hooked in.
	 */
	@Override
	public void setIgnorePackageCompile(boolean ignorePackageCompile) {
		this.ignorePackageCompile = ignorePackageCompile;
	}

}
