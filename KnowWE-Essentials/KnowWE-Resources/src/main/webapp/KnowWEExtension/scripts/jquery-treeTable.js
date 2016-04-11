/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
(function() {
	var jq$, Node, Tree, methods;

	jq$ = jQuery;
	Node = (function() {
		function Node(row, tree, settings) {
			var parentId;
			this.row = row;
			this.tree = tree;
			this.settings = settings;

			// TODO Ensure id/parentId is always a string (not int)
			this.id = this.row.data(this.settings.nodeIdAttr);

			// TODO Move this to a setParentId function?
			parentId = this.row.data(this.settings.parentIdAttr);
			if (parentId != null && parentId !== "") {
				this.parentId = parentId;
			}

			this.treeCell = jq$(this.row.children(this.settings.columnElType)[this.settings.column]);
			this.expander = jq$(this.settings.expanderTemplate);
			this.indenter = jq$(this.settings.indenterTemplate);
			this.children = [];
			this.initialized = false;
			this.treeCell.prepend(this.indenter);
		}

		Node.prototype.addChild = function(child) {
			return this.children.push(child);
		};

		Node.prototype.ancestors = function() {
			var ancestors, node;
			node = this;
			ancestors = [];
			while (node = node.parentNode()) {
				ancestors.push(node);
			}
			return ancestors;
		};

		Node.prototype.collapse = function() {
			this._hideChildren();
			this.row.removeClass("expanded").addClass("collapsed");
			this.expander.attr("title", this.settings.stringExpand);

			if (this.initialized && this.settings.onNodeCollapse != null) {
				this.settings.onNodeCollapse.apply(this);
			}

			return this;
		};

		// TODO destroy: remove event handlers, expander, indenter, etc.

		Node.prototype.expand = function() {
			if (this.initialized && this.settings.onNodeExpand != null) {
				this.settings.onNodeExpand.apply(this);
			}

			this.row.removeClass("collapsed").addClass("expanded");
			this._showChildren();
			this.expander.attr("title", this.settings.stringCollapse);

			return this;
		};

		Node.prototype.expanded = function() {
			return this.row.hasClass("expanded");
		};

		Node.prototype.hide = function() {
			this._hideChildren();
			this.row.hide();
			return this;
		};

		Node.prototype.isBranchNode = function() {
			if (this.children.length > 0
					|| this.row.data(this.settings.branchAttr) === true) {
				return true;
			} else {
				return false;
			}
		};

		Node.prototype.level = function() {
			return this.ancestors().length;
		};

		Node.prototype.parentNode = function() {
			if (this.parentId != null) {
				return this.tree[this.parentId];
			} else {
				return null;
			}
		};

		Node.prototype.removeChild = function(child) {
			var i = jq$.inArray(child, this.children);
			return this.children.splice(i, 1)
		};

		Node.prototype.render = function() {
			var settings = this.settings, target;
			if (settings.expandable === true && this.isBranchNode()) {
				this.indenter.html(this.expander);
				target = settings.clickableNodeNames === true ? this.treeCell
						: this.expander;
				target.unbind("click.treetable").bind(
						"click.treetable",
						function(event) {
							jq$(this).parents("table").treetable(
									"node",
									jq$(this).parents("tr").data(
											settings.nodeIdAttr)).toggle();
							return event.preventDefault();
						});
			}

			if (settings.expandable === true
					&& settings.initialState === "collapsed") {
				this.collapse();
			} else {
				this.expand();
			}

			this.indenter[0].style.paddingLeft = ""
					+ (this.level() * settings.indent) + "px";

			return this;
		};

		Node.prototype.reveal = function() {
			if (this.parentId != null) {
				this.parentNode().reveal();
			}
			return this.expand();
		};

		Node.prototype.setParent = function(node) {
			if (this.parentId != null) {
				this.tree[this.parentId].removeChild(this);
			}
			this.parentId = node.id;
			this.row.data(this.settings.parentIdAttr, node.id);
			return node.addChild(this);
		};

		Node.prototype.show = function() {
			if (!this.initialized) {
				this._initialize();
			}
			this.row.show();
			if (this.expanded()) {
				this._showChildren();
			}
			return this;
		};

		Node.prototype.toggle = function() {
			if (this.expanded()) {
				this.collapse();
			} else {
				this.expand();
			}
			return this;
		};

		Node.prototype._hideChildren = function() {
			var child, _i, _len, _ref, _results;
			_ref = this.children;
			_results = [];
			for (_i = 0, _len = _ref.length; _i < _len; _i++) {
				child = _ref[_i];
				_results.push(child.hide());
			}
			return _results;
		};

		Node.prototype._initialize = function() {
			this.render();
			if (this.settings.onNodeInitialized != null) {
				this.settings.onNodeInitialized.apply(this);
			}
			return this.initialized = true;
		};

		Node.prototype._showChildren = function() {
			var child, _i, _len, _ref, _results;
			_ref = this.children;
			_results = [];
			for (_i = 0, _len = _ref.length; _i < _len; _i++) {
				child = _ref[_i];
				_results.push(child.show());
			}
			return _results;
		};

		return Node;
	})();

	Tree = (function() {
		function Tree(table, settings) {
			this.table = table;
			this.settings = settings;
			this.tree = {};

			// Cache the nodes and roots in simple arrays for quick
			// access/iteration
			this.nodes = [];
			this.roots = [];
		}

		Tree.prototype.collapseAll = function() {
			var node, _i, _len, _ref, _results;
			_ref = this.nodes;
			_results = [];
			for (_i = 0, _len = _ref.length; _i < _len; _i++) {
				node = _ref[_i];
				_results.push(node.collapse());
			}
			return _results;
		};

		Tree.prototype.expandAll = function() {
			var node, _i, _len, _ref, _results;
			_ref = this.nodes;
			_results = [];
			for (_i = 0, _len = _ref.length; _i < _len; _i++) {
				node = _ref[_i];
				_results.push(node.expand());
			}
			return _results;
		};

		Tree.prototype.loadRows = function(rows) {
			var node, row, i;

			if (rows != null) {
				for (i = 0; i < rows.length; i++) {
					row = jq$(rows[i]);

					if (row.data(this.settings.nodeIdAttr) != null) {
						node = new Node(row, this.tree, this.settings);
						this.nodes.push(node);
						this.tree[node.id] = node;

						if (node.parentId != null) {
							this.tree[node.parentId].addChild(node);
						} else {
							this.roots.push(node);
						}
					}
				}
			}

			return this;
		};

		Tree.prototype.move = function(node, destination) {
			// Conditions:
			// 1: +node+ should not be inserted as a child of +node+ itself.
			// 2: +destination+ should not be the same as +node+'s current
			// parent (this
			// prevents +node+ from being moved to the same location where it
			// already
			// is).
			// 3: +node+ should not be inserted in a location in a branch if
			// this would
			// result in +node+ being an ancestor of itself.
			if (node !== destination && destination.id !== node.parentId
					&& jq$.inArray(node, destination.ancestors()) === -1) {
				node.setParent(destination);
				this._moveRows(node, destination);

				// Re-render parentNode if this is its first child node, and
				// therefore
				// doesn't have the expander yet.
				if (node.parentNode().children.length === 1) {
					node.parentNode().render();
				}
			}
			return this;
		};

		Tree.prototype.render = function() {
			var root, _i, _len, _ref;
			_ref = this.roots;
			for (_i = 0, _len = _ref.length; _i < _len; _i++) {
				root = _ref[_i];

				// Naming is confusing (show/render). I do not call render on
				// node from
				// here.
				root.show();
			}
			return this;
		};

		Tree.prototype._moveRows = function(node, destination) {
			var child, _i, _len, _ref, _results;
			node.row.insertAfter(destination.row);
			node.render();
			_ref = node.children;
			_results = [];
			for (_i = 0, _len = _ref.length; _i < _len; _i++) {
				child = _ref[_i];
				_results.push(this._moveRows(child, node));
			}
			return _results;
		};

		Tree.prototype.unloadBranch = function(node) {
			var child, children, i;

			for (i = 0; i < node.children.length; i++) {
				child = node.children[i];

				// Recursively remove all descendants of +node+
				this.unloadBranch(child);

				// Remove child from DOM (<tr>)
				child.row.remove();

				// Clean up Tree object (so Node objects are GC-ed)
				delete this.tree[child.id];
				this.nodes.splice(jq$.inArray(child, this.nodes), 1)
			}

			// Reset node's collection of children
			node.children = [];

			return this;
		};

		return Tree;
	})();

	// jQuery Plugin
	methods = {
		init : function(options) {
			var settings;

			settings = jq$.extend({
				branchAttr : "ttBranch",
				clickableNodeNames : false,
				column : 0,
				columnElType : "td", // i.e. 'td', 'th' or 'td,th'
				expandable : false,
				expanderTemplate : "<a href='#'>&nbsp;</a>",
				indent : 19,
				indenterTemplate : "<span class='indenter'></span>",
				initialState : "collapsed",
				nodeIdAttr : "ttId", // maps to data-tt-id
				parentIdAttr : "ttParentId", // maps to data-tt-parent-id
				stringExpand : "Expand",
				stringCollapse : "Collapse",

				// Events
				onInitialized : null,
				onNodeCollapse : null,
				onNodeExpand : null,
				onNodeInitialized : null
			}, options);

			return this.each(function() {
				var el, tree;

				tree = new Tree(this, settings);
				tree.loadRows(this.rows).render();

				el = jq$(this).addClass("treetable").data("treetable", tree);

				if (settings.onInitialized != null) {
					settings.onInitialized.apply(tree);
				}

				return el;
			});
		},

		destroy : function() {
			return this.each(function() {
				return jq$(this).removeData("treetable").removeClass(
						"treetable");
			});
		},

		collapseAll : function() {
			this.data("treetable").collapseAll();
			return this;
		},

		collapseNode : function(id) {
			var node = this.data("treetable").tree[id];

			if (node) {
				node.collapse();
			} else {
				throw new Error("Unknown node '" + id + "'");
			}

			return this;
		},

		expandAll : function() {
			this.data("treetable").expandAll();
			return this;
		},

		expandNode : function(id) {
			var node = this.data("treetable").tree[id];

			if (node) {
				node.expand();
			} else {
				throw new Error("Unknown node '" + id + "'");
			}

			return this;
		},

		loadBranch : function(node, rows) {
			rows = jq$(rows);
			if (node.children.length > 0) {
				rows.insertAfter(node.children[node.children.length - 1].row);
			} else {
				rows.insertAfter(node.row);
			}
			this.data("treetable").loadRows(rows);

			return this;
		},

		move : function(nodeId, destinationId) {
			var destination, node;

			node = this.data("treetable").tree[nodeId];
			destination = this.data("treetable").tree[destinationId];
			this.data("treetable").move(node, destination);

			return this;
		},

		node : function(id) {
			return this.data("treetable").tree[id];
		},

		reveal : function(id) {
			var node = this.data("treetable").tree[id];

			if (node) {
				node.reveal();
			} else {
				throw new Error("Unknown node '" + id + "'");
			}

			return this;
		},

		unloadBranch : function(node) {
			this.data("treetable").unloadBranch(node);
			return this;
		}
	};

	jq$.fn.treetable = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(
					arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			return jq$.error("Method " + method
					+ " does not exist on jQuery.treetable");
		}
	};

	// Expose classes to world
	this.TreeTable || (this.TreeTable = {});
	this.TreeTable.Node = Node;
	this.TreeTable.Tree = Tree;
}).call(this);

