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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import de.knowwe.plugin.Instantiation;


public class CommentModule implements Instantiation{

	static {
		importTypes();
	}
	
	private static Map<String, String> commentTypes;
	private static Map<String, Integer> ids;
	
	private static void importTypes() {
		ResourceBundle rb = ResourceBundle.getBundle("commentTypes");
		
		Map<String, String> types = new HashMap<String, String>();
		Map<String, Integer> ids = new HashMap<String, Integer>();
		
		Iterator<String> it = rb.keySet().iterator();
		
		while (it.hasNext()) {
			String type = (String) it.next();
			String path = rb.getString(type);
			if (path == null || path.length() == 0) {
				path = rb.getString("DEFAULT");
			}
			types.put(type, path);
			ids.put(type, 1);
		}
				
		CommentModule.commentTypes = types;	
		CommentModule.ids = ids;
		
	}
	
	protected static Map<String, String> getCommentTypes() {
		return commentTypes;
	}
	
	protected static Map<String, Integer> getIDs() {
		return ids;
	}
	
	@Override
	public void init(ServletContext context) {
	}
}
