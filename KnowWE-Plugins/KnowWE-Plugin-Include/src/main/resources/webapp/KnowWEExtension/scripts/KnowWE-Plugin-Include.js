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
 * Applies the changes of the tracking source since the given version (or all differences for version -1)
 * to the local content, optionally acknowledging them and optionally overriding conflicting local changes.
 */
KNOWWE.plugin.include.applyTrackingChanges = function(sectionId, version, acknowledge, override) {
	// no confirmation, the shown diff tells what happens
	jq$.ajax({
		url : KNOWWE.core.util.getURL({
			action : 'ApplyInterWikiTrackingChangesAction',
			SectionID : sectionId,
			version : version,
			acknowledge : acknowledge,
			override : !!override
		}),
		type : 'post',
		cache : false
	}).done(function() {
		window.location.reload();
	}).fail(function(xhr) {
		KNOWWE.notification.error(null, KNOWWE.plugin.include.errorMessage(xhr, 'Unable to apply the differences.'), 'tracking-apply', 10000);
	});
};

/**
 * Loads and shows the diff of the entry chosen in "Compare with source" (only while expanded), and
 * adapts the button to the entry.
 */
KNOWWE.plugin.include.showTrackingComparison = function(sectionId) {
	const container = document.getElementById('tracking-compare-' + sectionId);
	const select = document.getElementById('tracking-compare-select-' + sectionId);
	const apply = document.getElementById('tracking-compare-apply-' + sectionId);
	if (!container || !select || !container.closest('details').open) return;
	if (apply) apply.disabled = true;
	jq$.ajax({
		url : KNOWWE.core.util.getURL({
			action : 'InterWikiTrackingDiffAction',
			SectionID : sectionId,
			version : select.value
		}),
		cache : false
	}).done(function(html) {
		container.innerHTML = html;
		const diff = container.firstElementChild;
		if (!apply) return;
		const conflict = diff && diff.dataset.conflict === 'true';
		apply.disabled = !(diff && diff.dataset.applicable === 'true');
		apply.dataset.override = String(conflict);
		apply.textContent = conflict ? 'Override local changes by applying shown changes' : 'Apply shown changes';
	}).fail(function(xhr) {
		KNOWWE.notification.error(null, KNOWWE.plugin.include.errorMessage(xhr, 'Unable to load the differences.'), 'tracking-diff', 10000);
	});
};

/**
 * Applies the entry chosen in "Compare with source" without acknowledging, as other changes of the
 * source may still be pending.
 */
KNOWWE.plugin.include.applyTrackingComparison = function(sectionId) {
	const select = document.getElementById('tracking-compare-select-' + sectionId);
	const apply = document.getElementById('tracking-compare-apply-' + sectionId);
	if (!select) return;
	KNOWWE.plugin.include.applyTrackingChanges(sectionId, select.value, false, apply && apply.dataset.override === 'true');
};
