function drawTree(size, w, h, jsonsource, sectionID) {
	
	// define layout 
	
	var margin = {top: 20, right: 120, bottom: 20, left: 20};
    var fontsize = 10;
	var i = 0,
    	duration = 750,
    	root;
	
	 root = jsonsource;
	 root.x0 = height / 2;
	 root.y0 = 10;

	 // compute left margin based on rootlabel length 
	 margin.left = margin.left + root.concept.length * 5;
	
	 var width = 960 - margin.right - margin.left,
    	height = 800 - margin.top - margin.bottom;
	 
	 var depth;

	if (w != null || h != null) {
		if (w != null) {
			width = w - margin.right - margin.left;
		}
		if (h != null) {
			height = h - margin.top - margin.bottom;
		}
	} else if(size != null) {
		width = size - margin.right - margin.left;
	}
	
	 
	var tree = d3.layout.tree()
    	.size([height, width]);

	var diagonal = d3.svg.diagonal()
    	.projection(function(d) { return [d.y, d.x]; });

	var div = d3.select("#d3" + sectionID);
	

	
	var svg = div.append("svg")
				.attr("id", "svg"+sectionID)
				.attr("width", width + margin.right + margin.left)
				.attr("height", height + margin.top + margin.bottom)
				.append("g")
				.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
	
	// create buttons
	
	
	var expandButton = div.append("button")
			.attr("id", "expandButton"+sectionID)
			.attr("class", "d3treeButton")
			.attr("value", "Expand all")
			.on("click", function (d) {
				if(root.children) {
				click(root);
				}
				expandAllChildren(root);
											 
			})
			.append("img")
			.attr("src", "KnowWEExtension/images/treevis_icon_expandall.gif");
	
	var increaseTreeSizeButton = div.append("button")
			.attr("id", "IncreaseButton"+sectionID)
			.attr("class", "d3treeButton")
			.attr("value", "Increase tree size")
			.on("click", function (d){
				
				tree.size([tree.size()[0]*1.5, tree.size()[1]*1.5]);
				update(root);
				updateLabels(root);
			})
			.append("img")
			.attr("src", "KnowWEExtension/images/treevis_icon_increase.gif");
			
	var decreaseTreeSizeButton = div.append("button")
			.attr("id", "DecreaseButton"+sectionID)
			.attr("class", "d3treeButton")
			.attr("value", "Decrease tree size")
			.on("click", function (d){
				tree.size([tree.size()[0]/1.5, tree.size()[1]/1.5]);
				update(root);
				updateLabels(root);
			})
			.append("img")
			.attr("src", "KnowWEExtension/images/treevis_icon_decrease.gif");

	
	var increaseLabelsButton = div.append("button")
			.attr("id", "zoomInButton"+sectionID)
			.attr("class", "d3treeButton")
			.attr("value", "Increase label size")
			.on("click", function (d){
				
				
				fontsize = fontsize*1.1;
				svg.selectAll("text")
				.style("font-size", fontsize+"px");
				
			})
			.append("img")
			.attr("src", "KnowWEExtension/images/treevis_icon_zoomin.gif");

			
	var decreaseLabelsButton = div.append("button")
			.attr("type" , "button")
			.attr("id", "zoomOutButton"+sectionID)
			.attr("class", "d3treeButton")
			.attr("value", "Decrease label size")
			.on("click", function (d){
				
				fontsize = fontsize/1.1;
				svg.selectAll("text")
					.style("font-size", fontsize+"px");
				
			})
			.append("img")
			.attr("src", "KnowWEExtension/images/treevis_icon_zoomout.gif");
			
				// initialize zoom behaviour
	
	 d3.select("#svg"+sectionID)
     .call(d3.behavior.zoom()
     .scaleExtent([0.5, 5])
     .on("zoom", zoom))
	 .on("mouseclick.zoom", null);
	
	
	// compute new layout                                    
	 function update(source) {

		 
		 
		 
		  // Compute the new tree layout.
		  var nodes = tree.nodes(root).reverse(),
		      links = tree.links(nodes);

		  var sourcenode;
			 

		  // Update the nodes…
		  var node = svg.selectAll("g.node")
		      .data(nodes, function(d) { return d.id || (d.id = ++i); });

		  // Enter any new nodes at the parent's previous position.
		  var nodeEnter = node.enter().append("g")
		      .attr("class", "node")
		      .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
		      .attr("concept", function(d) {return d.concept})
		      .attr("expanded", "false")
		      .on("click", click);
		 
		
		nodeEnter.append("circle")
		      .attr("r", 1e-6);;
		      

		  // Adding the links 
		  var $a = nodeEnter.append("svg:a")
		  .attr("xlink:href", function(d) { return decodeURIComponent(d.conceptUrl);});
		  	
		  		  
		  // Creating Node text labels
		  $a.append("text")
		  	.attr("class", "nodetext")
		  	.attr("x", function(d) { return d.children || d._children ? 10 : 10; })
		  	.attr("dy", ".35em")
		  	.text(function(d) { return d.concept; })
		  	.style("pointer-events", "auto")
		  	.style("font-size", fontsize+"px");

		
		 // update position of all text labels
		
		updateLabels(source);
		

		// compute depth and normalize nodes 
			
		for(var k = 0; k<nodes.length;k++) {
				 if(nodes[k].concept==source.concept) {
					 sourcenode = nodes[k];
					 break;
				 	}
			}
			
		depth = computeDepth(sourcenode);
		
		
		nodes.forEach(function(d) { d.y = d.depth * depth; });
		
		
		
		  // Transition nodes to their new position.
		  var nodeUpdate = node.transition()
		      .duration(duration)
		      .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

		  nodeUpdate.select("circle")
	      		.attr("r", 4.5);;
	      		

		  nodeUpdate.select("text")
		      .style("fill-opacity", 1);

		  // Transition exiting nodes to the parent's new position.
		  var nodeExit = node.exit().transition()
		      .duration(duration)
		      .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
		      .remove();

		  nodeExit.select("circle")
		      .attr("r", 1e-6);

		  nodeExit.select("text")
		      .style("fill-opacity", 1e-6);

		  // Update the links…
		  var link = svg.selectAll("path.link")
		      .data(links, function(d) { return d.target.id; });

		  // Enter any new links at the parent's previous position.
		  link.enter().insert("path", "g")
		      .attr("class", "link")
		      .attr("d", function(d) {
		        var o = {x: source.x0, y: source.y0};
		        return diagonal({source: o, target: o});
		      });

		  // Transition links to their new position.
		  link.transition()
		      .duration(duration)
		      .attr("d", diagonal);

		  // Transition exiting nodes to the parent's new position.
		  link.exit().transition()
		      .duration(duration)
		      .attr("d", function(d) {
		        var o = {x: source.x, y: source.y};
		        return diagonal({source: o, target: o});
		      })
		      .remove();

		  
		  // Stash the old positions for transition.
		  nodes.forEach(function(d) {
		    d.x0 = d.x;
		    d.y0 = d.y;
		  });
		
			 svg.selectAll("g.node")
			  	.attr("class", function(d) { return d._children? "node expandable" : "node"});
			
				  
			 
		  
		}
	 
	// Toggle children on click.
	 function click(d) {
		  if (d.children) {
		    collapseAllChildren(d);
		  } else {
		    d.children = d._children;
		    d._children = null;
		  }
		  
		  update(d);
		  
		}

	 
	 function updateLabels(node) {
		 
		 var rootnode = jq$("g.node[concept='" + node.concept + "']")[0];
			var rootIsExpanded = rootnode.getAttribute("expanded");
			
			  
			  // update position of all text labels
			
			  var text = jq$(rootnode).find("text")[0];
				 
				 if(rootIsExpanded=="true") {
					 rootnode.setAttribute("expanded", "false");
					 text.setAttribute("x", "10");
					 text.setAttribute("text-anchor", "start");
				 } else if(rootIsExpanded=="false"){
					 rootnode.setAttribute("expanded", "true");
					 text.setAttribute("x", "-10");
					 text.setAttribute("text-anchor", "end");
				}
	 }
	// expand node and all of its children 
	 function expandAllChildren(node) {
			
			
			if(node._children) {
				click(node);
				node.children.forEach(expandAllChildren);
			} else if(node.children){
				node.children.forEach(expandAllChildren);
			}
			
}
	 
	 // collapse node and all of its children
	 function collapseAllChildren(node) {
		 if(node.children){
			 node.children.forEach(collapseAllChildren);
			 node._children = node.children;
			 node.children = null;
		 }
	 }
	 
// compute the necessary space between current and successor layer of the tree
	 function computeDepth(node) {
		 
		 if(node.depth==0) {
			 
			 return 200;
		 }
		 var sibblingNodes = [];
		 var parentNodes = [];
		 var nodes = tree.nodes(root);
		 var depth = 0;
		 nodes.forEach(function(d) {
			 if(d.depth>depth) {
				 depth = d.depth;
			 }
		 });;
		
		 
		 var requiredSpace = 0;
		 
		 for(var i = 1; i<= depth; i++) {
			 
			 sibblingNodes = [];
			 parentNodes = [];
			  nodes.forEach(function(d) {
				
				if(d.depth==depth-i+1) {
					 sibblingNodes.push(d);
				 } else if(d.depth==depth-i) {
					 parentNodes.push(d);
				 }
			  });;
			 
			 var requiredSpaceLeft = getMaximalExpandedLabelLength(sibblingNodes);
			 requiredSpaceLeft+= getMaximalCollapsedLabelLength(parentNodes);
			 if(requiredSpaceLeft>requiredSpace) {
				 requiredSpace = requiredSpaceLeft;
			 }
			  
		 }
		 
		if(requiredSpace<150) {
			return 150;
		} else return requiredSpace;
	 }
	 
	 
	 // compute the maximal length of all collapsed labels in the curren tree layer 
	 function getMaximalCollapsedLabelLength(node) {
		 var maxLength = 0;
		 for(var k = 0; k<node.length;k++) {
			 var concept = node[k].concept;
			 var gNode = jq$("g[concept='"+node[k].concept+"']")[0];
			 if(gNode.getAttribute("expanded")=="false") {
			 var text = jq$(gNode).find("text")[0];
			 var length = text.getComputedTextLength();
			 	if(length>maxLength) {
			 		maxLength=length;
			 	}
			 }
		 }
		 
		 // add distance between node and label
		 maxLength+= 15;
		 return maxLength;
	 }
	 
// compute the maximal length of all expanded labels in the curren tree layer 
	 function getMaximalExpandedLabelLength(node) {
		 var maxLength = 0;
		 for(var k = 0; k<node.length;k++) {
			 var concept = node[k].concept;
			 var gNode = jq$("g[concept='"+node[k].concept+"']")[0];
			 if(gNode.getAttribute("expanded")=="true") {  
				 var text = jq$(gNode).find("text")[0];
				 var length = text.getComputedTextLength();
				 if(length>maxLength) {
					 maxLength=length;
				 }	
			 }
		}
		 
		 // add distance between node and label
		 maxLength+= 15;
		 return maxLength;
	 }
	 
	 // implement the zoom functionality 
	 function zoom() {
		 
		 var scale = d3.event.scale,
		        translation = d3.event.translate,
		        tbound = -height * scale,
		        bbound = height * scale,
		        lbound = (-width + margin.right) * scale,
		        rbound = (width - margin.left) * scale;
		    // limit translation to thresholds
		    translation = [
		        Math.max(Math.min(translation[0], rbound), lbound),
		        Math.max(Math.min(translation[1], bbound), tbound)
		    ];
		    
		   
		    svg.attr("transform", "translate(" + translation + ")" + " scale(" + scale + ")");
		    
		}
	

	
	
	 root.children.forEach(collapseAllChildren);
	 update(root);
	
	d3.select(self.frameElement).style("height", "800px");
	

		
	
}