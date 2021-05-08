/**
 * Title: KnowWE-notification Notification mechanism used in KnowWE and
 * denkbares-Dialog (MobileApplication). This file uses the jQuery library and
 * is separated from KnowWE-helper.js in order to avoid conflicts with JSPWiki's
 * mootools.js and denkbares-Dialogs's scriptacouls.js
 */

KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;
/**
 * Class: KNOWWE.notification functions and variables for the notification
 * mechanism.
 */
KNOWWE.notification = function () {

	const messages = [];
	const repeatedMessageCounter = {};
	let idGenerator = 0;

	return {

		error_jqXHR: function (jqXHR) {
			let title = jqXHR.statusText;
			if (title) title = title.charAt(0).toUpperCase() + title.slice(1)
			KNOWWE.notification.error(title, _KA.xhrExtractMessage(jqXHR));
		},

		/**
		 * Show an error message (red)
		 *
		 * @param title bold text at start of the message
		 * @param details actual message text (normal font)
		 * @param id id of the message (allows to remove message)
		 * @param autoHideMillis optional number > 0 in milli seconds, after which the message will hide automatically
		 */
		error: function (title, details, id, autoHideMillis) {
			KNOWWE.notification._add('error', title, details, id, autoHideMillis);
		},

		/**
		 * Show a warning message (yellow)
		 *
		 * @param title bold text at start of the message
		 * @param details actual message text (normal font)
		 * @param id id of the message (allows to remove message)
		 * @param autoHideMillis optional number > 0 in milli seconds, after which the message will hide automatically
		 */
		warn: function (title, details, id, autoHideMillis) {
			KNOWWE.notification._add('warn', title, details, id, autoHideMillis);
		},

		/**
		 * Show a success message (green)
		 *
		 * @param title bold text at start of the message
		 * @param details actual message text (normal font)
		 * @param id id of the message (allows to remove message)
		 * @param autoHideMillis optional number > 0 in milli seconds, after which the message will hide automatically
		 */
		success: function (title, details, id, autoHideMillis) {
			KNOWWE.notification._add('success', title, details, id, autoHideMillis);
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
				});

				// title + message -> wrapper
				wrapper = jq$('<div></div>');
				title.appendTo(wrapper);
				message.appendTo(wrapper);

				// counter
				counter = jq$('<div></div>').attr(
					{
						id: 'KnowWENotificationCounter',
					});

				// quit
				quit = jq$('<div></div>').attr(
					{
						id: 'KnowWENotificationQuit',
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

		_add: function (severity, title, details, id, autoHideMillis, fromServer) {
			if (!id) id = idGenerator++;
			let duplicate = false;
			let i = 0;
			for (; i < messages.length; i++) {
				// noinspection EqualityComparisonWithCoercionJS
				if (messages[i].id == id) {
					duplicate = true;
					break;
				}
			}
			const message = {
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

			if (autoHideMillis > 0) {
				if (!repeatedMessageCounter.hasOwnProperty(id)) {
					repeatedMessageCounter[id] = 0
				}
				repeatedMessageCounter[id] += 1;
				let currentMessageCounter = repeatedMessageCounter[id];
				setTimeout(function() {
					if (currentMessageCounter !== repeatedMessageCounter[id]) return;
				  KNOWWE.notification.removeNotification(id)
				}, autoHideMillis);
			}
		},

		/**
		 * Shows the notification with the specified index
		 */
		_select: function (index) {

			KNOWWE.notification.activeIndex = index;
			const message = messages[index];
			const dom = KNOWWE.notification._getDom();

			// css
			dom.addClass("notification-" + message.severity);

			// title
			if (message.title) {
				const titleHTML = '<strong>' + message.title + ':</strong>&nbsp;';
				jq$('#KnowWENotificationTitle').html(titleHTML);
			}

			// message
			jq$('#KnowWENotificationMessage').html(message.details);

			// counter
			if (messages.length > 1) {
				let showNext = index < messages.length - 1;
				let showPrev = index > 0;

				let counterHTML = "";
				const counter = '<span class="notification-counter">(' + (index + 1) + '/'
					+ messages.length + ')</span>';
				const next = '<a class="next-notification' + (showNext ? '' : ' inactive') + '" href="#" onclick="KNOWWE.notification._select('
					+ (index + 1) + ');"><i class="far fa-angle-right"></i></a>';
				const prev = '<a class="prev-notification' + (showPrev ? '' : ' inactive') + '" href="#" onclick="KNOWWE.notification._select('
					+ (index - 1) + ');"><i class="far fa-angle-left"></i></a>';

        counterHTML += prev;
        counterHTML += counter;
        counterHTML += next;
					
				jq$('#KnowWENotificationCounter').html(counterHTML);
			} else {
				jq$('#KnowWENotificationCounter').html("");
			}

			jq$('#KnowWENotificationQuit')
				.html(
					'<span><a onclick="KNOWWE.notification.removeNotification(\''
					+ message.id + '\')' + '"><i class="far fa-times"></i></a></span>');

			// show notification bar
			jq$('#KnowWENotificationDom').show();
		},

		/**
		 * Removes a specified notification both from server and client
		 */
		removeNotification: function (id) {
			let index = -1;
			for (let i = 0; i < messages.length; i++) {
				// noinspection EqualityComparisonWithCoercionJS
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
			}
			else if (index === 0 && messages.length > 0) {
				KNOWWE.notification._select(index);
				// quit: no other notifications? hide notification
				// bar!
			}
			else {
				jq$('#KnowWENotificationDom').hide();
			}

			const params = {
				action: 'RemoveNotificationAction',
				notificationid: id
			};

			const options = {
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
			const params = {
				action: 'GetNotificationsAction'
			};

			const options = {
				url: KNOWWE.core.util.getURL(params),
				method: 'GET',
				response: {
					action: 'none',
					fn: function () {
						const notifications = JSON.parse(this.responseText);
						const idsToAdd = notifications.map(function (item) {
							return item.id;
						});
						// remove all messages that were from the server but are no longer present
						const idsToRemove = [];
						for (let i = 0; i < messages.length; i++) {
							if (messages[i].fromServer) {
								idsToRemove.push(messages[i].id);
							}
						}
						for (let i = 0; i < idsToRemove.length; i++) {
							if (idsToAdd.indexOf(idsToRemove[i]) === -1) {
								KNOWWE.notification.removeNotification(idsToRemove[i]);
							}
						}
						if (notifications.length > 0) {
							for (let i = 0; i < notifications.length; i++) {
								const notification = notifications[i];
								if (notification.type === "error") {
									KNOWWE.notification.error(null,
										notification.message,
										notification.id,
										0,
										true);
								} else if (notification.type === "warning") {
									KNOWWE.notification.warn(null,
										notification.message,
										notification.id,
										0,
										true);
								} else {
									KNOWWE.notification.success(null,
										notification.message,
										notification.id,
										0,
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
