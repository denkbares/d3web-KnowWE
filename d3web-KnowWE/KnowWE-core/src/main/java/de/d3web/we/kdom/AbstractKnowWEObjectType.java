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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import de.d3web.report.Message;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.DefaultErrorRenderer;
import de.d3web.we.kdom.report.DefaultNoticeRenderer;
import de.d3web.we.kdom.report.DefaultWarningRenderer;
import de.d3web.we.kdom.report.MessageRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class AbstractKnowWEObjectType implements KnowWEObjectType {

	/**
	 * This is just a (conventional) key under which messages can be stored (and
	 * then be found again) in the sectionStore
	 * 
	 */
	public static final String MESSAGES_STORE_KEY = "messages";

	/**
	 * the children types of the type. Used to serve the getAllowedChildrenTypes
	 * of the KnowWEObjectType interface
	 * 
	 * @see KnowWEObjectType#getAllowedChildrenTypes()
	 * 
	 */
	protected List<KnowWEObjectType> childrenTypes = new ArrayList<KnowWEObjectType>();

	// protected List<KnowWEObjectType> priorityChildrenTypes = new
	// ArrayList<KnowWEObjectType>();

	/**
	 * Manages the subtreeHandlers which are registered to this type
	 * 
	 * @see SubtreeHandler
	 */
	protected TreeMap<Priority, List<SubtreeHandler<? extends KnowWEObjectType>>> subtreeHandler = new TreeMap<Priority, List<SubtreeHandler<? extends KnowWEObjectType>>>();

	/**
	 * types can be activated and deactivated in KnowWE this field is holding
	 * the current state
	 */
	protected boolean isActivated = true;

	/**
	 * a flag for the updating mechanism, which manages translations to explicit
	 * knowledge formats
	 */
	private boolean isNotRecyclable = false;

	/**
	 * specifies whether there is a enumeration of siblings defined for this
	 * type (e.g., to add line numbers to line-based sections)
	 */
	protected boolean isNumberedType = false;

	public boolean isNumberedType() {
		return isNumberedType;
	}

	public void setNumberedType(boolean isNumberedType) {
		this.isNumberedType = isNumberedType;
	}

	/**
	 * The sectionFinder of this type, used to serve the getSectionFinder-method
	 * of the KnowWEObjectType interface
	 * 
	 * @see KnowWEObjectType#getSectioner()
	 */
	protected SectionFinder sectionFinder;

	/**
	 * Allows to set a specific sectionFinder for this type
	 * 
	 * @param sectionFinder
	 */
	public void setSectionFinder(SectionFinder sectionFinder) {
		this.sectionFinder = sectionFinder;
	}

	/**
	 * allows to set a custom renderer for a type (at initialization) if a
	 * custom renderer is set, it is used to present the type content in the
	 * wiki view
	 */
	protected KnowWEDomRenderer customRenderer = null;

	/**
	 * constructor calling init() which is abstract
	 * 
	 */
	public AbstractKnowWEObjectType() {
		// TODO: vb: this is dangerous behavior. Should be replaced. The objects
		// "init" method is called, before it is completely initialized by its
		// own constructor.
		init();
	}

	public AbstractKnowWEObjectType(SectionFinder sectionFinder) {
		this();
		this.sectionFinder = sectionFinder;
	}

	/**
	 * Returns the list of the registered ReviseSubtreeHandlers
	 * 
	 * @return list of handlers
	 */
	@Override
	public final TreeMap<Priority, List<SubtreeHandler<? extends KnowWEObjectType>>> getSubtreeHandlers() {
		return subtreeHandler;
	}

	public final List<SubtreeHandler<? extends KnowWEObjectType>> getSubtreeHandlers(Priority p) {
		List<SubtreeHandler<? extends KnowWEObjectType>> handlers = subtreeHandler.get(p);
		if (handlers == null) {
			handlers = new ArrayList<SubtreeHandler<? extends KnowWEObjectType>>();
			subtreeHandler.put(p, handlers);
		}
		return handlers;
	}

	/**
	 * Registers the given SubtreeHandlers with the given Priority.
	 */
	public final void addSubtreeHandler(Priority p, SubtreeHandler<? extends KnowWEObjectType> handler) {
		getSubtreeHandlers(p).add(handler);
	}

	/**
	 * Registers the given SubtreeHandlers at position <tt>pos</tt> in the List
	 * of SubtreeHandlers of the given Priority.
	 */
	public final void addSubtreeHandler(int pos, Priority p, SubtreeHandler<? extends KnowWEObjectType> handler) {
		getSubtreeHandlers(p).add(pos, handler);
	}

	/**
	 * Registers the given SubtreeHandlers with Priority.DEFAULT.
	 */
	public void addSubtreeHandler(SubtreeHandler<? extends KnowWEObjectType> handler) {
		getSubtreeHandlers(Priority.DEFAULT).add(handler);
	}

	/**
	 * A mechanism to help clean up old information, when a new version of a
	 * page is saved
	 * 
	 * @param articleName
	 * @param clearedTypes
	 */
	public void clearTypeStoreRecursivly(String articleName,
			Set<KnowWEType> clearedTypes) {
		cleanStoredInfos(articleName);
		clearedTypes.add(this);

		for (KnowWEObjectType type : childrenTypes) {
			if (type instanceof AbstractKnowWEObjectType
					&& !clearedTypes.contains(type)) {
				((AbstractKnowWEObjectType) type).clearTypeStoreRecursivly(
						articleName, clearedTypes);
			}
		}
	}

	/**
	 * @see KnowWEUtils#storeSingleMessage(KnowWEArticle, Section, Class, Class,
	 *      Object)
	 */
	public static void storeSingleMessage(KnowWEArticle article, Section<?> sec, Class<?> source, Message msg) {
		KnowWEUtils.storeSingleMessage(article, sec, source, Message.class, msg);
	}

	/**
	 * @see KnowWEUtils#clearMessages(KnowWEArticle, Section, Class, Class)
	 */
	public static void cleanMessages(KnowWEArticle article, Section<?> section, Class<?> source) {
		KnowWEUtils.clearMessages(article, section, source, Message.class);
	}

	/**
	 * @see KnowWEUtils#storeMessages(KnowWEArticle, Section, Class, Class,
	 *      Collection)
	 */
	public static void storeMessages(KnowWEArticle article, Section<?> section,
			Class<?> source, Collection<Message> messages) {
		KnowWEUtils.storeMessages(article, section, source, Message.class, messages);
	}

	/**
	 * @see KnowWEUtils#getMessages(KnowWEArticle, Section, Class)
	 */
	public static Collection<Message> getMessages(KnowWEArticle article, Section<?> section) {
		return KnowWEUtils.getMessages(article, section, Message.class);
	}

	/**
	 * @see KnowWEUtils#getMessages(KnowWEArticle, Section, Class, Class)
	 */
	public static Collection<Message> getMessages(KnowWEArticle article, Section<?> section, Class<?> source) {
		return KnowWEUtils.getMessages(article, section, source, Message.class);
	}

	/**
	 * @see KnowWEUtils#getMessagesFromSubtree(KnowWEArticle, Section, Class)
	 */
	public static Collection<Message> getMessagesFromSubtree(KnowWEArticle article, Section<?> section) {
		return KnowWEUtils.getMessagesFromSubtree(article, section, Message.class);
	}

	// /**
	// * Returns all the messages stored for this section put doesn't create a
	// new
	// * empty MessageList in the SectionStore if no MessageList is there yet.
	// * Returns <tt>null</tt> in this case.
	// *
	// * @param s
	// * @return
	// */
	// public static List<Message> getMessagesPassively(KnowWEArticle article,
	// Section s) {
	// Object o = KnowWEUtils.getStoredObject(
	// article.getWeb(), article.getTitle(), s.getId(),
	// MESSAGES_STORE_KEY);
	// if (o == null) {
	// return null;
	// }
	// else {
	// return toMessages(o, article, s);
	// }
	// }
	//
	// /**
	// * Returns all the messages stored for this section.
	// *
	// * @param s
	// * @return
	// */
	// public static List<Message> getMessages(KnowWEArticle article, Section s)
	// {
	// return toMessages(KnowWEUtils.getStoredObject(
	// article.getWeb(), article.getTitle(), s.getId(),
	// MESSAGES_STORE_KEY), article, s);
	// }
	//
	// private static List<Message> toMessages(Object o, KnowWEArticle article,
	// Section s) {
	// if (o instanceof List) {
	// return (List<Message>) o;
	// }
	// if (o == null) {
	// List<Message> msg = new ArrayList<Message>();
	// storeMessages(article, s, msg);
	// return msg;
	// }
	// return null;
	// }

	public void replaceChildType(KnowWEObjectType type,
			Class<? extends KnowWEObjectType> c)
			throws InvalidKDOMSchemaModificationOperation {
		if (c.isAssignableFrom(type.getClass())) {
			KnowWEObjectType toReplace = null;
			for (KnowWEObjectType child : childrenTypes) {
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
					+ " since it isnt a subclass of former");
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
	// KnowWEObjectType> s,
	// List<Message> messages) {
	// KnowWEUtils.storeSectionInfo(article.getWeb(), article
	// .getTitle(), s.getId(), MESSAGES_STORE_KEY, messages);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#deactivateType()
	 */
	public void deactivateType() {
		isActivated = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#activateType()
	 */
	public void activateType() {
		isActivated = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#getActivationStatus()
	 */
	public boolean getActivationStatus() {
		return isActivated;
	}

	public void findTypeInstances(Class clazz, List<KnowWEObjectType> instances) {
		boolean foundNew = false;
		if (this.getClass().equals(clazz)) {
			instances.add(this);
			foundNew = true;
		}
		if (foundNew) {
			for (KnowWEObjectType knowWEObjectType : childrenTypes) {
				knowWEObjectType.findTypeInstances(clazz, instances);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	protected abstract void init();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#getSectioner()
	 */
	@Override
	public SectionFinder getSectioner() {
		if (isActivated) {
			return sectionFinder;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#getAllowedChildrenTypes()
	 */
	@Override
	public List<KnowWEObjectType> getAllowedChildrenTypes() {
		return Collections.unmodifiableList(childrenTypes);
	}

	public boolean addChildType(KnowWEObjectType t) {
		return this.childrenTypes.add(t);
	}

	public boolean removeChildType(KnowWEObjectType t) {
		return this.childrenTypes.remove(t);
	}

	public KnowWEObjectType removeChild(int i) {
		return this.childrenTypes.remove(i);
	}

	@Override
	@Deprecated
	public Collection<Section> getAllSectionsOfType() {
		return null;
	}

	// public static String spanColorTitle(String text, String color, String
	// title) {
	// return KnowWEEnvironment.HTML_ST + "span title='" + title
	// + "' style='background-color:" + color + ";'"
	// + KnowWEEnvironment.HTML_GT + text + KnowWEEnvironment.HTML_ST
	// + "/span" + KnowWEEnvironment.HTML_GT;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEType#getRenderer()
	 */
	@Override
	public KnowWEDomRenderer getRenderer() {
		if (customRenderer != null) return customRenderer;

		return getDefaultRenderer();
	}

	public MessageRenderer getErrorRenderer() {
		return DefaultErrorRenderer.getInstance();
	}

	public de.d3web.we.kdom.report.MessageRenderer getNoticeRenderer() {
		return DefaultNoticeRenderer.getInstance();
	}

	public de.d3web.we.kdom.report.MessageRenderer getWarningRenderer() {
		return DefaultWarningRenderer.getInstance();
	}

	protected KnowWEDomRenderer getDefaultRenderer() {
		return DelegateRenderer.getInstance();
	}

	/**
	 * Allows to set a custom renderer for this type
	 * 
	 * @param renderer
	 */
	public void setCustomRenderer(KnowWEDomRenderer renderer) {
		this.customRenderer = renderer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEType#isAssignableFromType(java.lang.Class)
	 */
	@Override
	public boolean isAssignableFromType(Class<? extends KnowWEObjectType> clazz) {
		return clazz.isAssignableFrom(this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEType#isType(java.lang.Class)
	 */
	@Override
	public boolean isType(Class<? extends KnowWEObjectType> clazz) {
		return clazz.equals(this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#isLeafType()
	 */
	@Override
	public boolean isLeafType() {
		List<KnowWEObjectType> types = getAllowedChildrenTypes();
		return types == null || types.size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#isNotRecyclable()
	 */
	@Override
	public boolean isNotRecyclable() {
		return isNotRecyclable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.kdom.KnowWEObjectType#setNotRecyclable(boolean)
	 */
	@Override
	public final void setNotRecyclable(boolean notRecyclable) {
		this.isNotRecyclable = notRecyclable;
		for (KnowWEObjectType type : childrenTypes) {
			type.setNotRecyclable(notRecyclable);
		}

	}

}
