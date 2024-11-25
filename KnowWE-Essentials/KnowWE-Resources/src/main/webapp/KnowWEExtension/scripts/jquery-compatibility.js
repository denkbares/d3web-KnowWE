var jq$ = jQuery.noConflict();

// Store the original method in a variable
jq$.originalAjax = jq$.ajax;

// Override jq$.ajax
jq$.ajax = function(url, options) {
  // If the first argument is a URL (string) and options (settings) are provided
  if (typeof url === 'string') {
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