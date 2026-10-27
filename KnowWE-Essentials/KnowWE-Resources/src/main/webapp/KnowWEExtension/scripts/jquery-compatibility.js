var jq$ = jQuery.noConflict();

// Store the original method in a variable
jq$.originalAjax = jq$.ajax;

// Override jq$.ajax
jq$.ajax = function(url, options) {
  const settings = typeof url === 'object' ? url : options;
  const method = ((settings && (settings.method || settings.type)) || 'GET').toUpperCase();
  if (method === 'POST' && (!settings || settings.async !== false)) {
    return postWithXSRFRetry(typeof url === 'object' ? url : Object.assign({}, options, {url: url}));
  }

  // If the first argument is a URL (string) and options (settings) are provided
  if (typeof url === 'string' && !url.startsWith("data:")) {
    // Append the parameter to the URL
    url = _KA.appendXSRFToken(url);
  }
  // If the first argument is an object (settings)
  else if (typeof url === 'object') {

    // Append the parameter to the URL
    url.url = _KA.appendXSRFToken(url.url);
  }

  // Call the original method with the adjusted options
  return jq$.originalAjax.apply(this, [url, options]);
};

/**
 * Posts like jq$.ajax, but if the server rejects the anti-CSRF token because the session changed meanwhile, the token
 * is refreshed and the request is retried once. The callbacks of the settings only see the final attempt.
 */
function postWithXSRFRetry(settings) {
  const context = settings.context || settings;
  const createXhr = settings.xhr || jq$.ajaxSettings.xhr;
  const deferred = jq$.Deferred();
  let current;

  const attempt = function(token, mayRetry) {
    let nativeXhr;
    current = jq$.originalAjax(Object.assign({}, settings, {
      url: _KA.setXSRFToken(settings.url || window.location.href, token),
      success: null,
      error: null,
      complete: null,
      // jQuery hides the final url of followed redirects, which is the only sign of a rejection
      xhr: function() {
        nativeXhr = createXhr.apply(this, arguments);
        return nativeXhr;
      },
    }));
    const settle = function(resolve, args) {
      const jqXHR = resolve ? args[2] : args[0];
      if (!_KA.isXSRFRejection(nativeXhr && nativeXhr.responseURL)) {
        resolve ? deferred.resolveWith(context, args) : deferred.rejectWith(context, args);
      } else if (mayRetry) {
        _KA.refreshXSRFToken().then(token => attempt(token, false),
          () => deferred.rejectWith(context, [jqXHR, "error", "Forbidden"]));
      } else {
        deferred.rejectWith(context, [jqXHR, "error", "Forbidden"]);
      }
    };
    current.done(function() {
      settle(true, arguments);
    }).fail(function() {
      settle(false, arguments);
    });
  };

  const call = function(callbacks, args) {
    [].concat(callbacks || []).forEach(callback => callback.apply(context, args));
  };
  deferred.done(function(data, status, jqXHR) {
    call(settings.success, arguments);
    call(settings.complete, [jqXHR, status]);
  }).fail(function(jqXHR, status) {
    call(settings.error, arguments);
    call(settings.complete, [jqXHR, status]);
  });

  attempt(undefined, true);
  return deferred.promise({
    abort: function(statusText) {
      current.abort(statusText);
      return this;
    },
  });
}
