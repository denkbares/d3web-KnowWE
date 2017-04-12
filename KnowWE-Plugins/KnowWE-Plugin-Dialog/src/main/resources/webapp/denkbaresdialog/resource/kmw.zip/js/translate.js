var Translate = {
	DEFAULT_LANGUAGE: "en",
	currentLanguage: "en",
	entries: []
};

Translate.get = function (key) {
	var text = Translate.entries[Translate.currentLanguage][key];
	if (! text) {
		CCMessage.warn("access to unknown text constant: "+key);
		return key;
	}
	else {
		return text;
	}
};

Translate.initCurrentLanguage = function () {
	var url = window.location.href;
	var matches = /[\?&]lang=(..)/i.exec(url);
	var lang = matches[1];
	// use only if we have text entries for that language
	if (lang && Translate.entries[lang]) {
		Translate.currentLanguage = lang;
	}
};

Translate.getAvailableLanguages = function () {
	return ["de", "en", "nl"];
}

Translate.getCurrentLanguage = function () {
	return Translate.currentLanguage;
}

Translate.entries["de"] = {
	use_title: "Interview",
	use_button_new: "Neu",
	use_button_sync: "Sync",
	use_button_save: "Speichern",
	use_question_unknown: "unbekannt",
	
	result_title: "L&ouml;sungen",
	no_result_title: "Fehler nicht implementiert",
	no_result_text: "Dies ist eine Demonstrations-Anwendung. Der gesuchte Fehler wurde noch nicht implementiert.",	
	
	sync_title: "Synchronisation",
	
	sync_state_NOT_STARTED: "Initialisierung",
	sync_state_CONNECTING_SERVER: "Serververbindung herstellen",
	sync_state_DOWNLOAD_IN_PROGRESS: "Dateien werden heruntergeladen",
	sync_state_PATCH_IN_PROGRESS: "Dateien werden eingespielt",
	sync_state_RESTART_REQUIRED: "Neustart der Anwendung erforderlich",
	sync_state_FINISHED: "Erfolgreich abgeschlossen",
	sync_state_CANCEL_REQUESTED: "Abbruch vorbereiten",
	sync_state_CANCELED: "Abgebrochen",
	sync_state_ERROR_OCCURED: "Fehler aufgetreten",
	
	sync_success_title: "Aktualisierung Erfolgreich",
	sync_success_text: "Die Aktualisierung wurde erfolgreich durchgeführt. Es ist kein Neustart der Anwendung erforderlich, sie können mit der Verwendung fortfahren.",
	sync_restart_title: "Aktualisierung Heruntergeladen",
	sync_restart_text: "Die Aktualisierung wurde auf Ihren Rechner heruntergeladen. Um die Aktualisierung einzuspielen ist ein Neustart der Anwendung erforderlich.",
	sync_cancel_title: "Aktualisierung Abgebrochen",
	sync_cancel_text: "Die Aktualisierung wurde abgebrochen. Sie können mit der Verwendung fortfahren.",
	sync_error_title: "Es ist ein Fehler aufgetreten",
	sync_error_text: "Die Aktualisierung wurde wegen eines Fehlers abgebrochen. Sie können es später erneut versuchen und nun mit der Verwendung fortfahren. Tritt der Fehler wiederholt auf, kontaktieren Sie bitte den Systemadministrator.",
	
	sync_overview_title: "Verfügbare Aktualisierung",
	sync_overview_subtitle_update: "Es ist eine Aktualisierung verfügbar",
	sync_overview_file: "Datei",
	sync_overview_files: "Dateien",
	sync_overview_kbyte: "KByte",
	sync_overview_no_update: "Ihre Anwendung ist auf dem neuesten Stand.<br>Es ist keine Aktualisierung erforderlich.",
	
	sync_progress_title: "Fortschritt",
	sync_progress_text: "Bitte warten Sie. Die Aktualisierung wird durchgeführt.",
	
	sync_button_cancel: "Abbrechen",
	sync_button_restart: "Anwendung neu starten...",
	sync_button_continue: "Fortfahren", 
		
	check_button_next: "Weiter",
	check_button_fail: "Fehlgeschlagen",
	check_title_pruefling: "Pr&uuml;fling",
	check_title_durchfuehrung: "Durchf&uuml;hrung",
	check_title_kdt: "Kdt",
	check_title_ls: "LS",
	check_title_rs: "RS",
	check_title_mkf: "MKF"
};


