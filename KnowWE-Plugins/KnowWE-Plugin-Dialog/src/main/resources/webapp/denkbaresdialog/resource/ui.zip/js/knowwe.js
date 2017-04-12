/**
 * 
 * This class overwrites some of KnowWE's JS methods
 * to meet the requirements of DenkbaresDialog.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * 
 */

KNOWWE.notification.loadNotifications = function() {
	KNOWWE.notification.messages = [];
	var url = "../../../GetNotificationsAction/"+(new Date().getTime());
	new Ajax.Request(url, {
		onSuccess: function(transport) {
			var notifications = JSON.parse(transport.responseText);
			if (notifications.length > 0) {
			    for (var i = 0; i < notifications.length; i++) {
			        var notification = notifications[i];
				    if (notification.type == "error") {
					    KNOWWE.notification.error(null,
							    notification.message, notification.id);
				    } else {
					    KNOWWE.notification.warn(null,
							notification.message, notification.id);
				    }
			    }
		    } else {
		        jq$('#KnowWENotificationDom').hide();
		    }
		}
	});
}

KNOWWE.notification.removeNotification = function(id) {
	var url = "../../../RemoveNotificationAction/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			notificationid : id
		},
		onSuccess: function(transport) {
			var index = KNOWWE.notification.activeIndex;
			// quit: remove current message from stack
			KNOWWE.notification.messages.splice(index, 1);
			// quit: other notifications? show them!
			if (index > 0) {
				KNOWWE.notification._select(index - 1);
			} else if (index == 0 && KNOWWE.notification.messages.length > 1) {
				KNOWWE.notification._select(index);
			// quit: no other notifications? hide notification bar!
			} else {
				jq$('#KnowWENotificationDom').hide();
			}
		}
	});
}


if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	KNOWWE.plugin = function() {
		return {}
	}
}
if (typeof KNOWWE.plugin.d3webbasic == "undefined" || !KNOWWE.plugin.d3webbasic) {
	KNOWWE.plugin.d3webbasic = function() {
		return {}
	}
}
if (typeof KNOWWE.plugin.d3webbasic.actions == "undefined" || !KNOWWE.plugin.d3webbasic.actions) {
	KNOWWE.plugin.d3webbasic.actions = function() {
		return {}
	}
}

KNOWWE.plugin.d3webbasic.actions.resetSession = Interview.startCase;