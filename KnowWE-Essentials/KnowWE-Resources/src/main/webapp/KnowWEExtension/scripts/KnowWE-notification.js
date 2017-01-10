/**
 * Title: KnowWE-notification Notification mechanism used in KnowWE and
 * denkbares-Dialog (MobileApplication). This file uses the jQuery library and
 * is separated from KnowWE-helper.js in order to avoid conflicts with JSPWiki's
 * mootools.js and denkbares-Dialogs's scriptacouls.js
 */

if (typeof KNOWWE == "undefined" || !KNOWWE) {
	/**
	 * The KNOWWE global namespace object. If KNOWWE is already defined, the
	 * existing KNOWWE object will not be overwritten so that defined namespaces
	 * are preserved.
	 */
	var KNOWWE = {};
}
/**
 * Class: KNOWWE.notification functions and variables for the notification
 * mechanism.
 */
KNOWWE.notification = function () {

	var messages = [];
	var idGenerator = 0;

	return {

		error: function (title, details, id, fromServer) {
			KNOWWE.notification._add('error', title, details, id, fromServer);
		},

		warn: function (title, details, id, fromServer) {
			KNOWWE.notification._add('warn', title, details, id, fromServer);
		},

		success: function (title, details, id, fromServer) {
			KNOWWE.notification._add('success', title, details, id, fromServer);
		},

		_getDom: function () {
			if (!document.getElementById('KnowWENotificationDom')) {
				// title
				title = jq$('<div></div>').attr({
					id: 'KnowWENotificationTitle',
					style: 'float:left'
				});

				// message
				message = jq$('<div></div>').attr({
					id: 'KnowWENotificationMessage',
					style: 'width: 95%;'
				});

				// title + message -> wrapper
				wrapper = jq$('<div></div>');
				title.appendTo(wrapper);
				message.appendTo(wrapper);

				// counter
				counter = jq$('<div></div>').attr(
					{
						id: 'KnowWENotificationCounter',
						style: 'padding: 5px; ' + 'position: fixed; '
						+ 'top: 0px; ' + 'right: 25px;'
					});

				// quit
				quit = jq$('<div></div>').attr(
					{
						id: 'KnowWENotificationQuit',
						style: 'padding: 5px; ' + 'position: fixed; '
						+ 'top: 0px; ' + 'right: 10px;'
					});

				// dom
				dom = jq$('<div></div>').attr({
					id: 'KnowWENotificationDom'
				});
				wrapper.appendTo(dom);
				counter.appendTo(dom);
				quit.appendTo(dom);
				dom.appendTo('body');
			}
			return jq$('#KnowWENotificationDom');
		},

		_add: function (severity, title, details, id, fromServer) {
			if (!id) id = idGenerator++;
			var duplicate = false;
			var i = 0
			for (; i < messages.length; i++) {
				if (messages[i].id == id) {
					duplicate = true;
					break;
				}
			}
			var message = {
				severity: severity,
				title: title,
				details: details,
				id: id,
				fromServer: fromServer
			};
			if (duplicate) {
				messages[i] = message;
				KNOWWE.notification._select(i);
			} else {
				messages.push(message);
				KNOWWE.notification._select(messages.length - 1);
			}
		},

		/**
		 * Shows the notification with the specified index
		 */
		_select: function (index) {

			KNOWWE.notification.activeIndex = index;
			var message = messages[index];
			var dom = KNOWWE.notification._getDom();

			// set colors
			var startColor = '#fff6c3';
			var endColor = '#f9eba5';
			if (message.severity == 'error') {
				startColor = '#efd5d3';
				endColor = '#e6bbb8';
			}

			if (message.severity == 'success') {
				startColor = "rgba(186, 219, 189, 0.72)";
				endColor = "rgba(167, 249, 168, 0.72)";
			}

			// css
			dom.attr({
				style: 'opacity:0.95;' + 'position:fixed;' + 'z-index:2000;'
				+ 'border-bottom:1px solid #7a7a7a;' + 'top:0px;' + 'left:0px;'
				+ 'right:0px;' + 'width:100%;' + 'padding:5px;' + 'background-color: #f9eba5;'
				+ 'background-image: -webkit-gradient(linear, 0% 0%, 0% 100%, from(' + startColor
				+ '), to(' + endColor + '));' + 'background-image: -webkit-linear-gradient(top, '
				+ startColor + ',' + endColor + ');' + 'background-image: -moz-linear-gradient(top, '
				+ startColor + ',' + endColor + ');' + 'background-image: -ms-linear-gradient(top, '
				+ startColor + ',' + endColor + ');' + 'background-image: -o-linear-gradient(top, '
				+ startColor + ',' + endColor + ');'
			});

			// title
			if (message.title) {
				var titleHTML = '<strong>' + message.title + ':</strong>&nbsp;';
				jq$('#KnowWENotificationTitle').html(titleHTML);
			}

			// message
			jq$('#KnowWENotificationMessage').html(message.details);

			// counter
			if (messages.length > 1) {
				var counterHTML = "";
				var counter = '&nbsp;<span>(' + (index + 1) + '/'
					+ messages.length + ')</span>';
				var next = '&nbsp;<a href="#" onclick="KNOWWE.notification._select('
					+ (index + 1) + ');">&gt;</a>';
				var prev = '&nbsp;<a href="#" onclick="KNOWWE.notification._select('
					+ (index - 1) + ');">&lt;</a>';
				if (index == 0) {
					counterHTML += counter;
					counterHTML += next;
				} else if (index >= 0
					&& index < messages.length - 1) {
					counterHTML += prev;
					counterHTML += counter;
					counterHTML += next;
				} else if (index == messages.length - 1) {
					counterHTML += prev;
					counterHTML += counter;
				}
				jq$('#KnowWENotificationCounter').html(counterHTML);
			} else {
				jq$('#KnowWENotificationCounter').html("");
			}

			jq$('#KnowWENotificationQuit')
				.html(
					'<span><a onclick="KNOWWE.notification.removeNotification(\''
					+ message.id + '\')' + '">X</a></span>');

			// show notification bar
			jq$('#KnowWENotificationDom').show();
		},

		/**
		 * Removes a specified notification both from server and client
		 */
		removeNotification: function (id) {
			var index = -1;
			for (var i = 0; i < messages.length; i++) {
				if (messages[i].id == id) {
					index = i;
					break;
				}
			}
			if (index === -1) return;
			// quit: remove current message from stack
			messages.splice(index, 1);
			// quit: other notifications? show them!
			if (index > 0) {
				KNOWWE.notification._select(index - 1);
			} else if (index == 0
				&& messages.length > 1) {
				KNOWWE.notification._select(index);
				// quit: no other notifications? hide notification
				// bar!
			} else {
				jq$('#KnowWENotificationDom').hide();
			}

			var params = {
				action: 'RemoveNotificationAction',
				notificationid: id
			};

			var options = {
				url: KNOWWE.core.util.getURL(params),
				response: {
					action: 'none'
				}
			};
			new _KA(options).send();
		},

		/**
		 * Loads all notifications from the server and displays them.
		 */
		loadNotifications: function () {
			var params = {
				action: 'GetNotificationsAction'
			};

			var options = {
				url: KNOWWE.core.util.getURL(params),
				method: 'GET',
				response: {
					action: 'none',
					fn: function () {
						var notifications = JSON.parse(this.responseText);
						var idsToAdd = notifications.map(function (item) {
							return item.id;
						});
						// remove all messages that were from the server but are no longer present
						var idsToRemove = [];
						for (var i = 0; i < messages.length; i++) {
							if (messages[i].fromServer) {
								idsToRemove.push(messages[i].id);
							}
						}
						for (i = 0; i < idsToRemove.length; i++) {
							if (idsToAdd.indexOf(idsToRemove[i]) == -1) {
								KNOWWE.notification.removeNotification(idsToRemove[i]);
							}
						}
						if (notifications.length > 0) {
							for (i = 0; i < notifications.length; i++) {
								var notification = notifications[i];
								if (notification.type == "error") {
									KNOWWE.notification.error(null,
										notification.message,
										notification.id,
										true);
								} else {
									KNOWWE.notification.warn(null,
										notification.message,
										notification.id,
										true);
								}
							}
						}
					}
				}
			};
			new _KA(options).send();
		}
	}
}();

/**
 * Loads the notifications when DOM is ready
 */
(function init() {
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function () {
			KNOWWE.notification.loadNotifications();
			KNOWWE.helper.observer.subscribe('update',
				KNOWWE.notification.loadNotifications);
		});
	}
}());
