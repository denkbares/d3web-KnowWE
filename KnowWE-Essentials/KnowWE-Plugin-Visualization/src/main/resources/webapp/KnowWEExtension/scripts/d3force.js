function drawForce(size, w, h, array, linksList, baseurl, title, sectionID) {
	url = baseurl + "?page=" + title + "&concept=";
	
	var links = array;
	
	var nodes = {};

	// Compute the distinct nodes from the links.
	links.forEach(function(link) {
	  link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
	  link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
	});

	var width = 650;
	var height = width;

	if (w != null || h != null) {
		if (w != null) {
			width = w;
		}
		if (h != null) {
			height = h;
		}
	} else if (size != null) {
		width = size;
	}
	
	var force = d3.layout.force()
    				.nodes(d3.values(nodes))
    				.links(links)
    				.size([width, height])
    				.linkDistance(200)
    				.charge(-300) 
    				.gravity(0.05)
    				.on("tick", tick)
    				.start();
	
	var div = d3.select("#d3" + sectionID);
	
	var svg = div.append("svg:svg")
	    			.attr("width", width)
	    			.attr("height", height);
	
	var marker = svg.append("svg:defs").selectAll("marker")
	    		.data(linksList)
	    	.enter().append("svg:marker")
	    		.attr("id", String)
			    .attr("viewBox", "0 -5 10 10")
			    .attr("refX", 15)
			    .attr("refY", -1.5)
			    .attr("markerWidth", 10)
			    .attr("markerHeight", 10)
			    .attr("orient", "auto")
			.append("svg:path")
				 .attr("d", "M0,-5L10,0L0,5");
	
	var path = svg.append("svg:g").selectAll("path")
    			.data(force.links())
    		.enter().append("svg:path")
    			.attr("class", function(d) { return "link " + d.type; })
    			.attr("marker-end", function(d) { return "url(#" + d.type + ")"; })
    			.attr("id", function(d,i) { return sectionID + "path" + i; });

	var circle = svg.append("svg:g").selectAll("a")
				.data(force.nodes())
				.enter().append("svg:a")
					.attr("xlink:href", function(d) { return url + d.name })
					.attr("target", "_top")
				.append("svg:circle")
					.attr("r", 10)
					.call(force.drag);

	var nodelabel = svg.append("svg:g").selectAll("g")
    			.data(force.nodes())
    		.enter().append("svg:g");
	
		// A copy of the text with a thick white stroke for legibility.
		nodelabel.append("svg:text")
			.attr("x", 8)
			.attr("y", ".31em")
			.attr("class", "shadow")
			.text(function(d) { return d.name; });
		
		nodelabel.append("svg:text")
			.attr("x", 8)
			.attr("y", ".31em")
			.text(function(d) { return d.name; });
		
	var edgelabel = svg.append("svg:g").selectAll(".edgelabel")
			.data(force.links())
		.enter().append("svg:text")
			.style("pointer-events", "none")
			.attr({'class':'edgelabel',
				'id':function(d){ return d.type; },
//				'dx':80,
				'dy':-5,
				'font-size':10,
				'fill':'#aaa'});
	
		edgelabel.append("svg:textPath")
      		.attr('xlink:href', function(d,i) {return "#" + sectionID + "path" + i})
      		.attr("startOffset", "30%")
      		.style("pointer-events", "none")
      		.text(function(d){ return d.type; });
	
	// Use elliptical arc path segments to doubly-encode directionality.
	function tick() {
		path.attr("d", function(d) {
			var dx = d.target.x - d.source.x,
	        	dy = d.target.y - d.source.y,
	        	dr = Math.sqrt(dx * dx + dy * dy);
			return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
		});

		circle.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});

		nodelabel.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});
	}

}

