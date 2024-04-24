function Flowchart(parent, id, width, height, idCounter) {
  this.parent = document.getElementById(parent);
  this.id = id || this.createID('sheet');
  this.width = width;
  this.height = height;
  this.idCounter = idCounter || 0;

  this.nodes = [];
  this.rules = [];
  this.dom = null;
  this.router = new Router(this);
  this.selection = [];
}

Flowchart.imagePath = "cc/image/";

/**
 * xml parsing with backward compatibility from older generated flowchart xml files
 * that are not really xml compatible
 */
Flowchart.parseXML = function(text) {
  try {
    var xml = jq$.parseXML(text);
    return xml;
  } catch (error) {
    xml = document.createElement('data');
    xml.innerHTML = text;
    return xml;
  }
};

Flowchart.loadFlowchart = function(kdomid, parent) {

  var params = {
    action: 'LoadFlowchartAction',
    SectionID: kdomid
  };

  // options for AJAX request
  options = {
    url: KNOWWE.core.util.getURL(params),
    response: {
      fn: function() {
        KNOWWE.core.util.updateProcessingState(-1);
        var xml = this.responseXML;
        // workaround if xml parser fails (backward compatibility)
        if (!xml || xml.getElementsByTagName("flowchart").length === 0) {
          xml = Flowchart.parseXML(this.responseText);
        }
        Flowchart.update(parent, kdomid, xml);
        KNOWWE.plugin.anchor.init();
      },
      onError: function() {
        //TODO handle error
        KNOWWE.core.util.updateProcessingState(-1);
      }
    }
  };

  // send AJAX request
  KNOWWE.core.util.updateProcessingState(1);
  new _KA(options).send();


};

Flowchart.update = function(parent, kdomid, xml) {
  KNOWWE.helper.observer.notify('beforeflowchartrendered');
  var flow = Flowchart.createFromXML(parent, xml);
  flow.kdomid = kdomid;
  KNOWWE.helper.observer.notify('flowchartrendered', {flow: flow});
  return flow;
};

Flowchart.prototype.createID = function(prefix) {
  this.idCounter++;
  var id = '#' + (prefix || "XX") + "_" + this.idCounter;
  while (this.findObject(id)) {
    this.idCounter++;
    id = '#' + (prefix || "XX") + "_" + this.idCounter;
  }
  return id;
};

Flowchart.prototype.intersectsNode = function(x1, y1, x2, y2) {
  var intersect = false;
  for (var i = 0; i < this.nodes.length; i++) {
    var node = this.nodes[i];
    if (node.intersects(x1, y1, x2, y2)) {
      return node;
    }
  }
  return null;
};

Flowchart.prototype.getIntersectNodes = function(x1, y1, x2, y2) {
  var nodes = [];
  for (var i = 0; i < this.nodes.length; i++) {
    var node = this.nodes[i];
    if (node.intersects(x1, y1, x2, y2)) {
      nodes.push(node);
    }
  }
  return nodes;
};

Flowchart.prototype.setSize = function(width, height, exactSize) {
  this.width = width;
  this.height = height;
  if (this.dom) {
    var w, h;
    if (exactSize) {
      w = width + 1;
      h = height;
    } else {
      w = Math.ceil(width / 10.0) * 10 + 1;
      h = Math.ceil(height / 10.0) * 10;
    }
    var div = jq$('.Flowchart')[0];
    div.style.width = w + 'px';
    div.style.height = h + 'px';
  }
};

Flowchart.prototype.getContentPane = function() {
  return this.dom.firstChild;
};

Flowchart.prototype.isVisible = function() {
  return (this.dom != null);
};

Flowchart.prototype.focus = function() {
};

Flowchart.prototype.setVisible = function(visible) {
  if (!this.isVisible() && visible) {
    this.router.withDelayedReroute(function() {
      // ==> show Node
      this.dom = this.render();
      this.parent.querySelectorAll(".loadingSpinner").forEach(e => e.remove());
      this.parent.appendChild(this.dom);
      // before showing childs, parent must be visible to enable dragging library
      for (var i = 0; i < this.nodes.length; i++) this.nodes[i].setVisible(visible);
      for (var i = 0; i < this.rules.length; i++) this.rules[i].setVisible(visible);
    }.bind(this));
  } else if (this.isVisible() && !visible) {
    // ==> hide Node
    this.parent.removeChild(this.dom);
    for (var i = 0; i < this.nodes.length; i++) this.nodes[i].setVisible(visible);
    for (var i = 0; i < this.rules.length; i++) this.rules[i].setVisible(visible);
    this.dom = null;
  }
};


