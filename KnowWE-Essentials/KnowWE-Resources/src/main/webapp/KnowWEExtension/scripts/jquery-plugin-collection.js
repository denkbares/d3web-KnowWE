/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/**
 * KnowWE specific util methods using jQuery!
 */

(function(jq$) {

	/**
	 * Checks whether the selector returned any elements
	 */
	jq$.fn.exists = function() {
		return this.length > 0;
	};

	/**
	 * Scales the selected elements using a smooth css transition.
	 */
	jq$.fn.scale = function(scale) {
		if (scale) {
			this.css('transform', 'scale(' + scale + ')');
		} else {
			scale = "1.0";
			const transform = this.css('transform');
			if (transform !== "none" && transform !== "") {
				scale = /^matrix\((\d+(\.\d+)?), .+$/.exec(transform)[1];
			}
			return parseFloat(scale);
		}
	};


	jq$.fn.copyToClipboard = function(text) {
		const $temp = jq$("<input>");
		// in case we have a model dialog, we append to that one, otherwise the select() will not work
		let $parent = jq$(".ui-dialog");
		if (!$parent.exists()) {
			// otherwise we can use body
			$parent = jq$("body");
		}
		$parent.append($temp);
		if (!text) {
			text = jq$(this).text();
		}
		$temp.val(text).select();
		document.execCommand("copy");
		$temp.remove();
	};

	/**
	 * Sets or reads the current caret or cursor/selection.
	 *
	 * jq$("#id").caret(); // get the begin/end caret position
	 * jq$("#id").caret().begin;
	 * jq$("#id").caret().end;
	 * jq$("#otherId").caret(5); // set the caret position by index
	 * jq$("#otherId").caret(1, 5); // select a range
	 *
	 * This is an extract of the "Masked Input Plugin",
	 * Copyright (c) 2007-2014 Josh Bush (digitalbush.com).
	 */
	$.fn.caret = function(begin, end) {
		if (this.length === 0) return;
		if (typeof begin === 'number') {
			end = (typeof end === 'number') ? end : begin;
			return this.each(function() {
				if (this.setSelectionRange) {
					this.setSelectionRange(begin, end);
				} else if (this.createTextRange) {
					const range = this.createTextRange();
					range.collapse(true);
					range.moveEnd('character', end);
					range.moveStart('character', begin);
					try {
						range.select();
					} catch (ex) {
					}
				}
			});
		} else {
			if (this[0].setSelectionRange) {
				begin = this[0].selectionStart;
				end = this[0].selectionEnd;
			} else if (document.selection && document.selection.createRange) {
				let range = document.selection.createRange();
				begin = 0 - range.duplicate().moveStart('character', -100000);
				end = begin + range.text.length;
			}
			return {begin: begin, end: end};
		}
	};

	jq$.rerenderCounter = 0;
	jq$.lastRerenderRequests = {};

	/**
	 * Rerenders the selected elements. For now, the complete default markups or ReRenderSectionMarkers are rerendered.
	 * You can also just select successors of the default markup, the method will automatically choose the right
	 * elements to rerender.
	 */
	jq$.fn.rerender = function(options) {

		if (!options) options = {};
		let checkReplaceNeeded = options.checkReplaceNeeded;
		let beforeReplace = options.beforeReplace;
		let callback = options.callback;
		delete options.checkReplaceNeeded;
		delete options.beforeReplace;
		delete options.callback;

		function showGlobalProcessingState() {
			return typeof options.globalProcessingState === "undefined" || options.globalProcessingState === true;
		}

		this.each(function(i) {
			let $element = jq$(this);

			let id = $element.attr('sectionId');
			if (!id) id = $element.attr('id');

			const currentCounter = jq$.rerenderCounter++;
			const data = {
				SectionID: id,
				counter: currentCounter,
				localSectionStorage: KNOWWE.helper.getLocalSectionStorage(id)
			};
			jq$.extend(data, options);
			jq$.lastRerenderRequests[id] = currentCounter;


			if (showGlobalProcessingState()) {
				KNOWWE.core.util.updateProcessingState(1);
			}
			KNOWWE.helper.observer.notify("beforeRerender", $element);
			jq$.ajax({
				url: KNOWWE.core.util.getURL({
					action: 'ReRenderContentPartAction'
				}),
				type: 'post',
				cache: false,
				data: data
			}).success(function(data, status, jqXHR) {
				if (checkReplaceNeeded) {
					if (!checkReplaceNeeded.call(this, data, status, jqXHR)) {
						return
					}
				} else if (jqXHR.status === 304) {
					return; // no changes, do nothing
				}
				if (beforeReplace) {
					beforeReplace.call(this, $element, data);
				}
				const parsed = JSON.parse(data);
				if (jq$.lastRerenderRequests[id] !== parsed.counter) {
					// console.log("Skipping: " + parsed.counter);
					return; // another render request was send already, abort this one.
				}
				const html = parsed.html;
				if ($element.is('.ReRenderSectionMarker')) {
					$element.children().remove();
					$element.append(html);
				} else {
					const $newElement = jq$(html);
					$element.replaceWith($newElement);
					$element = $newElement;
				}
				jq$('#knowWEInfoStatus').val(parsed.status);
				KNOWWE.core.actions.init();
				KNOWWE.helper.observer.notify("afterRerender", $element);
				if (callback) {
					callback.call(this, $element);
				}
			}).always(function() {
				if (showGlobalProcessingState()) {
					KNOWWE.core.util.updateProcessingState(-1);
				}
			});
			// console.log("Send " + currentCounter + " " + id);
		});

	};

})(jQuery);

/**
 * External Plugins below
 */

/*!
 * jQuery Cookie Plugin v1.4.1
 * https://github.com/carhartl/jquery-cookie
 *
 * Copyright 2013 Klaus Hartl
 * Released under the MIT license
 */
(function(factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD
		define(['jquery'], factory);
	} else if (typeof exports === 'object') {
		// CommonJS
		factory(require('jquery'));
	} else {
		// Browser globals
		factory(jQuery);
	}
}(function(jq$) {

	const pluses = /\+/g;

	function encode(s) {
		return config.raw ? s : encodeURIComponent(s);
	}

	function decode(s) {
		if (config.raw) {
			return s;
		} else {
			try {
				return decodeURIComponent(s);
			} catch (err) {
				console.warn("Exception while decoding '" + s + "':", err);
				return s;
			}
		}
	}

	function stringifyCookieValue(value) {
		return encode(config.json ? JSON.stringify(value) : String(value));
	}

	function parseCookieValue(s) {
		if (s.indexOf('"') === 0) {
			// This is a quoted cookie as according to RFC2068, unescape...
			s = s.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\');
		}

		try {
			// Replace server-side written pluses with spaces.
			// If we can't decode the cookie, ignore it, it's unusable.
			// If we can't parse the cookie, ignore it, it's unusable.
			s = decodeURIComponent(s.replace(pluses, ' '));
			return config.json ? JSON.parse(s) : s;
		} catch (e) {
		}
	}

	function read(s, converter) {
		const value = config.raw ? s : parseCookieValue(s);
		return jq$.isFunction(converter) ? converter(value) : value;
	}

	let config = jq$.cookie = function(key, value, options) {

		// Write

		if (value !== undefined && !jq$.isFunction(value)) {
			options = jq$.extend({}, config.defaults, options);

			if (typeof options.expires === 'number') {
				const days = options.expires;
				let t = options.expires = new Date();
				t.setTime(+t + days * 864e+5);
			}

			return (document.cookie = [
				encode(key), '=', stringifyCookieValue(value),
				options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
				options.path ? '; path=' + options.path : '',
				options.domain ? '; domain=' + options.domain : '',
				options.secure ? '; secure' : ''
			].join(''));
		}

		// Read

		let result = key ? undefined : {};

		// To prevent the for loop in the first place assign an empty array
		// in case there are no cookies at all. Also prevents odd result when
		// calling jq$.cookie().
		const cookies = document.cookie ? document.cookie.split('; ') : [];

		let i = 0;
		const l = cookies.length;
		for (; i < l; i++) {
			const parts = cookies[i].split('=');
			const name = decode(parts.shift());
			let cookie = parts.join('=');

			if (key && key === name) {
				// If second argument (value) is a function it's a converter...
				result = read(cookie, value);
				break;
			}

			// Prevent storing a cookie that we couldn't decode.
			if (!key && (cookie = read(cookie)) !== undefined) {
				result[name] = cookie;
			}
		}

		return result;
	};

	config.defaults = {};

	jq$.removeCookie = function(key, options) {
		if (jq$.cookie(key) === undefined) {
			return false;
		}

		// Must not alter options, thus extending a fresh object...
		jq$.cookie(key, '', jq$.extend({}, options, {expires: -1}));
		return !jq$.cookie(key);
	};

}));


