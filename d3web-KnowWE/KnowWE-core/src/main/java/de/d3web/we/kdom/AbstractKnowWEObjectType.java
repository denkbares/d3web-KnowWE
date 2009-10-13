/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.utils.KnowWEUtils;

public abstract class AbstractKnowWEObjectType implements KnowWEObjectType {

	protected List<KnowWEObjectType> childrenTypes = new ArrayList<KnowWEObjectType>();
	protected List<KnowWEObjectType> priorityChildrenTypes = new ArrayList<KnowWEObjectType>();
	protected List<ReviseSubTreeHandler> subtreeHandler = new ArrayList<ReviseSubTreeHandler>();
	
	public static final String MESSAGES_STORE_KEY = "messages";
	
//	protected KnowWETypeStore typeStore;
	
	public List<ReviseSubTreeHandler> getSubtreeHandler() {
		return subtreeHandler;
	}
	
//	public KnowWETypeStore getTypeStore() {
//		if(typeStore == null) {
//			typeStore = new KnowWETypeStore();
//		}
//		return typeStore;
//	}
	
	public void clearTypeStoreRecursivly(String articleName, Set<KnowWEType> clearedTypes) {
		cleanStoredInfos(articleName);
		clearedTypes.add(this);
		
		//if(this.typeStore != null) this.typeStore.clearStoreForArticle(articleName);
		
		for(KnowWEObjectType type : childrenTypes) {
			if(type instanceof AbstractKnowWEObjectType && !clearedTypes.contains(type)) {
				((AbstractKnowWEObjectType)type).clearTypeStoreRecursivly(articleName,clearedTypes);
			}
		}
	}
	

	public void addReviseSubtreeHandler(ReviseSubTreeHandler handler) {
		subtreeHandler.add(handler);
	}

	protected boolean isActivated = true;
	protected boolean isNumberedType = false;
	
	private boolean isNotRecyclable = false;
	
	public boolean isNumberedType() {
		return isNumberedType;
	}

	public void setNumberedType(boolean isNumberedType) {
		this.isNumberedType = isNumberedType;
	}

	protected SectionFinder sectionFinder;
	protected KnowWEDomRenderer customRenderer = null;

	public AbstractKnowWEObjectType() {
		
		init();
	}
	
	public List<Message> getMessages(Section s) {
		return toMessages(KnowWEUtils.getStoredObject(KnowWEEnvironment.DEFAULT_WEB,
				s.getTitle(), s.getId(), MESSAGES_STORE_KEY), s);
		
	}
	
	public List<Message> getOldMessages(Section s) {
		return toMessages(KnowWEUtils.getOldStoredObject(KnowWEEnvironment.DEFAULT_WEB,
				s.getTitle(), s.getId(), MESSAGES_STORE_KEY), s);
		
	}
	
	private List<Message> toMessages(Object o, Section s) {
		if(o instanceof List) {
			return (List<Message>) o;
		}
		if (o == null) {
			List<Message> msg = new ArrayList<Message>();
			storeMessages(s, msg);
			return msg;
		}
		return null;
	}
	
	public void storeMessages(Section s, List<Message> messages) {
		KnowWEUtils.storeSectionInfo(KnowWEEnvironment.DEFAULT_WEB,
				s.getTitle(), s.getId(), MESSAGES_STORE_KEY, messages);
	}

	public void deactivateType() {
		isActivated = false;
	}
	public void activateType() {
		isActivated = true;
	}	
	
	public boolean getActivationStatus() {
		return isActivated;
	}
	
	public void findTypeInstances(Class clazz, List<KnowWEObjectType> instances) {
		if (this.getClass().equals(clazz)) {
			instances.add(this);
		}
		for (KnowWEObjectType knowWEObjectType : childrenTypes) {
			knowWEObjectType.findTypeInstances(clazz, instances);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName().substring(
				this.getClass().getName().lastIndexOf('.') + 1);
	}

	protected abstract void init();

	@Override
	public SectionFinder getSectioner() {
		if (isActivated) {
			return sectionFinder;
		}
		return null;
	}

	@Override
	public List<KnowWEObjectType> getAllowedChildrenTypes() {
		return this.childrenTypes;
	}
	

	@Override
	@Deprecated
	public Collection<Section> getAllSectionsOfType() {
		return null;
	}


	public static String spanColorTitle(String text, String color, String title) {
		return KnowWEEnvironment.HTML_ST + "span title='" + title
				+ "' style='background-color:" + color + ";'"
				+ KnowWEEnvironment.HTML_GT + text + KnowWEEnvironment.HTML_ST
				+ "/span" + KnowWEEnvironment.HTML_GT;
	}


	@Override
	public final void reviseSubtree(Section s) {
		for(ReviseSubTreeHandler handler : subtreeHandler) {
			handler.reviseSubtree(s);
		}
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		if (customRenderer != null)
			return customRenderer;

		return getDefaultRenderer();
	}

	public KnowWEDomRenderer getDefaultRenderer() {
		return DelegateRenderer.getInstance();
	}

	public void setCustomRenderer(KnowWEDomRenderer renderer) {
		this.customRenderer = renderer;
	}

	public IntermediateOwlObject getOwl(Section s) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		
		for (Section cur : s.getChildren()) {
			if (cur.getObjectType() instanceof AbstractKnowWEObjectType) {
				AbstractKnowWEObjectType handler = (AbstractKnowWEObjectType) cur
						.getObjectType();
				io.merge(handler.getOwl(cur));
			}
		}
		return io;
	}
	
	@Override
	public boolean isAssignableFromType(Class<? extends KnowWEObjectType> clazz) {
		return clazz.isAssignableFrom(this.getClass());
	}

	@Override
	public boolean isType(Class<? extends KnowWEObjectType> clazz) {
		
		return clazz.equals(this.getClass());
	}
	
	@Override
	public boolean isLeaveType() {
		List<KnowWEObjectType> types = getAllowedChildrenTypes();
		return types == null || types.size() == 0;
	}
	
	@Override
	public boolean isNotRecyclable() {
		return isNotRecyclable;
	}
	
	/**
	 * This method needs to be used AFTER the declaration of allowed 
	 * childrentypes!!!
	 */
	@Override
	public final void setNotRecyclable(boolean notRecyclable) {
		this.isNotRecyclable = notRecyclable;
		for (KnowWEObjectType type:childrenTypes) {
			type.setNotRecyclable(notRecyclable);
		}
		
	}
}
