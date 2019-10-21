const Translate = {
  DEFAULT_LANGUAGE: "en",
  currentLanguage: "en",
  entries: []
};

Translate.get = function(key) {

  let text;
  if (Translate.currentLanguage === 'NO_LANG') {
    text = Translate.entries[Translate.DEFAULT_LANGUAGE][key];
  } else {
    const localeSplitted = Translate.currentLanguage.split("_");
    const language = localeSplitted[0].toLowerCase();
    if (!Translate.entries[language]) {
      text = Translate.entries[Translate.DEFAULT_LANGUAGE][key];
    } else {
      text = Translate.entries[language][key];
    }
  }
  if (!text) {
    CCMessage.warn("access to unknown text constant: " + key);
    return key;
  } else {
    return text;
  }
};

Translate.initCurrentLanguage = function() {
  const url = window.location.href;
  const matches = /[\?&]lang=(\w*)/i.exec(url);
  const lang = matches && matches[1];
  if (lang) {
    if (lang === 'NO_LANG') {//explicit "NO_LANGUAGE"
      Translate.currentLanguage = lang;
      return;
    }
    //split at underscore to get the language
    const splittedLang = lang.split("_");
    // use only if we have text entries for that language
    if (splittedLang[0]) {
      Translate.currentLanguage = lang;
    }
  }
};

Translate.getAvailableLanguages = function() {
  return ["de", "en", "nl"];
};

Translate.getCurrentLanguage = function() {
  return Translate.currentLanguage;
};

Translate.fillLanguageSelector = function() {
  const url = "../../../GetLanguages/" + (new Date().getTime());
  new Ajax.Request(url, {
    method: 'get',
    parameters: {
      lang: Translate.getCurrentLanguage()
    },
    onSuccess: function(transport) {
      const xml = transport.responseXML;
      const langs = xml.getElementsByTagName("lang");
      if (!langs) {
        return;
      }
      const languageSelector = $('language-selector');
      const currentLanguage = Translate.getCurrentLanguage();

      let isCurrentLangAvailable = false;
      if (langs) {
        for (let i = 0; i < langs.length; i++) {
          const langCode = langs[i].getAttribute('code');
          const newOption = new Option(KBInfo._nodeText(langs[i]), langCode);
          languageSelector.options[languageSelector.length] = newOption;
          if (currentLanguage === langCode) {
            languageSelector.options[languageSelector.length - 1].selected = true;
            isCurrentLangAvailable = true;
          }
        }
        if (langs.length > 1) {
          languageSelector.style.display = 'inline';
        }
        // if current language is not available
        // (but there is at least one) then select first one
        if (!isCurrentLangAvailable && langs.length >= 1) {
          Translate.changeLanguage();
        }
      }
    },
    onFailure: function() {
      CCMessage.warn('AJAX Verbindungs-Fehler', "");
    },
    onException: function(transport, exception) {
      CCMessage.warn('AJAX interner Fehler', exception);
    }
  });
};

Translate.changeLanguage = function() {
  const languageSelector = $('language-selector');
  const selectedIndex = languageSelector.selectedIndex;
  const selectedValue = languageSelector.options[selectedIndex].value;
  window.location.href = window.location.pathname + "?lang=" + selectedValue;
};

Translate.entries["de"] = {
  use_title: "Interview",
  use_button_new: "Neu",
  use_button_sync: "Sync",
  use_button_save: "Speichern",
  use_button_save_successful: "Speichern erfolgreich",
  use_button_save_as: "Speichern unter...",
  use_button_load: "Laden",
  use_question_unknown: "unbekannt",

  result_title: "L&ouml;sungen",

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

  select_title: "Wissensbasis w&auml;hlen",
  select_no_kb_message: "Keine Wissensbasis verf&uuml;gbar.<br>Bitte &ouml;ffnen Sie zuerst eine Wissensbasis &uuml;ber das Men&uuml;.",
  select_target: "Pr&uuml;fschritt w&auml;hlen",
  loading_title: "Bitte warten"
};


Translate.entries["en"] = {
  use_title: "Interview",
  use_button_new: "New",
  use_button_sync: "Sync",
  use_button_save: "Save",
  use_button_save_successful: "Save successful",
  use_button_save_as: "Save as...",
  use_button_load: "Load",
  use_question_unknown: "unknown",

  result_title: "Solutions",

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

  select_title: "Select Knowledge Base",
  select_target: "Select Target",
  select_no_kb_message: "No knowledge base available.<br>Please open an existing knowledge base first.",

  loading_title: "Please wait"
};

Translate.entries["nl"] = {
  use_title: "Interview",
  use_button_new: "Nieuw",
  use_button_sync: "Sync",
  use_button_save: "Besparen",
  use_button_save_successful: "Besparen succesvol",
  use_button_save_as: "Besparen als...",
  use_question_unknown: "onbekend",

  result_title: "Oplossingen",

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

  select_title: "Klach klaant",
  select_no_kb_message: "Geen kennisbasis beschikbaar.<br>Gelieve voor het eerst opent een bestaande kennisbasis.",

  loading_title: "even geduld aub",
  select_target: "Selecteer doel"
};


// finally extract current language from url
Translate.initCurrentLanguage();
