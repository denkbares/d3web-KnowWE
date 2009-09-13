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

package de.d3web.we.upload;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.fileupload.FileItem;

/**
 * 
 * @author smark
 *
 */
public class UploadManager {

	private static UploadManager instance = null;

	private ArrayList<UploadHandler> handler = new ArrayList<UploadHandler>();
	
	public static synchronized UploadManager getInstance() {
		if (instance == null) {
			instance = new UploadManager();

		}
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Registers an UploadHandler that should be processed.
	 * @param e
	 */
	public void registerHandler(UploadHandler e)
	{
		handler.add(e);
	}
	
	/**
	 * Removes an UploadHandler from the available handlers.
	 * @param e
	 */
	public void deleteHandler(UploadHandler e)
	{
		if( handler.contains(e) )
			handler.remove( e );
	}
	
	
	/**
	 * Handles the file that should be uploaded to the WIKi.
	 * @param fileItems
	 * @return
	 */
	public String manageUpload(Collection<FileItem> fileItems) {

		String redirect;
		
		for (UploadHandler h : handler) 
		{
			redirect = h.handle( fileItems );
			if( redirect != "" )
				return redirect;
		}
		
		return "redirect:Wiki.jsp?page=" + "DefaultKnOfficeUploadPage";
	}
}