(function(jq$) {
	jq$.waitForFinalEvent = (function() {
		const timers = {};
		return function(callback, ms, uniqueId) {
			if (!uniqueId) {
				uniqueId = "Don't call this twice without a uniqueId";
			}
			if (timers[uniqueId]) {
				clearTimeout(timers[uniqueId]);
			}
			timers[uniqueId] = setTimeout(callback, ms);
		};
	})();
})(jQuery);

// noinspection JSCommentMatchesSignature
/**
 * Version 1.7.1
 *
 * ** means there is basic unit tests for this parameter.
 *
 * @name  Jeditable
 * @type  jQuery
 * @param String  target             (POST) URL or function to send edited content to **
 * @param Hash    options            additional options
 * @param String  options[method]    method to use to send edited content (POST or PUT) **
 * @param Function options[callback] Function to run after submitting edited content **
 * @param String  options[name]      POST parameter name of edited content
 * @param String  options[id]        POST parameter name of edited div id
 * @param Hash    options[submitdata] Extra parameters to send when submitting edited content.
 * @param String  options[type]      text, textarea or select (or any 3rd party input type) **
 * @param Integer options[rows]      number of rows if using textarea **
 * @param Integer options[cols]      number of columns if using textarea **
 * @param Mixed   options[height]    'auto', 'none' or height in pixels **
 * @param Mixed   options[width]     'auto', 'none' or width in pixels **
 * @param String  options[loadurl]   URL to fetch input content before editing **
 * @param String  options[loadtype]  Request type for load url. Should be GET or POST.
 * @param String  options[loadtext]  Text to display while loading external content.
 * @param Mixed   options[loaddata]  Extra parameters to pass when fetching content before editing.
 * @param Mixed   options[data]      Or content given as paramameter. String or function.**
 * @param String  options[indicator] indicator html to show when saving
 * @param String  options[tooltip]   optional tooltip text via title attribute **
 * @param String  options[event]     jQuery event such as 'click' of 'dblclick' **
 * @param String  options[submit]    submit button value, empty means no button **
 * @param String  options[cancel]    cancel button value, empty means no button **
 * @param String  options[cssclass]  CSS class to apply to input form. 'inherit' to copy from parent. **
 * @param String  options[style]     Style to apply to input form 'inherit' to copy from parent. **
 * @param String  options[select]    true or false, when true text is highlighted ??
 * @param String  options[placeholder] Placeholder text or html to insert when element is empty. **
 * @param String  options[onblur]    'cancel', 'submit', 'ignore' or function ??
 *
 * @param Function options[onsubmit] function(settings, original) { ... } called before submit
 * @param Function options[onreset]  function(settings, original) { ... } called before reset
 * @param Function options[afterreset]  function(settings, original) { ... } called after reset
 * @param Function options[onerror]  function(settings, original, xhr) { ... } called on error
 *
 * @param Hash    options[ajaxoptions]  jQuery Ajax options. See docs.jquery.com.
 *
 */

