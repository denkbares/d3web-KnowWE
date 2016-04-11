function drawWheel(size, w, h, json, sectionID) {
	
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
		height = size;
	}

	var radius = width / 2,
	    padding = 5,
	    duration = 1000, 
	    x = d3.scale.linear().range([0, 2 * Math.PI]),
	    y = d3.scale.pow().domain([0, 1]).range([0, radius]),
		color = d3.scale.category20c();
	
	var div = d3.select("#d3" + sectionID);
	
	var svg = div.append("svg")
		.attr("id", "svg"+sectionID)
	    .attr("width", width + padding * 2)
	    .attr("height", height + padding * 2) 
	  .append("g")
	    .attr("transform", "translate(" + [radius + padding, radius + padding] + ")");
	
	var partition = d3.layout.partition()
		.sort(null)
	    .value(function(d) { return 7 });
	
	var arc = d3.svg.arc()
	    .startAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x))); })
	    .endAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx))); })
	    .innerRadius(function(d) { return Math.max(0, y(d.y)); })
	    .outerRadius(function(d) { return Math.max(0, y(d.y + d.dy)); });

	var nodes = partition.nodes(json);
	  
	  // graph
	  var path = svg.selectAll("path")
	      .data(nodes)
	    .enter().append("path")
	      .attr("id", function(d, i) { return "path-" + i; })
	      .attr("d", arc)
	      .attr("fill-rule", "evenodd")
	      //.style("fill", function(d) { return color((d.children ? d : d.parent).name); }) 
	      .style("fill", colour)
	      .on("click", click);
	  
	  // text	  
	  var text = svg.selectAll("text").data(nodes);
	  var textEnter = text.enter().append("text")
	  		.style("fill-opacity", 1)
	  		.style("fill", function(d) {
	  			return brightness(d3.rgb(colour(d))) < 125 ? "#eee" : "#000";
	  		})
	  		.attr("text-anchor", function(d) {
	  			if (d.depth > 0) {
	  				return x(d.x + d.dx / 2) > Math.PI ? "end" : "start";
	  			} return "middle";
	  		})
	  		.attr("dy", ".2em")
	  		.attr("transform", function(d) {
	  			if (d.depth > 0) {
	  				var textpathRadius = Math.max(0, y(d.y)) / 3.5;
	  				var multiline = (d.concept.length > 7),
  						angle = x(d.x + d.dx / 2) * 180 / Math.PI - 90,
  						rotate = angle + (multiline ? -.5 : 0);
	  				return "rotate(" + rotate + ")translate(" + (y(d.y) + padding) + ")rotate(" + (angle > 90 ? -180 : 0) + ")";
	  			}
	  		})
	  		.on("click", click);
	  
	  addText(textEnter);
	  
	  // click
	  function click(d) {
	    path.transition()
	      .duration(750)
	      .attrTween("d", arcTween(d));
	    
	    // Somewhat of a hack as we rely on arcTween updating the scales.
	    text.style("visibility", function(e) {
	    		return isParentOf(d, e) ? null : d3.select(this).style("visibility");
	    	})
	    	.transition()
	    	.duration(duration)
	    	.attrTween("text-anchor", function(d) {
	    		return function() {
	    			if (d.depth > 0) {
		  				return x(d.x + d.dx / 2) > Math.PI ? "end" : "start";
		  			} return "middle";
	    		};
	    	})
	    	.attrTween("transform", function(d) {
	    		var multiline = (d.concept || "").split(" ").length > 1;
	    		return function() {
	    			if (d.depth > 0) {
		  				var multiline = (d.concept.length > 7),
	  						angle = x(d.x + d.dx / 2) * 180 / Math.PI - 90,
	  						rotate = angle + (multiline ? -.5 : 0);
		  				return "rotate(" + rotate + ")translate(" + (y(d.y) + padding) + ")rotate(" + (angle > 90 ? -180 : 0) + ")";
		  			}
	    		};
	    	})
	    	.style("fill-opacity", function(e) { return isParentOf(d, e) ? 1 : 1e-6; })
	    	.each("end", function(e) {
	    		d3.select(this).style("visibility", isParentOf(d, e) ? null : "hidden");
	    	});
	   }
	
	// Interpolate the scales!
	function arcTween(d) {
	  var xd = d3.interpolate(x.domain(), [d.x, d.x + d.dx]),
	      yd = d3.interpolate(y.domain(), [d.y, 1]),
	      yr = d3.interpolate(y.range(), [d.y ? 20 : 0, radius]);
	  return function(d, i) {
	    return i
	        ? function(t) { return arc(d); }
	        : function(t) { x.domain(xd(t)); y.domain(yd(t)).range(yr(t)); return arc(d); };
	  };
	}
	
	function isParentOf(p, c) {
		if (p === c) return true;
		if (p.children) {
			return p.children.some(function(d) {
					return isParentOf(d, c);
			});
		}
		return false;
	} 
	
	function colour(d) {
		if (d.children) {
			// There is a maximum of two children!
			var colours = d.children.map(colour),
			a = d3.hsl(colours[0]),
			b = d3.hsl(colours[1]);
			// L*a*b* might be better here...
			return d3.hsl((a.h + b.h) / 2, a.s * 1.2, a.l / 1.2);
		}
		// ffe4c4
		return "#0F8B0A";
	} 
	
	function maxY(d) {
		return d.children ? Math.max.apply(Math, d.children.map(maxY)) : d.y + d.dy;
	} 
	
	function brightness(rgb) {
		return rgb.r * .299 + rgb.g * .587 + rgb.b * .114;
	} 
	
	function addText(textEnter) {
		textEnter.append("tspan")
	  		.attr("x", 0)
	  		.attr("style", function(d) { 
	  			if (d.depth < 1) {
	  				return "font-weight:bold";
	  			} 
	  		})
	  		.text(function(d) { 
	  			if (d.depth > 0) {
	  				return d.concept.substr(0, 8); 
	  			} return d.concept.substr(0,12);
	  		});
	  
	  textEnter.append("tspan")
	  		.attr("x", 0)
	  		.attr("dy", "1em")
	  		.attr("style", function(d) { 
	  			if (d.depth < 1) {
	  				return "font-weight:bold";
	  			} 
	  		})
			.text(function(d) { 
				if (d.depth > 0) {
					return  d.concept.substr(8, 8); 
				} return d.concept.substr(12, 24);
			});
	  
	  textEnter.append("tspan")
		.attr("x", 0)
		.attr("dy", "1em")
		.attr("style", function(d) { 
	  		if (d.depth < 1) {
	  			return "font-weight:bold";
	  		} 
	  	})
		.text(function(d) { 
			if (d.depth > 0) {
				return d.concept.substr(16, 8); 
			} return d.concept.substr(24, d.concept.length);
		});
	  
	  textEnter.append("tspan")
		.attr("x", 0)
		.attr("dy", "1em")
		.text(function(d) { 
			if (d.depth > 0) {
				return d.concept.substr(24, d.concept.length - 18); 
			}
		});
	}

}