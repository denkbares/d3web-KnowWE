InsertSectionMarker = {
	show: function(x, y) {
		if (!this.marker) {
			this.marker = new Element('div');
			this.marker.style.position = 'absolute';
			this.marker.innerHTML = '<div class="InsertSectionMarker"></div>';
			document.body.appendChild(this.marker);
		}
		this.marker.style.left = x+'px';
		this.marker.style.top = y+'px';
	},
	hide: function() {
		if (this.marker) {
			this.marker.remove();
			this.marker = null;
		}
	},
	createDrag: function(element, handle) {
		new Drag.Base(element, {
			handle : handle,
			onStart: function(el) {
				var pos = el.getPosition();
				this.dx = this.mouse.start.x - pos.x;
				this.dy = this.mouse.start.y - pos.y;
			},
			onSnap: function(el) {
				this.myClone = el.clone();
				this.myClone.style.position = 'absolute';
//				this.myClone.style.width = el.offsetWidth + 'px';
//				this.myClone.style.height = el.offsetHeight + 'px';
				document.body.appendChild(this.myClone);
				this.myClone.addClass('draggingInsertSectionMenuItem');
			},
			onDrag: function(el, b, c, d) {
				if (this.myClone) {
					var mouse = this.mouse.now;
					this.myClone.style.left = (mouse.x - this.dx) + 'px';
					this.myClone.style.top = (mouse.y - this.dy) + 'px';
					InsertSectionMarker.hide();
					var content = $('pagecontent');
					var childs = content.childNodes;
					var prev = null;
					for (var i=0; i<childs.length; i++) {
						var child = childs[i];
						if (!child.offsetTop) continue; // ignore text nodes
						var fromPos, toPos, insertPos;
						if (prev) {
							fromPos = Math.floor(prev.offsetTop + prev.offsetHeight/2);
							toPos = Math.floor(child.offsetTop + child.offsetHeight/2);
							insertPos = Math.floor((child.offsetTop + prev.offsetTop + prev.offsetHeight)/2);
						}
						else {
							fromPos = child.offsetTop;
							toPos = Math.floor(child.offsetTop + child.offsetHeight/2);
							insertPos = child.offsetTop;
						}
						if (mouse.y >= fromPos && mouse.y < toPos) {
							InsertSectionMarker.show(childs[i].offsetLeft, insertPos);
						}
						prev = child;
					}
					if (!prev) {
						// wenn kein element drinnen ist, dann einfach in die contents einfügen
						if (mouse.y >= content.offsetTop && mouse.y <= content.offsetTop + content.offsetHeight) {
							InsertSectionMarker.show(
								contents.offsetLeft+10, 
								Math.floor(content.offsetTop + content.offsetHeight/2));
						}
					}
				}
			},
			onComplete: function(el) {
				if (this.myClone) {
					this.myClone.remove();
					this.myClone = null;
				}
				InsertSectionMarker.hide();
			}
		});	
	}
};

