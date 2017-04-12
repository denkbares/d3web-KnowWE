var Sync = {
	serverURL: "http://localhost:4001/",
	CHECK_UPDATE_FREQUENCY: 60 * 1000,
	CHECK_PROGRESS_FREQUENCY: 100,
	VERSION_ALIAS: "productive",
	
	infoXML: null,
	infoListeners: [],
	checkProgressTaskStarted: false,
	progress: 0,
	progressState: "NOT_STARTED",
	progressDescription: "",
	progressListeners: []
};

Sync.addInfoListener = function (listener) {
	Sync.infoListeners.push(listener);
}

Sync.addProgressListener = function (listener) {
	Sync.progressListeners.push(listener);
}

Sync.startUpdate = function () {
	var url = "../../../StartUpdate/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			url: Sync.serverURL,
			alias: Sync.VERSION_ALIAS
		},		
		onSuccess: function(transport) {
		},
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
	Sync.progressListeners.each(function (f) { f(); });	
}

Sync.cancelUpdate = function () {
	var url = "../../../CancelUpdate/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
		},
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
	Sync.progressListeners.each(function (f) { f(); });	
}


Sync.startCheckProgressTask = function () {
	Sync.checkProgressTaskStarted = true;
	Sync._iterateCheckProgress();
};

Sync.stopCheckProgressTask = function () {
	Sync.checkProgressTaskStarted = false;
};

Sync._iterateCheckProgress = function () {
	if (Sync.checkProgressTaskStarted) {
		Sync.checkProgress();
		window.setTimeout("Sync._iterateCheckProgress();", Sync.CHECK_PROGRESS_FREQUENCY);
	}
};

Sync.checkProgress = function () {
	var url = "../../../CheckProgress/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
			if (Sync.checkProgressTaskStarted) {
				var xml = transport.responseXML;
				Sync.progress = Number(xml.firstChild.getAttribute("percent"));
				Sync.progressState = xml.firstChild.getAttribute("state");
			}
		},
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
	Sync.progressListeners.each(function (f) { f(); });	
};


Sync.isUpdateFinished = function() {
	return Sync.progressState == "FINISHED";
};

Sync.isRestartRequired = function() {
	return Sync.progressState == "RESTART_REQUIRED";
};

Sync.isUpdateCanceled = function() {
	return Sync.progressState == "CANCELED";
};

Sync.isUpdateError = function() {
	return Sync.progressState == "ERROR_OCCURED";
};

Sync.startCheckUpdateTask = function () {
	Sync.checkUpdate();
	window.setTimeout("Sync.startCheckUpdateTask();", Sync.CHECK_UPDATE_FREQUENCY);
};

Sync.checkUpdate = function () {
	var url = "../../../CheckUpdates/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			url: Sync.serverURL,
			alias: Sync.VERSION_ALIAS
		},		
		onSuccess: function(transport) {
			Sync.infoXML = transport.responseXML;
			Sync._updateSync();
		},
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
};

Sync._updateSync = function () {
	Sync.infoListeners.each(function (f) { f(); });
};

Sync.isUpdateAvailable = function () {
	if (!Sync.infoXML) return false;
	var xml = Sync.infoXML.firstChild;
	return (xml.getElementsByTagName("file").length > 0);
};

Sync.getUpdateDescription = function () {
	if (!Sync.infoXML) return null;
	var xml = Sync.infoXML.firstChild;
	var count = 0, size = 0, time = new Date().getTime();
	var files = xml.getElementsByTagName("file");
	for (var i=0; i<files.length; i++) {
		count++;
		size += Number(files[i].getAttribute("size"));
		var ft = Number(files[i].getAttribute("time"));
		if (ft < time) time = ft;
	}
	if (count == null) return null;
	size = Math.round(size / 102.4) / 10.0;
	var date = new Date(time);
	var html = 
		count + " " + Translate.get(count == 1 ? "sync_overview_file" : "sync_overview_files") + 
		", " + size + " " + Translate.get("sync_overview_kbyte") +
		"<br>" +
		date.toLocaleDateString();
	return html;
};

Sync.getProgress = function () {
	return Sync.progress;
};

Sync.getProgressDescription = function () {
	return Translate.get("sync_state_"+Sync.progressState);
};