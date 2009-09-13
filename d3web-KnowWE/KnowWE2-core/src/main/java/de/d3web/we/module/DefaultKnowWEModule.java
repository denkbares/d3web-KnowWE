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

package de.d3web.we.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public abstract class DefaultKnowWEModule extends AbstractXMLObjectType implements KnowWEModule {
	
	public DefaultKnowWEModule(String type) {
		super(type);
		
	}
	
	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> rootTypes = new ArrayList<KnowWEObjectType>();
		rootTypes.add(this);
		return rootTypes;
	}

	
	@Override
	public Collection<Section> getAllSectionsOfType() {
		//TODO override in all existing types...
			return null;
	}
	
	@Override
	public String getName() {
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

		
	@Override
	public void initModule(ServletContext context) {
		//does nothing as default
	}
	

//	@Override
//	public KnowWEParseResult modifyAndInsert(String topic, String web,
//			String text, KnowledgeBaseManagement kbm) {
//		return new KnowWEParseResult(new Report(),topic,text);
//	}
	
	public void onSave(String topic) {
		//do nothing
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return DelegateRenderer.getInstance();
	}

//	@Override
//	public String preCacheModifications(String text, KnowledgeBase kb,
//			String topicname) {
//		return text;
//	}



	
	

}
