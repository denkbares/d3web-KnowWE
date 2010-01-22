SyntaxHighlighter.all();
SyntaxHighlighter.defaults['toolbar'] = false;

function a(){
alert('b');
SyntaxHighlighter.highlight();
}
KNOWWE.helper.observer.subscribe('quick-edit', a); 