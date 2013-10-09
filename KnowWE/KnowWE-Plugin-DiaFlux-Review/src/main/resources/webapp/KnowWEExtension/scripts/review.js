if (typeof DiaFlux == "undefined" || !DiaFlux) {
    var DiaFlux = {};
}

DiaFlux.ReviewTool = (function(flow) {
	var panel;
	var flowDOM;
	var popup;
	
	var review;
	
	//if an old version of the page is shown, then disable editing
	var editable = true;
	var modState = false;
	var currentEdit = null;
	var currentSelection= null;
	var filters= "";
	
	var PRIORITIES = ['low', 'important', 'critical'];
	var PRIORITIES_SELECTOR = "." + PRIORITIES.join(', .');
	var PRIORITY_CLASSES = PRIORITIES.join(' ');
	var STATES = ['open', 'resolved'];
	
	return {
		
		init : function(){
			flowDOM = jq$(flow.dom);
			panel = jq$("<div>", {id: flow.kdomid + "-review" ,"class" : "reviewPanel"}).appendTo(flowDOM.parent());
			popup = jq$("<div>", {'class': 'reviewPopup'}).appendTo(document.body);
			
			var pagever = KNOWWE.helper.gup('version');
			if (pagever && pagever != KNOWWE.helper.getPageVersion()){
				editable = false;
			}
			
			if (editable){
				this.createToolbar(panel);
			} else {
				jq$("<div>", {'class': 'information'}).text('An outdated version of this article is shown. You cannot edit the review.').appendTo(panel);
			}
			this.getReview(flow, panel);
			
			this.addMarkingHandler();
			this.addPopupHandler();
			
			this.bindUnloadFunction(panel);

		},
		
		bindUnloadFunction : function(panel) {
			jq$(window).bind('beforeunload', function(){
				if (this.isModified())
					return "edit.areyousure".localize();
			}.bind(this));
		},
		
		createToolbar : function(panel){
			var toolbar = jq$("<div>", {"class":"reviewToolbar"}).appendTo(panel);
			jq$("<a>",{"class":"saveReview", title: "Save review"})
			.appendTo(toolbar)
			.on('click', 
				function(event){
					this.saveReview(review, flow.kdomid);
					this.setModified(false);
				}.bind(this)
			);
			
			jq$("<a>",{"class":"addItem", title: "Add new review item"})
			.appendTo(toolbar)
			.on('click', 
				function(event){
					this.addItem(review, panel);
				}.bind(this)
			);
			
//			var filter = jq$("<div>", {"class":"itemFilter"}).appendTo(panel);
//			this.createFilterPanel(filter, PRIORITIES);
//			this.createFilterPanel(filter, STATES, [false, true]);
			
		},
		
//		createFilterPanel : function(parent, buttons, states){
//			var panel = jq$("<div>", {"class":"filterPanel"}).appendTo(parent);
//			var tool = this;
//			for (var i = 0; i < buttons.length; i++) {
//				var classes = "filterButton " + buttons[i];
//				if (!states || (states && states[i])) {
//					classes += " pressed";
//					filters += " ." + buttons[i];
//				}
//				
//				jq$("<div>", {"class":  classes, title: buttons[i]}).appendTo(panel)
//				.on('click', function(e){
//					var button = jq$(this);
//					button.toggleClass('pressed');
//					if (button.hasClass(button.attr('title'))){
//						filters += " ." + button.attr('title');
//					} else {
//						filters.replace(" ." + button.attr('title'), "");
//					}
//					tool.refreshFilters();
//				});
//			}
//		},
//		
//		refreshFilters : function(){
//			panel.find('li').filter(filters).each(function(index, elem){
//				elem.hide();
//			});
//			
//		},
		
		addItem : function(review, panel){
			var item = review.newItem();
			this.setModified(true);
			this.renderItem(item, panel.find('ul')).dblclick(); //trigger edit
		},
		
		saveReview : function(review, kdomid){
			var params = {
				action : 'SaveReviewAction',
				SectionID: kdomid
			};
			
			var options = {
				url: KNOWWE.core.util.getURL( params ),
				data: review.toXML()
								
		    };
			
			KNOWWE.core.util.updateProcessingState(1);
			try{
				new _KA(options).send();
			} catch(e) {
				//TODO
			}
			KNOWWE.core.util.updateProcessingState(-1);
			this.setModified(false);
		},
		
		addMarkingHandler : function(){
			flowDOM.find('.Node').on('click', 
				function(event) {
					if (this.isEditing()){
						var prio  = currentEdit.data('item').getPriority();
						this.mark(event.delegateTarget, prio, true);
						
					}
				}.bind(this)
			);
			flowDOM.find('.rule_selector').on('click', 
				function(event) {
					if (this.isEditing()){
						var prio  = currentEdit.data('item').getPriority();
						var set = jq$(event.delegateTarget).closest('.Rule'); 
						this.mark(set, prio, true);
					}
				}.bind(this)
			);
		},
		
		addPopupHandler : function(){
			var tool = this;
			
			flowDOM.find('.Node').hover(function(event) {
				if (tool.isEditing()) return;
				
				currentContent = this;
				tool.showPopup(jq$(this).closest('.Node'), event.pageX, event.pageY);
				
			}, function(event){
				popup.hide();
			});
			
			flowDOM.find('.rule_selector').hover(function(event) {
				if (tool.isEditing()) return;
				
				tool.showPopup(jq$(this).closest('.Rule'), event.pageX, event.pageY);
			
			}, function(event){
				popup.hide();	
			});
		},
		
		showPopup : function(elem, x, y) {
			popup.empty();
			var allRevs = this.getReviewsContaining(elem.attr('id'));
			
			var content = jq$('<div>');
			for (var prio = PRIORITIES.length - 1; prio >= 0; prio--){
				var revs = allRevs[PRIORITIES[prio]];
				if (revs) {
					jq$('<div>').text(PRIORITIES[prio] + ' priority:').appendTo(content);
					var list = jq$('<ol>').appendTo(content);
					for (var i = 0; i < revs.length; i++) {
						jq$('<li>', {value: revs[i].getId()}).text(revs[i].getDescription()).appendTo(list);
					}
					
				}
				
			}
			if (content.children().length) {
				popup.append(content).css('top', y).css('left', x).show();
			}
			
		},
		
		getReviewsContaining : function(id) {
			var result = {}; 
			var items = review.getItems();
			
			next:
			for (var j = 0; j < items.length; j++) {
				var marks = items[j].getMarkings();
				var prio = items[j].getPriority();
				for (var k = 0; k < marks.length; k++) {
					if (marks[k] == id) {
						if (!result[PRIORITIES[prio]]) {
							result[PRIORITIES[prio]] = []
						}
						result[PRIORITIES[prio]].push(items[j]);
						continue next;
					}
				}
				
			}
			return result;
		},
		
		isEditing : function() {
			if (currentEdit)
				return true;
			else 
				return false;
		},
		
		isModified : function() {
			return modState;
		},
		
		setModified : function(modified) {
			modState = modified;
			if (modState) {
				panel.find('a.saveReview').addClass('modified');
			} else {
				panel.find('a.saveReview').removeClass('modified');
			}
		},

		getReview : function(flow, panel){
			var params = {
				action : 'LoadReviewAction',
				SectionID: flow.kdomid
			};
			var that = this;
			
			var options = {
				url: KNOWWE.core.util.getURL( params ),
		        response : {
		            fn: function(){that.loadReview(this.responseXML, panel);}
		        }
		    };
		    new _KA(options).send();
			
		},
		
		loadReview : function(response, panel){	
			review = DiaFlux.Review.fromXML(response);
			review.checkItems(flow);
			this.showReview(review, panel);
		},
		
		showReview : function(review, panel){
			var list = jq$("<ul>", {"class":"reviewList"}).appendTo(panel);
			var items = review.getItems();
			
			for (var i = 0; i < items.length; i++){
				this.renderItem(items[i], list);
			}
			this.showAllMarkings();
			var itemToSelect = KNOWWE.helper.gup('item');
			if (itemToSelect) {
				this.selectById(itemToSelect);
			}
			
			
		},
		
		renderItem : function(item, list){
			var li = jq$("<li>", {id:flow.kdomid + "-item-" + item.getId()})
				.appendTo(list)
				.on('click', function(){this.handleSelection(li);}.bind(this))
				.on('dblclick', function() {if (editable)jq$(this).find('a.editItem').click();});
			li.data('item', item);
			this.renderItemContent(item, li);
			return li;
		},
		
		renderItemContent : function(item, li){
			li.empty();
			var container = jq$("<div>", {"class" : "reviewItem"}).appendTo(li);
			var date = item.getDate();
			var info = "#" + item.getId() + " by " + item.getAuthor() + " at " + date.toLocaleString(); 
			var infoDiv = jq$('<div>', {'class' : 'reviewItemInfo'}).text(info).appendTo(container);
			var statusDiv = jq$("<div>", {"class" : "reviewItemStatus"}).appendTo(infoDiv);
			var priorityDiv = jq$("<div>", {"class" : "reviewItemPriority"}).appendTo(statusDiv);
			var stateDiv = jq$("<div>", {"class" : "reviewItemState"}).appendTo(statusDiv);

			this.renderIcon(priorityDiv, PRIORITIES, item.getPriority());
			this.renderIcon(stateDiv, STATES, item.getState());
			jq$('<div>', {'class' : 'reviewText'}).text(item.getDescription()).appendTo(container);
			this.renderComments(container, item);
			if (editable){
				this.addItemActions(item, infoDiv);
			}
			
			if (item.isOutdated()){
				jq$('<div>', {'class': 'information'}).html('This issue can not be displayed properly in this version. ').appendTo(container)
				.append(jq$('<a>', {href: this.createLink(item)}).text('Open the according version of the article.'));
			}

		}, 
		
		createLink : function(item){
			var page = KNOWWE.helper.gup('page');
			return 'Wiki.jsp?page=' + page +'&version=' + item.getPageRev() + '&item=' +item.getId();
		},
		
		createDiffLink : function(version1, version2){
			var page = KNOWWE.helper.gup('page');
			return 'Diff.jsp?page=' + page +'&r1=' + version2 +'&r2=' + version1;
		},
			
		handleSelection : function(li){
			if (this.isEditing()) return;
			if (currentSelection == li) {
				this.deselect();
				this.showAllMarkings();
			} else {
				this.select(li);
			}
			
		},
		
		select : function(li){
			currentSelection = li;
			li.closest('ul').find('li').removeClass('selected');
			li.addClass('selected');
			this.showMarkings(li);
		},
		
		selectById : function(id){
			var li = panel.find('li#'+flow.kdomid + '-item-' + id);
			if (li) this.select(li);
		},
		
		deselect : function(){
			currentSelection.removeClass('selected');
			this.removeAllMarkings(currentSelection);
			currentSelection = null;
			
		},
		
		collectMarkings : function(li){
			return jq$.map(flowDOM.find(PRIORITIES_SELECTOR), function(val, key) {
				return val.getAttribute('id');
			});
			
		},
		
		showMarkings : function(li){
			this.removeAllMarkings();
			var item=li.data('item');
			var markings = item.getMarkings();
			
			for (var i = 0; i < markings.length; i++){
				this.markById(markings[i], item.getPriority(), false);
			}
		},
		
		showAllMarkings : function() {
			var allMarks = {};
			var items = review.getItems();
			for (var i = 0; i <items.length; i++) {
				var prio = items[i].getPriority()
				var markings = items[i].getMarkings();
				for (var j = 0; j < markings.length; j++){
					allMarks[markings[j]] = Math.max(allMarks[markings[j]] || 0, prio);
				}
			}
		
			this.removeAllMarkings();

			for (var id in allMarks) {
			  if (allMarks.hasOwnProperty(id)) {
				  this.markById(id, allMarks[id], false);
			  }
			}
			
		},
		
		markById : function(id, prio, toggle){
			var flowElem = flow.findObject(id);
			if (flowElem) this.mark(flowElem.getDOM(), prio, false);
		},
		
		mark : function(elem, prio, toggle){
			var colorclass = PRIORITIES[prio];
			if (toggle) {
				for (var i = 0; i < PRIORITIES.length; i++){
					if (elem.hasClass(PRIORITIES[i])) {
						elem.removeClass(PRIORITIES[i]);
						return;
					}
				}
				elem.addClass(colorclass);
			} else {
				elem.addClass(colorclass);
			}
		},

		removeAllMarkings : function(){
			flowDOM.find(PRIORITIES_SELECTOR).removeClass(PRIORITY_CLASSES);
		},
		
		addItemActions : function(item, parent){
			
			var actionsDiv = jq$("<div>", {"class":"reviewActions"}).appendTo(parent);
			jq$("<a>", {"class":"editItem", title: "Edit this issue"}).appendTo(actionsDiv).on('click', this.editItem.bind(this));
			jq$("<a>", {"class":"addComment", title: "Add comment"}).appendTo(actionsDiv).on('click', this.addComment.bind(this));
			jq$("<a>", {"class":"deleteItem", title: "Delete this issue"}).appendTo(actionsDiv).on('click', this.deleteItem.bind(this));
			
		},
		
		editItem : function(event){
			if (currentEdit) return; //allow only one edit
			var li = jq$(event.delegateTarget).closest('li');
			li.addClass('editing');
			currentEdit = li;
			this.select(li);
			var item = li.data('item');
			
			var prioDiv = li.find('.reviewItemPriority');
			this.createImageDropdown(prioDiv, 'itemPrio', PRIORITIES, item.getPriority(), function(i){
				item.setPriority(i);
			});
			
			li.find('.reviewItemState').remove();
//			var stateDiv = li.find('.reviewItemState');
//			this.createImageDropdown(stateDiv, 'itemState', STATES, item.getState(), function(i){
//				if (i == 0){
//					item.setResolvedPageRev(-1);
//				} else {
//					var rev = KNOWWE.helper.getPageVersion();
//					item.setResolvedPageRev(rev);
//				}
//			});
			
			var parent = li.find('.reviewText').empty();
			var textArea = jq$('<textarea>', {rows: 5}).val(item.getDescription()).appendTo(parent).focus();
			
			var actionsDiv = jq$('<div>', { "class": "editActions"}).appendTo(parent);
			
			var endEdit = function(){
				currentEdit = null;
				li.removeClass('editing');
				this.renderItemContent(item, li);
			}.bind(this);
			
			jq$('<a>', {'class':'saveChanges'}).text('Ok ').on('click',
				function() {
					item.setDescription(textArea.val());
					item.setMarkings(this.collectMarkings(li));
					item.setPriority(prioDiv.find('input[name=itemPrio]:checked').val());
					this.setModified(true);
					endEdit();
				}.bind(this)
			).appendTo(actionsDiv);
			
			jq$('<a>',{'class':'discardChanges'}).text('Cancel').on('click', endEdit).appendTo(actionsDiv);
			event.preventDefault();
		},
		
		renderComments : function(parent, item) {
			var container = jq$('<div>', {'class' : 'reviewComments'}).appendTo(parent);
			var comments = item.getComments();
			for (var i = 1; i < comments.length; i++){
				var info = "Comment by " + comments[i].getAuthor() + " at " + comments[i].getDate().toLocaleString();
				var infoDiv = jq$('<div>', {'class' : 'reviewCommentInfo'}).text(info);
				var commentDiv = jq$('<div>', {'class' : 'reviewComment'}).appendTo(container)
				.append(infoDiv)
				.append(jq$('<div>', {'class' : 'reviewCommentText'}).text(comments[i].getText()));
				var state = comments[i].getState();
				if (comments[i - 1].getState() != state){
					var hint = jq$('<div>', {}).appendTo(commentDiv);
					var text;
					if (state == 0){
						hint.text("This issue was changed to '" + STATES[0] + "' again.")
					} else {
						var issueRev = item.getPageRev();
						var resolvedRev = comments[i].getResolvedPageRev();
						var diffChild;
						if (issueRev == resolvedRev){
							diffChild = jq$('<span>').text("No differences.");
							
						} else {
							//TODO would be better, if each comment had a pagerev, in case its state is set to open again
							var diffLink = this.createDiffLink(issueRev, resolvedRev);
							diffChild = jq$('<a>', {href: diffLink}).text("Show differences.")
						}
						
						hint.text("This issue was changed to '" + STATES[1] + "'. ").append(diffChild);
					}
					var stateDiv = jq$('<div>', {'class' : 'reviewItemState'}).appendTo(infoDiv);
					stateDiv.append(this.renderIcon(hint, STATES, state));
				}
			}
		},
		
		createImageDropdown : function(parent, name, options, checked, callback){
			var selectParent = jq$('<div>', {"class": 'image-dropdown'}).appendTo(parent);
			
			var tool = this;
			for (var i = 0; i < options.length; i++) {
				var config = {
						type: 'radio',
						id: options[i],
						name: name,
						value: i,
						checked : i == checked
				};
				jq$('<input>', config)
				.change(function(e){
					var $this = jq$(this);
					var index = $this.val();
					if ($this.attr('checked')){
						if (callback) callback(index);
					}
					
					tool.renderIcon(parent, options, index);
					selectParent.fadeOut('fast');
					e.stopPropagation();
				}).appendTo(selectParent);
				selectParent.append(jq$('<label>', {'for': options[i]}).text(options[i]));
			}
			
			parent.on('click', function(e) {
				selectParent.fadeIn('fast');
			});
			this.renderIcon(parent, options, checked);
			
		},
		
		renderIcon : function(parent, options, checked){
			parent.find('.imageLabel').remove();
			return jq$("<label>", {'class' :'imageLabel', 'for': options[checked], title : options[checked]}).prependTo(parent);
		},
		
		addComment : function(event) {
			if (currentEdit) return; //allow only one edit
			var li = jq$(event.delegateTarget).closest('li');
			li.addClass('editing');
			currentEdit = li;
			this.select(li);
			var item = li.data('item');
			
			var parent = li.find('div.reviewComments');
			var textArea = jq$('<textarea>', {rows: 5}).appendTo(parent).focus();
			var stateDiv = jq$('<div>', {'class' : 'reviewItemState'}).css({'position': 'absolute'}).appendTo(parent);
			this.createImageDropdown(stateDiv, 'itemState', STATES, item.getState());
			
			var actionsDiv = jq$('<div>', { "class": "editActions"}).appendTo(parent);
			
			var endEdit = function() {
				currentEdit = null;
				li.removeClass('editing');
				this.renderItemContent(item, li);
			}.bind(this);
			
			jq$('<a>', {'class':'saveChanges'}).text('Ok ').on('click',
				function() {
					var text = textArea.val();
					var oldState = item.getState();
					var comment = item.newComment(textArea.val());
					var state = stateDiv.find('input[name=itemState]:checked').val()
					if (state != oldState) {
						if (state == 1){
							comment.setResolvedPageRev(KNOWWE.helper.getPageVersion());
						}
						else {
							comment.setResolvedPageRev(-1);
						}
					}
					
					this.setModified(true);
					endEdit();
				}.bind(this)
			).appendTo(actionsDiv);
			
			jq$('<a>',{'class':'discardChanges'}).text('Cancel').on('click', endEdit).appendTo(actionsDiv);
		},
		
		deleteItem : function(event){
			var li = jq$(event.delegateTarget).closest('li');
			var item = li.data('item');
			if (!confirm("Do you really want to delete review #" + item.getId() + "?")) return;
			review.removeItem(item);
			li.remove();
			this.deselect();
			this.setModified(true);
			
		}
	}
});

