function TextArea(area) {
	if (area.textarea != null) {
		return area.textarea;
	}
	this.area = $(area);
	area.textarea = this;
	this.area.undoHistory = [];
	this.area.redoHistory = [];
	jq$(this.area).keydown(jq$.proxy(function(event) {
		this.handleKeyDown(event);
	}, this));
	jq$(this.area).on('paste', jq$.proxy(function(event) {
		this.snapshot();
	}, this));
	jq$(this.area).select(jq$.proxy(function() {
		this.snapshot();
	}, this));
	this.snapshot();
	return this
}

TextArea.initialize = function(area) {
	return new TextArea(area);
};
TextArea.getSelection = function(area) {
	return new TextArea(area).getSelection();
};
TextArea.getCursor = function(area) {
	return new TextArea(area).getCursor();
};
TextArea.getSelectionCoordinates = function(area) {
	return new TextArea(area).getSelectionCoordinates();
};
TextArea.isSelectionAtStartOfLine = function(area) {
	return new TextArea(area).isSelectionAtStartOfLine();
};
TextArea.replaceSelection = function(a, g) {
	return new TextArea(a).replaceSelection(g);
};
TextArea.prototype.isLongerSelection = function() {
	return this.getSelection().length > 0 && this.getSelection().indexOf('\n') >= 0;
};
TextArea.prototype.handleKeyDown = function(event) {
	// with this line, we remove a hack of jspwiki-edit.js,
	// that is no longer needed but instead messes with keydown
	// events of tab (keycode == 9) in chrome/webkit
	var $editorarea = $('editorarea');
	if ($editorarea) $editorarea.removeEvents('keydown');

	event = jq$.event.fix(event);
	if (_EC.isModifier(event)) {
		if (event.which == 89 || (event.which == 90 && event.shiftKey)) { // Y
			event.stopPropagation();
			event.preventDefault();
			this.redo();
			return;
		}
		if (event.which == 90) { // Z
			event.stopPropagation();
			event.preventDefault();
			this.snapshot();
			this.undo();
			return;
		}
	}
	var isAltOnly = !event.ctrlKey && !event.metaKey && event.altKey;
	var isCmdOnly = (!event.ctrlKey && event.metaKey && !event.altKey)
		|| (event.ctrlKey && !event.metaKey && !event.altKey);
	var isLongerSelection = this.isLongerSelection();
	if (event.which == 38 && isAltOnly) { // alt + UP
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("up");
		return;
	}
	// alt + DOWN
	if (event.which == 40 && isAltOnly) { // alt + DOWN
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("down");
		return;
	}
	// - + selection length > 0
	if ((event.which == 189 || event.which == 173) && isLongerSelection) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("minusRight");
		return;
	}
	// cmd + 7
	if (event.which == 55 && isCmdOnly && isLongerSelection) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("commentRight");
		return;
	}
	// # + selection length > 0
	if ((event.which == 191 || event.which == 163 || event.which == 220) && isLongerSelection) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("hashRight");
		return;
	}
	// * + selection length > 0
	if ((event.which == 187 || event.which == 171 || event.which == 221) && event.shiftKey && isLongerSelection) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("starRight");
		return;
	}
	// TAB + !SHIFT
	if (event.which == 9 && !event.shiftKey && !(event.ctrlKey || event.altKey)) {
		event.stopPropagation();
		event.preventDefault();
		event.stopImmediatePropagation();
		this.snapshot();
		this.moveLines("tab");
		return;
	}
	// TAB + SHIFT
	if (event.which == 9 && event.shiftKey && !(event.ctrlKey || event.altKey)) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("tabShift");
		return;
	}
	// TAB + !SHIFT + ALT|CTRL + selection length = 0
	if (event.which == 9 && !event.shiftKey && (event.ctrlKey || event.altKey)) {
		event.stopPropagation();
		event.preventDefault();
		event.stopImmediatePropagation();
		this.insertText("\t");
		return;
	}
	// SPACE + selection length > 0 + !SHIFT
	if (event.which == 32 && isLongerSelection && !event.shiftKey) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("space");
		return;
	}
	// SPACE + selection length > 0 + SHIFT
	if (event.which == 32 && isLongerSelection && event.shiftKey) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("spaceShift");
		return;
	}
	// alt + D, cmd + D
	if (event.which == 68 && (isAltOnly || isCmdOnly)) {
		event.stopPropagation();
		event.preventDefault();
		this.snapshot();
		this.moveLines("delete");
		return;
	}
	if (event.which == 13 && !event.ctrlKey && !event.altKey) {
		this.snapshot();
		// late processing the intend, after events have completed
		// to avoid conflict with e.g. auto-complete
		var intend = this.getIntend();
		setTimeout(jq$.proxy(function() {
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
TextArea.prototype.onSave = function() {

};
TextArea.prototype.onCancel = function() {

};
TextArea.prototype.moveLines = function(direction) {
	var area = this.area;
	var originalSelection = this.getSelectionCoordinates();
	this.extendSelectionToFullLines(area);
	var lines = this.getSelection();

	// make sure that we have a "\n" at the end (can happen in last line)
	var missingLF = lines.charCodeAt(lines.length - 1) != 10;
	if (missingLF) {
		// add a line break
		lines = lines + "\n";
	}

	this.insertText("");
	if (direction == "delete") {
		return;
	}

	var insertionPos = this.getCursor(area);
	var newStart = insertionPos;
	var newEnd = -1;

	var text = area.getValue();
	var splitLines = lines.split("\n");
	if (direction == "up") {
		insertionPos = text.substring(0, newStart - 1).lastIndexOf('\n') + 1;
		newStart = insertionPos;
	}
	else if (direction == "down") {
		newStart = text.substring(newStart).indexOf('\n');
		if (newStart == -1) {
			insertionPos = text.length;
			lines = "\n" + lines.substring(0, lines.length - 1);
			newStart = insertionPos + 1;
		}
		else {
			insertionPos += newStart + 1;
			newStart = insertionPos;
		}
	}
	else if (direction == "minusRight") {
		lines = "";
		for (var line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "-" + splitLines[line] + "\n";
		}
	}
	else if (direction == "hashRight") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "#" + splitLines[line] + "\n";
		}
	}
	else if (direction == "commentRight") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "// " + splitLines[line] + "\n";
		}
	}
	else if (direction == "starRight") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "*" + splitLines[line] + "\n";
		}
	}
	else if (direction == "tab") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + "\t" + splitLines[line] + "\n";
		}
		if (originalSelection.start == originalSelection.end) {
			newStart = originalSelection.start + 1;
			newEnd = newStart;

		}
	}
	else if (direction == "tabShift") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			if (splitLines[line].substring(0, 1) == "-") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == "#") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == "*") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 2) == "    ") {
				lines = lines + splitLines[line].substring(2) + "\n";
			}
			else if (splitLines[line].substring(0, 2) == "  ") {
				lines = lines + splitLines[line].substring(2) + "\n";
			}
			else if (splitLines[line].substring(0, 3) == "// ") {
				lines = lines + splitLines[line].substring(3) + "\n";
			}
			else if (splitLines[line].substring(0, 2) == "//") {
				lines = lines + splitLines[line].substring(2) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == " ") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == "\t") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else {
				lines = lines + splitLines[line] + "\n";
			}
		}
	}
	else if (direction == "space") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			lines = lines + " " + splitLines[line] + "\n";
		}
	}
	else if (direction == "spaceShift") {
		lines = "";
		for (line = 0; line < splitLines.length - 1; line++) {
			if (splitLines[line].substring(0, 1) == "-") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == "#") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == "*") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == " ") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else if (splitLines[line].substring(0, 1) == "\t") {
				lines = lines + splitLines[line].substring(1) + "\n";
			}
			else {
				lines = lines + splitLines[line] + "\n";
			}
		}
	}
	this.setSelection(insertionPos);
	this.insertText(lines);
	if (newEnd == -1) newEnd = newStart + lines.length;
	this.setSelection(newStart, newEnd);
};
TextArea.prototype.getLinesLimits = function() {
	var area = this.area;
	var text = area.getValue();
	var sel = this.getSelectionCoordinates();
	var sel1 = Math.min(sel.start, sel.end);
	var sel2 = Math.max(sel.start, sel.end);
	if (sel2 > sel1 && text.charCodeAt(sel2 - 1) == 10) sel2--;
	var start = text.substring(0, sel1).lastIndexOf("\n") + 1;
	var end = text.substring(sel2).indexOf("\n");
	if (end == -1) end = text.length;
	else end += 1 + sel2;

	return {start : start, end : end}
};
TextArea.prototype.extendSelectionToFullLines = function() {
	var linesLimits = this.getLinesLimits();
	this.setSelection(linesLimits.start, linesLimits.end);
};
TextArea.prototype.undo = function() {
	var area = this.area;
	while (true) {
		if (area.undoHistory.length == 0) return;
		var shot = area.undoHistory.pop();
		area.redoHistory.push(shot);
		if (shot.text != area.getValue()) break;
	}
	this.restoreSnapshot(shot);
};
TextArea.prototype.redo = function() {
	var area = this.area;
	while (true) {
		if (area.redoHistory.length == 0) return;
		var shot = area.redoHistory.pop();
		area.undoHistory.push(shot);
		if (shot.text != area.getValue()) break;
	}
	this.restoreSnapshot(shot);
};
TextArea.prototype.snapshot = function() {
	var area = this.area;
	var text = area.getValue();
	// avoid duplicate entries
	if (area.undoHistory.length > 0 && area.undoHistory[area.undoHistory.length - 1].text == text) return;
	if (area.redoHistory.length > 0 && area.redoHistory[area.redoHistory.length - 1].text == text) return;
	var sel = this.getSelectionCoordinates();
	// if we have a redo history, we take the recent element back to the undo history,
	// because it was the text fields original state before editing again
	if (area.redoHistory.length > 0) {
		var shot = area.redoHistory.pop();
		area.undoHistory.push(shot);
		area.redoHistory = [];
	}
	area.undoHistory.push({
		text : text,
		start : sel.start,
		end : sel.end,
		scroll : area.scrollTop
	});
};
TextArea.prototype.restoreSnapshot = function(shot) {
	var area = this.area;
	area.value = shot.text;
	var sel = this.setSelection(shot.start, shot.end);
	area.scrollTop = shot.scroll;
};
TextArea.prototype.getSelection = function() {
	var b = this.getSelectionCoordinates();
	var text = this.area.getValue().substring(b.start, b.end);
	return text;
};
TextArea.prototype.setSelection = function(f, a) {
	var area = this.area;
	if (!a) {
		a = f
	}
	if (area.setSelectionRange != undefined) {
		area.setSelectionRange(f, a)
	} else {
		var c = area.value, d = c.substr(f, a - f).replace(/\r/g, "").length;
		f = c.substr(0, f).replace(/\r/g, "").length;
		var b = area.createTextRange();
		b.collapse(true);
		b.moveEnd("character", f + d);
		b.moveStart("character", f);
		b.select()
	}

	return this
};