(function(jq$) {

	jq$.fn.editable = function(target, options) {

		if ('disable' === target) {
			jq$(this).data('disabled.editable', true);
			return;
		}
		if ('enableCompositeEdit' === target) {
			jq$(this).data('disabled.editable', false);
			return;
		}
		if ('destroy' === target) {
			jq$(this)
				.unbind(jq$(this).data('event.editable'))
				.removeData('disabled.editable')
				.removeData('event.editable');
			return;
		}

		const settings = jq$.extend({}, jq$.fn.editable.defaults, {target: target}, options);

		/* setup some functions */
		const plugin = jq$.editable.types[settings.type].plugin || function() {
		};
		const submit = jq$.editable.types[settings.type].submit || function() {
		};
		const buttons = jq$.editable.types[settings.type].buttons
			|| jq$.editable.types['defaults'].buttons;
		const content = jq$.editable.types[settings.type].content
			|| jq$.editable.types['defaults'].content;
		const element = jq$.editable.types[settings.type].element
			|| jq$.editable.types['defaults'].element;
		const reset = jq$.editable.types[settings.type].reset
			|| jq$.editable.types['defaults'].reset;
		const callback = settings.callback || function() {
		};
		const onedit = settings.onedit || function() {
		};
		const onsubmit = settings.onsubmit || function() {
		};
		const onreset = settings.onreset || function() {
		};
		const afterreset = settings.afterreset || function() {
		};
		const onerror = settings.onerror || reset;

		/* show tooltip */
		if (settings.tooltip) {
			jq$(this).attr('title', settings.tooltip);
		}

		settings.autowidth = 'auto' === settings.width;
		settings.autoheight = 'auto' === settings.height;

		return this.each(function() {

			/* save this to self because this changes when scope changes */
			const self = this;

			/* inlined block elements lose their width and height after first edit */
			/* save them for later use as workaround */
			const savedwidth = jq$(self).width();
			const savedheight = jq$(self).height();

			/* save so it can be later used by jq$.editable('destroy') */
			jq$(this).data('event.editable', settings.event);

			/* if element is empty add something clickable (if requested) */
			if (!jq$.trim(jq$(this).html())) {
				jq$(this).html(settings.placeholder);
			}

			jq$(this).bind(settings.event, function(e) {

				/* abort if disabled for this element */
				if (true === jq$(this).data('disabled.editable')) {
					return;
				}

				/* prevent throwing an exeption if edit field is clicked again */
				if (self.editing) {
					return;
				}

				/* abort if onedit hook returns false */
				if (false === onedit.apply(this, [settings, self])) {
					return;
				}

				/* prevent default action and bubbling */
				e.preventDefault();
				e.stopPropagation();

				/* remove tooltip */
				if (settings.tooltip) {
					jq$(self).removeAttr('title');
				}

				/* figure out how wide and tall we are, saved width and height */
				/* are workaround for http://dev.jquery.com/ticket/2190 */
				if (0 === jq$(self).width()) {
					//jq$(self).css('visibility', 'hidden');
					settings.width = savedwidth;
					settings.height = savedheight;
				} else {
					if (settings.width !== 'none') {
						settings.width =
							settings.autowidth ? jq$(self).width() : settings.width;
					}
					if (settings.height !== 'none') {
						settings.height =
							settings.autoheight ? jq$(self).height() : settings.height;
					}
				}
				//jq$(this).css('visibility', '');

				/* remove placeholder text, replace is here because of IE */
				if (jq$(this).html().toLowerCase().replace(/(;|")/g, '') ===
					settings.placeholder.toLowerCase().replace(/(;|")/g, '')) {
					jq$(this).html('');
				}

				self.editing = true;
				self.revert = jq$(self).html();
				jq$(self).html('');

				/* create the form object */
				const form = jq$('<form />');

				/* apply css or style or both */
				if (settings.cssclass) {
					if ('inherit' === settings.cssclass) {
						form.attr('class', jq$(self).attr('class'));
					} else {
						form.attr('class', settings.cssclass);
					}
				}

				if (settings.style) {
					if ('inherit' === settings.style) {
						form.attr('style', jq$(self).attr('style'));
						/* IE needs the second line or display wont be inherited */
						form.css('display', jq$(self).css('display'));
					} else {
						form.attr('style', settings.style);
					}
				}

				/* add main input element to form and store it in input */
				const input = element.apply(form, [settings, self]);

				/* set input content via POST, GET, given data or existing value */
				let input_content;
				let t;
				if (settings.loadurl) {
					t = setTimeout(function() {
						input.disabled = true;
						content.apply(form, [settings.loadtext, settings, self]);
					}, 100);

					const loaddata = {};
					loaddata[settings.id] = self.id;
					if (jq$.isFunction(settings.loaddata)) {
						jq$.extend(loaddata, settings.loaddata.apply(self, [self.revert, settings]));
					} else {
						jq$.extend(loaddata, settings.loaddata);
					}
					jq$.ajax({
						type: settings.loadtype,
						url: settings.loadurl,
						data: loaddata,
						async: false,
						success: function(result) {
							window.clearTimeout(t);
							input_content = result;
							input.disabled = false;
						}
					});
				} else if (settings.data) {
					input_content = settings.data;
					if (jq$.isFunction(settings.data)) {
						input_content = settings.data.apply(self, [self.revert, settings]);
					}
				} else {
					input_content = self.revert;
				}
				content.apply(form, [input_content, settings, self]);

				input.attr('name', settings.name);

				/* add buttons to the form */
				buttons.apply(form, [settings, self]);

				/* add created form to self */
				jq$(self).append(form);

				/* attach 3rd party plugin if requested */
				plugin.apply(form, [settings, self]);

				/* focus to first visible form element */
				jq$(':input:visible:enabled:first', form).focus();

				/* highlight input contents when requested */
				if (settings.select) {
					input.select();
				}

				/* discard changes if pressing esc */
				input.keydown(function(e) {
					if (e.keyCode === 27) {
						e.preventDefault();
						//self.reset();
						reset.apply(form, [settings, self]);
					}
				});

				/* discard, submit or nothing with changes when clicking outside */
				/* do nothing is usable when navigating with tab */
				if ('cancel' === settings.onblur) {
					input.blur(function(e) {
						/* prevent canceling if submit was clicked */
						t = setTimeout(function() {
							reset.apply(form, [settings, self]);
						}, 500);
					});
				} else if ('submit' === settings.onblur) {
					input.blur(function(e) {
						/* prevent double submit if submit was clicked */
						t = setTimeout(function() {
							form.submit();
						}, 200);
					});
				} else if (jq$.isFunction(settings.onblur)) {
					input.blur(function(e) {
						settings.onblur.apply(self, [input.val(), settings]);
					});
				} else {
					input.blur(function(e) {
						/* TODO: maybe something here */
					});
				}

				form.submit(function(e) {

					if (t) {
						clearTimeout(t);
					}

					/* do no submit */
					e.preventDefault();

					/* call before submit hook. */
					/* if it returns false abort submitting */
					if (false !== onsubmit.apply(form, [settings, self])) {
						/* custom inputs call before submit hook. */
						/* if it returns false abort submitting */
						if (false !== submit.apply(form, [settings, self])) {

							/* check if given target is function */
							if (jq$.isFunction(settings.target)) {
								const str = settings.target.apply(self, [input.val(), settings]);
								jq$(self).html(str);
								self.editing = false;
								callback.apply(self, [self.innerHTML, settings]);
								/* TODO: this is not dry */
								if (!jq$.trim(jq$(self).html())) {
									jq$(self).html(settings.placeholder);
								}
							} else {
								/* add edited content and id of edited element to POST */
								const submitdata = {};
								submitdata[settings.name] = input.val();
								submitdata[settings.id] = self.id;
								/* add extra data to be POST:ed */
								if (jq$.isFunction(settings.submitdata)) {
									jq$.extend(submitdata, settings.submitdata.apply(self, [self.revert, settings]));
								} else {
									jq$.extend(submitdata, settings.submitdata);
								}

								/* quick and dirty PUT support */
								if ('PUT' === settings.method) {
									submitdata['_method'] = 'put';
								}

								/* show the saving indicator */
								jq$(self).html(settings.indicator);

								/* defaults for ajaxoptions */
								const ajaxoptions = {
									type: 'POST',
									data: submitdata,
									dataType: 'html',
									url: settings.target,
									success: function(result, status) {
										if (ajaxoptions.dataType === 'html') {
											jq$(self).html(result);
										}
										self.editing = false;
										callback.apply(self, [result, settings]);
										if (!jq$.trim(jq$(self).html())) {
											jq$(self).html(settings.placeholder);
										}
									},
									error: function(xhr, status, error) {
										onerror.apply(form, [settings, self, xhr]);
									}
								};

								/* override with what is given in settings.ajaxoptions */
								jq$.extend(ajaxoptions, settings.ajaxoptions);
								jq$.ajax(ajaxoptions);

							}
						}
					}

					/* show tooltip again */
					jq$(self).attr('title', settings.tooltip);

					return false;
				});
			});

			/* privileged methods */
			this.reset = function(form) {
				/* prevent calling reset twice when blurring */
				if (this.editing) {
					/* before reset hook, if it returns false abort reseting */
					if (false !== onreset.apply(form, [settings, self])) {
						jq$(self).html(self.revert);
						self.editing = false;
						if (!jq$.trim(jq$(self).html())) {
							jq$(self).html(settings.placeholder);
						}
						/* show tooltip again */
						if (settings.tooltip) {
							jq$(self).attr('title', settings.tooltip);
						}
					}
					if (false !== afterreset.apply(form, [settings, self])) {
					}
				}
			};
		});

	};


	// noinspection JSUnusedGlobalSymbols
	jq$.editable = {
		types: {
			defaults: {
				element: function(settings, original) {
					const input = jq$('<input type="hidden">');
					jq$(this).append(input);
					return (input);
				},
				content: function(string, settings, original) {
					jq$(':input:first', this).val(string);
				},
				reset: function(settings, original) {
					original.reset(this);
				},
				buttons: function(settings, original) {
					const form = this;
					if (settings.submit) {
						/* if given html string use that */
						let submit;
						if (settings.submit.match(/>jq$/)) {
							submit = jq$(settings.submit).click(function() {
								if (submit.attr("type") !== "submit") {
									form.submit();
								}
							});
							/* otherwise use button with given string as text */
						} else {
							submit = jq$('<button type="submit" />');
							submit.html(settings.submit);
						}
						jq$(this).append(submit);
					}
					if (settings.cancel) {
						/* if given html string use that */
						let cancel;
						if (settings.cancel.match(/>jq$/)) {
							cancel = jq$(settings.cancel);
							/* otherwise use button with given string as text */
						} else {
							cancel = jq$('<button type="cancel" />');
							cancel.html(settings.cancel);
						}
						jq$(this).append(cancel);

						jq$(cancel).click(function() {
							//original.reset();
							let reset;
							if (jq$.isFunction(jq$.editable.types[settings.type].reset)) {
								reset = jq$.editable.types[settings.type].reset;
							} else {
								reset = jq$.editable.types['defaults'].reset;
							}
							reset.apply(form, [settings, original]);
							return false;
						});
					}
				}
			},
			text: {
				element: function(settings, original) {
					const input = jq$('<input />');
					if (settings.width !== 'none') {
						input.width(settings.width);
					}
					if (settings.height !== 'none') {
						input.height(settings.height);
					}
					/* https://bugzilla.mozilla.org/show_bug.cgi?id=236791 */
					//input[0].setAttribute('autocomplete','off');
					input.attr('autocomplete', 'off');
					jq$(this).append(input);
					return (input);
				}
			},
			textarea: {
				element: function(settings, original) {
					const textarea = jq$('<textarea />');
					if (settings.rows) {
						textarea.attr('rows', settings.rows);
					} else if (settings.height !== "none") {
						textarea.height(settings.height);
					}
					if (settings.cols) {
						textarea.attr('cols', settings.cols);
					} else if (settings.width !== "none") {
						textarea.width(settings.width);
					}
					jq$(this).append(textarea);
					return (textarea);
				}
			},
			select: {
				element: function(settings, original) {
					const select = jq$('<select />');
					jq$(this).append(select);
					return (select);
				},
				content: function(data, settings, original) {
					/* If it is string assume it is json. */
					let json;
					if (String === data.constructor) {
						eval('var json = ' + data);
					} else {
						/* Otherwise assume it is a hash already. */
						json = data;
					}
					for (let key in json) {
						if (!json.hasOwnProperty(key)) {
							continue;
						}
						if ('selected' === key) {
							continue;
						}
						const option = jq$('<option />').val(key).append(json[key]);
						jq$('select', this).append(option);
					}
					/* Loop option again to set selected. IE needed this... */
					jq$('select', this).children().each(function() {
						if (jq$(this).val() === json['selected'] ||
							jq$(this).text() === jq$.trim(original.revert)) {
							jq$(this).attr('selected', 'selected');
						}
					});
				}
			}
		},

		/* Add new input type */
		addInputType: function(name, input) {
			jq$.editable.types[name] = input;
		}
	};

	// publicly accessible defaults
	jq$.fn.editable.defaults = {
		name: 'value',
		id: 'id',
		type: 'text',
		width: 'auto',
		height: 'auto',
		event: 'click.editable',
		onblur: 'cancel',
		loadtype: 'GET',
		loadtext: 'Loading...',
		placeholder: 'Click to edit',
		loaddata: {},
		submitdata: {},
		ajaxoptions: {}
	};

})(jQuery);

jQuery.fn.insertAt = function(index, element) {
	const lastIndex = this.children().size();
	if (index < 0) {
		index = Math.max(0, lastIndex + 1 + index)
	}
	this.append(element);
	if (index < lastIndex) {
		this.children().eq(index).before(this.children().last())
	}
	return this;
};

/*!
 Autosize 1.18.15
 license: MIT
 http://www.jacklmoore.com/autosize
 */
(function($) {
	const defaults = {
			className: 'autosizejs',
			id: 'autosizejs',
			append: '\n',
			callback: false,
			resizeDelay: 10,
			placeholder: true
		},

		// border:0 is unnecessary, but avoids a bug in Firefox on OSX
		copy = '<textarea tabindex="-1" style="position:absolute; top:-999px; left:0; right:auto; bottom:auto; border:0; padding: 0; -moz-box-sizing:content-box; -webkit-box-sizing:content-box; box-sizing:content-box; word-wrap:break-word; height:0 !important; min-height:0 !important; overflow-y:hidden; transition:none; -webkit-transition:none; -moz-transition:none;"/>',

		// line-height is conditionally included because IE7/IE8/old Opera do not return the correct value.
		typographyStyles = [
			'fontFamily',
			'fontSize',
			'fontWeight',
			'fontStyle',
			'letterSpacing',
			'textTransform',
			'wordSpacing',
			'textIndent',
			'whiteSpace'
		];
	let // to keep track which textarea is being mirrored when adjust() is called.
		mirrored;
	const // the mirror element, which is used to calculate what size the mirrored element should be.
		mirror = $(copy).data('autosize', true)[0];

	// test that line-height can be accurately copied.
	mirror.style.lineHeight = '99px';
	if ($(mirror).css('lineHeight') === '99px') {
		typographyStyles.push('lineHeight');
	}
	mirror.style.lineHeight = '';

	$.fn.autosize = function(options) {
		if (!this.length) {
			return this;
		}

		options = $.extend({}, defaults, options || {});

		if (mirror.parentNode !== document.body) {
			$(document.body).append(mirror);
		}

		return this.each(function() {
			const ta = this,
				$ta = $(ta);
			let maxHeight,
				minHeight,
				boxOffset = 0;
			const callback = $.isFunction(options.callback),
				originalStyles = {
					height: ta.style.height,
					overflow: ta.style.overflow,
					overflowY: ta.style.overflowY,
					wordWrap: ta.style.wordWrap,
					resize: ta.style.resize
				};
			let timeout,
				width = $ta.width();
			const taResize = $ta.css('resize');

			if ($ta.data('autosize')) {
				// exit if autosize has already been applied, or if the textarea is the mirror element.
				return;
			}
			$ta.data('autosize', true);

			if ($ta.css('box-sizing') === 'border-box' || $ta.css('-moz-box-sizing') === 'border-box' || $ta.css('-webkit-box-sizing') === 'border-box') {
				boxOffset = $ta.outerHeight() - $ta.height();
			}

			// IE8 and lower return 'auto', which parses to NaN, if no min-height is set.
			minHeight = Math.max(parseFloat($ta.css('minHeight')) - boxOffset || 0, $ta.height());

			$ta.css({
				//overflow: 'hidden',
				//wordWrap: 'break-word' // horizontal overflow is hidden, so break-word is necessary for handling words longer than the textarea width
				overflowY: 'hidden',
			});

			if (taResize === 'vertical') {
				$ta.css('resize', 'none');
			} else if (taResize === 'both') {
				$ta.css('resize', 'horizontal');
			}

			// The mirror width must exactly match the textarea width, so using getBoundingClientRect because it doesn't round the sub-pixel value.
			// window.getComputedStyle, getBoundingClientRect returning a width are unsupported, but also unneeded in IE8 and lower.
			function setWidth() {
				let width;
				const style = window.getComputedStyle ? window.getComputedStyle(ta, null) : false;

				if (style) {

					width = ta.getBoundingClientRect().width;

					if (width === 0 || typeof width !== 'number') {
						width = parseFloat(style.width);
					}

					$.each(['paddingLeft', 'paddingRight', 'borderLeftWidth', 'borderRightWidth'], function(i, val) {
						width -= parseFloat(style[val]);
					});
				} else {
					width = $ta.width();
				}

				mirror.style.width = Math.max(width, 0) + 'px';
			}

			function initMirror() {
				const styles = {};

				mirrored = ta;
				mirror.className = options.className;
				mirror.id = options.id;
				maxHeight = parseFloat($ta.css('maxHeight'));

				// mirror is a duplicate textarea located off-screen that
				// is automatically updated to contain the same text as the
				// original textarea.  mirror always has a height of 0.
				// This gives a cross-browser supported way getting the actual
				// height of the text, through the scrollTop property.
				$.each(typographyStyles, function(i, val) {
					styles[val] = $ta.css(val);
				});

				$(mirror).css(styles).attr('wrap', $ta.attr('wrap'));

				setWidth();

				// Chrome-specific fix:
				// When the textarea y-overflow is hidden, Chrome doesn't reflow the text to account for the space
				// made available by removing the scrollbar. This workaround triggers the reflow for Chrome.
				if (window.chrome) {
					const width = ta.style.width;
					ta.style.width = '0px';
					const ignore = ta.offsetWidth;
					ta.style.width = width;
				}
			}

			// Using mainly bare JS in this function because it is going
			// to fire very often while typing, and needs to very efficient.
			function adjust() {
				let height, original;

				if (mirrored !== ta) {
					initMirror();
				} else {
					setWidth();
				}

				if (!ta.value && options.placeholder) {
					// If the textarea is empty, copy the placeholder text into
					// the mirror control and use that for sizing so that we
					// don't end up with placeholder getting trimmed.
					mirror.value = ($ta.attr("placeholder") || '');
				} else {
					mirror.value = ta.value;
				}

				mirror.value += options.append || '';
				mirror.style.overflowY = ta.style.overflowY;
				original = parseFloat(ta.style.height);

				// Setting scrollTop to zero is needed in IE8 and lower for the next step to be accurately applied
				mirror.scrollTop = 0;

				mirror.scrollTop = 9e4;

				// Using scrollTop rather than scrollHeight because scrollHeight is non-standard and includes padding.
				height = mirror.scrollTop;

				if (maxHeight && height > maxHeight) {
					ta.style.overflowY = 'scroll';
					height = maxHeight;
				} else {
					ta.style.overflowY = 'hidden';
					if (height < minHeight) {
						height = minHeight;
					}
				}

				height += boxOffset;

				if (original !== height) {
					ta.style.height = height + 'px';

					// Trigger a repaint for IE8 for when ta is nested 2 or more levels inside an inline-block
					// noinspection SillyAssignmentJS
					mirror.className = mirror.className;

					if (callback) {
						options.callback.call(ta, ta);
					}
					$ta.trigger('autosize.resized');
				}
			}

			function resize() {
				clearTimeout(timeout);
				timeout = setTimeout(function() {
					const newWidth = $ta.width();

					if (newWidth !== width) {
						width = newWidth;
						adjust();
					}
				}, parseInt(options.resizeDelay, 10));
			}

			if ('onpropertychange' in ta) {
				if ('oninput' in ta) {
					// Detects IE9.  IE9 does not fire onpropertychange or oninput for deletions,
					// so binding to onkeyup to catch most of those occasions.  There is no way that I
					// know of to detect something like 'cut' in IE9.
					$ta.on('input.autosize keyup.autosize', adjust);
				} else {
					// IE7 / IE8
					$ta.on('propertychange.autosize', function() {
						if (event.propertyName === 'value') {
							adjust();
						}
					});
				}
			} else {
				// Modern Browsers
				$ta.on('input.autosize', adjust);
			}

			// Set options.resizeDelay to false if using fixed-width textarea elements.
			// Uses a timeout and width check to reduce the amount of times adjust needs to be called after window resize.

			if (options.resizeDelay !== false) {
				$(window).on('resize.autosize', resize);
			}

			// Event for manual triggering if needed.
			// Should only be needed when the value of the textarea is changed through JavaScript rather than user input.
			$ta.on('autosize.resize', adjust);

			// Event for manual triggering that also forces the styles to update as well.
			// Should only be needed if one of typography styles of the textarea change, and the textarea is already the target of the adjust method.
			$ta.on('autosize.resizeIncludeStyle', function() {
				mirrored = null;
				adjust();
			});

			$ta.on('autosize.destroy', function() {
				mirrored = null;
				clearTimeout(timeout);
				$(window).off('resize', resize);
				$ta
					.off('autosize')
					.off('.autosize')
					.css(originalStyles)
					.removeData('autosize');
			});

			// Call adjust in case the textarea already contains text.
			adjust();
		});
	};
}(jQuery || $)); // jQuery or jQuery-like library, such as Zepto


/**
 * DatePicker and DateTimePicker, extended from jQuery UI
 */
(function(jq$) {

	jq$.datepicker.setDefaults({dateFormat: "dd.mm.yy"});

	const datepickerFunction = jq$.fn.datepicker;
	jq$.fn.datepicker = function() {
		this.attr('placeholder', "dd.mm.yyyy");
		return datepickerFunction.apply(this, arguments);
	};

	// ok, this implementation is pretty lame so far, implement something better if needed
	jq$.fn.datetimepicker = function() {
		this.attr('placeholder', "dd.mm.yyyy HH:MM");
	}
})(jQuery);


(function(jq$) {
	/**
	 * Converts a list of items (created e.g. using jq$.sortable()) that it auto-appends items as required. There will
	 * be an empty line at the end. If the line is filled, new items will be appended. Items may be reordered using
	 * ALT+UP / ALT+DOWN, or deleted using ALT+D / CMD+D.
	 *
	 * The parameters may contain the following options:
	 * <ul>
	 *     <li><b>createLine</b>: function with no parameters, to create a new empty line to be appended.
	 *     <li><b>isEmptyLine</b> (optional): function that gets the dom-element of a line.
	 *     The function should return true is the line is empty; by default a line is empty if all text fields are empty
	 *     <li><b>hasFocus</b> (optional): function that gets the dom-element of a line.
	 *     The function should return true if the line has the focus; by default a line has the focus if any text field has the focus
	 *     <li><b>requestFocus</b> (optional): function that gets the dom-element of a line.
	 *     The function should request the focus for the line; by default the first text field is focused
	 * </ul>
	 *
	 * @param params the parameters to configure the auto-appending list
	 * @returns {jQuery}
	 */
	// TODO: check using of typescript to define classes for each line to be edited, including params-functions and "html <-> wikitext" conversion
	jq$.fn.appendable = function(params) {
		const $list = jq$(this[0]);

		// function to check for empty line:
		// by default an item is empty not any text fields has a value
		const isEmptyLine = params.isEmpty || function(item) {
			return !jq$(item).find("input, textarea").is(function() {
				return jq$.trim(jq$(this).val()) !== '';
			});
		};

		// function to create a new line
		const createLine = params.create;

		// function to check for focus:
		// by default if any input or textarea has focus
		const hasFocus = params.hasFocus || function(item) {
			return jq$(item).find("input, textarea").is(':focus');
		}

		// function to request the focus:
		// by default focus the first input or textarea
		const requestFocus = params.requestFocus || function(item) {
			return jq$(item).find("input, textarea").focus();
		}

		const ensureEmptyLines = function() {
			let $last = $list.children().last();
			if ($last.length === 1 && isEmptyLine($last.get(0))) {
				// clean-up multiple succeeding empty lines
				let $prev = $last.prev();
				if ($prev.length === 1 && isEmptyLine($prev.get(0))) {
					(hasFocus($prev.get(0)) ? $last : $prev).remove();
				}
			} else {
				// create empty line if required
				$list.append(createLine());
			}
		}

		$list.addClass("ui-appendable");
		ensureEmptyLines();

		// make sure that there is always one empty item at the end
		$list.on('input', ensureEmptyLines);

		$list.keydown(function(event) {
			let $item = $list.children().filter(function() {
				return hasFocus(this);
			});
			if ($item.length !== 1) return;

			const sortable = $list.hasClass('ui-sortable');
			const isNoModifier = !event.ctrlKey && !event.metaKey && !event.altKey && !event.shiftKey;
			const isAltOnly = !event.ctrlKey && !event.metaKey && event.altKey;
			const isCmdOnly = (!event.ctrlKey && event.metaKey && !event.altKey)
				|| (event.ctrlKey && !event.metaKey && !event.altKey);

			// use UP / DOWN to move cursor between lines
			if ((event.which === 38 || event.which === 40) && isNoModifier) {
				event.stopPropagation();
				event.preventDefault();
				const $text = $item.find('.ui-wiki-content-edit-area');
				if ($text.length > 0) {
					TextArea.focusNextTextArea(event.which === 38 ? $text.first() : $text.last(), event.which === 38);
				}
				return;
			}

			// alt + UP
			if (event.which === 38 && isAltOnly && sortable) {
				event.stopPropagation();
				event.preventDefault();
				let $prev = $item.prev();
				if ($prev.length === 1) $prev.insertAfter($item);
				ensureEmptyLines();
				return;
			}

			// alt + DOWN
			if (event.which === 40 && isAltOnly && sortable) {
				event.stopPropagation();
				event.preventDefault();
				let $next = $item.next();
				if ($next.length === 1) $next.insertBefore($item);
				ensureEmptyLines();
				return;
			}

			// alt + D, cmd + D
			if (event.which === 68 && (isAltOnly || isCmdOnly)) {
				event.stopPropagation();
				event.preventDefault();
				// remove, but not the last one
				const $next = $item.next();
				if ($next.length === 1) {
					requestFocus($next.get(0));
					$item.remove();
					ensureEmptyLines();
				}
				return;
			}
		});

		// this is needed, so others can keep chaining off of this
		return this;
	}
})(jQuery);
