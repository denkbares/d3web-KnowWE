function TextArea(area) {
	if (area.textarea != null) {
		return area.textarea;
	}
	this.area = jq$(area)[0];
	if (this.area.addClass) {
		this.area.addClass('ui-wiki-content-edit-area');
	} else {
	  this.area.addClassName('ui-wiki-content-edit-area');
	}
	area.textarea = this;
	this.area.undoHistory = [];
	this.area.redoHistory = [];
	jq$(this.area).keydown(jq$.proxy(function (event) {
		this.handleKeyDown(event);
	}, this));
	jq$(this.area).on('paste', jq$.proxy(function (event) {
		this.snapshot();
	}, this));
	jq$(this.area).select(jq$.proxy(function () {
		this.snapshot();
	}, this));
	this.snapshot();
	return this
}

TextArea.initialize = function (area) {
	return new TextArea(area);
};
TextArea.getSelection = function (area) {
	return new TextArea(area).getSelection();
};
TextArea.getCursor = function (area) {
	return new TextArea(area).getCursor();
};
TextArea.getSelectionCoordinates = function (area) {
	return new TextArea(area).getSelectionCoordinates();
};
TextArea.isSelectionAtStartOfLine = function (area) {
	return new TextArea(area).isSelectionAtStartOfLine();
};
TextArea.replaceSelection = function (a, g) {
	return new TextArea(a).replaceSelection(g);
};

TextArea.focusNextTextArea = function (currentOrNull, backwards) {
	// if no other completion section is available, but there are more edit areas, proceed to the next area
	const areas = jq$('.ui-wiki-content-edit-area');
	const index = areas.index(currentOrNull);
	const next = backwards
		? areas.get((index >= 1 ? index : areas.length) - 1)
		: areas.get((index + 1) % areas.length);
	if (!next) return null;
	next.focus();
	return next;
}

TextArea.prototype.isLongerSelection = function () {
	return this.getSelection().length > 0 && this.getSelection().indexOf('\n') >= 0;
};
// noinspection JSUnusedGlobalSymbols
TextArea.prototype.handleKeyDown = function (event) {
	// with this line, we remove a hack of jspwiki-edit.js,
	// that is no longer needed but instead messes with keydown
	// events of tab (keycode == 9) in chrome/webkit
	const $editor = jq$('.editor')[0];
	if ($editor) $editor.removeEvents('keydown');

	event = jq$.event.fix(event);
	if (_EC.isModifier(event)) {
		if (event.which === 89 || (event.which === 90 && event.shiftKey)) { // Y
			event.stopPropagation();
			event.preventDefault();
			this.redo();
			return;
		}
		if (event.which === 90) { // Z
			event.stopPropagation();
			event.preventDefault();
			this.snapshot();
			this.undo();
			return;
		}
	}
	const isAltOnly = !event.ctrlKey && !event.metaKey && event.altKey;
	const isCmdOnly = (!event.ctrlKey && event.metaKey && !event.altKey)
		|| (event.ctrlKey && !event.metaKey && !event.altKey);
	const isLongerSelection = this.isLongerSelection();
	const isCursorBeforeText = this.isCursorBeforeText();
	const isSingleLine = this.area.rows === 1;

	if (event.which === 38 && isAltOnly && !isSingleLine) { // alt + UP
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("up");
		return;
	}
	// alt + DOWN
	if (event.which === 40 && isAltOnly && !isSingleLine) { // alt + DOWN
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("down");
		return;
	}
	// - + selection length > 0
	if ((event.which === 189 || event.which === 173) && isLongerSelection && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("minusRight");
		return;
	}
	// cmd + 7
	if (event.which === 55 && isCmdOnly && isLongerSelection && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("commentRight");
		return;
	}
	// # + selection length > 0
	if ((event.which === 191 || event.which === 163 || event.which === 220) && isLongerSelection && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("hashRight");
		return;
	}
	// * + selection length > 0
	if ((event.which === 187 || event.which === 171 || event.which === 221) && event.shiftKey && isLongerSelection && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("starRight");
		return;
	}
	// TAB + !SHIFT
	if (event.which === 9 && !event.shiftKey && !(event.ctrlKey || event.altKey) && (isLongerSelection || isCursorBeforeText) && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		event.stopImmediatePropagation();
		this.snapshot();
		this.moveLines("tab");
		return;
	}
	// TAB + SHIFT
	if (event.which === 9 && event.shiftKey && !(event.ctrlKey || event.altKey) && (isLongerSelection || isCursorBeforeText) && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("tabShift");
		return;
	}
	// TAB + !SHIFT + ALT|CTRL + selection length = 0
	if (event.which === 9 && !event.shiftKey && (event.ctrlKey || event.altKey) && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		event.stopImmediatePropagation();
		this.insertText("\t");
		return;
	}
	// SPACE + selection length > 0 + !SHIFT
	if (event.which === 32 && isLongerSelection && !event.shiftKey && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("space");
		return;
	}
	// SPACE + selection length > 0 + SHIFT
	if (event.which === 32 && isLongerSelection && event.shiftKey && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("spaceShift");
		return;
	}
	// alt + D, cmd + D
	if (event.which === 68 && (isAltOnly || isCmdOnly) && !isSingleLine) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("delete");
		return;
	}
	if (event.which === 13 && !event.ctrlKey && !event.altKey) {
		this.snapshot();
		// late processing the intend, after events have completed
		// to avoid conflict with e.g. auto-complete
		const intend = this.getIntend();
		setTimeout(jq$.proxy(function () {
			if (!this.isSelectionAtStartOfLine()) return;
			this.insertText(intend);
		}, this));
		return;
	}
	// snapshot on cursor keys
	if (event.which >= 37 && event.which <= 40) {
		this.snapshot();
	}
	// snapshot on commands
	if (event.which >= 65 && event.which <= 90 && (event.control || event.alt || event.meta)) {
		this.snapshot();
	}
};