TextArea.prototype.getCursor = function() {
	return this.getSelectionCoordinates().start
};

TextArea.prototype.getIntend = function() {
	var area = this.area;
	var text = area.getValue().substring(0, this.getCursor(area));
	var pos = text.lastIndexOf("\n") + 1;
	var intend = "";
	while (pos < text.length) {
		var c = text.charAt(pos++);
		if (c == '\t' || c == ' ') intend += c;
		else break;
	}
	return intend;
};
TextArea.prototype.getSelectionCoordinates = function() {
	var area = this.area;
	var f = $(area), e = {
		start : 0,
		end : 0,
		thin : true
	};
	if (f.selectionStart != undefined) {
		e = {
			start : f.selectionStart,
			end : f.selectionEnd
		}
	} else {
		var a = document.selection.createRange();
		if (!a || a.parentElement() != f) {
			return e
		}
		var c = a.duplicate(), b = f.value, d = b.length
			- b.match(/[\n\r]*$/)[0].length;
		c.moveToElementText(f);
		c.setEndPoint("StartToEnd", a);
		e.end = d - c.text.length;
		c.setEndPoint("StartToStart", a);
		e.start = d - c.text.length
	}
	e.thin = (e.start == e.end);
	return e
};
TextArea.prototype.replaceSelection = function(g) {
	var h = g.replace(/\r/g, ""), d = this.area, c = d.scrollTop;
	if (d.selectionStart != undefined) {
		var b = d.selectionStart, e = d.selectionEnd, i = d.value;
		d.value = i.substr(0, b) + h + i.substr(e);
		d.selectionStart = b;
		d.selectionEnd = b + h.length;
	} else {
		d.focus();
		var f = document.selection.createRange();
		f.text = h;
		f.collapse(true);
		f.moveStart("character", -h.length);
		f.select();
	}
	d.focus();
	d.scrollTop = c;
	jq$(d).trigger("change");
};
TextArea.prototype.insertText = function(text) {
	this.replaceSelection(text);
	this.setSelection(this.getSelectionCoordinates().end);
};
TextArea.prototype.isSelectionAtStartOfLine = function() {
	var a = this.getCursor();
	return ((a <= 0) || (this.area.value.charAt(a - 1).match(/[\n\r]/)));
};