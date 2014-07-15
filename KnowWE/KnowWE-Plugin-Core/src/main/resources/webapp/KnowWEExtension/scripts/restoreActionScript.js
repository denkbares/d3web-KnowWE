/*
 * Copyright (C) 2012 denkbares GmbH
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
 * restores old Version of the Article
 * 
 * @author Benedikt Kaemmerer
 * @created 17.12.2012
 */

function restoreActionScript() {
	var version = document.getElementById('version').value
	var params = {
		restoreThisVersion : version,
		action : 'RestoreAction'
	};
	var options = {
		url : KNOWWE.core.util.getURL(params),
		loader : true,
		response : {
			fn : function() {
				window.location = "Wiki.jsp?page=" + this.responseText
			},
			onError : _EC.onErrorBehavior
		}
	};
	new _KA(options).send();

}
