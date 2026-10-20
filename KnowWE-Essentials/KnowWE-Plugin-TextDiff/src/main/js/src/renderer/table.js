// The table renderer, the same markup DiffHtmlRenderer produces on the server, built from a computed diff.
import {lineModel} from "../diff/index.js";

const MIN_ELIDE = 2;

const EXPAND_ICON =
	'<svg class="icon" viewBox="0 0 16 16" width="16" height="16" fill="currentColor" aria-hidden="true">' +
	'<path fill-rule="evenodd" d="M8.177.677l2.896 2.896a.25.25 0 0 1-.177.427H8.75v1.25a.75.75 0 0 1-1.5 0V4H5.104a.25.25 0 0 1-.177-.427L7.823.677a.25.25 0 0 1 .354 0zM7.25 10.75a.75.75 0 0 1 1.5 0V12h2.146a.25.25 0 0 1 .177.427l-2.896 2.896a.25.25 0 0 1-.354 0L4.927 12.427A.25.25 0 0 1 5.104 12H7.25v-1.25zm-5-2a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5zM6 8a.75.75 0 0 1-.75.75h-.5a.75.75 0 0 1 0-1.5h.5A.75.75 0 0 1 6 8zm2.25.75a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5zM12 8a.75.75 0 0 1-.75.75h-.5a.75.75 0 0 1 0-1.5h.5A.75.75 0 0 1 12 8zm2.25.75a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5z"/>' +
	'</svg>';

function escapeHtml(text) {
	return text.replace(/[&<>"']/g, c => ({"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&apos;"})[c]);
}

function lineNumberMode(lines) {
	let hasOld = false, hasNew = false;
	for (const line of lines) {
		hasOld ||= line.old > 0;
		hasNew ||= line.new > 0;
		if (hasOld && hasNew) return {cssClass: "two-line-numbers", columns: 4, old: true, new: true};
	}
	if (hasOld && !hasNew) return {cssClass: "single-line-number old-only", columns: 3, old: true, new: false};
	if (hasNew && !hasOld) return {cssClass: "single-line-number new-only", columns: 3, old: false, new: true};
	return {cssClass: "two-line-numbers", columns: 4, old: true, new: true};
}

function visibility(lines, contextLines) {
	const visible = new Array(lines.length).fill(contextLines < 0);
	if (contextLines < 0) return visible;
	for (let i = 0; i < lines.length; i++) {
		if (lines[i].status === "common") continue;
		for (let j = Math.max(0, i - contextLines); j <= Math.min(lines.length - 1, i + contextLines); j++) visible[j] = true;
	}
	return visible;
}

function row(line, hidden, mode) {
	const sign = line.status === "added" ? "+" : line.status === "removed" ? "−" : "";
	const classes = line.status + (hidden ? " hidden" : "");
	let html = '<tr class="line ' + classes + '" part="line ' + classes + '">';
	if (mode.old) html += '<td class="num old" part="num old">' + (line.old > 0 ? line.old : "") + '</td>';
	if (mode.new) html += '<td class="num new" part="num new">' + (line.new > 0 ? line.new : "") + '</td>';
	html += '<td class="sign" part="sign">' + sign + '</td>';
	html += '<td class="text" part="text">' + escapeHtml(line.text) + '</td></tr>';
	return html;
}

/** The inner HTML of the diff frame as a table, with unchanged regions beyond the context lines folded. */
export function renderTable(diff, {contextLines = 3} = {}) {
	if (diff.stats.added === 0 && diff.stats.removed === 0) {
		return '<p class="diff-note" part="note">No changes.</p>';
	}
	const lines = lineModel(diff.aLines, diff.bLines, diff.chunks);
	const mode = lineNumberMode(lines);
	const visible = visibility(lines, contextLines);
	let html = '<table class="diff ' + mode.cssClass + '"><tbody>';
	let i = 0;
	while (i < lines.length) {
		if (visible[i]) {
			html += row(lines[i++], false, mode);
			continue;
		}
		const start = i;
		while (i < lines.length && !visible[i]) i++;
		const hidden = i - start;
		if (hidden < MIN_ELIDE) {
			for (let j = start; j < i; j++) html += row(lines[j], false, mode);
			continue;
		}
		html += '<tr class="elided" part="elided" data-hidden-count="' + hidden + '"><td colspan="' + mode.columns + '">' +
			'<button class="expand" part="expand-button" type="button" aria-label="Show ' + hidden + ' unchanged lines">' +
			EXPAND_ICON + '<span class="label" part="expand-label">Show ' + hidden + ' unchanged lines</span></button></td></tr>';
		for (let j = start; j < i; j++) html += row(lines[j], true, mode);
	}
	return html + '</tbody></table>';
}