/**
 * Gets left cumulative offset of flowchart content pane.
 * @return left offset of flowchart
 */
Flowchart.prototype.getLeft = function() {
  return jq$('#' + this.id)[0].cumulativeOffset().left;
};

/**
 * Gets top cumulative offset of flowchart content pane.
 * @return top offset of flowchart
 */
Flowchart.prototype.getTop = function() {
  return jq$('#' + this.id)[0].cumulativeOffset().top;
};

Flowchart.prototype.addNode = function(node) {
  this.nodes.push(node);

  // workaround: immediately increase flow size to avoid layout bug of node dom
  if (this.dom) {
    var div = this.dom.select('.Flowchart')[0];
    this.width = Math.max(this.width, node.getLeft() + 500);
    this.height = Math.max(this.height, node.getTop() + 500);
    div.style.width = this.width + 'px';
    div.style.height = this.height + 'px';
  }
};

Flowchart.prototype.removeNode = function(node) {
  this.nodes.remove(node);
};

Flowchart.prototype.findNode = function(id) {
  for (var i = 0; i < this.nodes.length; i++) {
    var node = this.nodes[i];
    if (node.getNodeModel().fcid == id) return node;
  }
  return null;
};

Flowchart.prototype.findRule = function(id) {
  for (var i = 0; i < this.rules.length; i++) {
    var rule = this.rules[i];
    if (rule.fcid == id) return rule;
  }
  return null;
};


/**
 * Flowchart.isSelected
 * returns if the specified object is currently selected.
 *
 * @param {Node | Rule} nodeOrRule
 */
Flowchart.prototype.isSelected = function(nodeOrRule) {
  return this.selection.contains(nodeOrRule);
};

/**
 * Flowchart.getSelectedNodes
 * returns the currently selected nodes as an array.
 */
Flowchart.prototype.getSelectedNodes = function() {
  return jq$.grep(this.selection, function(item) {
    return item instanceof Node;
  });
};

/**
 * Flowchart.getSelectedRules
 * returns the currently selected rules as an array.
 */
Flowchart.prototype.getSelectedRules = function() {
  return jq$.grep(this.selection, function(item) {
    return item instanceof Rule;
  });
};

/**
 * Flowchart.setSelection
 * Sets the selection to the specified Node(s) or Rule(s).
 * This method signals the selected elements to be highlighted.
 *
 * @param {Node | Rule | [Node, ..., Rule, ...]} nodeOrRuleOrArray
 * @param {boolean} addToSelection (default: false)
 */
Flowchart.prototype.setSelection = function(nodeOrRuleOrArray, addToSelection, removeFromSelection) {
  // request Focus
  this.focus();
  // create new and defined selection array
  // for the items to be selected
  var newSelection;

  // array is null, if click on no object
  if (nodeOrRuleOrArray == null) {
    if (addToSelection) {
      //if ctrl was pressed, preserve the selection
      return;
    }
    newSelection = [];
  } else if (DiaFluxUtils.isArray(nodeOrRuleOrArray)) {
    newSelection = nodeOrRuleOrArray;
  } else {
    newSelection = [nodeOrRuleOrArray];
  }

  if (removeFromSelection) {
    // deselect some nodes
    for (var i = 0; i < newSelection.length; i++) {
      this.selection.remove(newSelection[i]);
      newSelection[i].setSelectionVisible(false);
    }
  } else {
    // deselect existing selection if a 'total' set action is desired
    if (!addToSelection) {
      for (var i = 0; i < this.selection.length; i++) {
        this.selection[i].setSelectionVisible(false);
      }
      this.selection = [];
    }

    // otherwise (add or set) select some nodes
    for (var i = 0; i < newSelection.length; i++) {
      this.selection.push(newSelection[i]);
      newSelection[i].setSelectionVisible(true);
    }
  }
};

Flowchart.prototype.findObject = function(id) {
  return this.findNode(id) || this.findRule(id);
};

Flowchart.prototype.addRule = function(rule) {
  this.rules.push(rule);
  if (this.isVisible()) {
    this.router.rerouteNodes([rule.sourceNode, rule.targetNode]);
  }
};

Flowchart.prototype.removeRule = function(rule) {
  this.rules.remove(rule);
  if (this.isVisible()) {
    this.router.rerouteNodes([rule.sourceNode, rule.targetNode]);
  }
};


Flowchart.prototype.render = function() {
  var w = Math.ceil(this.width / 10.0) * 10 + 1;
  var h = Math.ceil(this.height / 10.0) * 10 + 1;

  var contentPane;
  var dom = Builder.node('div', {
      id: this.id,
      className: 'FlowchartGroup'
    },
    [
      contentPane = Builder.node('div', {
        className: 'Flowchart',
        style: "width: " + w + "px; height:" + h + "px;"
      })
    ]);
  dom.__flowchart = this;

  this.createDroppables();

  return dom;
};