(function($) {
	$.extend({
		wrapCallbacks : function() {
			var args = [];
			for ( var i = 0, l = arguments.length; i < l; i++) {
				if (typeof arguments[i] === "undefined"
						|| arguments[i] === null)
					continue;
				if (arguments[i] instanceof Array)
					args.push($.wrapCallbacks(arguments[i]));
				else
					args.push(arguments[i]);
			}
			return function() {
				for ( var i = 0, l = args.length; i < l; i++)
					args[i].apply(this);
			};
		}
	});
})(jQuery);

(function($) {
	var fnc = function(options) {
		var default_settings = {
			loadBranches : false,
			persist : false,
			article : ""
		};
		function defined(a) {
			return typeof a !== "undefined";
		}
		options = $.extend(default_settings, options);
		var table = $(this);
		// table.addClass('treeTable');
		var persistStore;


		var storageString = simpleStorage.get("TreeTablePersistance-"+options.article);
		if (storageString != null)
			persistStore = $.parseJSON(storageString);

		if (persistStore == null) {
			persistStore = {
				paths : []
			}
		}

		var _needToExpand = [];
		var _needToCollapse = [];
		function setCollapseOrExpandNodeStateInStore(node, expanded) {

			var typeDepthArray = new Array();
			typeDepthArray = getTypeDepthTree(new Array(), node);

			if (!expanded) {
				for ( var i = 0; i < persistStore.paths.length; i++) {
					if (compareTypeDepthsArrays(persistStore.paths[i],
							typeDepthArray)) {
						persistStore.paths.splice(i, 1);
					}
				}
			} else {
				var found = false;
				for ( var i = 0; i < persistStore.paths.length; i++) {
					if (compareTypeDepthsArrays(persistStore.paths[i],
							typeDepthArray)) {
						found = true;
					}
				}
				if (!found) {
					persistStore.paths.push(typeDepthArray);
				}
				var stop;
			}
			var storageString = JSON.stringify(persistStore);
			simpleStorage.set("TreeTablePersistance-" + options.article, storageString);
		}

		//necessary because node ids change after a new build of the site
		function getTypeDepthTree(array, node) {

			var domNode = $("tr[data-tt-id=" + node.id + "]").find('td:first')
					.get(0);
			var clickedDomNodeParent = $(domNode).parent();

			var clickedDomNodeParentId;

			if (node.parentId == null) {
				clickedDomNodeParentId = $(domNode).attr('data-tt-parent-id');
			} else {
				clickedDomNodeParentId = (clickedDomNodeParent)
						.attr('data-tt-parent-id');
			}

			array.push(getDepthInSiblingsWithSameType(node, domNode,
					clickedDomNodeParent, clickedDomNodeParentId));

			if (node.parentId == null) {
				return array.reverse();
			} else {
				return getTypeDepthTree(array, node.parentNode());
			}

		}

		// get previous siblings with same AbstractType
		function getDepthInSiblingsWithSameType(node, domNode,
				clickedDomNodeParent, clickedDomNodeParentId) {
			var typeDepth = 0;
			var path = {};
			var clickedDomNodePrevSiblings = $(clickedDomNodeParent)
					.prevAll(
							"tr[data-tt-parent-id=" + clickedDomNodeParentId
									+ "]")
					.each(
							function(index, value) {
								var cell = $(value).find('td:first').get(0).textContent;

								if (cell.trim() === domNode.textContent.trim()) {
									typeDepth++;
								}
								//
							});
			path["abstractType"] = domNode.textContent.trim();
			path["depth"] = typeDepth;

			return path;
		}

		function compareTypeDepthsArrays(array1, array2) {
			// if the other array is a falsy value, return
			if (!array1)
				return false;

			// compare lengths - can save a lot of time
			if (array2.length != array1.length)
				return false;

			for ( var i = 0; i < array2.length; i++) {
				// Check if we have nested arrays
				if (array2[i] instanceof Array && array1[i] instanceof Array) {
					// recurse into the nested arrays
					if (!array2[i].compare(array1[i]))
						return false;
				} else if (array2[i].abstractType != array1[i].abstractType
						|| array2[i].depth != array1[i].depth) {
					// Warning - two different object instances will never be
					// equal: {x:20} != {x:20}
					return false;
				}
			}
			return true;
		}

		function updateNodeStates() {
			for ( var i = 0, len = _needToExpand.length; i < len; i++)
				table.treetable("expandNode", _needToExpand[i]);
			for ( var i = 0, len = _needToCollapse.length; i < len; i++)
				table.treetable("collapseNode", _needToCollapse[i]);
			_needToExpand.splice();
			_needToCollapse.splice();
		}
		function setCollapseOrExpandNodeStateInTreeTable(node) {

			var typeDepthArray = getTypeDepthTree(new Array(), node);
			var val = false;
			for ( var i = 0; i < persistStore.paths.length; i++) {
				if (compareTypeDepthsArrays(persistStore.paths[i],
						typeDepthArray)) {
					val = true;
				}
			}
			if (val && !node.expanded()) {
				if (typeof table.data("treetable") === "undefined")
					_needToExpand.push(node.id);

				else {
					node.initialized = true; // patch
					node.expand();
				}
			} else if (val === null && node.expanded()) {
				if (typeof table.data("treetable") === "undefined")
					_needToCollapse.push(node.id);
				else {
					node.initialized = true; // patch
					node.collapse();
				}
			}
		}
		function onNodeInitialized() {
			if (options.persist)
				setCollapseOrExpandNodeStateInTreeTable(this);
		}
		function onNodeCollapse() {
			var node = this;
			if (options.persist)
				setCollapseOrExpandNodeStateInStore(node, false);
			if (options.loadBranches)
				table.treetable("unloadBranch", node);
		}
		function onNodeExpand() {
			var node = this;
			if (options.persist)
				setCollapseOrExpandNodeStateInStore(node, true);
			// Render loader/spinner while loading
			if (options.loadBranches) {
				$.ajax({
					async : false, // Must be false, otherwise loadBranch
					// happens after showChildren?
					url : node.row.data('path')
				}).done(function(html) {
					var rows = $(html).find('tbody>tr');
					table.treetable("loadBranch", node, rows);
				});
			}
		}

		options.onNodeInitialized = $.wrapCallbacks(onNodeInitialized,
				options.onNodeInitialized);
		options.onNodeExpand = $.wrapCallbacks(onNodeExpand,
				options.onNodeExpand);
		options.onNodeCollapse = $.wrapCallbacks(onNodeCollapse,
				options.onNodeCollapse);
		options.onInitialized = $.wrapCallbacks(updateNodeStates,
				options.onInitialized);
		table.treetable($.extend({
			expandable: true
		}, options));
		return this;
	};

	$.fn.extend({
		agikiTreeTable : function(options) {
			return this.each(function() {
				return fnc.call(this, options);
			});
		}
	});
})(jQuery);
