/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces
 * are preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

/**
 * The KNOWWE.plugin global namespace object. If KNOWWE.plugin is already defined, the
 * existing KNOWWE.plugin object will not be overwritten so that defined namespaces
 * are preserved.
 */
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	KNOWWE.plugin = function() {
		return {}
	}
}

/**
 * The KNOWWE.plugin.correction global namespace object. If KNOWWE.plugin.correction is already defined, the
 * existing KNOWWE.plugin.correction object will not be overwritten so that defined namespaces
 * are preserved.
 * 
 * @author Alex Legler
 */
KNOWWE.plugin.correction = function() {
	return {}
}();

/**
 * Namespace: KNOWWE.plugin.correction
 * The correction namespace.
 */
KNOWWE.plugin.correction = function() {
	var registeredCorrections = {};
	var currentlyOpenedPopup;

	return {
		/**
		 * Performs correction for a given Section ID and correction
		 */
		doCorrection : function(sectionID, correction, actionClass) {
			var actionClass = (typeof actionClass === 'undefined') ? 'KDOMReplaceTermNameAction' : actionClass;
			var params = {
					action : actionClass,
					TargetNamespace :  sectionID,
					KWiki_Topic : KNOWWE.helper.gup('page'),
					KWikitext : encodeURIComponent(correction.replace(/\s*$/im,""))
			};

			var options = {
					url : KNOWWE.core.util.getURL(params),
					loader : true,
					response : {
						action : 'none',
						fn : function() { 
							window.location.reload();
						},
						onError : function(http) {
							KNOWWE.helper.message.showMessage(http.responseText, "AJAX call failed");
						}
					}
			};
			new _KA(options).send();  
		}
	}
}();
