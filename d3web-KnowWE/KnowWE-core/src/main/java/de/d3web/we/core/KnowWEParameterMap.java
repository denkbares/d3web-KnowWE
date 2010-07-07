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

package de.d3web.we.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KnowWEParameterMap extends HashMap<String, String> {

	private HttpSession session;
	private ServletContext context;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private KnowWEUserContext wikiContext;
	
	public KnowWEParameterMap(KnowWEUserContext wikiContext,
			HttpServletRequest rq, HttpServletResponse rp, 
			ServletContext c, KnowWEEnvironment e) {
		if (rq != null) {
			this.session = rq.getSession();
		}
		this.wikiContext = wikiContext;
		this.request = rq;
		this.response = rp;
		this.context = c;
        
		Enumeration<String> paramNames = rq.getParameterNames();
		if ((paramNames != null)) {
			while ((paramNames.hasMoreElements())) {
				String name = paramNames.nextElement();
				
				String value = "";
				try {
					value = URLDecoder.decode( rq.getParameter(name) ,"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					value = rq.getParameter(name);
				}
				this.put(name, value);
			}
		} 
		handlePostData();
	}

	public KnowWEParameterMap(String name, String value) {

		this.put(name, value);
	}

	public HttpSession getSession() {
		return session;
	}

	public ServletContext getContext() {
		return context;
	}

	public String getUser() {
		return this.get(KnowWEAttributes.USER);
	}

	public String getWeb() {
		return this.get(KnowWEAttributes.WEB);
	}

	public String getTopic() {
		String page = this.get(KnowWEAttributes.TOPIC);
		if(page == null) {
			page = this.get("page");
		}
		return page;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	
	public HttpServletResponse getResponse() {
		return response;
	}

	public KnowWEUserContext getWikiContext() {
		return this.wikiContext;
	}
	
	private void handlePostData() {
		try {
			String str;
			StringBuilder post = new StringBuilder();
			
			BufferedReader input = new BufferedReader(new InputStreamReader( request.getInputStream() ));
			
			while((str = input.readLine()) != null) {
			    post.append( str + "\n" );
			}
			
			input.close();
			int pos = post.indexOf("=");
			if( pos != -1 ) {
				String key, value;
				
				key = post.substring(0, pos);
			    value = post.substring(pos + 1);
			    this.put(key, value);
			}

		} catch (IOException e) {
			//no or wrong post data do nothing
		}		
	}	
}