/**
 * Review class
 * Stores items
 */
DiaFlux.Review = function(flowName, idCounter){
	var items = [];
	
	return {
		
		getFlowName : function() {
			return flowName;
		},

		getItems : function() {
			return items;
		},

		addItem : function(item) {
			items.push(item);
		},

		removeItem : function(item) {
			var index = items.indexOf(item);
			if (index != -1)
			items.splice(index, 1);
		},

		getIdCounter : function() {
			return idCounter;
		},

		newItem : function() {
			var item = new DiaFlux.ReviewItem(++idCounter);
			item.newComment();
			item.setPageRev(KNOWWE.helper.getPageVersion());
			this.addItem(item);
			return item;
		},
		
		checkItems : function(flow) {
			for(var i = 0; i < items.length; i++) {
				items[i].check(flow);
			}
		},

		toXML : function() {
			var xml = '<?xml version="1.0" encoding="UTF-8"?>\n';
			xml += '<review idCounter="' + this.getIdCounter() +'" flowName="' + this.getFlowName() + '">\n';

			for (var i = 0; i < items.length; i++){
				xml += items[i].toXML();
			}
			
			xml += '</review>\n';
			return xml;
			
		}
		
	}
}

// TODO use an appropriate escape routine
DiaFlux.Review.escapeXML = function(string) {
	return jq$('<div>').text(string).html();
}

