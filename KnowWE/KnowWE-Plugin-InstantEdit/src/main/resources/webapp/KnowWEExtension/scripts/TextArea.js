
if (!TextArea) {
	var TextArea = {
		initialize : function(a, suppressHotkeys) {
			this.textarea = $(a);
			if (!suppressHotkeys) {
				this.textarea.addEvent("keydown", this.handleKeyDown.bind(this));
			}
			return this
		},
		handleKeyDown : function (event) {
			event = new Event(event);
			if ((event.control && !event.alt) || (!event.control && event.alt)) {
				if (event.code == 83) { // S
					event.stop();
					KNOWWE.plugin.instantEdit.save(this.textarea.getParent().getProperty('id'));
					return;					
				}
				if (event.code == 81) { // Q
					event.stop();
					KNOWWE.plugin.instantEdit.cancel(this.textarea.getParent().getProperty('id'));
					return;	
				}
			}
		},
		getSelection : function(c) {
			var a = $(c);
			if (!a) {
				return ""
			}
			var b = this.getSelectionCoordinates(c);
			return a.getValue().substring(b.start, b.end)
		},
		setSelection : function(f, a) {
			var e = this.textarea;
			if (!a) {
				a = f
			}
			if ($defined(e.setSelectionRange)) {
				e.setSelectionRange(f, a)
			} else {
				var c = e.value, d = c.substr(f, a - f).replace(/\r/g, "").length;
				f = c.substr(0, f).replace(/\r/g, "").length;
				var b = e.createTextRange();
				b.collapse(true);
				b.moveEnd("character", f + d);
				b.moveStart("character", f);
				b.select()
			}
			return this
		},
		getCursor : function(a) {
			return this.getSelectionCoordinates(a).start
		},
		getSelectionCoordinates : function(g) {
			var f = $(g), e = {
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
		replaceSelection : function(a, g) {
			var h = g.replace(/\r/g, ""), d = $(a), c = d.scrollTop;
			if ($defined(d.selectionStart)) {
				var b = d.selectionStart, e = d.selectionEnd, i = d.value;
				d.value = i.substr(0, b) + h + i.substr(e);
				d.selectionStart = b;
				d.selectionEnd = b + h.length
			} else {
				d.focus();
				var f = document.selection.createRange();
				f.text = h;
				f.collapse(true);
				f.moveStart("character", -h.length);
				f.select()
			}
			d.focus();
			d.scrollTop = c;
			d.fireEvent("change");
			return
		},
		isSelectionAtStartOfLine : function(c) {
			var b = $(c);
			if (!b) {
				return false
			}
			var a = this.getCursor(c);
			return ((a <= 0) || (b.value.charAt(a - 1).match(/[\n\r]/)))
		}
	};
}