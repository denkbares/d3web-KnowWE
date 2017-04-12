var SelectKB = { };

SelectKB.displayBases = function () {
	var url = "../../../GetAvailableKnowledgeBases/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
		},
		onSuccess: function(transport) {
			var xml = transport.responseXML;
			// render bases of response
			var bases = xml.getElementsByTagName("base");
			var html = "";
			if (bases && bases.length > 0) {
				for (var i=0; i<bases.length; i++) {
					var name = bases[i].getAttribute("name");
					var description = bases[i].getAttribute("description");
					var icon = bases[i].getAttribute("icon");
					var baseHTML = "";
					baseHTML += "<div>";
					if (icon) {
						baseHTML += LookAndFeel.renderImage("../../../"+icon, "25%", 48);
					}
					baseHTML += "<b>"+name+"</b>";
					if (description) {
						baseHTML += "<br>"+description;
					}
					baseHTML += "</div>";
					html += LookAndFeel.renderSection(
							baseHTML,
							"CCAjaxIndicator.show();window.location.href=\"../../../SelectAvailableKnowledgeBase?index="+i+"&lang="+Translate.getCurrentLanguage()+"\";",
							false);
				}
			}
			else {
				html = "<p style='margin: 10px 0px 10px 0px;'>"+Translate.get("select_no_kb_message")+"</p>";
			}
			html = LookAndFeel.renderBox(html);
			$("bases").innerHTML = html;
		},
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
};