DiaFlux.Review.fromXML = function(xml){
	var idCounter = parseInt(xml.getElementsByTagName('review')[0].getAttribute('idCounter'));
	var flow = xml.getElementsByTagName('review')[0].getAttribute('flowName');
	var xmlItems = xml.getElementsByTagName('item');
	var review = new DiaFlux.Review(flow, idCounter);
	
	for (var i = 0; i < xmlItems.length; i++){
		review.addItem(DiaFlux.ReviewItem.fromXML(xmlItems[i]));
	}
	
	return review;
}


/**
 * Review item class
 * Stores one issue
 */
DiaFlux.ReviewItem = function(id){
	var markings = [];
	var pageRev = -1;
	var priority = 1;
	var state = 0;
	var outdated = false;
	//stores all comments. 
	//the first one corresponds to the properties of the review item
	var comments = [];

	return {
		
		getId : function() {
			return id;
		},
		
		getDate : function() {
			return comments[0].getDate();
		},
		
		getAuthor : function() {
			return comments[0].getAuthor();
		},
		
		setPageRev : function(newPageRev) {
			pageRev = newPageRev;
		},
		
		getPageRev : function() {
			return pageRev;
		},
		
		setDescription : function(desc) {
			comments[0].setText(desc);
		},
		
		getDescription : function() {
			return comments[0].getText();
		},
		
		setPriority : function(prio) {
			priority = prio;
		},
		
		getPriority : function() {
			return priority;
		},
		
		getState : function() {
			return comments[comments.length - 1].getState();
		},
		
		setMarkings : function(marks) {
			markings = marks;
		},
		
		getMarkings : function() {
			return markings;
		},
		
		addComment : function(comment) {
			comments.push(comment);
		},
		
		newComment : function(text) {
			var comment = new DiaFlux.ReviewComment(new Date(), KNOWWE.helper.getUsername());
			comment.setText(text || '');
			if (comments.length > 0) {
				comment.setResolvedPageRev(comments[comments.length - 1].getResolvedPageRev());
			}
			this.addComment(comment);
			return comment;
		},
		
		getComments : function() {
			return comments;
		},

		check : function(flow){
			for (var i = 0; i < markings.length; i++){
				if (!flow.findObject(markings[i])){
					outdated = true;
				}
			}
		},
		
		isOutdated : function() {
			return outdated;
		},
		
		toXML : function() {
			var xml = '<item id="' + this.getId() + '" ';
		
			xml += 'pageRev="' + this.getPageRev() +'" ';
			xml += 'priority="' + this.getPriority() + '" ';
			xml += '>\n';
			xml += '<comments>\n';
			
			for (var i = 0; i < comments.length; i++){
				xml += comments[i].toXML();
			}
			
			xml += '</comments>\n';
			
//			xml += '<description>' + DiaFlux.Review.escapeXML(this.getDescription()) + '</description>\n';
			xml += '<markings>\n';
			
			var markings = this.getMarkings();
			
			for (var i = 0; i < markings.length; i++){
				xml += '<mark id="' + markings[i] + '"/>\n';
			}
			xml += '</markings>\n';
			xml += '</item>\n';
			return xml;
		}
	}
}



