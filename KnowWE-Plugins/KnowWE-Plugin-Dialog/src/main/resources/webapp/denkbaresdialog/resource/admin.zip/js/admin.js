var Admin = { };

Admin.addRepositoryFile = function (path, isCurrent) {
	var url = "/KnowWE/action/AddRepositoryFile/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			path: path,
			current: isCurrent ? "true" : "false"
		},
		onSuccess: function(transport) {
			alert("Datei erfolgreich hinzugef�gt.");
		},
		onFailure: function(transport) {
			alert(
				"Beim hinzugef�gen der Datei ist ein Fehler aufgetreten: " +
				"\n" + transport.status + " " + transport.statusText +
				(transport.text ? "\n" + transport.text : ""));
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
};
