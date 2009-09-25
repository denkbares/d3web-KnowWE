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

/**
 * 
 */
package de.d3web.we.action;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.javaEnv.KnowWEParameterMap;

/**
 * @author kazamatzuri
 *
 */
public class OwlUploadAction implements KnowWEAction{

    /* (non-Javadoc)
     * @see de.d3web.we.javaEnv.KnowWEAction#perform(de.d3web.we.javaEnv.KnowWEParameterMap)
     */
    @Override
    public String perform(KnowWEParameterMap parameterMap) {
	String output="";
	HttpServletRequest request = parameterMap.getRequest();
	boolean success=true;
	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	if (!isMultipart)
	    return "no Fileupload";
	File uploadfile=null;
	FileItemFactory factory = new DiskFileItemFactory();
	ServletFileUpload upload = new ServletFileUpload(factory);
	List<FileItem> items;
	try {
	     items = upload.parseRequest(request);
	} catch (FileUploadException e) {
	    return "upload Error "+e.getMessage();	    
	}
	Iterator<FileItem> iter = items.iterator();
	while (iter.hasNext()) {
	    FileItem item = iter.next();
	    String filename=item.hashCode()+"";
	    uploadfile=new File(KnowWEEnvironment.getInstance().getDefaultModulesTxtPath()+File.separatorChar+"owlincludes"+File.separatorChar+filename);
	    try {
		if (uploadfile.createNewFile()){
		    if (uploadfile.canWrite()){
			item.write(uploadfile);
		    }
		}
	    } catch (IOException e) {
		output+=e.getMessage();
		return output;
	    } catch (Exception e) {
		output+=e.getMessage();		
		return output;
	    }
	}
	SemanticCore sc = SemanticCore.getInstance();
	sc.loadOwlFile(uploadfile);
	return output;
    }

}
