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

  /**
   * Copy from https://stackoverflow.com/questions/499126/jquery-set-cursor-position-in-text-area
   * Uses:
   * jq$('#elem').selectRange(3,5); // select a range of text
   * jq$('#elem').selectRange(3); // set cursor position
   */
  jq$.fn.selectRange = function(start, end) {
    if (end === undefined) {
      end = start;
    }
    return this.each(function() {
      if ('selectionStart' in this) {
        this.selectionStart = start;
        this.selectionEnd = end;
      } else if (this.setSelectionRange) {
        this.setSelectionRange(start, end);
      } else if (this.createTextRange) {
        var range = this.createTextRange();
        range.collapse(true);
        range.moveEnd('character', end);
        range.moveStart('character', start);
        range.select();
      }
    });
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
				localSectionStorage: KNOWWE.helper.getLocalSectionStorage(id, true)
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


//jquery-autogrow for automatic input field resizing (customized for KnowWE renaming)

(function(jq$) {
  let inherit;
  inherit = ['font', 'letter-spacing'];
  return jq$.fn.autoGrowRenameField = function(options) {
    let comfortZone, remove, _ref;
    remove = (options === 'remove' || options === false) || !!(options != null ? options.remove : void 0);
    comfortZone = (_ref = options != null ? options.comfortZone : void 0) != null ? _ref : options;
    if (comfortZone != null) {
      comfortZone = +comfortZone;
    }
    return this.each(function() {
      let check, cz, input, growWithSpan, prop, styles, testSubject, _i, _j, _len, _len1;
      input = jq$(this);
      growWithSpan = input.closest("span.toolMenuDecorated");
      testSubject = input.next().filter('div.autogrow');
      if (testSubject.length && remove) {
        input.unbind('input.autogrow');
        return testSubject.remove();
      } else if (testSubject.length) {
        styles = {};
        for (_i = 0, _len = inherit.length; _i < _len; _i++) {
          prop = inherit[_i];
          styles[prop] = input.css(prop);
        }
        testSubject.css(styles);
        if (comfortZone != null) {
          check = function() {
            testSubject.text(input.val());
            growWithSpan.width(testSubject.width() + comfortZone);
            return input.width(testSubject.width() + comfortZone);
          };
          input.unbind('input.autogrow');
          input.bind('input.autogrow', check);
          return check();
        }
      } else if (!remove) {

        input.css('min-width', '15px');
        growWithSpan.css('min-width', '15px');
        growWithSpan.css('padding-right', '10px');

        styles = {
          position: 'absolute',
          top: -99999,
          left: -99999,
          width: 'auto',
          visibility: 'hidden'
        };
        for (_j = 0, _len1 = inherit.length; _j < _len1; _j++) {
          prop = inherit[_j];
          styles[prop] = input.css(prop);
        }
        testSubject = jq$('<div class="autogrow"/>').css(styles);
        testSubject.insertAfter(input);
        cz = comfortZone != null ? comfortZone : 70;
        check = function() {
          testSubject.text(input.val());
          growWithSpan.width(testSubject.width() + cz);
          return input.width(testSubject.width() + cz);
        };
        input.bind('input.autogrow', check);
        return check();
      }
    });
  };
})(typeof Zepto !== "undefined" && Zepto !== null ? Zepto : jQuery);

/** @preserve jQuery.floatThead 2.2.1 - https://mkoryak.github.io/floatThead/ - Copyright (c) 2012 - 2020 Misha Koryak **/
// @license MIT

/* @author Misha Koryak
 * @projectDescription position:fixed on steroids. Lock a table header in place while scrolling.
 *
 * Dependencies:
 * jquery 1.9.0+ [required] OR jquery 1.7.0+ jquery UI core
 *
 * https://mkoryak.github.io/floatThead/
 *
 * Tested on FF13+, Chrome 21+, IE9, IE10, IE11, EDGE
 */
(function( $ ) {
  /**
   * provides a default config object. You can modify this after including this script if you want to change the init defaults
   * @type {!Object}
   */
  $.floatThead = $.floatThead || {};
  $.floatThead.defaults = {
    headerCellSelector: 'tr:visible:first>*:visible', //thead cells are this.
    zIndex: 1001, //zindex of the floating thead (actually a container div)
    position: 'auto', // 'fixed', 'absolute', 'auto'. auto picks the best for your table scrolling type.
    top: 0, //String or function($table) - offset from top of window where the header should not pass above
    bottom: 0, //String or function($table) - offset from the bottom of the table where the header should stop scrolling
    scrollContainer: function($table) { // or boolean 'true' (use offsetParent) | function -> if the table has horizontal scroll bars then this is the container that has overflow:auto and causes those scroll bars
      return $([]);
    },
    responsiveContainer: function($table) { // only valid if scrollContainer is not used (ie window scrolling). this is the container which will control y scrolling at some mobile breakpoints
      return $([]);
    },
    getSizingRow: function($table, $cols, $fthCells){ // this is only called when using IE,
      // override it if the first row of the table is going to contain colgroups (any cell spans greater than one col)
      // it should return a jquery object containing a wrapped set of table cells comprising a row that contains no col spans and is visible
      return $table.find('tbody tr:visible:first>*:visible');
    },
    ariaLabel: function($table, $headerCell, columnIndex) { // This function will run for every header cell that exists in the table when we add aria-labels.
      // Override to customize the aria-label. NOTE: These labels will be added to the 'sizer cells' which get added to the real table and are not visible by the user (only screen readers),
      // The number of sizer columns might not match the header columns in your real table - I insert one sizer header cell per column. This means that if your table uses colspans or multiple header rows,
      // this will not be reflected by sizer cells. This is why I am giving you the `columnIndex`.
      return $headerCell.text();
    },
    floatTableClass: 'floatThead-table',
    floatWrapperClass: 'floatThead-wrapper',
    floatContainerClass: 'floatThead-container',
    copyTableClass: true, //copy 'class' attribute from table into the floated table so that the styles match.
    autoReflow: false, //(undocumented) - use MutationObserver api to reflow automatically when internal table DOM changes
    debug: false, //print possible issues (that don't prevent script loading) to console, if console exists.
    support: { //should we bind events that expect these frameworks to be present and/or check for them?
      bootstrap: true,
      datatables: true,
      jqueryUI: true,
      perfectScrollbar: true
    },
    floatContainerCss: {"overflow-x": "hidden"} // undocumented - css applied to the floatContainer
  };

  var util = (function underscoreShim(){
    var that = {};
    var hasOwnProperty = Object.prototype.hasOwnProperty, isThings = ['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp'];
    that.has = function(obj, key) {
      return hasOwnProperty.call(obj, key);
    };
    that.keys = Object.keys || function(obj) {
      if (obj !== Object(obj)) throw new TypeError('Invalid object');
      var keys = [];
      for (var key in obj) if (that.has(obj, key)) keys.push(key);
      return keys;
    };
    var idCounter = 0;
    that.uniqueId = function(prefix) {
      var id = ++idCounter + '';
      return prefix ? prefix + id : id;
    };
    $.each(isThings, function(){
      var name = this;
      that['is' + name] = function(obj) {
        return Object.prototype.toString.call(obj) === '[object ' + name + ']';
      };
    });
    that.debounce = function(func, wait, immediate) {
      var timeout, args, context, timestamp, result;
      return function() {
        context = this;
        args = arguments;
        timestamp = new Date();
        var later = function() {
          var last = (new Date()) - timestamp;
          if (last < wait) {
            timeout = setTimeout(later, wait - last);
          } else {
            timeout = null;
            if (!immediate) result = func.apply(context, args);
          }
        };
        var callNow = immediate && !timeout;
        if (!timeout) {
          timeout = setTimeout(later, wait);
        }
        if (callNow) result = func.apply(context, args);
        return result;
      };
    };
    return that;
  })();

  var globalCanObserveMutations = typeof MutationObserver !== 'undefined';


  //browser stuff
  var ieVersion = function(){for(var a=3,b=document.createElement("b"),c=b.all||[];a = 1+a,b.innerHTML="<!--[if gt IE "+ a +"]><i><![endif]-->",c[0];);return 4<a?a:document.documentMode}();
  var isFF = /Gecko\//.test(navigator.userAgent);
  var isWebkit = /WebKit\//.test(navigator.userAgent);
  var isRTL = /rtl/i.test(document.documentElement.dir || '');

  if(!(ieVersion || isFF || isWebkit)){
    ieVersion = 11; //yey a hack!
  }

  //safari 7 (and perhaps others) reports table width to be parent container's width if max-width is set on table. see: https://github.com/mkoryak/floatThead/issues/108
  var isTableWidthBug = function(){
    if(isWebkit) {
      var $test = $('<div>').css('width', 0).append(
        $('<table>').css('max-width', '100%').append(
          $('<tr>').append(
            $('<th>').append(
              $('<div>').css('min-width', 100).text('X')
            )
          )
        )
      );
      $("body").append($test);
      var ret = ($test.find("table").width() === 0);
      $test.remove();
      return ret;
    }
    return false;
  };

  var createElements = !isFF && !ieVersion; //FF can read width from <col> elements, but webkit cannot

  var $window = $(window);

  var buggyMatchMedia = isFF && window.matchMedia; // TODO remove when fixed: https://bugzilla.mozilla.org/show_bug.cgi?id=774398

  if(!window.matchMedia || buggyMatchMedia) {
    var _beforePrint = window.onbeforeprint;
    var _afterPrint = window.onafterprint;
    window.onbeforeprint = function () {
      _beforePrint && _beforePrint();
      $window.triggerHandler("fth-beforeprint");
    };
    window.onafterprint = function () {
      _afterPrint && _afterPrint();
      $window.triggerHandler("fth-afterprint");
    };
  }

  /**
   * @param eventName
   * @param cb
   */
  function windowResize(eventName, cb){
    if(ieVersion === 8){ //ie8 is crap: https://github.com/mkoryak/floatThead/issues/65
      var winWidth = $window.width();
      var debouncedCb = util.debounce(function(){
        var winWidthNew = $window.width();
        if(winWidth !== winWidthNew){
          winWidth = winWidthNew;
          cb();
        }
      }, 1);
      $window.on(eventName, debouncedCb);
    } else {
      $window.on(eventName, util.debounce(cb, 1));
    }
  }

  function getClosestScrollContainer($elem) {
    var elem = $elem[0];
    var parent = elem.parentElement;

    do {
      var pos = window
        .getComputedStyle(parent)
        .getPropertyValue('overflow');

      if (pos !== 'visible') break;

    } while (parent = parent.parentElement);

    if(parent === document.body){
      return $([]);
    }
    return $(parent);
  }


  function debug(str){
    window && window.console && window.console.error && window.console.error("jQuery.floatThead: " + str);
  }

  //returns fractional pixel widths
  function getOffsetWidth(el) {
    var rect = el.getBoundingClientRect();
    return rect.width || rect.right - rect.left;
  }

  /**
   * try to calculate the scrollbar width for your browser/os
   * @return {Number}
   */
  function scrollbarWidth() {
    var d = document.createElement("scrolltester");
    d.style.cssText = 'width:100px;height:100px;overflow:scroll!important;position:absolute;top:-9999px;display:block';
    document.body.appendChild(d);
    var result = d.offsetWidth - d.clientWidth;
    document.body.removeChild(d);
    return result;
  }

  /**
   * Check if a given table has been datatableized (https://datatables.net)
   * @param $table
   * @return {Boolean}
   */
  function isDatatable($table){
    if($table.dataTableSettings){
      for(var i = 0; i < $table.dataTableSettings.length; i++){
        var table = $table.dataTableSettings[i].nTable;
        if($table[0] === table){
          return true;
        }
      }
    }
    return false;
  }

  function tableWidth($table, $fthCells, isOuter){
    // see: https://github.com/mkoryak/floatThead/issues/108
    var fn = isOuter ? "outerWidth": "width";
    if(isTableWidthBug && $table.css("max-width")){
      var w = 0;
      if(isOuter) {
        w += parseInt($table.css("borderLeft"), 10);
        w += parseInt($table.css("borderRight"), 10);
      }
      for(var i=0; i < $fthCells.length; i++){
        w += getOffsetWidth($fthCells.get(i));
      }
      return w;
    } else {
      return $table[fn]();
    }
  }
  $.fn.floatThead = function(map){
    map = map || {};

    if(ieVersion < 8){
      return this; //no more crappy browser support.
    }

    if(util.isFunction(isTableWidthBug)) {
      isTableWidthBug = isTableWidthBug();
    }

    if(util.isString(map)){
      var command = map;
      var args = Array.prototype.slice.call(arguments, 1);
      var ret = this;
      this.filter('table').each(function(){
        var $this = $(this);
        var opts = $this.data('floatThead-lazy');
        if(opts){
          $this.floatThead(opts);
        }
        var obj = $this.data('floatThead-attached');
        if(obj && util.isFunction(obj[command])){
          var r = obj[command].apply(this, args);
          if(r !== undefined){
            ret = r;
          }
        }
      });
      return ret;
    }
    var opts = $.extend({}, $.floatThead.defaults || {}, map);

    $.each(map, function(key, val){
      if((!(key in $.floatThead.defaults)) && opts.debug){
        debug("Used ["+key+"] key to init plugin, but that param is not an option for the plugin. Valid options are: "+ (util.keys($.floatThead.defaults)).join(', '));
      }
    });
    if(opts.debug){
      var v = $.fn.jquery.split(".");
      if(parseInt(v[0], 10) === 1 && parseInt(v[1], 10) <= 7){
        debug("jQuery version "+$.fn.jquery+" detected! This plugin supports 1.8 or better, or 1.7.x with jQuery UI 1.8.24 -> http://jqueryui.com/resources/download/jquery-ui-1.8.24.zip")
      }
    }

    this.filter(':not(.'+opts.floatTableClass+')').each(function(){
      var floatTheadId = util.uniqueId();
      var $table = $(this);
      if($table.data('floatThead-attached')){
        return true; //continue the each loop
      }
      if(!$table.is('table')){
        throw new Error('jQuery.floatThead must be run on a table element. ex: $("table").floatThead();');
      }
      var canObserveMutations = opts.autoReflow && globalCanObserveMutations; //option defaults to false!
      var mObs = null; //mutation observer lives in here if we can use it / make it
      var $header = $table.children('thead:first');
      var $tbody = $table.children('tbody:first');
      if($header.length === 0 || $tbody.length === 0){
        if(opts.debug) {
          if($header.length === 0){
            debug('The thead element is missing.');
          } else{
            debug('The tbody element is missing.');
          }
        }
        $table.data('floatThead-lazy', opts);
        $table.unbind("reflow").one('reflow', function(){
          $table.floatThead(opts);
        });
        return;
      }
      if($table.data('floatThead-lazy')){
        $table.unbind("reflow");
      }
      $table.data('floatThead-lazy', false);

      var headerFloated = true;
      var scrollingTop, scrollingBottom;
      var scrollbarOffset = {vertical: 0, horizontal: 0};
      if(util.isFunction(scrollbarWidth)) {
        scrollbarWidth = scrollbarWidth();
      }

      var lastColumnCount = 0; //used by columnNum()

      if(opts.scrollContainer === true){
        opts.scrollContainer = getClosestScrollContainer;
      }

      var $scrollContainer = opts.scrollContainer($table) || $([]); //guard against returned nulls
      var locked = $scrollContainer.length > 0;
      var $responsiveContainer = locked ? $([]) : opts.responsiveContainer($table) || $([]);
      var responsive = isResponsiveContainerActive();

      var useAbsolutePositioning = null;



      if (opts.position === 'auto') {
        useAbsolutePositioning = null;
      } else if (opts.position === 'fixed') {
        useAbsolutePositioning = false;
      } else if (opts.position === 'absolute'){
        useAbsolutePositioning = true;
      } else if (opts.debug) {
        debug('Invalid value given to "position" option, valid is "fixed", "absolute" and "auto". You passed: ', opts.position);
      }

      if(useAbsolutePositioning == null){ //defaults: locked=true, !locked=false
        useAbsolutePositioning = locked;
      }
      var $caption = $table.find("caption");
      var haveCaption = $caption.length === 1;
      if(haveCaption){
        var captionAlignTop = ($caption.css("caption-side") || $caption.attr("align") || "top") === "top";
      }

      var $fthGrp = $('<fthfoot>').css({
        'display': 'table-footer-group',
        'border-spacing': 0,
        'height': 0,
        'border-collapse': 'collapse',
        'visibility': 'hidden'
      });

      var wrappedContainer = false; //used with absolute positioning enabled. did we need to wrap the scrollContainer/table with a relative div?
      var $wrapper = $([]); //used when absolute positioning enabled - wraps the table and the float container
      var absoluteToFixedOnScroll = ieVersion <= 9 && !locked && useAbsolutePositioning; //on IE using absolute positioning doesn't look good with window scrolling, so we change position to fixed on scroll, and then change it back to absolute when done.
      var $floatTable = $("<table/>");
      var $floatColGroup = $("<colgroup/>");
      var $tableColGroup = $table.children('colgroup:first');
      var existingColGroup = true;
      if($tableColGroup.length === 0){
        $tableColGroup = $("<colgroup/>");
        existingColGroup = false;
      }
      var colSelector = existingColGroup ? "col:visible" : "col";
      var $fthRow = $('<fthtr>').css({ //created unstyled elements (used for sizing the table because chrome can't read <col> width)
        'display': 'table-row',
        'border-spacing': 0,
        'height': 0,
        'border-collapse': 'collapse'
      });
      var $floatContainer = $('<div>').css(opts.floatContainerCss).attr('aria-hidden', 'true');
      var floatTableHidden = false; //this happens when the table is hidden and we do magic when making it visible
      var $newHeader = $("<thead/>");
      var $sizerRow = $('<tr class="size-row"/>');
      var $sizerCells = $([]);
      var $tableCells = $([]); //used for sizing - either $sizerCells or $tableColGroup cols. $tableColGroup cols are only created in chrome for borderCollapse:collapse because of a chrome bug.
      var $headerCells = $([]);
      var $fthCells = $([]); //created elements

      $newHeader.append($sizerRow);
      $table.prepend($tableColGroup);
      if(createElements){
        $fthGrp.append($fthRow);
        $table.append($fthGrp);
      }

      $floatTable.append($floatColGroup);
      $floatContainer.append($floatTable);
      if(opts.copyTableClass){
        $floatTable.attr('class', $table.attr('class'));
      }
      $floatTable.attr({ //copy over some deprecated table attributes that people still like to use. Good thing people don't use colgroups...
        'cellpadding': $table.attr('cellpadding'),
        'cellspacing': $table.attr('cellspacing'),
        'border': $table.attr('border')
      });
      var tableDisplayCss = $table.css('display');
      $floatTable.css({
        'borderCollapse': $table.css('borderCollapse'),
        'border': $table.css('border'),
        'display': tableDisplayCss
      });
      if(!locked){
        $floatTable.css('width', 'auto');
      }
      if(tableDisplayCss === 'none'){
        floatTableHidden = true;
      }

      $floatTable.addClass(opts.floatTableClass).css({'margin': 0, 'border-bottom-width': 0}); //must have no margins or you won't be able to click on things under floating table

      if(useAbsolutePositioning){
        var makeRelative = function($container, alwaysWrap){
          var positionCss = $container.css('position');
          var relativeToScrollContainer = (positionCss === "relative" || positionCss === "absolute");
          var $containerWrap = $container;
          if(!relativeToScrollContainer || alwaysWrap){
            var css = {"paddingLeft": $container.css('paddingLeft'), "paddingRight": $container.css('paddingRight')};
            $floatContainer.css(css);
            $containerWrap = $container.data('floatThead-containerWrap') || $container.wrap(
              $('<div>').addClass(opts.floatWrapperClass).css({
                'position': 'relative',
                'clear': 'both'
              })
            ).parent();
            $container.data('floatThead-containerWrap', $containerWrap); //multiple tables inside one scrolling container - #242
            wrappedContainer = true;
          }
          return $containerWrap;
        };
        if(locked){
          $wrapper = makeRelative($scrollContainer, true);
          $wrapper.prepend($floatContainer);
        } else {
          $wrapper = makeRelative($table);
          $table.before($floatContainer);
        }
      } else {
        $table.before($floatContainer);
      }


      $floatContainer.css({
        position: useAbsolutePositioning ? 'absolute' : 'fixed',
        marginTop: 0,
        top:  useAbsolutePositioning ? 0 : 'auto',
        zIndex: opts.zIndex,
        willChange: 'transform'
      });
      $floatContainer.addClass(opts.floatContainerClass);
      updateScrollingOffsets();

      var layoutFixed = {'table-layout': 'fixed'};
      var layoutAuto = {'table-layout': $table.css('tableLayout') || 'auto'};
      var originalTableWidth = $table[0].style.width || ""; //setting this to auto is bad: #70
      var originalTableMinWidth = $table.css('minWidth') || "";

      function eventName(name){
        return name+'.fth-'+floatTheadId+'.floatTHead'
      }

      function setHeaderHeight(){
        var headerHeight = 0;
        $header.children("tr:visible").each(function(){
          headerHeight += $(this).outerHeight(true);
        });
        if($table.css('border-collapse') === 'collapse') {
          var tableBorderTopHeight = parseInt($table.css('border-top-width'), 10);
          var cellBorderTopHeight = parseInt($table.find("thead tr:first").find(">*:first").css('border-top-width'), 10);
          if(tableBorderTopHeight > cellBorderTopHeight) {
            headerHeight -= (tableBorderTopHeight / 2); //id love to see some docs where this magic recipe is found..
          }
        }
        $sizerRow.outerHeight(headerHeight);
        $sizerCells.outerHeight(headerHeight);
      }


      function setFloatWidth(){
        var tw = tableWidth($table, $fthCells, true);
        var $container = responsive ? $responsiveContainer : $scrollContainer;
        var width = $container.length ? getOffsetWidth($container[0]) : tw;
        var floatContainerWidth = $container.css("overflow-y") !== 'hidden' ? width - scrollbarOffset.vertical : width;
        $floatContainer.width(floatContainerWidth);
        if(locked){
          var percent = 100 * tw / (floatContainerWidth);
          $floatTable.css('width', percent+'%');
        } else {
          $floatTable.css('width', tw+'px');
        }
      }

      function updateScrollingOffsets(){
        scrollingTop = (util.isFunction(opts.top) ? opts.top($table) : opts.top) || 0;
        scrollingBottom = (util.isFunction(opts.bottom) ? opts.bottom($table) : opts.bottom) || 0;
      }

      /**
       * get the number of columns and also rebuild resizer rows if the count is different than the last count
       */
      function columnNum(){
        var count;
        var $headerColumns = $header.find(opts.headerCellSelector);
        if(existingColGroup){
          count = $tableColGroup.find(colSelector).length;
        } else {
          count = 0;
          $headerColumns.each(function () {
            count += parseInt(($(this).attr('colspan') || 1), 10);
          });
        }
        if(count !== lastColumnCount){
          lastColumnCount = count;
          var cells = [], cols = [], psuedo = [];
          $sizerRow.empty();
          for(var x = 0; x < count; x++){
            var cell = document.createElement('th');
            cell.setAttribute('aria-label', opts.ariaLabel($table, $headerColumns.eq(x), x));
            cell.className = 'floatThead-col';
            $sizerRow[0].appendChild(cell);
            cols.push('<col/>');
            psuedo.push(
              $('<fthtd>').css({
                'display': 'table-cell',
                'height': 0,
                'width': 'auto'
              })
            );
          }

          if(existingColGroup){
            cols = $tableColGroup.html();
          } else {
            cols = cols.join('');
          }

          if(createElements){
            $fthRow.empty();
            $fthRow.append(psuedo);
            $fthCells = $fthRow.find('fthtd');
          }

          $sizerCells = $sizerRow.find("th");
          if(!existingColGroup){
            $tableColGroup.html(cols);
          }
          $tableCells = $tableColGroup.find(colSelector);
          $floatColGroup.html(cols);
          $headerCells = $floatColGroup.find(colSelector);

        }
        return count;
      }

      function refloat(){ //make the thing float
        if(!headerFloated){
          headerFloated = true;
          if(useAbsolutePositioning){ //#53, #56
            var tw = tableWidth($table, $fthCells, true);
            var wrapperWidth = $wrapper.width();
            if(tw > wrapperWidth){
              $table.css('minWidth', tw);
            }
          }
          $table.css(layoutFixed);
          $floatTable.css(layoutFixed);
          $floatTable.append($header); //append because colgroup must go first in chrome
          $tbody.before($newHeader);
          setHeaderHeight();
        }
      }
      function unfloat(){ //put the header back into the table
        if(headerFloated){
          headerFloated = false;
          if(useAbsolutePositioning){ //#53, #56
            $table.width(originalTableWidth);
          }
          $newHeader.detach();
          $table.prepend($header);
          $table.css(layoutAuto);
          $floatTable.css(layoutAuto);
          $table.css('minWidth', originalTableMinWidth); //this looks weird, but it's not a bug. Think about it!!
          $table.css('minWidth', tableWidth($table, $fthCells)); //#121
        }
      }
      var isHeaderFloatingLogical = false; //for the purpose of this event, the header is/isnt floating, even though the element
                                           //might be in some other state. this is what the header looks like to the user
      function triggerFloatEvent(isFloating){
        if(isHeaderFloatingLogical !== isFloating){
          isHeaderFloatingLogical = isFloating;
          $table.triggerHandler("floatThead", [isFloating, $floatContainer])
        }
      }
      function changePositioning(isAbsolute){
        if(useAbsolutePositioning !== isAbsolute){
          useAbsolutePositioning = isAbsolute;
          $floatContainer.css({
            position: useAbsolutePositioning ? 'absolute' : 'fixed'
          });
        }
      }
      function getSizingRow($table, $cols, $fthCells, ieVersion){
        if(createElements){
          return $fthCells;
        } else if(ieVersion) {
          return opts.getSizingRow($table, $cols, $fthCells);
        } else {
          return $cols;
        }
      }

      /**
       * returns a function that updates the floating header's cell widths.
       * @return {Function}
       */
      function reflow(){
        var i;
        var numCols = columnNum(); //if the tables columns changed dynamically since last time (datatables), rebuild the sizer rows and get a new count

        return function(){
          //Cache the current scrollLeft value so that it can be reset post reflow
          var scrollLeft = $floatContainer.scrollLeft();
          $tableCells = $tableColGroup.find(colSelector);
          var $rowCells = getSizingRow($table, $tableCells, $fthCells, ieVersion);

          if($rowCells.length === numCols && numCols > 0){
            if(!existingColGroup){
              for(i=0; i < numCols; i++){
                $tableCells.eq(i).css('width', '');
              }
            }
            unfloat();
            var widths = [];
            for(i=0; i < numCols; i++){
              widths[i] = getOffsetWidth($rowCells.get(i));
            }
            for(i=0; i < numCols; i++){
              $headerCells.eq(i).width(widths[i]);
              $tableCells.eq(i).width(widths[i]);
            }
            refloat();
          } else {
            $floatTable.append($header);
            $table.css(layoutAuto);
            $floatTable.css(layoutAuto);
            setHeaderHeight();
          }
          //Set back the current scrollLeft value on floatContainer
          $floatContainer.scrollLeft(scrollLeft);
          $table.triggerHandler("reflowed", [$floatContainer]);
        };
      }

      function floatContainerBorderWidth(side){
        var border = $scrollContainer.css("border-"+side+"-width");
        var w = 0;
        if (border && ~border.indexOf('px')) {
          w = parseInt(border, 10);
        }
        return w;
      }

      function isResponsiveContainerActive(){
        return $responsiveContainer.css("overflow-x") === 'auto';
      }
      /**
       * first performs initial calculations that we expect to not change when the table, window, or scrolling container are scrolled.
       * returns a function that calculates the floating container's top and left coords. takes into account if we are using page scrolling or inner scrolling
       * @return {Function}
       */
      function calculateFloatContainerPosFn(){
        var scrollingContainerTop = $scrollContainer.scrollTop();

        //this floatEnd calc was moved out of the returned function because we assume the table height doesn't change (otherwise we must reinit by calling calculateFloatContainerPosFn)
        var floatEnd;
        var tableContainerGap = 0;
        var captionHeight = haveCaption ? $caption.outerHeight(true) : 0;
        var captionScrollOffset = captionAlignTop ? captionHeight : -captionHeight;

        var floatContainerHeight = $floatContainer.height();
        var tableOffset = $table.offset();
        var tableLeftGap = 0; //can be caused by border on container (only in locked mode)
        var tableTopGap = 0;
        if(locked){
          var containerOffset = $scrollContainer.offset();
          tableContainerGap = tableOffset.top - containerOffset.top + scrollingContainerTop;
          if(haveCaption && captionAlignTop){
            tableContainerGap += captionHeight;
          }
          tableLeftGap = floatContainerBorderWidth('left');
          tableTopGap = floatContainerBorderWidth('top');
          tableContainerGap -= tableTopGap;
        } else {
          floatEnd = tableOffset.top - scrollingTop - floatContainerHeight + scrollingBottom + scrollbarOffset.horizontal;
        }
        var windowTop = $window.scrollTop();
        var windowLeft = $window.scrollLeft();
        var getScrollContainerLeft = function(){
          return (isResponsiveContainerActive() ?  $responsiveContainer : $scrollContainer).scrollLeft() || 0;
        };
        var scrollContainerLeft = getScrollContainerLeft();

        return function(eventType){
          responsive = isResponsiveContainerActive();

          var isTableHidden = $table[0].offsetWidth <= 0 && $table[0].offsetHeight <= 0;
          if(!isTableHidden && floatTableHidden) {
            floatTableHidden = false;
            setTimeout(function(){
              $table.triggerHandler("reflow");
            }, 1);
            return null;
          }
          if(isTableHidden){ //it's hidden
            floatTableHidden = true;
            if(!useAbsolutePositioning){
              return null;
            }
          }

          if(eventType === 'windowScroll'){
            windowTop = $window.scrollTop();
            windowLeft = $window.scrollLeft();
          } else if(eventType === 'containerScroll'){
            if($responsiveContainer.length){
              if(!responsive){
                return; //we dont care about the event if we arent responsive right now
              }
              scrollContainerLeft = $responsiveContainer.scrollLeft();
            } else {
              scrollingContainerTop = $scrollContainer.scrollTop();
              scrollContainerLeft = $scrollContainer.scrollLeft();
            }
          } else if(eventType !== 'init') {
            windowTop = $window.scrollTop();
            windowLeft = $window.scrollLeft();
            scrollingContainerTop = $scrollContainer.scrollTop();
            scrollContainerLeft =  getScrollContainerLeft();
          }
          if(isWebkit && (windowTop < 0 || (isRTL && windowLeft > 0 ) || ( !isRTL && windowLeft < 0 )) ){
            //chrome overscroll effect at the top of the page - breaks fixed positioned floated headers
            return;
          }

          if(absoluteToFixedOnScroll){
            if(eventType === 'windowScrollDone'){
              changePositioning(true); //change to absolute
            } else {
              changePositioning(false); //change to fixed
            }
          } else if(eventType === 'windowScrollDone'){
            return null; //event is fired when they stop scrolling. ignore it if not 'absoluteToFixedOnScroll'
          }

          tableOffset = $table.offset();
          if(haveCaption && captionAlignTop){
            tableOffset.top += captionHeight;
          }
          var top, left;
          var tableHeight = $table.outerHeight();

          if(locked && useAbsolutePositioning){ //inner scrolling, absolute positioning
            if (tableContainerGap >= scrollingContainerTop) {
              var gap = tableContainerGap - scrollingContainerTop + tableTopGap;
              top = gap > 0 ? gap : 0;
              triggerFloatEvent(false);
            } else if(scrollingContainerTop - tableContainerGap > tableHeight - floatContainerHeight){
              // scrolled past table but there is space in the container under it..
              top = tableHeight - floatContainerHeight - scrollingContainerTop - tableContainerGap;
            } else {
              top = wrappedContainer ? tableTopGap : scrollingContainerTop;
              //headers stop at the top of the viewport
              triggerFloatEvent(true);
            }
            left = tableLeftGap;
          } else if(!locked && useAbsolutePositioning) { //window scrolling, absolute positioning
            if(windowTop > floatEnd + tableHeight + captionScrollOffset){
              top = tableHeight - floatContainerHeight + captionScrollOffset + scrollingBottom; //scrolled past table
            } else if (tableOffset.top >= windowTop + scrollingTop) {
              top = 0; //scrolling to table
              unfloat();
              triggerFloatEvent(false);
            } else {
              top = scrollingTop + windowTop - tableOffset.top + tableContainerGap + (captionAlignTop ? captionHeight : 0);
              refloat(); //scrolling within table. header floated
              triggerFloatEvent(true);
            }
            left =  scrollContainerLeft;
          } else if(locked && !useAbsolutePositioning){ //inner scrolling, fixed positioning
            if (tableContainerGap > scrollingContainerTop || scrollingContainerTop - tableContainerGap > tableHeight) {
              top = tableOffset.top - windowTop;
              unfloat();
              triggerFloatEvent(false);
            } else {
              top = tableOffset.top + scrollingContainerTop  - windowTop - tableContainerGap;
              refloat();
              triggerFloatEvent(true);
              //headers stop at the top of the viewport
            }
            left = tableOffset.left + scrollContainerLeft - windowLeft;
          } else if(!locked && !useAbsolutePositioning) { //window scrolling, fixed positioning
            if(windowTop > floatEnd + tableHeight + captionScrollOffset){
              top = tableHeight + scrollingTop - windowTop + floatEnd + captionScrollOffset;
              //scrolled past the bottom of the table
            } else if (tableOffset.top > windowTop + scrollingTop) {
              top = tableOffset.top - windowTop;
              refloat();
              triggerFloatEvent(false); //this is a weird case, the header never gets unfloated and i have no no way to know
              //scrolled past the top of the table
            } else {
              //scrolling within the table
              top = scrollingTop;
              triggerFloatEvent(true);
            }
            left = tableOffset.left + scrollContainerLeft - windowLeft;
          }
          return {top: Math.round(top), left: Math.round(left)};
        };
      }
      /**
       * returns a function that caches old floating container position and only updates css when the position changes
       * @return {Function}
       */
      function repositionFloatContainerFn(){
        var oldTop = null;
        var oldLeft = null;
        var oldScrollLeft = null;
        return function(pos, setWidth, setHeight){
          if(pos != null && (oldTop !== pos.top || oldLeft !== pos.left)){
            if(ieVersion === 8){
              $floatContainer.css({
                top: pos.top,
                left: pos.left
              });
            } else {
              var transform = 'translateX(' + pos.left + 'px) translateY(' + pos.top + 'px)';
              var cssObj = {
                '-webkit-transform' : transform,
                '-moz-transform'    : transform,
                '-ms-transform'     : transform,
                '-o-transform'      : transform,
                'transform'         : transform,
                'top': 0,
                'left': 0,
              };
              $floatContainer.css(cssObj);
            }
            oldTop = pos.top;
            oldLeft = pos.left;
          }
          if(setWidth){
            setFloatWidth();
          }
          if(setHeight){
            setHeaderHeight();
          }
          var scrollLeft = (responsive ? $responsiveContainer : $scrollContainer).scrollLeft();
          if(!useAbsolutePositioning || oldScrollLeft !== scrollLeft){
            $floatContainer.scrollLeft(scrollLeft);
            oldScrollLeft = scrollLeft;
          }
        }
      }

      /**
       * checks if THIS table has scrollbars, and finds their widths
       */
      function calculateScrollBarSize(){ //this should happen after the floating table has been positioned
        if($scrollContainer.length){
          if(opts.support && opts.support.perfectScrollbar && $scrollContainer.data().perfectScrollbar){
            scrollbarOffset = {horizontal:0, vertical:0};
          } else {
            if($scrollContainer.css('overflow-x') === 'scroll'){
              scrollbarOffset.horizontal = scrollbarWidth;
            } else {
              var sw = $scrollContainer.width(), tw = tableWidth($table, $fthCells);
              var offsetv = sh < th ? scrollbarWidth : 0;
              scrollbarOffset.horizontal = sw - offsetv < tw ? scrollbarWidth : 0;
            }
            if($scrollContainer.css('overflow-y') === 'scroll'){
              scrollbarOffset.vertical = scrollbarWidth;
            } else {
              var sh = $scrollContainer.height(), th = $table.height();
              var offseth = sw < tw ? scrollbarWidth : 0;
              scrollbarOffset.vertical = sh - offseth < th ? scrollbarWidth : 0;
            }
          }
        }
      }
      //finish up. create all calculation functions and bind them to events
      calculateScrollBarSize();

      var flow;

      var ensureReflow = function(){
        flow = reflow();
        flow();
      };

      ensureReflow();

      var calculateFloatContainerPos = calculateFloatContainerPosFn();
      var repositionFloatContainer = repositionFloatContainerFn();

      //this must come after reflow because reflow changes scrollLeft back to 0 when it rips out the thead
      repositionFloatContainer(calculateFloatContainerPos('init'), true);

      var windowScrollDoneEvent = util.debounce(function(){
        repositionFloatContainer(calculateFloatContainerPos('windowScrollDone'), false);
      }, 1);

      var windowScrollEvent = function(){
        updateScrollingOffsets();
        repositionFloatContainer(calculateFloatContainerPos('windowScroll'), false);
        if(absoluteToFixedOnScroll){
          windowScrollDoneEvent();
        }
      };
      var containerScrollEvent = function(){
        repositionFloatContainer(calculateFloatContainerPos('containerScroll'), false);
      };


      var windowResizeEvent = function(){
        if($table.is(":hidden")){
          return;
        }
        updateScrollingOffsets();
        calculateScrollBarSize();
        ensureReflow();
        calculateFloatContainerPos = calculateFloatContainerPosFn();
        repositionFloatContainer = repositionFloatContainerFn();
        repositionFloatContainer(calculateFloatContainerPos('resize'), true, true);
      };
      var reflowEvent = util.debounce(function(){
        if($table.is(":hidden")){
          return;
        }
        calculateScrollBarSize();
        updateScrollingOffsets();
        ensureReflow();
        calculateFloatContainerPos = calculateFloatContainerPosFn();
        repositionFloatContainer(calculateFloatContainerPos('reflow'), true, true);
      }, 1);

      /////// printing stuff
      var beforePrint = function(){
        unfloat();
      };
      var afterPrint = function(){
        refloat();
      };
      var printEvent = function(mql){
        //make printing the table work properly on IE10+
        if(mql.matches) {
          beforePrint();
        } else {
          afterPrint();
        }
      };

      var matchMediaPrint = null;
      if(window.matchMedia && window.matchMedia('print').addListener && !buggyMatchMedia){
        matchMediaPrint = window.matchMedia("print");
        matchMediaPrint.addListener(printEvent);
      } else {
        $window.on('fth-beforeprint', beforePrint);
        $window.on('fth-afterprint', afterPrint);
      }
      ////// end printing stuff


      if(locked){ //internal scrolling
        if(useAbsolutePositioning){
          $scrollContainer.on(eventName('scroll'), containerScrollEvent);
        } else {
          $scrollContainer.on(eventName('scroll'), containerScrollEvent);
          $window.on(eventName('scroll'), windowScrollEvent);
        }
      } else { //window scrolling
        $responsiveContainer.on(eventName('scroll'), containerScrollEvent);
        $window.on(eventName('scroll'), windowScrollEvent);
      }

      $window.on(eventName('load'), reflowEvent); //for tables with images

      windowResize(eventName('resize'), windowResizeEvent);
      $table.on('reflow', reflowEvent);
      if(opts.support && opts.support.datatables && isDatatable($table)){
        $table
          .on('filter', reflowEvent)
          .on('sort',   reflowEvent)
          .on('page',   reflowEvent);
      }

      if(opts.support && opts.support.bootstrap) {
        $window.on(eventName('shown.bs.tab'), reflowEvent); // people cant seem to figure out how to use this plugin with bs3 tabs... so this :P
      }
      if(opts.support && opts.support.jqueryUI) {
        $window.on(eventName('tabsactivate'), reflowEvent); // same thing for jqueryui
      }


      if (canObserveMutations) {
        var mutationElement = null;
        if(util.isFunction(opts.autoReflow)){
          mutationElement = opts.autoReflow($table, $scrollContainer)
        }
        if(!mutationElement) {
          mutationElement = $scrollContainer.length ? $scrollContainer[0] : $table[0]
        }
        mObs = new MutationObserver(function(e){
          var wasTableRelated = function(nodes){
            return nodes && nodes[0] && (nodes[0].nodeName === "THEAD" || nodes[0].nodeName === "TD"|| nodes[0].nodeName === "TH");
          };
          for(var i=0; i < e.length; i++){
            if(!(wasTableRelated(e[i].addedNodes) || wasTableRelated(e[i].removedNodes))){
              reflowEvent();
              break;
            }
          }
        });
        mObs.observe(mutationElement, {
          childList: true,
          subtree: true
        });
      }

      //attach some useful functions to the table.
      $table.data('floatThead-attached', {
        destroy: function(){
          var ns = '.fth-'+floatTheadId;
          unfloat();
          $table.css(layoutAuto);
          $tableColGroup.remove();
          createElements && $fthGrp.remove();
          if($newHeader.parent().length){ //only if it's in the DOM
            $newHeader.replaceWith($header);
          }
          triggerFloatEvent(false);
          if(canObserveMutations){
            mObs.disconnect();
            mObs = null;
          }
          $table.off('reflow reflowed');
          $scrollContainer.off(ns);
          $responsiveContainer.off(ns);
          if (wrappedContainer) {
            if ($scrollContainer.length) {
              $scrollContainer.unwrap();
            }
            else {
              $table.unwrap();
            }
          }
          if(locked){
            $scrollContainer.data('floatThead-containerWrap', false);
          } else {
            $table.data('floatThead-containerWrap', false);
          }
          $table.css('minWidth', originalTableMinWidth);
          $floatContainer.remove();
          $table.data('floatThead-attached', false);
          $window.off(ns);
          $window.off('fth-beforeprint fth-afterprint'); // Not bound with id, so cant use ns.
          if (matchMediaPrint) {
            matchMediaPrint.removeListener(printEvent);
          }
          beforePrint = afterPrint = function(){};

          return function reinit(){
            return $table.floatThead(opts);
          }
        },
        reflow: function(){
          reflowEvent();
        },
        setHeaderHeight: function(){
          setHeaderHeight();
        },
        getFloatContainer: function(){
          return $floatContainer;
        },
        getRowGroups: function(){
          if(headerFloated){
            return $floatContainer.find('>table>thead').add($table.children("tbody,tfoot"));
          } else {
            return $table.children("thead,tbody,tfoot");
          }
        }
      });
    });
    return this;
  };
})((function(){
  var $ = window.jQuery;
  if(typeof module !== 'undefined' && module.exports && !$) {
    // only use cjs if they dont have a jquery for me to use, and we have commonjs
    $ = require('jquery');
  }
  return $;
})());