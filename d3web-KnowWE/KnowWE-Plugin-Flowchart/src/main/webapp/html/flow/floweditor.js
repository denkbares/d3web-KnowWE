var FlowEditor = {

	Version: '0.0.3',
	REQUIRED_SCRIPTACULOUS: '1.8.2',
	REQUIRED_PROTOTYPE: '1.6.0.3',
	REQUIRED_KBINFO: '0.0.1',
	
	basePath: null, 
	imagePath: null, 
	
	requireJS: function(libraryName) {
		// inserting via DOM fails in Safari 2.0, so brute force approach
		document.write('<script type="text/javascript" src="'+libraryName+'"><\/script>');
	},
	
	requireCSS: function(cssName) {
		// inserting via DOM fails in Safari 2.0, so brute force approach
		document.write('<link rel="stylesheet" type="text/css" href="'+cssName+'"><\/link>');
	},
	
	load: function() {
	    function convertVersionString(versionString) {
	      var v = versionString.replace(/_.*|\./g, '');
	      v = parseInt(v + '0'.times(4-v.length));
	      return versionString.indexOf('_') > -1 ? v-1 : v;
	    }
	
		if((typeof Prototype=='undefined') ||
				(typeof Element == 'undefined') ||
				(typeof Element.Methods=='undefined') ||
				(convertVersionString(Prototype.Version) <
				convertVersionString(FlowEditor.REQUIRED_PROTOTYPE)))
			throw("FlowPlugin requires the Prototype JavaScript framework >= " +
				FlowEditor.REQUIRED_PROTOTYPE);
	
		if((typeof Scriptaculous=='undefined') ||
				(convertVersionString(Scriptaculous.Version) <
				convertVersionString(FlowEditor.REQUIRED_SCRIPTACULOUS)))
			throw("FlowPlugin requires the Scriptaculous JavaScript framework >= " +
				FlowEditor.REQUIRED_SCRIPTACULOUS);
	  
		if((typeof KBInfo=='undefined') ||
				(convertVersionString(KBInfo.Version) <
				convertVersionString(FlowEditor.REQUIRED_KBINFO)))
			throw("FlowPlugin requires the KBInfo library >= " +
				FlowEditor.REQUIRED_KBINFO);
	  
		var js = /floweditor\.js(\?.*)?$/;
		$$('head script[src]').findAll(function(s) {
			return s.src.match(js);
		}).each(function(s) {
			var path = s.src.replace(js, '');
			FlowEditor.basePath = path;
			FlowEditor.imagePath = path+"../image/";
			('flowchart,action,guard,node,rule,nodeeditor,router,rollup').split(',').each(function(include) { 
				FlowEditor.requireJS(path+include+'.js');
			});
			FlowEditor.requireCSS(path+'floweditor.css');
			FlowEditor.requireCSS(path+'flowchart.css');
			FlowEditor.requireCSS(path+'nodeeditor.css');
			FlowEditor.requireCSS(path+'node.css');
			FlowEditor.requireCSS(path+'rule.css');
			FlowEditor.requireCSS(path+'guard.css');
	    });
	}
};

FlowEditor.load();