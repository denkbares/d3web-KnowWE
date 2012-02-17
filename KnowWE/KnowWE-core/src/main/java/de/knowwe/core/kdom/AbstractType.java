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
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.parsing.Parser;
import de.knowwe.core.kdom.parsing.Sectionizable;
import de.knowwe.core.kdom.parsing.Sectionizer;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.KnowWERenderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.DefaultErrorRenderer;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.plugin.Plugins;

public abstract class AbstractType implements Type, Sectionizable {

	/**
	 * the children types of the type. Used to serve the getAllowedChildrenTypes
	 * of the Type interface
	 * 
	 * @see Type#getAllowedChildrenTypes()
	 * 
	 */
	protected List<Type> childrenTypes = new ArrayList<Type>();

	/**
	 * Manages the subtreeHandlers which are registered to this type
	 * 
	 * @see SubtreeHandler
	 */
	private final TreeMap<Priority, List<SubtreeHandler<? extends Type>>> subtreeHandler = new TreeMap<Priority, List<SubtreeHandler<? extends Type>>>();

	/**
	 * SubtreeHandlers are plugged lazy, because the PluginManager is not
	 * initialized on startup.
	 */
	private boolean subtreeHandlersPlugged = false;

	/**
	 * types can be activated and deactivated in KnowWE this field is holding
	 * the current state
	 */
	protected boolean isActivated = true;

	/**
	 * determines whether this type is sectionized after creating the knowledge
	 * or before as all other types
	 * 
	 * @see AbstractType#isPostBuildSectionizing()
	 */
	protected boolean postBuildSectionizing = false;

	/**
	 * a flag for the updating mechanism, which manages translations to explicit
	 * knowledge formats
	 */
	private boolean isNotRecyclable = false;

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
	protected boolean isNumberedType = false;

	/**
	 * a flag to allow or disallow global types for this type.
	 */
	protected boolean allowesGlobalTypes = true;

	public boolean isNumberedType() {
		return isNumberedType;
	}

	public void setNumberedType(boolean isNumberedType) {
		this.isNumberedType = isNumberedType;
	}

	/**
	 * The sectionFinder of this type, used to serve the getSectionFinder-method
	 * of the Type interface
	 * 
	 * @see Type#getSectionFinder()
	 */
	protected SectionFinder sectionFinder;

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
	 * allows to set a custom renderer for a type (at initialization) if a
	 * custom renderer is set, it is used to present the type content in the
	 * wiki view
	 */
	private KnowWERenderer<?> renderer = DelegateRenderer.getInstance();

	/**
	 * constructor calling init() which is abstract
	 * 
	 */
	public AbstractType() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("KnowWE_config");
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

