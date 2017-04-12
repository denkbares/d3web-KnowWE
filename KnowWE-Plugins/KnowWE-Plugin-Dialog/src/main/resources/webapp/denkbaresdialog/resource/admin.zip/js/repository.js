var Repository = {
	listeners: [],
	infoXML: null,
	versionsets: [],
	aliases: []
};

Repository.addUpdateListener = function(listener) {
	Repository.listeners.push(listener);
};

Repository.updateRepository = function(listener) {
	var url = "/KnowWE/action/GetRepositoryInfo/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
			Repository._parseInfoXML(transport.responseXML);
			for (var i=0; i<Repository.listeners.length; i++) {
				Repository.listeners[i]();
			}
		},
		onFailure: function(transport) {
			alert(
				"Beim aktualisieren des Repositories ist ein Fehler aufgetreten: " +
				"\n" + transport.status + " " + transport.statusText +
				(transport.text ? "\n" + transport.text : ""));
		},
		onException: function(transport, exception) {
			alert('AJAX interner Fehler'+exception);
		}
	});
};

Repository.setAlias = function(alias, version) {
	var url = "/KnowWE/action/SetAlias/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			name: alias,
			version: version
		},
		onSuccess: function(transport) {
			Repository.updateRepository();
		},
		onFailure: function(transport) {
			alert(
				"Beim aktualisieren des Repositories ist ein Fehler aufgetreten: " +
				"\n" + transport.status + " " + transport.statusText +
				(transport.text ? "\n" + transport.text : ""));
		},
		onException: function(transport, exception) {
			alert('AJAX interner Fehler'+exception);
		}
	});
};

Repository._collect = function(array, fun) {
	var result = [];
	for (var i=0; i<array.length; i++) result.push(fun(array[i]));
	return result;
}

Repository._parseInfoXML = function(dom) {
	Repository.infoXML = dom;
	Repository.aliases = Repository._collect(dom.getElementsByTagName("alias"), function (node) {
		return {
			name: node.getAttribute("name"), 
			version: node.getAttribute("version") 
		};
	}).sortBy(function(alias) {return alias.name;});
	Repository.versionsets = Repository._collect(dom.getElementsByTagName("versionset"), function (node) {
		return  {
			version: node.getAttribute("version"),
			comment: node.getAttribute("comment"),
			date: new Date(Number(node.getAttribute("millis"))),
			files: Repository._collect(node.getElementsByTagName("file"), function (file) {
				return {
					name: file.getAttribute("name"),
					date: new Date(Number(file.getAttribute("millis"))),
					checksum: file.getAttribute("checksum")
				};
			}).sortBy(function(file) {return file.name;})
		};
	}).sortBy(function(set) {return set.date.getTime();});
};

Repository._getVersionSet = function(version) {
	for (var i=0; i<Repository.versionsets.length; i++) {
		if (Repository.versionsets[i].version == version) return Repository.versionsets[i]; 
	}
	return null;
};

Repository.getVersionDescription = function(version) {
	var set = Repository._getVersionSet(version);
	var html = "";
	html += set.comment;
	html += "<br>Version: " + set.version;
	html += "<br>Created: " + LookAndFeel.dateToString(set.date);
	return html;
};

Repository.getVersionFiles = function(version) {
	var set = Repository._getVersionSet(version);
	var html = "";
	html += "<table><tr><th>File</th><th>Date</th><th>Checksum</th></tr>";
	if (set.files.length == 0) {
		html += "<tr><td colspan=3><center>" +
				"--- no files associated ---" +
				"</center></td></tr>";
	}
	for (var i=0; i<set.files.length; i++) {
		var file = set.files[i];
		html += "<tr>" +
				"<td>" + file.name + "</td>" +
				"<td>" + LookAndFeel.dateToString(file.date) + "</td>" +
				"<td><span title=" + file.checksum + ">..." + file.checksum.substring(24) + "</span></td>" +
				"</tr>";
	}
	html += "</table>";
	return html;
};


Repository.addVersionSet = function (newVersion, comment, path, baseVersion) {
	var url = "/KnowWE/action/AddVersionSet/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			path: path,
			create: newVersion,
			base: baseVersion,
			comment: comment
		},
		onSuccess: function(transport) {
			alert("Datei erfolgreich hinzugef�gt.");
			window.location.replace("repository.html");
		},
		onFailure: function(transport) {
			alert(
				"Beim hinzugef�gen der Datei ist ein Fehler aufgetreten: " +
				"\n" + transport.status + " " + transport.statusText +
				(transport.text ? "\n" + transport.text : ""));
		},
		onException: function(transport, exception) {
			alert('AJAX interner Fehler:' + exception);
		}
	}); 
};

Repository.deleteVersionSet = function(version) {
	var url = "/KnowWE/action/DeleteVersionSet/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			version: version
		},
		onSuccess: function(transport) {
			Repository.updateRepository();
		},
		onFailure: function(transport) {
			alert(
				"Beim L�schen der Version ist ein Fehler aufgetreten: " +
				"\n" + transport.status + " " + transport.statusText +
				(transport.text ? "\n" + transport.text : ""));
		},
		onException: function(transport, exception) {
			alert('AJAX interner Fehler'+exception);
		}
	});
};