TextArea.prototype.onSave = function () {
};

// noinspection JSUnusedGlobalSymbols
TextArea.prototype.onCancel = function () {
};

TextArea.prototype.moveLines = function (direction) {
	const area = this.area;
	const originalSelection = this.getSelectionCoordinates();
	this.extendSelectionToFullLines(area);
	let lines = this.getSelection();

	// make sure that we have a "\n" at the end (can happen in last line)
	const missingLF = lines.charCodeAt(lines.length - 1) !== 10;
	if (missingLF) {
		// add a line break
		lines = lines + "\n";
	}

	this.insertText("");
	if (direction === "delete") {
		return;
	}

	let insertionPos = this.getCursor(area);
	let newStart = insertionPos;
	let newEnd = -1;

	const text = area.value;
	const splitLines = lines.split("\n");
	if (direction === "up") {
		insertionPos = text.substring(0, newStart - 1).lastIndexOf('\n') + 1;
		newStart = insertionPos;
	}
	else if (direction === "down") {
		newStart = text.substring(newStart).indexOf('\n');
		if (newStart === -1) {
			insertionPos = text.length;
			lines = "\n" + lines.substring(0, lines.length - 1);
			newStart = insertionPos + 1;
		}
		else {
			insertionPos += newStart + 1;
			newStart = insertionPos;
		}
	}
	else if (direction === "minusRight") {
		lines = "";
		for (let line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "-" + splitLines[line] + "\n";
		}
	}
	else if (direction === "hashRight") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "#" + splitLines[line] + "\n";
		}
	}
	else if (direction === "commentRight") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "// " + splitLines[line] + "\n";
		}
	}
	else if (direction === "starRight") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "*" + splitLines[line] + "\n";
		}
	}
	else if (direction === "tab") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "\t" + splitLines[line] + "\n";
		}
		if (originalSelection.start === originalSelection.end) {
			newStart = originalSelection.start + 1;
			newEnd = newStart;

		}
	}
	else if (direction === "tabShift") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			if (splitLines[line].substring(0, 1) === "-") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === "#") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === "*") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 2) === "    ") {
				lines = lines + splitLines[line].substring(2) + "\n";
			}
			else if (splitLines[line].substring(0, 2) === "  ") {
				lines = lines + splitLines[line].substring(2) + "\n";
			}
			else if (splitLines[line].substring(0, 3) === "// ") {
				lines = lines + splitLines[line].substring(3) + "\n";
			}
			else if (splitLines[line].substring(0, 2) === "//") {
				lines = lines + splitLines[line].substring(2) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === " ") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === "\t") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else {
				lines = lines + splitLines[line] + "\n";
			}
		}
	}
	else if (direction === "space") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + " " + splitLines[line] + "\n";
		}
	}
	else if (direction === "spaceShift") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			if (splitLines[line].substring(0, 1) === "-") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === "#") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === "*") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === " ") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) === "\t") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else {
				lines = lines + splitLines[line] + "\n";
			}
		}
	}
	this.setSelection(insertionPos);
	this.insertText(lines);
	if (newEnd === -1) newEnd = newStart + lines.length;
	this.setSelection(newStart, newEnd);
};
TextArea.prototype.getLinesLimits = function () {
	const area = this.area;
	const text = area.value;
	const sel = this.getSelectionCoordinates();
	const sel1 = Math.min(sel.start, sel.end);
	let sel2 = Math.max(sel.start, sel.end);
	if (sel2 > sel1 && text.charCodeAt(sel2 - 1) === 10) sel2--;
	const start = text.substring(0, sel1).lastIndexOf("\n") + 1;
	let end = text.substring(sel2).indexOf("\n");
	if (end === -1) end = text.length;
	else end += 1 + sel2;

	return {start: start, end: end}
};
TextArea.prototype.extendSelectionToFullLines = function () {
	const linesLimits = this.getLinesLimits();
	this.setSelection(linesLimits.start, linesLimits.end);
};
TextArea.prototype.undo = function () {
	const area = this.area;
	let shot;
	while (true) {
		if (area.undoHistory.length === 0) return;
		shot = area.undoHistory.pop();
		area.redoHistory.push(shot);
		if (shot.text !== area.value) break;
	}
	this.restoreSnapshot(shot);
};
TextArea.prototype.redo = function () {
	const area = this.area;
	let shot;
	while (true) {
		if (area.redoHistory.length === 0) return;
		shot = area.redoHistory.pop();
		area.undoHistory.push(shot);
		if (shot.text !== area.value) break;
	}
	this.restoreSnapshot(shot);
};
TextArea.prototype.snapshot = function () {
	const area = this.area;
	const text = area.value;
	// avoid duplicate entries
	if (area.undoHistory.length > 0 && area.undoHistory[area.undoHistory.length - 1].text === text) return;
	if (area.redoHistory.length > 0 && area.redoHistory[area.redoHistory.length - 1].text === text) return;
	const sel = this.getSelectionCoordinates();
	// if we have a redo history, we take the recent element back to the undo history,
	// because it was the text fields original state before editing again
	if (area.redoHistory.length > 0) {
		const shot = area.redoHistory.pop();
		area.undoHistory.push(shot);
		area.redoHistory = [];
	}
	area.undoHistory.push({
		text: text,
		start: sel.start,
		end: sel.end,
		scroll: area.scrollTop
	});
};
TextArea.prototype.restoreSnapshot = function (shot) {
	this.area.value = shot.text;
	this.setSelection(shot.start, shot.end);
	this.area.scrollTop = shot.scroll;
};
TextArea.prototype.getSelection = function () {
	const b = this.getSelectionCoordinates();
	return this.area.value.substring(b.start, b.end);
};
TextArea.prototype.setSelection = function (f, a) {
	const area = this.area;
	if (!a) {
		a = f
	}
	if (area.setSelectionRange !== undefined) {
		area.setSelectionRange(f, a)
	} else {
		const c = area.value, d = c.substr(f, a - f).replace(/\r/g, "").length;
		f = c.substr(0, f).replace(/\r/g, "").length;
		const b = area.createTextRange();
		b.collapse(true);
		b.moveEnd("character", f + d);
		b.moveStart("character", f);
		b.select()
	}

	return this
};

