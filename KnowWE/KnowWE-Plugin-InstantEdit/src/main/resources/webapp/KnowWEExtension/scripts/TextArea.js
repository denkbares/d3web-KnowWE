
if (!TextArea) {
	var TextArea = {
		initialize : function(area, suppressHotkeys) {
			if (this.textarea == null) {
				this.textarea = new Object();
			}
			area = $(area);
			id = area.getProperty('id');
			this.textarea[id] = area;
			this.textarea[id].undoHistory = [];
			this.textarea[id].redoHistory = [];
			if (!suppressHotkeys) {
				this.textarea[id].addEvent("keydown", this.handleKeyDown.bind(this));
				this.textarea[id].addEvent("select", function(event){
					this.snapshot(event);
				}.bind(this));
			}
			return this
		},
		getArea : function(event) {
			return  this.textarea[$(event.target).getProperty('id')];
			
		},
		handleKeyDown : function (event) {
			event = new Event(event);
			area = this.getArea(event);
			if ((!event.meta && event.control && !event.alt) 
					|| (!event.meta && !event.control && event.alt) 
					|| (event.meta && !event.control && !event.alt)) {
				if (event.code == 83) { // S
					event.stop();
					KNOWWE.plugin.instantEdit.save(area.getParent().getProperty('id'));
					return;					
				}
				if (event.code == 81 || event.code == 27) { // Q or ESC
					event.stop();
					KNOWWE.plugin.instantEdit.cancel(area.getParent().getProperty('id'));
					return;	
				}
				if (event.code == 89 || (event.code == 90 && event.shift)) { // Y
					event.stop();
					this.redo(area);
					return;	
				}
				if (event.code == 90) { // Z
					event.stop();
					this.snapshot(event);
					this.undo(area);
					return;	
				}
			}
			if (event.code == 38 && !event.control && !event.meta && event.alt) { // alt + UP
				event.stop();
				this.snapshot(event);
				this.moveLines(area, "up");
				return;
			}
			if (event.code == 40 && !event.control && !event.meta && event.alt) { // alt + DOWN
				event.stop();
				this.snapshot(event);
				this.moveLines(area, "down");
				return;
			}
			if (event.code == 9 && !event.control && !event.alt) {
				event.stop();
				this.insertText(area, "\t");
				return;
			}
			if (event.code == 13 && !event.control && !event.alt) {
				this.snapshot(event);
				// late processing the intend, after events have completed
				// to avoid conflict with e.g. auto-complete
				var intend = this.getIntend(area);
				setTimeout(function () {
					if (!this.isSelectionAtStartOfLine(area)) return;
					this.insertText(area, intend);
				}.bind(this));
				return;
			}
			// snapshot on cursor keys
			if (event.code >= 37 && event.code <= 40) {
				this.snapshot(event);
			}
			// snapshot on commands
			if (event.code >= 65 && event.code <= 90 && (event.control || event.alt || event.meta)) {
				this.snapshot(event);
			}
		},
		moveLines: function (area, direction) {
			this.extendSelectionToFullLines(area);
			// get lines and
			var lines = this.getSelection(area);
			// make sure that we have a "\n" at the end (not happens in last line)
			var missingLF = lines.charCodeAt(lines.length-1) != 10;
			if (missingLF) {
				// add a line break
				lines = lines + "\n";
				// but the also remove one additional line break before
				var sel = this.getSelectionCoordinates(area);
				if (sel.start > 0) this.setSelection(area, sel.start-1, sel.end);
			}
			this.insertText(area, "");
			var text = area.getValue();
			var curPos = this.getCursor(area);
			if (missingLF) curPos++;
			
			missingLF = false;
			var newPos;
			if (direction == "up") {
				newPos = text.substring(0, curPos-1).lastIndexOf('\n')+1;
			}
			else if (direction == "down"){
				newPos = text.substring(curPos).indexOf('\n');
				if (newPos == -1) {
					missingLF = true;
					newPos = text.length;
					lines = "\n" + lines.substring(0, lines.length-1);
				}
				else newPos += curPos + 1;
			}
			this.setSelection(area, newPos);
			this.insertText(area, lines);
			if (missingLF) {
				this.setSelection(area, newPos + 1, newPos + lines.length);
			}
			else {
				this.setSelection(area, newPos, newPos + lines.length);
			}
		},
		extendSelectionToFullLines: function(area) {
			var text = area.getValue();
			var sel = this.getSelectionCoordinates(area);
			var sel1 = Math.min(sel.start, sel.end);
			var sel2 = Math.max(sel.start, sel.end);
			if (sel2 > sel1 && text.charCodeAt(sel2-1) == 10) sel2--;
			var start = text.substring(0, sel1).lastIndexOf("\n") + 1;
			var end = text.substring(sel2).indexOf("\n");
			if (end == -1) end = text.length;
			else end += 1 + sel2;
			this.setSelection(area, start, end);
		},
		undo: function(area) {
			while (true) {
				if (area.undoHistory.length == 0) return;
				var shot = area.undoHistory.pop();
				area.redoHistory.push(shot);
				if (shot.text != area.getValue()) break;
			}
			this.restoreSnapshot(area, shot);
		},
		redo: function(area) {
			while (true) {
				if (area.redoHistory.length == 0) return;
				var shot = area.redoHistory.pop();
				area.undoHistory.push(shot);
				if (shot.text != area.getValue()) break;
			}
			this.restoreSnapshot(area, shot);
		},
		snapshot: function(event) {
			area = this.getArea(event);
			var text = area.getValue();
			// avoid duplicate entries
			if (area.undoHistory.length > 0 && area.undoHistory[area.undoHistory.length-1].text == text) return; 
			if (area.redoHistory.length > 0 && area.redoHistory[area.redoHistory.length-1].text == text) return; 
			var sel = this.getSelectionCoordinates(area);
			// if we have a redo history, we take the recent element back to the undo history,
			// because it was the text fields original state before editing again
			if (area.redoHistory.length > 0) {
				var shot = area.redoHistory.pop();
				area.undoHistory.push(shot);
				area.redoHistory = [];
			}
			area.undoHistory.push({
				text: text,
				start: sel.start,
				end: sel.end,
				scroll: area.scrollTop
				});
		},
		restoreSnapshot: function(area, shot) {
			area.value = shot.text;
			var sel = this.setSelection(area, shot.start, shot.end);
			area.scrollTop = shot.scroll;
		},
		getSelection : function(c) {
			var a = $(c);
			if (!a) {
				return ""
			}
			var b = this.getSelectionCoordinates(c);
			return a.getValue().substring(b.start, b.end)
		},
		setSelection : function(area, f, a) {
			if (!a) {
				a = f
			}
			if ($defined(area.setSelectionRange)) {
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
		},
		getCursor : function(area) {
			return this.getSelectionCoordinates(area).start
		},
		getIntend : function(area) {
			var text = area.getValue().substring(0, this.getCursor(area));
			var pos = text.lastIndexOf("\n") + 1;
			var intend = "";
			while (pos < text.length) {
				var c = text.charAt(pos++);
				if (c == '\t' || c == ' ') intend += c;
				else break;
			}
			return intend;
		},
		getSelectionCoordinates : function(area) {
			var f = $(area), e = {
				start : 0,
				end : 0,
				thin : true
			};
			if ($defined(f.selectionStart)) {
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
		},
		replaceSelection : function(area, text) {
			var h = text.replace(/\r/g, ""), d = area, c = d.scrollTop;
			if ($defined(d.selectionStart)) {
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
			d.fireEvent("change");
			return;
		},
		insertText : function(area, text) {
			this.replaceSelection(area, text);
			this.setSelection(area, this.getSelectionCoordinates(area).end);
		},
		isSelectionAtStartOfLine : function(c) {
			var b = $(c);
			if (!b) {
				return false;
			}
			var a = this.getCursor(c);
			return ((a <= 0) || (b.value.charAt(a - 1).match(/[\n\r]/)));
		}
	};
}