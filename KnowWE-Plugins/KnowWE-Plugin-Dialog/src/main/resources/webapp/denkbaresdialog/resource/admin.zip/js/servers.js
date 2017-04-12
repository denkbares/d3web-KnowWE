var Servers = {
	listeners: [],
	managedServers: [
		{
			name: "local test server",
			url: "http://localhost:4001/",
			alias: "test"
		},
		{
			name: "my productive server",
			url: "http://localhost:4001/",
			alias: "productive"
		},
		{
			name: "non-existing productive server",
			url: "http://nowhere:4001/",
			alias: "productive"
		}
	]
};

Servers.addListener = function(listener) {
	Servers.listeners.push(listener);
};

Servers._fireListeners = function() {
	for (var i=0; i<Servers.listeners.length; i++) {
		Servers.listeners[i]();
	}	
};

Servers.checkUpdates = function() {
	for (var i=0; i<Servers.managedServers.length; i++) {
		Servers.checkUpdate(Servers.managedServers[i]);
	}
}

Servers.checkUpdate = function(managedServer) {
	var url = "/KnowWE/action/CheckUpdates/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			url: managedServer.url,
			alias: managedServer.alias
		},		
		onSuccess: function(transport) {
			managedServer.infoXML = transport.responseXML;
			managedServer.responseCode = transport.status;
			managedServer.responseText = transport.statusText;
			Servers._fireListeners();
		},
		onFailure: function(transport) {
			managedServer.infoXML = null;
			managedServer.responseCode = transport.status;
			managedServer.responseText = transport.statusText;
			Servers._fireListeners();
		},
		onException: function(transport, exception) {
			alert('AJAX interner Fehler: '+exception);
		}
	}); 
};

Servers.getUpdateState = function (managedServer) {
	var needsUpdate = false;
	if (managedServer.infoXML) {
		needsUpdate = managedServer.infoXML.firstChild.getElementsByTagName("file").length > 0;
	}
	if (managedServer.responseCode == null) {
		return "<span class=wait>waiting for response...</span>";
	}
	else if (managedServer.responseCode < 299) {
		return needsUpdate ? "<span class=warn>online, update available</span>" : "<span class=ok>online</span>";
	}
	else {
		return "<span class=error>connection error (" + managedServer.responseCode + ": " + managedServer.responseText + ")</span>";
	}
};

Servers.getUpdateDescription = function (managedServer) {
	if (!managedServer.infoXML) return null;
	var xml = managedServer.infoXML.firstChild;
	var count = 0, size = 0, time = new Date().getTime();
	var files = xml.getElementsByTagName("file");
	for (var i=0; i<files.length; i++) {
		count++;
		size += Number(files[i].getAttribute("size"));
		var ft = Number(files[i].getAttribute("time"));
		if (ft < time) time = ft;
	}
	if (count == 0) return null;
	size = Math.round(size / 102.4) / 10.0;
	var date = new Date(time);
	var html = 
		count + " " + (count == 1 ? "File" : "Files") + 
		", " + size + " KByte" +
		", " + date.toLocaleDateString();
	return html;
};


