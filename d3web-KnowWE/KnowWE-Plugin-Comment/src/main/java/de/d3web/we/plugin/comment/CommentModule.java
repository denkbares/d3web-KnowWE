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

package de.d3web.we.plugin.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import de.d3web.we.action.ForumBoxAction;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.module.AbstractDefaultKnowWEModule;
import de.d3web.we.plugin.forum.Forum;


public class CommentModule extends AbstractDefaultKnowWEModule{

	private static Map<String, String> commentTypes;
	private static CommentModule instance;
		
	public static CommentModule getInstance() {
		if(instance == null) {
			instance = new CommentModule();
		}
		return instance;
	}
	
	private void importTypes() {
		ResourceBundle rb = ResourceBundle.getBundle("commentTypes");
		Map<String, String> commentTypes = new HashMap<String, String>();
		
		Iterator<String> it = rb.keySet().iterator();
		
		while (it.hasNext()) {
			String type = (String) it.next();
			String path = rb.getString(type);
			if (path == null || path.length() == 0) {
				path = rb.getString("DEFAULT");
			}
			commentTypes.put(type, path);
		}
		
		CommentModule.commentTypes = commentTypes;	
	}
	
	protected static Map<String, String> getCommentTypes() {
		return commentTypes;
	}
	
	@Override
	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> rootTypes = new ArrayList<KnowWEObjectType>();
		rootTypes.add(new CommentType());
		rootTypes.add(new Forum());
		return rootTypes;
	}
	
	@Override
	public void initModule(ServletContext context) {
		super.initModule(context);
		importTypes();
	}
	
	public void addAction(
			Map<Class<? extends KnowWEAction>, KnowWEAction> actionMap) {
		actionMap.put(de.d3web.we.action.ForumBoxAction.class, new ForumBoxAction());
	}
}
