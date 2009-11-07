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

import java.util.Vector;

/**
 * KnowWEScriptLoader.
 * 
 * The KnowWEScriptLoader is responsible to create the javascript includes.
 * Please use the Loader to register your own javascript files. The KnowWEScriptLoader
 * was introduced to ensure the correct order of the javascript files, because
 * the KnowWE-helper and KnowWE-core files should loaded first.
 * 
 * @author smark
 * @since 2009/11/04
 */
public class KnowWEScriptLoader {

	/**
	 * Stores the registered javascript files.
	 */
	private Vector<String> js = new Vector<String>();
	
	/**
	 * The default path were the javascripts are stored on the server.
	 * Used to minimize typing when adding new scripts to the loader. 
	 */
	private String defaultPath = "KnowWEExtension/scripts/";
	
	/**
	 * Instance of the Loader. The loader is implemented as a Singleton.
	 */
	private static KnowWEScriptLoader instance;

	/**
	 * Returns the instance of the KnowWEScriptLoader.
	 * 
	 * @return instance
	 *         The instance of the KnowWEScriptLoader
	 */
	public static synchronized KnowWEScriptLoader getInstance() {
		if (instance == null) {
			instance = new KnowWEScriptLoader();
		}
		return instance;
	}	
	
	/**
	 * Adds a javascript file to the loader.
	 * Note: Only the file name has to be added. The KnowWEScriptLoader knows the 
     * default javascript file location.
	 * 
	 * @param file
     *         The javascript file that should be added.
     * @param first
     *         If TRUE the file will be added as the first element, 
     *         otherwise at the end.
	 */
	public void add( String file , boolean first){
		if(!this.js.contains( file )){
			if(first){
			    this.js.add( 0, file );
			} else {
				this.js.add( file );
			}
		}
	}
	
	/**
	 * Removes a formerly added javascript file.
     * 
	 * @param file
     *         The javascript file that should be removed.
	 */
	public void remove( String file ){
		if(this.js.contains( file )){
			this.js.remove( file );
		}
	}
	
	/**
	 * Creates the javascript include HTML string.
     *
	 * @return String
	 *         The javascript includes.
	 */
	public String getIncludes(){
		StringBuilder result = new StringBuilder();
		
		for(int i = 0; i < this.js.size(); i++){
			result.append( "<script type=\"text/javascript\" src=\"" );
			result.append( this.defaultPath + this.js.get( i ) );
			result.append( "\"></script>" );	
		}
		return result.toString();
	}
}
