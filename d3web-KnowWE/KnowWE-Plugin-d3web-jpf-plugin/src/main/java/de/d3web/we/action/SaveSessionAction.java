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

package de.d3web.we.action;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.persistence.SessionPersistenceHandler;
import de.d3web.we.utils.KnowWEUtils;

public class SaveSessionAction extends DeprecatedAbstractKnowWEAction {

	private SimpleDateFormat dateFormat;

	public SaveSessionAction(String name) {
		dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("yyyyMMddHHmmss");
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String userID = parameterMap.get(KnowWEAttributes.USER);
		// String userID = (String) BasicUtils.getModelAttribute(model,
		// KnowWEAttributes.USER, String.class, true);

		String dir = KnowWEUtils.getSessionPath(parameterMap);
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		String sessionFileName = userID + "_" + dateFormat.format(new Date()) + ".xml";
		File session = new File(dir, sessionFileName);
		Broker broker = D3webModule.getBroker(parameterMap);
		try {
			SessionPersistenceHandler.getInstance().saveSession(broker, session.toURI().toURL());
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "done";
	}

}
