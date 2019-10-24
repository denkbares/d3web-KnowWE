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
	let idGenerator = 0;

	return {

		error_jqXHR: function (jqXHR) {
			KNOWWE.notification.error(jqXHR.statusText, _KA.xhrExtractMessage(jqXHR));
		},

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
		},

		/**
		 * Shows the notification with the specified index
		 */
		_select: function (index) {

			KNOWWE.notification.activeIndex = index;
			const message = messages[index];
			const dom = KNOWWE.notification._getDom();

			// set colors
			let startColor = '#fff6c3';
			let endColor = '#f9eba5';
			if (message.severity === 'error') {
				startColor = '#efd5d3';
				endColor = '#e6bbb8';
			}

			if (message.severity === 'success') {
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
				const titleHTML = '<strong>' + message.title + ':</strong>&nbsp;';
				jq$('#KnowWENotificationTitle').html(titleHTML);
			}

			// message
			jq$('#KnowWENotificationMessage').html(message.details);

			// counter
			if (messages.length > 1) {
				let counterHTML = "";
				const counter = '&nbsp;<span>(' + (index + 1) + '/'
					+ messages.length + ')</span>';
				const next = '&nbsp;<a href="#" onclick="KNOWWE.notification._select('
					+ (index + 1) + ');">&gt;</a>';
				const prev = '&nbsp;<a href="#" onclick="KNOWWE.notification._select('
					+ (index - 1) + ');">&lt;</a>';
				if (index === 0) {
					counterHTML += counter;
					counterHTML += next;
				} else if (index >= 0
					&& index < messages.length - 1) {
					counterHTML += prev;
					counterHTML += counter;
					counterHTML += next;
				} else if (index === messages.length - 1) {
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
			} else if (index === 0 && messages.length > 1) {
				KNOWWE.notification._select(index);
				// quit: no other notifications? hide notification
				// bar!
			} else {
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
										true);
								} else if (notification.type === "warning") {
									KNOWWE.notification.warn(null,
										notification.message,
										notification.id,
										true);
								} else {
									KNOWWE.notification.success(null,
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