DiaFlux.ReviewItem.fromXML = function(itemXML){
	var id = parseInt(itemXML.getAttribute('id'));
	var item = new DiaFlux.ReviewItem(id);

	var pageRev = itemXML.getAttribute('pageRev');
	var priority = itemXML.getAttribute('priority');
	
	var commentsXML = itemXML.getElementsByTagName('comment');
	
	for (var i = 0; i < commentsXML.length; i++){
		item.addComment(DiaFlux.ReviewComment.fromXML(commentsXML[i]));
	}
		
	var markXML = itemXML.getElementsByTagName('markings')[0].getElementsByTagName('mark');
	var markings = [];
	
	for (var i = 0; i < markXML.length;i++){
		markings.push(markXML[i].getAttribute('id'));
	}
	
	item.setMarkings(markings);
	item.setPageRev(pageRev);
	item.setPriority(priority);
	
	return item; 
	
}

DiaFlux.ReviewComment = function(date, author) {
	var text = "";
	var resolvedPageRev = -1;
	var state = 0;
	return {
		
		getDate : function() {
			return date;
		},
		
		getAuthor : function() {
			return author;
		},

		getText : function(){
			return text;
		},
		
		setText : function(newText){
			text = newText;
		},
		
		setResolvedPageRev : function(rev) {
			resolvedPageRev = rev;
			if (resolvedPageRev != -1) {
				state = 1;
			} else {
				state = 0;
			}
		},
		
		getResolvedPageRev : function() {
			return resolvedPageRev;
		},
		
		getState : function() {
			return state;
		},
		
		toXML : function(){
			var xml = '<comment ';
			xml += 'date="' + this.getDate() + '" ';
			xml += 'resolvedPageRev="' + this.getResolvedPageRev() + '" ';
			xml += 'author="' + this.getAuthor() +'">';
			xml += DiaFlux.Review.escapeXML(this.getText());
			xml += '</comment>\n'
			
			return xml;
		}
		
		
	};
}

DiaFlux.ReviewComment.fromXML = function(commentXML){
	
	var date = new Date(commentXML.getAttribute('date'));
	var author = commentXML.getAttribute('author');
	var resolvedPageRev = commentXML.getAttribute('resolvedPageRev');
	var description = "";
	if (commentXML.firstChild){
		description = commentXML.firstChild.nodeValue;
	}
	var comment = new DiaFlux.ReviewComment(date, author);
	comment.setText(description);
	comment.setResolvedPageRev(resolvedPageRev);
	return comment;
	
} 

KNOWWE.helper.observer.subscribe("flowchartrendered", function(){
	new DiaFlux.ReviewTool(this.flow).init();
});

KNOWWE.helper.observer.subscribe("flowchartlinked", function(){
	var nodes = jq$(this.flow.dom).find('.Node a>div:first-child');
	var links = jq$(this.flow.dom).find('.Node a');
	links.on('click', function(e){
		// prevent def link behavior, if ctrl is not clicked (opens in new window)
		if (!e.ctrlKey) {
			e.preventDefault();
		}
	});
//	nodes.unwrap();
});

