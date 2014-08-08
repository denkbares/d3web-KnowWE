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
                options.path    ? '; path=' + options.path : '',
                options.domain  ? '; domain=' + options.domain : '',
                options.secure  ? '; secure' : ''
            ].join(''));
        }

        // key and possibly options given, get cookie...
        options = value || {};
        var decode = options.raw ? function(s) { return s; } : decodeURIComponent;

        var pairs = document.cookie.split('; ');
        for (var i = 0, pair; pair = pairs[i] && pairs[i].split('='); i++) {
            if (decode(pair[0]) === key) return decode(pair[1] || ''); // IE saves cookies with empty string as "c; ", e.g. without "=" as opposed to EOMB, thus pair[1] may be undefined
        }
        return null;
    };
    jq$.extend({
    	  getUrlVars: function(){
    	    var vars = [], hash;
    	    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    	    for(var i = 0; i < hashes.length; i++)
    	    {
    	      hash = hashes[i].split('=');
    	      vars.push(hash[0]);
    	      vars[hash[0]] = hash[1];
    	    }
    	    return vars;
    	  },
    	  getUrlVar: function(name){
    	    return $.getUrlVars()[name];
    	  }
    });
})(jQuery);

(function (jq$) {
	jQuery.fn.exists = function () {
		return this.length > 0;
	}

	jQuery.fn.scale = function (scale) {
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

(function (jq$) {

	jq$.fn.editable = function (target, options) {

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

		var settings = jq$.extend({}, jq$.fn.editable.defaults, {target: target}, options);

		/* setup some functions */
		var plugin = jq$.editable.types[settings.type].plugin || function () {
		};
		var submit = jq$.editable.types[settings.type].submit || function () {
		};
		var buttons = jq$.editable.types[settings.type].buttons
			|| jq$.editable.types['defaults'].buttons;
		var content = jq$.editable.types[settings.type].content
			|| jq$.editable.types['defaults'].content;
		var element = jq$.editable.types[settings.type].element
			|| jq$.editable.types['defaults'].element;
		var reset = jq$.editable.types[settings.type].reset
			|| jq$.editable.types['defaults'].reset;
		var callback = settings.callback || function () {
		};
		var onedit = settings.onedit || function () {
		};
		var onsubmit = settings.onsubmit || function () {
		};
		var onreset = settings.onreset || function () {
		};
		var afterreset = settings.afterreset || function () {
		};
		var onerror = settings.onerror || reset;

		/* show tooltip */
		if (settings.tooltip) {
			jq$(this).attr('title', settings.tooltip);
		}

		settings.autowidth = 'auto' == settings.width;
		settings.autoheight = 'auto' == settings.height;

		return this.each(function () {

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

			jq$(this).bind(settings.event, function (e) {

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
					var t = setTimeout(function () {
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
						type: settings.loadtype,
						url: settings.loadurl,
						data: loaddata,
						async: false,
						success: function (result) {
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
				input.keydown(function (e) {
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
					input.blur(function (e) {
						/* prevent canceling if submit was clicked */
						t = setTimeout(function () {
							reset.apply(form, [settings, self]);
						}, 500);
					});
				} else if ('submit' == settings.onblur) {
					input.blur(function (e) {
						/* prevent double submit if submit was clicked */
						t = setTimeout(function () {
							form.submit();
						}, 200);
					});
				} else if (jq$.isFunction(settings.onblur)) {
					input.blur(function (e) {
						settings.onblur.apply(self, [input.val(), settings]);
					});
				} else {
					input.blur(function (e) {
						/* TODO: maybe something here */
					});
				}

				form.submit(function (e) {

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
									type: 'POST',
									data: submitdata,
									dataType: 'html',
									url: settings.target,
									success: function (result, status) {
										if (ajaxoptions.dataType == 'html') {
											jq$(self).html(result);
										}
										self.editing = false;
										callback.apply(self, [result, settings]);
										if (!jq$.trim(jq$(self).html())) {
											jq$(self).html(settings.placeholder);
										}
									},
									error: function (xhr, status, error) {
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
			this.reset = function (form) {
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
		types: {
			defaults: {
				element: function (settings, original) {
					var input = jq$('<input type="hidden">');
					jq$(this).append(input);
					return(input);
				},
				content: function (string, settings, original) {
					jq$(':input:first', this).val(string);
				},
				reset: function (settings, original) {
					original.reset(this);
				},
				buttons: function (settings, original) {
					var form = this;
					if (settings.submit) {
						/* if given html string use that */
						if (settings.submit.match(/>jq$/)) {
							var submit = jq$(settings.submit).click(function () {
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

						jq$(cancel).click(function (event) {
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
			text: {
				element: function (settings, original) {
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
					return(input);
				}
			},
			textarea: {
				element: function (settings, original) {
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
					return(textarea);
				}
			},
			select: {
				element: function (settings, original) {
					var select = jq$('<select />');
					jq$(this).append(select);
					return(select);
				},
				content: function (data, settings, original) {
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
					jq$('select', this).children().each(function () {
						if (jq$(this).val() == json['selected'] ||
							jq$(this).text() == jq$.trim(original.revert)) {
							jq$(this).attr('selected', 'selected');
						}
					});
				}
			}
		},

		/* Add new input type */
		addInputType: function (name, input) {
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

