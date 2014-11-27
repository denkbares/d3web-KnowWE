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
(function(jq$) {
	jq$.cookie = function(key, value, options) {

		// key and at least value given, set cookie...
		if (arguments.length > 1 && (!/Object/.test(Object.prototype.toString.call(value)) || value === null || value === undefined)) {
			options = jq$.extend({}, options);

			if (value === null || value === undefined) {
				options.expires = -1;
			}

			if (typeof options.expires === 'number') {
				var days = options.expires, t = options.expires = new Date();
				t.setDate(t.getDate() + days);
			}

			value = String(value);

			return (document.cookie = [
				encodeURIComponent(key), '=', options.raw ? value : encodeURIComponent(value),
				options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
				options.path ? '; path=' + options.path : '',
				options.domain ? '; domain=' + options.domain : '',
				options.secure ? '; secure' : ''
			].join(''));
		}

		// key and possibly options given, get cookie...
		options = value || {};
		var decode = options.raw ? function(s) {
			return s;
		} : decodeURIComponent;

		var pairs = document.cookie.split('; ');
		for (var i = 0, pair; pair = pairs[i] && pairs[i].split('='); i++) {
			if (decode(pair[0]) === key) return decode(pair[1] || ''); // IE saves cookies with empty string as "c; ", e.g. without "=" as opposed to EOMB, thus pair[1] may be undefined
		}
		return null;
	};
	jq$.extend({
		getUrlVars : function() {
			var vars = [], hash;
			var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
			for (var i = 0; i < hashes.length; i++) {
				hash = hashes[i].split('=');
				vars.push(hash[0]);
				vars[hash[0]] = hash[1];
			}
			return vars;
		},
		getUrlVar : function(name) {
			return $.getUrlVars()[name];
		}
	});
})(jQuery);