Translate.entries["en"] = {
	use_title: "Interview",
	use_button_new: "New",
	use_button_sync: "Sync",
	use_button_save: "Save",
	use_question_unknown: "unknown",
	
	result_title: "Solutions",
	no_result_title: "Fault not implemented yet",
	no_result_text: "This application is for demonstration purposes only. The fault is not implemented yet.",	
	
	sync_title: "Synchronize",
	
	sync_state_NOT_STARTED: "Initializing",
	sync_state_CONNECTING_SERVER: "Connecting Server",
	sync_state_DOWNLOAD_IN_PROGRESS: "Download Files",
	sync_state_PATCH_IN_PROGRESS: "Apply Files",
	sync_state_RESTART_REQUIRED: "Restart Required",
	sync_state_FINISHED: "Update Successful",
	sync_state_CANCEL_REQUESTED: "Cancel requested",
	sync_state_CANCELED: "Update Canceled",
	sync_state_ERROR_OCCURED: "Error Occured",
	
	sync_success_title: "Update Successful",
	sync_success_text: "The update was successful. It is not neccessary to restart the application. You can continue using it.",
	sync_restart_title: "Update Downloaded",
	sync_restart_text: "The update has been downloaded to your system. To update the application, a restart of the application is neccessary. Please restart your application now.",
	sync_cancel_title: "Update Canceled",
	sync_cancel_text: "The update has been canceled. You can continue using it.",
	sync_error_title: "Error Occured",
	sync_error_text: "The update has been canceled due to an error. You can try to update later, maybe after restarting your application. For now you can continue using the application. If the error occurs again, please contact your system administrator.",
	
	sync_overview_title: "Available Update",
	sync_overview_subtitle_update: "An update is available",
	sync_overview_file: "File",
	sync_overview_files: "Files",
	sync_overview_kbyte: "KByte",
	sync_overview_no_update: "Your application is up to date.<br>No synchronization is required.",
	
	sync_progress_title: "Progress",
	sync_progress_text: "Please wait, the application will be updated.",
	
	sync_button_cancel: "Cancel",
	sync_button_restart: "Restart Application...",
	sync_button_continue: "Continue",
	
	check_button_next: "Next",
	check_button_fail: "Step Failed",
	check_title_pruefling: "Pr&uuml;fling",
	check_title_durchfuehrung: "Durchf&uuml;hrung",
	check_title_kdt: "Kdt",
	check_title_ls: "LS",
	check_title_rs: "RS",
	check_title_mkf: "MKF"
};

Translate.entries["nl"] = {
	use_title: "Interview",
	use_button_new: "Nieuw",
	use_button_sync: "Sync",
	use_button_save: "Besparen",
	use_question_unknown: "onbekend",
	
	result_title: "Oplossingen",
	no_result_title: "Fout niet uitgevoerd",
	no_result_text: "Dit is een demonstratie toepassing. De gevraagde was nog niet geimplementeerd.",	
	
	sync_title: "Synchroniseren",
	
	sync_state_NOT_STARTED: "Initialiseren",
	sync_state_CONNECTING_SERVER: "Aansluiten Server",
	sync_state_DOWNLOAD_IN_PROGRESS: "Bestanden downloaden",
	sync_state_PATCH_IN_PROGRESS: "Breng Bestanden",
	sync_state_RESTART_REQUIRED: "Opnieuw opstarten is vereist",
	sync_state_FINISHED: "Succesvolle Update",
	sync_state_CANCEL_REQUESTED: "Annuleren gevraagd",
	sync_state_CANCELED: "Update is geannuleerd",
	sync_state_ERROR_OCCURED: "Fout opgetreden",
	
	sync_success_title: "Succesvolle Update",
	sync_success_text: "De update was succesvol. Het is niet nodig om de toepassing opnieuw starten. U kunt blijven gebruiken.",
	sync_restart_title: "Update gedownload",
	sync_restart_text: "De update is gedownload op uw systeem. Voor het bijwerken van toepassing, een herstart van de aanvraag noodzakelijk is. Gelieve uw aanvraag nu opnieuw opstarten.",
	sync_cancel_title: "Update is geannuleerd",
	sync_cancel_text: "De update is geannuleerd. U kunt blijven gebruiken.",
	sync_error_title: "Fout opgetreden",
	sync_error_text: "De update is geannuleerd wegens een fout. U kunt proberen om later te werken, misschien na opnieuw opstarten van uw aanvraag. Voor nu kun je nog steeds gebruik van de toepassing. Als de fout weer optreedt, neem dan contact op met uw systeembeheerder.",
	
	sync_overview_title: "Beschikbare update",
	sync_overview_subtitle_update: "Een update is beschikbaar",
	sync_overview_file: "Vijl",
	sync_overview_files: "Archief",
	sync_overview_kbyte: "KByte",
	sync_overview_no_update: "Uw aanvraag wordt up-to-date.<br>Geen synchronisatie nodig is.",
	
	sync_progress_title: "Vooruitgang",
	sync_progress_text: "Even geduld aub, zal de aanvraag worden bijgewerkt.",
	
	sync_button_cancel: "Annuleren",
	sync_button_restart: "Start Application ...",
	sync_button_continue: "Voortzetten",
	
	check_button_next: "volgende",
	check_button_fail: "gezakt",
	check_title_pruefling: "Examinandus",
	check_title_durchfuehrung: "Uitvoering",
	check_title_kdt: "Kdt",
	check_title_ls: "LS",
	check_title_rs: "RS",
	check_title_mkf: "MKF"
};


// finally extract current language from url
Translate.initCurrentLanguage();