	@SuppressWarnings("unchecked")
	private void addPluggedSubtreeHandlers() {
		if (!PluginManager.isInitialized()) return;
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_SubtreeHandler);
		for (Extension e : extensions) {
			if (e.getParameter("scope").equals(this.getClass().getName())) {
				@SuppressWarnings("rawtypes")
				SubtreeHandler handler = (SubtreeHandler) e.getSingleton();
				int priorityValue = Integer.parseInt(e.getParameter("handlerpriority"));
				Priority priority = Priority.getPriority(priorityValue);
				addSubtreeHandler(priority, handler);
			}
		}
	}

	public AbstractType(SectionFinder sectionFinder) {
		this();
		this.sectionFinder = sectionFinder;
	}

	/**
	 * Returns the list of the registered ReviseSubtreeHandlers
	 * 
	 * @return list of handlers
	 */
	@Override
	public final TreeMap<Priority, List<SubtreeHandler<? extends Type>>> getSubtreeHandlers() {
		if (!subtreeHandlersPlugged) {
			subtreeHandlersPlugged = true;
			addPluggedSubtreeHandlers();
		}
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
	public final void addSubtreeHandler(Priority p, SubtreeHandler<? extends Type> handler) {
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

	public void replaceChildType(Type type,
			Class<? extends Type> c)
			throws InvalidKDOMSchemaModificationOperation {
		if (c.isAssignableFrom(type.getClass())) {
			Type toReplace = null;
			for (Type child : childrenTypes) {
				if (child.getClass().equals(c)) {
					toReplace = child;
				}
			}
			childrenTypes.set(childrenTypes.indexOf(toReplace), type);

		}
		else {
			throw new InvalidKDOMSchemaModificationOperation("class"
					+ c.toString() + " may not be replaced by: "
					+ type.getClass().toString()
					+ " since it isn't a subclass of former");
		}

	}

	//
	// /**
	// * Stores a list of messages under to message-store-key
	// *
	// * @param article is the article, the message is getting stored for. Be
	// * aware, that this is not automatically the article the section is
	// * directly linked to (because this Section might be included), but
	// * the article that is calling this, for example while revising.
	// * @param s
	// * @param messages
	// */
	// public static void storeMessages(KnowWEArticle article, Section<? extends
	// Type> s,
	// List<Message> messages) {
	// KnowWEUtils.storeSectionInfo(article.getWeb(), article
	// .getTitle(), s.getId(), MESSAGES_STORE_KEY, messages);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#deactivateType()
	 */
	@Override
	public void deactivateType() {
		isActivated = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#activateType()
	 */
	@Override
	public void activateType() {
		isActivated = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#getActivationStatus()
	 */
	@Override
	public boolean getActivationStatus() {
		return isActivated;
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

	// /**
	// * use normal constructor!
	// */
	// @Deprecated
	// protected void init() {
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Sectionizable#getSectioFinder()
	 */
	@Override
	public final SectionFinder getSectionFinder() {
		if (isActivated) {
			return sectionFinder;
		}
		return null;
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
	public List<Type> getAllowedChildrenTypes() {
		return Collections.unmodifiableList(childrenTypes);
	}

	public void clearAllowedChildren() {
		this.childrenTypes = new ArrayList<Type>();
	}

	public void clearSubtreeHandlers() {
		this.subtreeHandler.clear();
	}

	public boolean addChildType(Type t) {
		return this.childrenTypes.add(t);
	}

	public boolean removeChildType(Type t) {
		return this.childrenTypes.remove(t);
	}

	public Type removeChild(int i) {
		return this.childrenTypes.remove(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEType#getRenderer()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public final KnowWERenderer getRenderer() {
		return renderer;
	}

	@Override
	public MessageRenderer getErrorRenderer() {
		return DefaultErrorRenderer.INSTANCE_ERROR;
	}

	@Override
	public MessageRenderer getNoticeRenderer() {
		return DefaultErrorRenderer.INSTANCE_NOTE;
	}

	@Override
	public MessageRenderer getWarningRenderer() {
		return DefaultErrorRenderer.INSTANCE_WARNING;
	}

	/**
	 * Allows to set a custom renderer for this type
	 * 
	 * @param renderer
	 */
	public void setRenderer(@SuppressWarnings("rawtypes") KnowWERenderer renderer) {
		this.renderer = renderer;
	}

	// /**
	// * This enables a second sectionizing stage. Normally all Sections get
	// * sectionized at the start of building an article and after that, the
	// * knowledge gets created by the SubtreeHandlers.
	// * <p />
	// * If this returns true, the Sections of this ObjectType don't get
	// * sectionized further until after creating the knowledge (from other
	// * Sections). Therefore it is possible to use all the stuff created by the
	// * SubtreeHandlers to sectionze this Section.
	// *
	// * @created 14.06.2010
	// */
	// public boolean isPostBuildSectionizing() {
	// return this.postBuildSectionizing;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEType#isAssignableFromType(java.lang.Class)
	 */
	@Override
	public boolean isAssignableFromType(Class<? extends Type> clazz) {
		return clazz.isAssignableFrom(this.getClass());
	}

	@Override
	public boolean allowesGlobalTypes() {
		return this.allowesGlobalTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEType#isType(java.lang.Class)
	 */
	@Override
	public boolean isType(Class<? extends Type> clazz) {
		return clazz.equals(this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#isLeafType()
	 */
	@Override
	public boolean isLeafType() {
		List<Type> types = getAllowedChildrenTypes();
		return types == null || types.size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#isNotRecyclable()
	 */
	@Override
	public boolean isNotRecyclable() {
		return isNotRecyclable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.Type#setNotRecyclable(boolean)
	 */
	@Override
	public final void setNotRecyclable(boolean notRecyclable) {
		this.isNotRecyclable = notRecyclable;
		for (Type type : childrenTypes) {
			type.setNotRecyclable(notRecyclable);
		}

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
