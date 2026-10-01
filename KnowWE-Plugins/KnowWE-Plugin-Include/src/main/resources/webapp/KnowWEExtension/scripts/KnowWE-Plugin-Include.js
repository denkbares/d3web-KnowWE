/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	KNOWWE.plugin = {};
}

/**
 * Namespace: KNOWWE.core.plugin.include
 */
KNOWWE.plugin.include = {};

KNOWWE.plugin.include.updateVersion = function(sectionID, newVersion) {
	var params = {
		action : 'ReplaceKDOMNodeAction',
		TargetNamespace : sectionID,
		KWikitext : newVersion,
	};
	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
			fn : function() {
				location.reload();
			},
			onError : function() {
				if (this.status == null) return;
				switch (this.status) {
				case 0:
					KNOWWE.notification.error(null, 
							"Server appears to be offline.", status);
					break;
				case 404:
					KNOWWE.notification.error(null, 
							"This page no longer exists. Please reload.", status);
					break;
				case 403:
					KNOWWE.notification.error(null,
							"You do not have the permission to edit this page.", status);
					break;
				default:
					KNOWWE.notification.error(null,
							"Unexpected error " + this.status + ". Please reload the page.", status);
					break;
				}
			}
		}
	}
	new _KA(options).send();
};
/**
 * Returns the message of a failed request: the message of a JSON error response, otherwise the response text
 * or the given fallback.
 */
KNOWWE.plugin.include.errorMessage = function(xhr, fallback) {
	if (xhr.responseJSON && xhr.responseJSON.message) return xhr.responseJSON.message;
	try {
		const json = JSON.parse(xhr.responseText);
		if (json && json.message) return json.message;
	}
	catch (ignored) {
		// no JSON response
	}
	return xhr.responseText || fallback;
};

/**
 * Loads and shows the diff of the entry chosen in "Differences since" of an InterWikiImport in tracking mode,
 * and enables "Apply shown differences and acknowledge" if the entry can be applied.
 */
KNOWWE.plugin.include.showTrackingDiff = function(sectionId) {
	const container = document.getElementById('tracking-diff-' + sectionId);
	const select = document.getElementById('tracking-diff-select-' + sectionId);
	const apply = document.getElementById('tracking-apply-' + sectionId);
	if (!container || !select) return;
	if (apply) {
		apply.disabled = true;
		apply.style.display = select.value === '' ? 'none' : '';
	}
	if (select.value === '') {
		container.style.display = 'none';
		return;
	}
	jq$.ajax({
		url : KNOWWE.core.util.getURL({
			action : 'InterWikiTrackingDiffAction',
			SectionID : sectionId,
			version : select.value
		}),
		cache : false
	}).done(function(html) {
		container.innerHTML = html;
		container.style.display = 'block';
		const diff = container.firstElementChild;
		if (apply) apply.disabled = !(diff && diff.dataset.applicable === 'true');
	}).fail(function(xhr) {
		KNOWWE.notification.error(null, KNOWWE.plugin.include.errorMessage(xhr, 'Unable to load the differences.'), 'tracking-diff', 10000);
	});
};

/**
 * Applies the shown differences of an InterWikiImport in tracking mode and acknowledges them.
 */
KNOWWE.plugin.include.applyTrackingDiff = function(sectionId) {
	const select = document.getElementById('tracking-diff-select-' + sectionId);
	if (!select || select.value === '') return;
	// all changes to the reference replace the local content
	if (select.value === '-1' && !confirm('Replace the local content below the markup with the current reference text from the source wiki? Local deviations in this range will be lost.')) return;
	jq$.ajax({
		url : KNOWWE.core.util.getURL({
			action : 'ApplyInterWikiTrackingChangesAction',
			SectionID : sectionId,
			version : select.value
		}),
		type : 'post',
		cache : false
	}).done(function() {
		window.location.reload();
	}).fail(function(xhr) {
		KNOWWE.notification.error(null, KNOWWE.plugin.include.errorMessage(xhr, 'Unable to apply the differences.'), 'tracking-apply', 10000);
	});
};