// implemented in floweditor.js
Flowchart.prototype.createDroppables = function() {
};


Flowchart.prototype.getContentSize = function() {
  var maxX = this.width;
  var maxY = this.height;
  for (var i = 0; i < this.nodes.length; i++) {
    maxX = Math.max(maxX, this.nodes[i].getLeft() + this.nodes[i].getWidth());
    maxY = Math.max(maxY, this.nodes[i].getTop() + this.nodes[i].getHeight());
  }
  return [maxX, maxY];
};

Flowchart.prototype.findRulesForNode = function(node) {
  // TODO: shall be optimized by build an hashtable for each node!!!
  var result = [];
  for (var i = 0; i < this.rules.length; i++) {
    var rule = this.rules[i];
    if (rule.sourceNode == node || rule.targetNode == node) {
      result.push(rule);
    }
  }
  return result;
};


Flowchart.prototype.addFromXML = function(xmlDom, dx, dy) {
  this.router.withDelayedReroute(function() {
    this._addFromXML(xmlDom, dx, dy);
  }.bind(this));
};

Flowchart.prototype._addFromXML = function(xmlDom, dx, dy) {
  var pasteOptions = {
    flowchart: this,
    idMap: {},
    allIDs: [],
    translate: {left: dx, top: dy}
  };
  pasteOptions.createID = function(id) {
    if (this.flowchart.findObject(id)) {
      var newID = this.flowchart.createID();
      this.idMap[id] = newID;
      this.allIDs.push(newID);
      return newID;
    } else {
      this.allIDs.push(id);
      return id;
    }
  }.bind(pasteOptions);
  pasteOptions.getID = function(id) {
    if (this.idMap[id]) id = this.idMap[id];
    return id;
  }.bind(pasteOptions);

  try {
    if (typeof FlowEditor !== 'undefined') FlowEditor.avoidAutoResize = true;

    // nodes
    var nodeDoms = xmlDom.getElementsByTagName('node');
    for (var i = 0; i < nodeDoms.length; i++) {
      Node.createFromXML(this, nodeDoms[i], pasteOptions);
    }

    // rules
    var ruleDoms = xmlDom.getElementsByTagName('edge');
    for (var i = 0; i < ruleDoms.length; i++) {
      Rule.createFromXML(this, ruleDoms[i], pasteOptions);
    }

    // select paste objects
    var sel = [];
    for (var i = 0; i < pasteOptions.allIDs.length; i++) {
      var item = this.findObject(pasteOptions.allIDs[i]);
      if (item.isVisible()) {
        sel.push(item);
      }
    }
  } finally {
    if (typeof FlowEditor !== 'undefined') FlowEditor.avoidAutoResize = false;
  }

  this.setSelection(jq$.grep(sel, function(item) {
    return (item instanceof Node);
  }), false, false);
  return pasteOptions;
};

Flowchart.createFromXML = function(parent, xmlDom) {
  if (xmlDom.nodeName.toLowerCase() != 'flowchart') {
    return Flowchart.createFromXML(
      parent,
      xmlDom.getElementsByTagName('flowchart')[0]);
  }

  // direkt attributes
  var id = xmlDom.getAttribute('fcid');
  var width = parseInt(xmlDom.getAttribute('width')) || 650;
  var height = parseInt(xmlDom.getAttribute('height')) || 400;
  var name = xmlDom.getAttribute('name');
  var icon = xmlDom.getAttribute('icon');
  var idCounter = xmlDom.getAttribute('idCounter');
  var autostart = xmlDom.getAttribute('autostart');

  // create flowchart
  var flowchart = new Flowchart(parent, id, width, height, idCounter);
  flowchart.name = name;
  flowchart.icon = icon;

  if (autostart) {
    flowchart.autostart = (autostart == "true")
  } else {
    flowchart.autostart = false;
  }

  flowchart.router.withDelayedReroute(function() {
    flowchart.addFromXML(xmlDom, 0, 0);
    if (parent) flowchart.setVisible(true);
  });
  return flowchart;
};