(function(jq$) {
	jQuery.fn.exists = function() {
		return this.length > 0;
	}

	jQuery.fn.scale = function(scale) {
		if (scale) {
			this.css('transform', 'scale(' + scale + ')');
		} else {
			scale = "1.0";
			var transform = this.css('transform');
			if (transform != "none") {
				scale = /^matrix\((\d+(\.\d+)?), .+$/.exec(transform)[1];
			}
			return parseFloat(scale);
		}
	}

})(jQuery);

(function(jq$) {
	jq$.waitForFinalEvent = (function() {
		var timers = {};
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

		if ('disable' == target) {
			jq$(this).data('disabled.editable', true);
			return;
		}
		if ('enableCompositeEdit' == target) {
			jq$(this).data('disabled.editable', false);
			return;
		}
		if ('destroy' == target) {
			jq$(this)
				.unbind(jq$(this).data('event.editable'))
				.removeData('disabled.editable')
				.removeData('event.editable');
			return;
		}

		var settings = jq$.extend({}, jq$.fn.editable.defaults, {target : target}, options);

		/* setup some functions */
		var plugin = jq$.editable.types[settings.type].plugin || function() {
			};
		var submit = jq$.editable.types[settings.type].submit || function() {
			};
		var buttons = jq$.editable.types[settings.type].buttons
			|| jq$.editable.types['defaults'].buttons;
		var content = jq$.editable.types[settings.type].content
			|| jq$.editable.types['defaults'].content;
		var element = jq$.editable.types[settings.type].element
			|| jq$.editable.types['defaults'].element;
		var reset = jq$.editable.types[settings.type].reset
			|| jq$.editable.types['defaults'].reset;
		var callback = settings.callback || function() {
			};
		var onedit = settings.onedit || function() {
			};
		var onsubmit = settings.onsubmit || function() {
			};
		var onreset = settings.onreset || function() {
			};
		var afterreset = settings.afterreset || function() {
			};
		var onerror = settings.onerror || reset;

		/* show tooltip */
		if (settings.tooltip) {
			jq$(this).attr('title', settings.tooltip);
		}

		settings.autowidth = 'auto' == settings.width;
		settings.autoheight = 'auto' == settings.height;

		return this.each(function() {

			/* save this to self because this changes when scope changes */
			var self = this;

			/* inlined block elements lose their width and height after first edit */
			/* save them for later use as workaround */
			var savedwidth = jq$(self).width();
			var savedheight = jq$(self).height();

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
				if (0 == jq$(self).width()) {
					//jq$(self).css('visibility', 'hidden');
					settings.width = savedwidth;
					settings.height = savedheight;
				} else {
					if (settings.width != 'none') {
						settings.width =
							settings.autowidth ? jq$(self).width() : settings.width;
					}
					if (settings.height != 'none') {
						settings.height =
							settings.autoheight ? jq$(self).height() : settings.height;
					}
				}
				//jq$(this).css('visibility', '');

				/* remove placeholder text, replace is here because of IE */
				if (jq$(this).html().toLowerCase().replace(/(;|")/g, '') ==
					settings.placeholder.toLowerCase().replace(/(;|")/g, '')) {
					jq$(this).html('');
				}

				self.editing = true;
				self.revert = jq$(self).html();
				jq$(self).html('');

				/* create the form object */
				var form = jq$('<form />');

				/* apply css or style or both */
				if (settings.cssclass) {
					if ('inherit' == settings.cssclass) {
						form.attr('class', jq$(self).attr('class'));
					} else {
						form.attr('class', settings.cssclass);
					}
				}

				if (settings.style) {
					if ('inherit' == settings.style) {
						form.attr('style', jq$(self).attr('style'));
						/* IE needs the second line or display wont be inherited */
						form.css('display', jq$(self).css('display'));
					} else {
						form.attr('style', settings.style);
					}
				}

				/* add main input element to form and store it in input */
				var input = element.apply(form, [settings, self]);

				/* set input content via POST, GET, given data or existing value */
				var input_content;

				if (settings.loadurl) {
					var t = setTimeout(function() {
						input.disabled = true;
						content.apply(form, [settings.loadtext, settings, self]);
					}, 100);

					var loaddata = {};
					loaddata[settings.id] = self.id;
					if (jq$.isFunction(settings.loaddata)) {
						jq$.extend(loaddata, settings.loaddata.apply(self, [self.revert, settings]));
					} else {
						jq$.extend(loaddata, settings.loaddata);
					}
					jq$.ajax({
						type : settings.loadtype,
						url : settings.loadurl,
						data : loaddata,
						async : false,
						success : function(result) {
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
					if (e.keyCode == 27) {
						e.preventDefault();
						//self.reset();
						reset.apply(form, [settings, self]);
					}
				});

				/* discard, submit or nothing with changes when clicking outside */
				/* do nothing is usable when navigating with tab */
				var t;
				if ('cancel' == settings.onblur) {
					input.blur(function(e) {
						/* prevent canceling if submit was clicked */
						t = setTimeout(function() {
							reset.apply(form, [settings, self]);
						}, 500);
					});
				} else if ('submit' == settings.onblur) {
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
								var str = settings.target.apply(self, [input.val(), settings]);
								jq$(self).html(str);
								self.editing = false;
								callback.apply(self, [self.innerHTML, settings]);
								/* TODO: this is not dry */
								if (!jq$.trim(jq$(self).html())) {
									jq$(self).html(settings.placeholder);
								}
							} else {
								/* add edited content and id of edited element to POST */
								var submitdata = {};
								submitdata[settings.name] = input.val();
								submitdata[settings.id] = self.id;
								/* add extra data to be POST:ed */
								if (jq$.isFunction(settings.submitdata)) {
									jq$.extend(submitdata, settings.submitdata.apply(self, [self.revert, settings]));
								} else {
									jq$.extend(submitdata, settings.submitdata);
								}

								/* quick and dirty PUT support */
								if ('PUT' == settings.method) {
									submitdata['_method'] = 'put';
								}

								/* show the saving indicator */
								jq$(self).html(settings.indicator);

								/* defaults for ajaxoptions */
								var ajaxoptions = {
									type : 'POST',
									data : submitdata,
									dataType : 'html',
									url : settings.target,
									success : function(result, status) {
										if (ajaxoptions.dataType == 'html') {
											jq$(self).html(result);
										}
										self.editing = false;
										callback.apply(self, [result, settings]);
										if (!jq$.trim(jq$(self).html())) {
											jq$(self).html(settings.placeholder);
										}
									},
									error : function(xhr, status, error) {
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
					;
				}
			};
		});

	};


	jq$.editable = {
		types : {
			defaults : {
				element : function(settings, original) {
					var input = jq$('<input type="hidden">');
					jq$(this).append(input);
					return (input);
				},
				content : function(string, settings, original) {
					jq$(':input:first', this).val(string);
				},
				reset : function(settings, original) {
					original.reset(this);
				},
				buttons : function(settings, original) {
					var form = this;
					if (settings.submit) {
						/* if given html string use that */
						if (settings.submit.match(/>jq$/)) {
							var submit = jq$(settings.submit).click(function() {
								if (submit.attr("type") != "submit") {
									form.submit();
								}
							});
							/* otherwise use button with given string as text */
						} else {
							var submit = jq$('<button type="submit" />');
							submit.html(settings.submit);
						}
						jq$(this).append(submit);
					}
					if (settings.cancel) {
						/* if given html string use that */
						if (settings.cancel.match(/>jq$/)) {
							var cancel = jq$(settings.cancel);
							/* otherwise use button with given string as text */
						} else {
							var cancel = jq$('<button type="cancel" />');
							cancel.html(settings.cancel);
						}
						jq$(this).append(cancel);

						jq$(cancel).click(function(event) {
							//original.reset();
							if (jq$.isFunction(jq$.editable.types[settings.type].reset)) {
								var reset = jq$.editable.types[settings.type].reset;
							} else {
								var reset = jq$.editable.types['defaults'].reset;
							}
							reset.apply(form, [settings, original]);
							return false;
						});
					}
				}
			},
			text : {
				element : function(settings, original) {
					var input = jq$('<input />');
					if (settings.width != 'none') {
						input.width(settings.width);
					}
					if (settings.height != 'none') {
						input.height(settings.height);
					}
					/* https://bugzilla.mozilla.org/show_bug.cgi?id=236791 */
					//input[0].setAttribute('autocomplete','off');
					input.attr('autocomplete', 'off');
					jq$(this).append(input);
					return (input);
				}
			},
			textarea : {
				element : function(settings, original) {
					var textarea = jq$('<textarea />');
					if (settings.rows) {
						textarea.attr('rows', settings.rows);
					} else if (settings.height != "none") {
						textarea.height(settings.height);
					}
					if (settings.cols) {
						textarea.attr('cols', settings.cols);
					} else if (settings.width != "none") {
						textarea.width(settings.width);
					}
					jq$(this).append(textarea);
					return (textarea);
				}
			},
			select : {
				element : function(settings, original) {
					var select = jq$('<select />');
					jq$(this).append(select);
					return (select);
				},
				content : function(data, settings, original) {
					/* If it is string assume it is json. */
					if (String == data.constructor) {
						eval('var json = ' + data);
					} else {
						/* Otherwise assume it is a hash already. */
						var json = data;
					}
					for (var key in json) {
						if (!json.hasOwnProperty(key)) {
							continue;
						}
						if ('selected' == key) {
							continue;
						}
						var option = jq$('<option />').val(key).append(json[key]);
						jq$('select', this).append(option);
					}
					/* Loop option again to set selected. IE needed this... */
					jq$('select', this).children().each(function() {
						if (jq$(this).val() == json['selected'] ||
							jq$(this).text() == jq$.trim(original.revert)) {
							jq$(this).attr('selected', 'selected');
						}
					});
				}
			}
		},

		/* Add new input type */
		addInputType : function(name, input) {
			jq$.editable.types[name] = input;
		}
	};

	// publicly accessible defaults
	jq$.fn.editable.defaults = {
		name : 'value',
		id : 'id',
		type : 'text',
		width : 'auto',
		height : 'auto',
		event : 'click.editable',
		onblur : 'cancel',
		loadtype : 'GET',
		loadtext : 'Loading...',
		placeholder : 'Click to edit',
		loaddata : {},
		submitdata : {},
		ajaxoptions : {}
	};

})(jQuery);

jQuery.fn.insertAt = function(index, element) {
	var lastIndex = this.children().size();
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
(function ($) {
	var
		defaults = {
			className: 'autosizejs',
			id: 'autosizejs',
			append: '\n',
			callback: false,
			resizeDelay: 10,
			placeholder: true
		},

	// border:0 is unnecessary, but avoids a bug in Firefox on OSX
		copy = '<textarea tabindex="-1" style="position:absolute; top:-999px; left:0; right:auto; bottom:auto; border:0; padding: 0; -moz-box-sizing:content-box; -webkit-box-sizing:content-box; box-sizing:content-box; word-wrap:break-word; height:0 !important; min-height:0 !important; overflow:hidden; transition:none; -webkit-transition:none; -moz-transition:none;"/>',

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
		],

	// to keep track which textarea is being mirrored when adjust() is called.
		mirrored,

	// the mirror element, which is used to calculate what size the mirrored element should be.
		mirror = $(copy).data('autosize', true)[0];

	// test that line-height can be accurately copied.
	mirror.style.lineHeight = '99px';
	if ($(mirror).css('lineHeight') === '99px') {
		typographyStyles.push('lineHeight');
	}
	mirror.style.lineHeight = '';

	$.fn.autosize = function (options) {
		if (!this.length) {
			return this;
		}

		options = $.extend({}, defaults, options || {});

		if (mirror.parentNode !== document.body) {
			$(document.body).append(mirror);
		}

		return this.each(function () {
			var
				ta = this,
				$ta = $(ta),
				maxHeight,
				minHeight,
				boxOffset = 0,
				callback = $.isFunction(options.callback),
				originalStyles = {
					height: ta.style.height,
					overflow: ta.style.overflow,
					overflowY: ta.style.overflowY,
					wordWrap: ta.style.wordWrap,
					resize: ta.style.resize
				},
				timeout,
				width = $ta.width(),
				taResize = $ta.css('resize');

			if ($ta.data('autosize')) {
				// exit if autosize has already been applied, or if the textarea is the mirror element.
				return;
			}
			$ta.data('autosize', true);

			if ($ta.css('box-sizing') === 'border-box' || $ta.css('-moz-box-sizing') === 'border-box' || $ta.css('-webkit-box-sizing') === 'border-box'){
				boxOffset = $ta.outerHeight() - $ta.height();
			}

			// IE8 and lower return 'auto', which parses to NaN, if no min-height is set.
			minHeight = Math.max(parseFloat($ta.css('minHeight')) - boxOffset || 0, $ta.height());

			$ta.css({
				overflow: 'hidden',
				overflowY: 'hidden',
				wordWrap: 'break-word' // horizontal overflow is hidden, so break-word is necessary for handling words longer than the textarea width
			});

			if (taResize === 'vertical') {
				$ta.css('resize','none');
			} else if (taResize === 'both') {
				$ta.css('resize', 'horizontal');
			}

			// The mirror width must exactly match the textarea width, so using getBoundingClientRect because it doesn't round the sub-pixel value.
			// window.getComputedStyle, getBoundingClientRect returning a width are unsupported, but also unneeded in IE8 and lower.
			function setWidth() {
				var width;
				var style = window.getComputedStyle ? window.getComputedStyle(ta, null) : false;

				if (style) {

					width = ta.getBoundingClientRect().width;

					if (width === 0 || typeof width !== 'number') {
						width = parseFloat(style.width);
					}

					$.each(['paddingLeft', 'paddingRight', 'borderLeftWidth', 'borderRightWidth'], function(i,val){
						width -= parseFloat(style[val]);
					});
				} else {
					width = $ta.width();
				}

				mirror.style.width = Math.max(width,0) + 'px';
			}

			function initMirror() {
				var styles = {};

				mirrored = ta;
				mirror.className = options.className;
				mirror.id = options.id;
				maxHeight = parseFloat($ta.css('maxHeight'));

				// mirror is a duplicate textarea located off-screen that
				// is automatically updated to contain the same text as the
				// original textarea.  mirror always has a height of 0.
				// This gives a cross-browser supported way getting the actual
				// height of the text, through the scrollTop property.
				$.each(typographyStyles, function(i,val){
					styles[val] = $ta.css(val);
				});

				$(mirror).css(styles).attr('wrap', $ta.attr('wrap'));

				setWidth();

				// Chrome-specific fix:
				// When the textarea y-overflow is hidden, Chrome doesn't reflow the text to account for the space
				// made available by removing the scrollbar. This workaround triggers the reflow for Chrome.
				if (window.chrome) {
					var width = ta.style.width;
					ta.style.width = '0px';
					var ignore = ta.offsetWidth;
					ta.style.width = width;
				}
			}

			// Using mainly bare JS in this function because it is going
			// to fire very often while typing, and needs to very efficient.
			function adjust() {
				var height, original;

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
					mirror.className = mirror.className;

					if (callback) {
						options.callback.call(ta,ta);
					}
					$ta.trigger('autosize.resized');
				}
			}

			function resize () {
				clearTimeout(timeout);
				timeout = setTimeout(function(){
					var newWidth = $ta.width();

					if (newWidth !== width) {
						width = newWidth;
						adjust();
					}
				}, parseInt(options.resizeDelay,10));
			}

			if ('onpropertychange' in ta) {
				if ('oninput' in ta) {
					// Detects IE9.  IE9 does not fire onpropertychange or oninput for deletions,
					// so binding to onkeyup to catch most of those occasions.  There is no way that I
					// know of to detect something like 'cut' in IE9.
					$ta.on('input.autosize keyup.autosize', adjust);
				} else {
					// IE7 / IE8
					$ta.on('propertychange.autosize', function(){
						if(event.propertyName === 'value'){
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

			$ta.on('autosize.destroy', function(){
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
