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

package de.d3web.we.renderer.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.Statement;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.SemanticCore;

public class GraphMLOwlRenderer extends AbstractKnowWEAction {
	private int id;
	private HashMap<String,String> nodes;
	private List<String> edges;
	private HashMap<String, String> idcache;
	@Override
	public String perform(KnowWEParameterMap parameterMap) {		
		String topicName = parameterMap.get(KnowWEAttributes.TOPIC);
		return getGraphML(topicName);
	}
	
	public String getGraphML(String topic){
		id=1;
		nodes = new HashMap<String, String>();
		edges = new ArrayList<String>();
		idcache=new HashMap<String, String>();
		
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n";		
		String footer = "</graphml>";
		String topicName = topic;
		String output="";
		SemanticCore sc = SemanticCore.getInstance();
		List<Statement> list = sc.getTopicStatements(topicName);
		if (list != null) {
			for (Statement cur : list) {
				String s = cur.getSubject().stringValue();
				String p = cur.getPredicate().stringValue();
				String o = cur.getObject().stringValue();
				s = s.substring(s.indexOf('#') + 1).replaceAll("<", "").replaceAll(">", "");
				o = o.substring(o.indexOf('#') + 1).replaceAll("<", "").replaceAll(">", "");
				p = p.substring(p.indexOf('#') + 1).replaceAll("<", "").replaceAll(">", "");		
				int sid=createNode(s);
				int oid=createNode(o);
				edges.add(getEdge(sid, oid));
			}
		}
		for (int i=1;i<id;i++){
			output+=nodes.get(i+"");
		}
		for (String edge:edges){
			output+=edge;
		}
		return header+getGraph(output,"undirected")+footer;
	}

	private int createNode(String name){
		if (idcache.get(name)==null){
			String node="<node id=\""+id+"\"><data key=\"name\">" +name + "</data><data key=\"gender\">M</data></node>\n";
			nodes.put(id+"",node);
			idcache.put(name, id+"");
			id++;			
			return id-1;
		} else {
			return Integer.parseInt(idcache.get(name));
		}
		
	}

	private String getEdge(int s, int t ) {
		return "<edge source=\"" + s + "\" target=\"" + t + "\"/>\n";
	}

//	private String getEdge(String f, String t, String c) {
//		return "<edge source=\"" + f + "\" target=\"" + t + "\" class=\"" + c
//				+ "\" />";
//	}

	private String getGraph(String content, String mode) {
		return "<graph id=\"G\" edgedefault=\"" 
				 + mode + "\">\n"+ "<key id=\"gender\" for=\"node\" attr.name=\"gender\" attr.type=\"string\"/>\n"+		
					"<key id=\"name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>\n"+ content
				+ "</graph>";
	}

}