Flowchart.prototype.getUsedArea = function() {
  var maxX = 0;
  var maxY = 0;
  var minX = this.width - 1;
  var minY = this.height - 1;

  for (var i = 0; i < this.nodes.length; i++) {
    minX = Math.min(minX, this.nodes[i].getLeft());
    minY = Math.min(minY, this.nodes[i].getTop());
    maxX = Math.max(maxX, this.nodes[i].getLeft() + this.nodes[i].getWidth());
    maxY = Math.max(maxY, this.nodes[i].getTop() + this.nodes[i].getHeight());
  }
  for (var i = 0; i < this.rules.length; i++) {
    var coords = this.rules[i].coordinates;
    for (var c = 0; c < coords.length; c++) {
      minX = Math.min(minX, coords[c][0]);
      minY = Math.min(minY, coords[c][1]);
      maxX = Math.max(maxX, coords[c][0]);
      maxY = Math.max(maxY, coords[c][1]);
    }
  }
  return {
    top: Math.min(minY, maxY),
    bottom: Math.max(minY, maxY),
    left: Math.min(minX, maxX),
    right: Math.max(minX, maxX),
    width: Math.abs(maxX - minX),
    height: Math.abs(maxY - minY)
  };
};


Flowchart.get = function(sectionId) {
  return jq$('#' + sectionId).find('.Flowchart');
};

Flowchart.getScale = function(sectionId) {
  return jq$('#' + sectionId).find('.Flowchart').scale();
};

Flowchart.zoom = function(sectionId, diff) {
  var scale = Flowchart.getScale(sectionId) + diff;
  if (scale > 1) return;
  var flowchart = Flowchart.get(sectionId);
  var width = parseInt(flowchart.css('width'));
  var height = parseInt(flowchart.css('height'));
  var marginLR = -(width - width * scale) / 2;
  var marginTB = -(height - height * scale) / 2;
  var pageWidth = parseInt(jq$('#pagecontent').css('width'));
  if (scale < 1 && pageWidth - (width * scale) > 0.001) {
    if (diff < 0) Flowchart.zoomToFit(sectionId);
    return;
  }
  if (scale < 0.4) {
    Flowchart.get(sectionId).css('background-image', "none");
  } else {
    //Flowchart.get(sectionId).css('background-image', 'url(cc/image/grid_10.png)');
  }
  flowchart.scale(scale);
  flowchart.css('margin', marginTB + "px " + marginLR + "px " + marginTB + "px " + marginLR + "px");
  var index = Flowchart.flowsToFit.indexOf(sectionId);
  if (index > -1) {
    Flowchart.flowsToFit.splice(index, 1);
  }
  Flowchart.handleButtonVisibility(sectionId);
};

Flowchart.zoom100 = function(sectionId) {
  Flowchart.zoom(sectionId, 1 - Flowchart.getScale(sectionId));
};

Flowchart.getZoomToFitDiff = function(sectionId, scale) {
  var width = parseInt(Flowchart.get(sectionId).css('width'));
  var pageWidth = parseInt(jq$('#pagecontent').css('width'));
  var diff = pageWidth / width - scale;
  if (scale + diff > 1) diff = 1 - scale;
  return diff;
};

Flowchart.zoomToFit = function(sectionId) {
  var scale = Flowchart.getScale(sectionId);
  var diff = Flowchart.getZoomToFitDiff(sectionId, scale);
  Flowchart.zoom(sectionId, diff);
  if (scale + diff < 1) {
    Flowchart.flowsToFit.push(sectionId);
  }
};

Flowchart.handleButtonVisibility = function(sectionId) {
  var scale = Flowchart.getScale(sectionId);
  var diff = Flowchart.getZoomToFitDiff(sectionId, scale);
  var items = jq$('#' + sectionId).find('.headerMenu .markupMenuInlineItem');
  items.each(function() {
    $this = jq$(this);
    var text = $this.text();
    if (scale > 0.9 && text === "+"
      || diff >= 0 && (text === "Fit" || text === "-")
      || scale === 1 && text === "100%") {
      $this.css('opacity', '0.5');
    } else {
      $this.css('opacity', '1');
    }
  });
};

Flowchart.flowsToFit = [];

jq$(window).resize(function() {
  jq$.waitForFinalEvent(function() {
    jq$.each(Flowchart.flowsToFit, function(key, sectionId) {
      Flowchart.zoomToFit(sectionId);
    });
  }, 300, "Flowchart ZoomToFitResizing");
});

if (typeof KNOWWE != "undefined") {
  KNOWWE.helper.observer.subscribe("flowchartrendered", function() {
    Flowchart.handleButtonVisibility(jq$(this.flow.dom).parents('.type_DiaFlux').attr('id'));
  });
}

if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
  jq$(document).ready(function(){
    jq$('.flowchartContainer').each(function() {
      let $container = jq$(this);
      Flowchart.loadFlowchart($container.attr("sectionid"), $container.attr("id"));
    })
  });
}


