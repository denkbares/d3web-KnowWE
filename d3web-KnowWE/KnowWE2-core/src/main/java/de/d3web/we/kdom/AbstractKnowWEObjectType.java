package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;

public abstract class AbstractKnowWEObjectType implements KnowWEObjectType {

	protected List<KnowWEObjectType> childrenTypes = new ArrayList<KnowWEObjectType>();
	protected List<KnowWEObjectType> priorityChildrenTypes = new ArrayList<KnowWEObjectType>();
	protected List<ReviseSubTreeHandler> subtreeHandler = new ArrayList<ReviseSubTreeHandler>();
	
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
	
	public void clearTypeStoreRecursivly(String articleName) {
		cleanStoredInfos(articleName);
		
		//if(this.typeStore != null) this.typeStore.clearStoreForArticle(articleName);
		
		for(KnowWEObjectType type : childrenTypes) {
			if(type instanceof AbstractKnowWEObjectType) {
				((AbstractKnowWEObjectType)type).clearTypeStoreRecursivly(articleName);
			}
		}
	}
	

	public void addReviseSubtreeHandler(ReviseSubTreeHandler handler) {
		subtreeHandler.add(handler);
	}

	protected boolean isActivated = true;
	protected boolean isNumberedType = false;
	
	public boolean isNumberedType() {
		return isNumberedType;
	}

	public void setNumberedType(boolean isNumberedType) {
		this.isNumberedType = isNumberedType;
	}

	protected SectionFinder sectionFinder;
	private KnowWEDomRenderer customRenderer = null;

	public AbstractKnowWEObjectType() {
		init();
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
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		return childrenTypes;
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
	public void reviseSubtree(Section s, KnowledgeRepresentationManager kbm, String web,
			KnowWEDomParseReport rep) {
		for(ReviseSubTreeHandler handler : subtreeHandler) {
			handler.reviseSubtree(s, kbm, web, rep);
		}
			
	}
	


	
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		if (customRenderer != null)
			return customRenderer;

		return getDefaultRenderer();
	}

	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}

//	public AbstractKnowWEObjectType findChildType(Class clazz) {
//		if (this.getClass().equals(clazz)) {
//			return this;
//		} else {
//			for (KnowWEObjectType child : childrenTypes) {
//				if (clazz.equals(child.getClass())) {
//					if (child instanceof AbstractKnowWEObjectType) {
//						return ((AbstractKnowWEObjectType) child);
//					}
//				}
//			}
//		}
//		return null;
//	}

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

}
