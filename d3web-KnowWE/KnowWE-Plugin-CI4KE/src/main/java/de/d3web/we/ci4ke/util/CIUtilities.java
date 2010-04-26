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

package de.d3web.we.ci4ke.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import com.ecyrd.jspwiki.WikiEngine;

import de.d3web.we.ci4ke.handling.TestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;

public class CIUtilities {

	/**
	 * Implodes the collection by concatenating all elements, 
	 * separated by the specified string. 
	 * @param collection The collection of objects
	 * @param separator The separator which "glues" the objects together 
	 * @return an imploded collection of objects
	 */
	public static <T> String implode(Collection<T> collection, String separator){
		StringBuilder ret = new StringBuilder();
		for(Iterator<T> i = collection.iterator(); i.hasNext(); ){
			ret.append(i.next());
			if(i.hasNext())
				ret.append(separator);
		}
		return ret.toString();
	}
	
	/**
	 * 
	 * @return the save path for ci-build xml´s
	 */
	public static File getCIBuildDir(){
		WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().
				getWikiConnector().getServletContext(), null);
		String wikiDir = we.getWikiProperties().getProperty("var.basedir");
		return new File(wikiDir,"/ci-builds/");
	}
	
	// RENDER - HELPERS
	
	public static String renderResultType(TestResultType resultType, int pixelSize){
		
		String imgBulb = "<img src='KnowWEExtension/images/"+
			pixelSize+"x"+pixelSize+"/%s.gif' alt='$s'>";
		
		switch(resultType){
			case SUCCESSFUL:
				imgBulb = String.format(imgBulb, "green", "green");
			case FAILED:
				imgBulb = String.format(imgBulb, "red", "red");
			case ERROR:
				imgBulb = String.format(imgBulb, "grey", "grey");
		}
		
		return imgBulb;
	}	
}