TextArea.prototype.getCursor = function () {
	return this.getSelectionCoordinates().start
};

TextArea.prototype.getIntend = function () {
	const area = this.area;
	const text = area.value.substring(0, this.getCursor(area));
	let pos = text.lastIndexOf("\n") + 1;
	let intend = "";
	while (pos < text.length) {
		const c = text.charAt(pos++);
		if (c === '\t' || c === ' ') intend += c;
		else break;
	}
	return intend;
};
TextArea.prototype.getSelectionCoordinates = function () {
	const f = this.area;
	let e = {
		start: 0,
		end: 0,
		thin: true
	};
	if (f.selectionStart !== undefined) {
		e = {
			start: f.selectionStart,
			end: f.selectionEnd
		}
	} else {
		let a = document.selection.createRange();
		if (!a || a.parentElement() !== f) {
			return e
		}
		const c = a.duplicate(), b = f.value, d = b.length
			- b.match(/[\n\r]*$/)[0].length;
		c.moveToElementText(f);
		c.setEndPoint("StartToEnd", a);
		e.end = d - c.text.length;
		c.setEndPoint("StartToStart", a);
		e.start = d - c.text.length
	}
	e.thin = (e.start === e.end);
	return e
};
TextArea.prototype.replaceSelection = function (g) {
	const h = g.replace(/\r/g, ""), d = this.area, c = d.scrollTop;
	if (d.selectionStart !== undefined) {
		const b = d.selectionStart, e = d.selectionEnd, i = d.value;
		d.value = i.substr(0, b) + h + i.substr(e);
		d.selectionStart = b;
		d.selectionEnd = b + h.length;
	} else {
		d.focus();
		const f = document.selection.createRange();
		f.text = h;
		f.collapse(true);
		f.moveStart("character", -h.length);
		f.select();
	}
	d.focus();
	d.scrollTop = c;
	jq$(d).trigger("change");
};
TextArea.prototype.insertText = function (text) {
	this.replaceSelection(text);
	this.setSelection(this.getSelectionCoordinates().end);
};
TextArea.prototype.isSelectionAtStartOfLine = function () {
	const a = this.getCursor();
	return ((a <= 0) || (this.area.value.charAt(a - 1).match(/[\n\r]/)));
};
TextArea.prototype.isCursorBeforeText = function () {
	const sel = this.getSelectionCoordinates();
	if (sel.start !== sel.end) return false;
	// look backwards from cursor
	for (let index = sel.start - 1; index >= 0; index--) {
		// if char before is return, no characters have been before the cursor
		if (this.area.value.charAt(index).match(/[\n\r]/)) return true;
		// if char before is not a white-space, there are characters before the cursor
		if (this.area.value.charAt(index).match(/[^\s]/)) return false;
	}
	// if we reached the start of the text, no characters are before the cursor in the first line
	return true;
};
