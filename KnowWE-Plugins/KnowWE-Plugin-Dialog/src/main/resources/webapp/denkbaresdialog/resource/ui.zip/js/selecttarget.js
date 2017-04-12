var SelectTarget = { };

SelectTarget.displayTargets = function () {
	var url = "../../../GetAlternativeTargets/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			lang: Translate.getCurrentLanguage()
		},
		onSuccess: function(transport) {
			var xml = transport.responseXML;
			// render targets of response
			var targets = xml.getElementsByTagName("target");
			var html = "";
			if (targets) {
				for (var i=0; i<targets.length; i++) {
					var name = targets[i].getAttribute("name");
					var description = targets[i].getAttribute("description");
					var baseHTML = "";
					baseHTML += "<div>";
					
					baseHTML += "<b>"+name+"</b>";
					if (description) {
						baseHTML += "<br>"+description;
					}
					baseHTML += "</div>";
					html += LookAndFeel.renderSection(
							baseHTML,
							"window.location.href=\"../../../SelectAlternativeTarget?index="+i+"&lang="+Translate.getCurrentLanguage()+"\"",
							false);
				}
			}
			html = LookAndFeel.renderBox(html);
			$("targets").innerHTML = html;
		},
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
};

SelectTarget.openSelection = function () {
	window.location = "../html/selectAlternatives.html?KWikiWeb=default_web&lang="+Translate.getCurrentLanguage();
};
