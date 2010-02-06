var editor = CodeMirror.fromTextArea("default-edit-area",{
	parserfile: ["tokenizegroovy.js", "parsegroovy.js"],
	path: "KnowWEExtension/scripts/CodeMirror-0.65/js/",
	stylesheet: "KnowWEExtension/scripts/CodeMirror-0.65/css/groovycolors.css",
	continuousScanning: 500,
	lineNumbers: true,
	textWrapping: false,
	tabMode: "spaces"
});