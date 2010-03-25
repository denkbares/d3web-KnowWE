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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.d3web.report.Message;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.DefaultErrorRenderer;
import de.d3web.we.kdom.report.DefaultNoticeRenderer;
import de.d3web.we.kdom.report.DefaultWarningRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
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
	 * @see ReviseSubTreeHandler
	 */
	protected List<ReviseSubTreeHandler> subtreeHandler = new ArrayList<ReviseSubTreeHandler>();

	/**
	 * types can be activated and deactivated in KnowWE this field is holding
	 * the current state
	 */
	protected boolean isActivated = true;

	/**
	 * a flag for the updating mechanism, which mananges translations to
	 * explicit knowledge formats
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
	public List<ReviseSubTreeHandler> getSubtreeHandler() {
		return subtreeHandler;
	}

	/**
	 * Can be used to register new ReviseSubtreeHandlers
	 * 
	 * @param handler
	 */
	public void addReviseSubtreeHandler(ReviseSubTreeHandler handler) {
		subtreeHandler.add(handler);
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
	 * Returns all the messages stored for this section put doesn't create a new
	 * empty MessageList in the SectionStore if no MessageList is there yet.
	 * Returns <tt>null</tt> in this case.
	 * 
	 * @param s
	 * @return
	 */
	public static List<Message> getMessagesPassively(KnowWEArticle article, Section s) {
		Object o = KnowWEUtils.getStoredObject(
				article.getWeb(), article.getTitle(), s.getId(),
				MESSAGES_STORE_KEY);
		if (o == null) {
			return null;
		}
		else {
			return toMessages(o, article, s);
		}
	}

	/**
	 * Returns all the messages stored for this section.
	 * 
	 * @param s
	 * @return
	 */
	public static List<Message> getMessages(KnowWEArticle article, Section s) {
		return toMessages(KnowWEUtils.getStoredObject(
				article.getWeb(), article.getTitle(), s.getId(),
				MESSAGES_STORE_KEY), article, s);
	}

	private static List<Message> toMessages(Object o, KnowWEArticle article, Section s) {
		if (o instanceof List) {
			return (List<Message>) o;
		}
		if (o == null) {
			List<Message> msg = new ArrayList<Message>();
			storeMessages(article, s, msg);
			return msg;
		}
		return null;
	}

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

	/**
	 * Stores a list of messages under to message-store-key
	 * 
	 * @param article is the article, the message is getting stored for. Be
	 *        aware, that this is not automatically the article the section is
	 *        directly linked to (because this Section might be included), but
	 *        the article that is calling this for example while revising.
	 * @param s
	 * @param messages
	 */
	public static void storeMessages(KnowWEArticle article, Section s,
			List<Message> messages) {
		KnowWEUtils.storeSectionInfo(article.getWeb(), article
				.getTitle(), s.getId(), MESSAGES_STORE_KEY, messages);
	}

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
	 * @see
	 * de.d3web.we.kdom.KnowWEObjectType#reviseSubtree(de.d3web.we.kdom.KnowWErticle
	 * , de.d3web.we.kdom.Section)
	 */
	@Override
	public final void reviseSubtree(KnowWEArticle article, Section s) {
		for (ReviseSubTreeHandler handler : subtreeHandler) {
			try {
				KDOMReportMessage message = handler.reviseSubtree(article, s);
				KDOMReportMessage.storeMessage(s, message);

			}
			catch (Throwable e) {
				String text = "unexpected internal error in subtree handler '" + handler + "'";
				Message msg = new Message(text + ": " + e);
				storeMessages(article, s, Arrays.asList(msg));
				// TODO: vb: store the error also in the article. (see below for
				// more details)
				// 
				// Idea 1:
				// Any unexpected error (and therefore catched here) of the
				// ReviseSubtreeHandlers
				// shall be remarked at the "article" object. When rendering the
				// article page,
				// the error should be placed at the top level. A KnowWEPlugin
				// to list all
				// erroneous articles as links with its errors shall be
				// introduced. A call to this
				// plugin should be placed in the LeftMenu-page.
				//
				// Idea 2:
				// The errors are not marked at the article, but as a special
				// message
				// type is added to this section. When rendering the page, the
				// error
				// message should be placed right before the section and the
				// content
				// of the section as original text in pre-formatted style (no
				// renderer
				// used!):
				// {{{ <div class=error>EXCEPTION WITH MESSAGE</div> ORIGINAL
				// TEXT }}}
			}
		}
		s.setReusedBy(article.getTitle(), true);
	}

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

	public KnowWEDomRenderer getErrorRenderer() {
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
