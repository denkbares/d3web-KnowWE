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

package de.d3web.we.ci4ke.handling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ListSelectionEvent;

import de.d3web.we.ci4ke.build.CIBuilder;

/**
 * TODO Comment outdated
 * Das Speichern eines "monitored Articles" soll den Hook auslösen.
 * Die Methode triggerHook(monitoredArticleTitle) löst dazu für jedes
 * registrierte Dashboard einen neuen Build aus.
 * Aufbau der Map:
 * Map( monitoredArticle --> Set( dashboardArticle + "$$$" + dashboardID ))
 * TODO: English comment 
 * Mit dashboardArticle UND DashboardID lässt sich ein Dashboard EINDEUTIG 
 * und ohne Suchaufwand identifizieren.
 * @author Marc-Oliver Ochlast
 *
 */
public class CIHookManager {
	
	private Map<String, List<CIHook>> hooks;
	
	private static final CIHookManager INSTANCE = new CIHookManager();
	
	private CIHookManager() {
		hooks = new TreeMap<String, List<CIHook>>();
		
		//NUR ZUM TEST!!!
//		registerHook("BLA", new Runnable(){
//			@Override
//			public void run() {
//				Logger.getLogger(CIEventForwarder.class.getName()).log(
//						Level.INFO, " ===START==== HelloWorld from a Thread!!! ======= ");				
//				for(int i = 0; i<10; i++)
//					Logger.getLogger(CIEventForwarder.class.getName()).log(
//							Level.INFO, " === "+i+" ==== HelloWorld from a Thread!!! ======= ");
//				Logger.getLogger(CIEventForwarder.class.getName()).log(
//						Level.INFO, " === END ==== HelloWorld from a Thread!!! ======= ");
//			}
//		});
	}
	
	public static CIHookManager getInstance() { return INSTANCE; }
	
	/**
	 * Registers a new onSave hook.
	 * @param monitoredArticleTitle
	 * @param dashboardArticleTitle
	 * @param dashboardID
	 * @return true, if the hook was successfully registered (or if the hook
	 * 			was already registered!). false if not.
	 */
	public boolean registerHook(String monitoredArticleTitle, 
			String dashboardArticleTitle, String dashboardID) {

		CIHook hook = new CIHook(dashboardArticleTitle,
				dashboardID);
		return registerHook(monitoredArticleTitle, hook);
	}
	
	public boolean registerHook(String monitoredArticleTitle, CIHook hook) {
		//some serious paramteter checking first!
		if(monitoredArticleTitle == null || monitoredArticleTitle.isEmpty()) {
			throw new IllegalArgumentException("monitoredArticleTitle is null or empty!");
		}
		
		if(!hooks.containsKey(monitoredArticleTitle)){
			//the first hook for this monitoredArticle. init the set
			List<CIHook> hookList = new ArrayList<CIHook>();
			hookList.add(hook);
			//and put it into the hooks map
			this.hooks.put(monitoredArticleTitle, hookList);
		} else {
			List<CIHook> hookList = hooks.get(monitoredArticleTitle);
			if(!hookList.contains(hook)) {
				hookList.add(hook);
				hooks.put(monitoredArticleTitle, hookList);
			}
		}
		return true;
	}
	
	public boolean deRegisterHook(String monitoredArticleTitle, CIHook hook) {
		return false;
	}
	
	public boolean deRegisterAllHooks(String monitoredArticleTitle) {
		return false;
	}
	
	/**
	 * Checks, if a specified dashboard holds a hook on a specific monitored
	 * Article
	 * @param monitoredArticleTitle
	 * @return
	 */
	public boolean containedInAHook(String monitoredArticleTitle, 
			String dashboardID) {
		
		List<CIHook> hookList = hooks.get(monitoredArticleTitle);
		for(CIHook hook : hookList)
			if(hook.getDashboardID().equals(dashboardID))
				return true;
		
		return false;
	}
	
	/**
	 * Checks, if a specified dashboard holds a hook on a specific monitored
	 * Article
	 * @param monitoredArticleTitle
	 * @return
	 */
	public boolean containedInAHook(String dashboardID) {
		
		for(Map.Entry<String, List<CIHook>> entry : hooks.entrySet()) {
			String monitoredArticleTitle = entry.getKey();
			if(containedInAHook(monitoredArticleTitle, dashboardID)==true) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Triggers the registered hooks for a given Article
	 * @param monitoredArticleTitle
	 */
	public void triggerHooks(String monitoredArticleTitle) {
		
		if(hooks.containsKey(monitoredArticleTitle)) {
			List<CIHook> hookList = hooks.get(monitoredArticleTitle);
			//Collections.sort(hookList); //sortieren um evtl unnötige CIBuilder
										  //Instanzen zu vermeiden
			
			for(CIHook hook : hookList) {
				Logger.getLogger(CIEventForwarder.class.getName()).log(
						Level.INFO, " >> CI >> Constructing and executing "+
						"new CIBuilder for "+hook);
				CIBuilder builder = new CIBuilder(hook);
				builder.executeBuild();
			}
		}
	}
	
	public static class CIHook implements Comparable<CIHook>{
		
		private String dashboardArticleTitle;
		private String dashboardID;
		
		public String getDashboardArticleTitle() {
			return dashboardArticleTitle;
		}

		public String getDashboardID() {
			return dashboardID;
		}

		public CIHook(String dashboardArticleTitle, 
				String dashboardID) {
			
			super();
			
			if(dashboardArticleTitle == null || dashboardArticleTitle.isEmpty()) {
				throw new IllegalArgumentException("dashboardArticleTitle is null or empty!");
			}
			if(dashboardID == null || dashboardID.isEmpty()) {
				throw new IllegalArgumentException("dashboardID is null or empty!");
			}				
			
			this.dashboardArticleTitle = dashboardArticleTitle;
			this.dashboardID = dashboardID;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((dashboardArticleTitle == null) ? 0
							: dashboardArticleTitle.hashCode());
			result = prime * result
					+ ((dashboardID == null) ? 0 : dashboardID.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CIHook other = (CIHook) obj;
			if (dashboardArticleTitle == null) {
				if (other.dashboardArticleTitle != null)
					return false;
			} else if (!dashboardArticleTitle
					.equals(other.dashboardArticleTitle))
				return false;
			if (dashboardID == null) {
				if (other.dashboardID != null)
					return false;
			} else if (!dashboardID.equals(other.dashboardID))
				return false;
			return true;
		}


		/**
		 * compares two CIHooks by comparing their "dashboardArticleTitle"
		 * lexicographically
		 */
		@Override
		public int compareTo(CIHook o) {
			return this.dashboardArticleTitle.compareTo(o.dashboardArticleTitle);
		}

		@Override
		public String toString() {
			return "CIHook [dashboardArticleTitle=" + dashboardArticleTitle
					+ ", dashboardID=" + dashboardID + "]";
		}
	}
}
