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

import java.util.LinkedList;

/**
 * KnowWERessourceLoader.
 * 
 * The KnowWERessourceLoader stores the JS and CSS files used to extend KNOWWE.
 * Please use the Loader to register your own JS and CSS files. The KnowWERessourceLoader
 * was introduced to ensure the correct order of the JS files, because
 * the KnowWE-helper and KnowWE-core files should loaded first.
 * 
 * @author smark
 * @since 2010/02/15
 */
public class KnowWERessourceLoader {

    /**
     * Requests a CSS to be inserted. Value is {@value}.
     */
    public static final String RESOURCE_STYLESHEET = "stylesheet";

    /**
     * Requests a script to be loaded. Value is {@value}.
     */
    public static final String RESOURCE_SCRIPT = "script";	
	
	/**
	 * The default path were the scripts are stored on the server.
	 * Used to minimize typing when adding new scripts to the loader. 
	 */
	public static final String defaultScript = "KnowWEExtension/scripts/";
	
	/**
	 * The default path were the CSS are stored on the server.
	 * Used to minimize typing when adding new CSS to the loader. 
	 */
	public static final String defaultStylesheet = "KnowWEExtension/css/";	    
    
	/**
	 * Stores the registered script files.
	 */
	private LinkedList<String> script = new LinkedList<String>();

	/**
	 * Stores the registered CSS files.
	 */
	private LinkedList<String> stylesheets = new LinkedList<String>();
	
	/**
	 * Instance of the Loader. The loader is implemented as a Singleton.
	 */
	private static KnowWERessourceLoader instance;

	/**
	 * Returns the instance of the KnowWERessourceLoader.
	 * 
	 * @return instance
	 *         The instance of the KnowWERessourceLoader
	 */
	public static synchronized KnowWERessourceLoader getInstance() {
		if (instance == null) {
			instance = new KnowWERessourceLoader();
		}
		return instance;
	}	
	
	/**
	 * Adds a resource file to the loader.
	 * Note: Only the file name has to be added. The KnowWERessourceLoader knows the 
     * default resource file location.
	 * 
	 * @param file
     *         The resource file that should be added.
     * @param first
     *         If TRUE the file will be added as the first element, 
     *         otherwise at the end.
	 */
	public void add( String file , String type){
		LinkedList<String> tmp = this.getList(type);
		
		if( !tmp.contains( file )) {
		    tmp.add( file );
		}
	}
	
	/**
	 * Adds a resource file to the loader as first element.
	 * Note: Only the file name has to be added. The KnowWERessourceLoader knows the 
     * default resource file location.
	 * 
	 * @param file
     *         The resource file that should be added.
     * @param first
     *         If TRUE the file will be added as the first element, 
     *         otherwise at the end.
	 */
	public void addFirst( String file , String type){
		LinkedList<String> tmp = this.getList(type);
		
		if( !tmp.contains( file )) {
		    tmp.addFirst( file );
		} else {
			tmp.remove( file );
			tmp.addFirst( file );
		}
	}	
	
	/**
	 * Removes a formerly added resource file.
     * 
	 * @param file
     *         The resource file that should be removed.
	 */
	public void remove( String file, String type ){
		LinkedList<String> tmp = this.getList(type);
		if( tmp.contains( file )) {
			tmp.remove( file );
		}
	}
	
	/**
	 * Returns the script files the loader knows.
     *
	 * @return String
	 *         The script files.
	 */
	public LinkedList<String> getScriptIncludes(){
		return this.script;
	}
	
	/**
	 * Returns the CSS files the loader knows.
     *
	 * @return String
	 *         The CSS files.
	 */
	public LinkedList<String> getStylesheetIncludes(){
		return this.stylesheets;
	}	
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	private LinkedList<String> getList( String type ){
		if( type.equals( RESOURCE_SCRIPT )) {
			return this.script;
		} else if( type.equals( RESOURCE_STYLESHEET )) {
			return this.stylesheets;
		} else {
			return new LinkedList<String>();
		}
	}
}
