/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

/**
 * SPARQL in the CodeMirror editor of KnowWE: highlighting and indentation of the bodies of all markups whose content
 * is a SPARQL query, and an editor tool to indent the query of the block at the caret.
 *
 * Loaded as module script of the SPARQL markups in plugin.xml, after the vendored CodeMirror bundle and the wiki
 * language. There is no Lezer grammar for SPARQL in the bundle, so the body is parsed by a stream parser. The
 * tokenizer follows the SPARQL mode of @codemirror/legacy-modes (MIT, https://github.com/codemirror/legacy-modes,
 * mode/sparql.js) and adds what DefaultMarkupType puts around the query: a comment behind the markup name and the
 * annotations "@name value" at the end of the body.
 */
window.SparqlLanguage = (function () {
	"use strict";

	if (typeof CodeMirror === "undefined" || typeof KnowWEWikiLanguage === "undefined") return null;

	const {StreamLanguage, HighlightStyle, LanguageSupport, indentRange} = CodeMirror.language;
	const {tags: t} = CodeMirror.lezer.highlight;

	/**
	 * All markups whose body is a SPARQL query, matched case insensitively. The visualizations belong to the OntoVis
	 * plugin, which depends on this one, a registration of a markup that is not installed has no effect.
	 */
	const markupNames = ["Sparql", "SparqlConsole", "SparqlConstruct", "SparqlVisualization", "SparqlVis"];

	function words(list) {
		return new RegExp("^(?:" + list.split(" ").join("|") + ")$", "i");
	}

	// keywords that start a query, an update or one of its main clauses
	const queryKeywords = words("base prefix select construct describe ask where insert delete data with load " +
		"clear drop create add move copy");
	const keywords = words("distinct reduced from named order by asc desc limit offset group having values undef " +
		"optional filter union minus graph service silent bind as in not exists using into to default all a");
	const builtins = words("str lang langmatches datatype bound iri uri bnode rand abs ceil floor round concat " +
		"strlen ucase lcase encode_for_uri contains strstarts strends strbefore strafter year month day hours " +
		"minutes seconds timezone tz now uuid struuid md5 sha1 sha256 sha384 sha512 coalesce if strlang strdt " +
		"sameterm isiri isuri isblank isliteral isnumeric regex substr replace count sum min max avg sample " +
		"group_concat separator");
	const booleans = words("true false");

	const NAME_START = "A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF" +
		"\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD";
	const NAME_CHAR = NAME_START + "_\\-0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040";
	const variable = new RegExp("^[" + NAME_START + "_0-9][" + NAME_CHAR + "]*");
	// a prefix name may contain dots, but neither start nor end with one
	const prefix = new RegExp("^(?:[" + NAME_START + "](?:[" + NAME_CHAR + ".]*[" + NAME_CHAR + "])?)?:");
	const local = new RegExp("^(?:[" + NAME_CHAR + ":]|%[0-9A-Fa-f]{2}|\\\\[_~.\\-!$&'()*+,;=/?#@%])" +
		"(?:[" + NAME_CHAR + ".:]|%[0-9A-Fa-f]{2}|\\\\[_~.\\-!$&'()*+,;=/?#@%])*");
	const number = /^(?:\d+\.?\d*(?:[eE][+-]?\d+)?|\.\d+(?:[eE][+-]?\d+)?)/;
	const iri = /^<[^<>"{}|^`\\\s]*>/;
	// like the content tokens of the wiki language, an annotation is "@name" at the start of a line
	const annotation = /^@[\wÀ-￿]+(?:\s*[:=])?(?=\s|$)/;
	const closing = {"}": "{", ")": "(", "]": "["};

	function tokenString(quote) {
		return (stream, state) => {
			let escaped = false, ch;
			while ((ch = stream.next()) != null) {
				if (!escaped && (quote.length === 1 ? ch === quote : ch === quote[0] && stream.match(quote.slice(1)))) {
					state.tokenize = null;
					return "string";
				}
				escaped = !escaped && ch === "\\";
			}
			// short strings end with the line, long strings continue on the next one
			if (quote.length === 1) state.tokenize = null;
			return "string";
		};
	}

	function tokenQuery(stream, state) {
		if (state.local) {
			state.local = false;
			if (stream.match(local)) {
				// a local name may not end with a dot, which ends the triple
				while (stream.current().endsWith(".") && !stream.current().endsWith("\\.")) stream.backUp(1);
				return "localName";
			}
		}
		const ch = stream.peek();
		if (ch === "#") {
			stream.skipToEnd();
			return "comment";
		}
		if (ch === "\"" || ch === "'") {
			const quote = stream.match(ch.repeat(3)) ? ch.repeat(3) : (stream.next(), ch);
			state.tokenize = tokenString(quote);
			return state.tokenize(stream, state);
		}
		if ((ch === "?" || ch === "$") && stream.match(new RegExp("^\\" + ch + variable.source.slice(1)))) {
			return "queryVariable";
		}
		if (ch === "<" && stream.match(iri)) return "iri";
		if (stream.match(/^_:[^\s.,;(){}\[\]]+/)) return "blankNode";
		if (stream.match(/^@[a-zA-Z]+(?:-[a-zA-Z0-9]+)*/)) return "languageTag";
		if (stream.match("^^")) return "meta";
		if (stream.match(number)) return "number";
		if (stream.match(prefix)) {
			state.local = true;
			return "prefix";
		}
		if (stream.match(/^[{}()\[\]]/)) {
			const bracket = stream.current();
			if (closing[bracket]) {
				if (state.context.length && state.context[state.context.length - 1] === closing[bracket]) state.context.pop();
			} else {
				state.context.push(bracket);
			}
			return "bracket";
		}
		if (stream.match(/^[.,;]/)) return "punctuation";
		if (stream.match(/^(?:\|\||&&|[!<>=]=?|[*+\-\/^|?])/)) return "operator";
		if (stream.match(/^[\wÀ-￿]+/)) {
			const word = stream.current();
			if (queryKeywords.test(word)) return "queryKeyword";
			if (keywords.test(word)) return "keyword";
			if (builtins.test(word)) return "function";
			if (booleans.test(word)) return "bool";
			return "name";
		}
		stream.next();
		return null;
	}

	const sparqlParser = {
		name: "sparql",
		startState: () => ({tokenize: null, context: [], local: false, line: 0, annotations: false}),
		copyState: state => ({...state, context: state.context.slice()}),
		token(stream, state) {
			if (stream.sol()) state.line++;
			if (state.tokenize) return state.tokenize(stream, state);
			// the annotations end the query, the value of an annotation runs up to the next one
			if (stream.sol() && !state.tokenize && stream.match(/^\s*(?=@)/, false)) {
				stream.eatSpace();
				if (stream.match(annotation)) {
					state.annotations = true;
					return "annotationName";
				}
			}
			if (state.annotations) {
				if (stream.eatSpace()) return null;
				stream.skipToEnd();
				return "annotationValue";
			}
			if (stream.eatSpace()) return null;
			// the body starts with the rest of the declaration line, which may hold a comment of the markup
			if (state.line === 1 && stream.match(/^\/\/.*/)) return "declarationComment";
			return tokenQuery(stream, state);
		},
		blankLine(state) {
			state.line++;
		},
		indent(state, textAfter, context) {
			if (state.annotations || state.tokenize) return null;
			let depth = state.context.length;
			const first = textAfter.charAt(0);
			if (depth && closing[first] === state.context[depth - 1]) depth--;
			return depth * context.unit;
		},
		languageData: {
			commentTokens: {line: "#"},
			closeBrackets: {brackets: ["(", "[", "{", "\"", "'"]},
			indentOnInput: /^\s*[}\])]$/,
		},
		tokenTable: {
			queryKeyword: t.definitionKeyword,
			keyword: t.keyword,
			function: t.function(t.variableName),
			bool: t.bool,
			queryVariable: t.special(t.variableName),
			iri: t.url,
			prefix: t.namespace,
			localName: t.name,
			name: t.name,
			blankNode: t.labelName,
			languageTag: t.meta,
			meta: t.meta,
			number: t.number,
			string: t.string,
			comment: t.lineComment,
			bracket: t.bracket,
			punctuation: t.separator,
			operator: t.operator,
			annotationName: t.annotation,
			annotationValue: t.attributeValue,
			declarationComment: t.lineComment,
		},
	};

	const sparqlLanguage = StreamLanguage.define(sparqlParser);

	// the colors of the editor, see the highlight style in KnowWE-Editor.js and the --syntax-* variables
	const color = {
		text: "var(--syntax-text, #000)",
		muted: "var(--syntax-muted, #868c95)",
		markup: "var(--syntax-markup, #1a3766)",
		definition: "var(--syntax-definition, #45607f)",
		variable: "var(--syntax-annotation, #0f6b74)",
		value: "var(--syntax-value, #357a28)",
		link: "var(--syntax-link, #173a64)",
	};

	/**
	 * Highlight style of SPARQL blocks. Query forms and clauses are bold like the block keywords of other markups,
	 * variables stand out, values use the value color and prefixes, operators and separators recede. Comments and
	 * annotations keep the colors of the editor's style.
	 */
	const sparqlHighlightStyle = HighlightStyle.define([
		{tag: t.definitionKeyword, color: color.markup, fontWeight: "600"},
		{tag: t.keyword, color: color.markup},
		{tag: t.function(t.variableName), color: color.definition},
		{tag: t.special(t.variableName), color: color.variable},
		{tag: [t.string, t.number, t.bool], color: color.value},
		{tag: t.url, color: color.link},
		{tag: t.name, color: color.text},
		{tag: [t.namespace, t.labelName, t.meta, t.separator, t.operator], color: color.muted},
		{tag: t.bracket, color: color.text},
	], {scope: sparqlLanguage});

	/** The CodeMirror language support of SPARQL, for snippets of a query outside of a page. */
	function sparql() {
		return new LanguageSupport(sparqlLanguage);
	}

	function isSparqlMarkup(name) {
		return markupNames.some(markup => markup.toLowerCase() === name.toLowerCase());
	}

	/** Returns the SPARQL markup block at the position, or null. */
	function sparqlBlock(state, pos) {
		const block = KnowWEWikiLanguage.markupBlock(state, pos);
		return block && isSparqlMarkup(block.name) ? block : null;
	}

	/**
	 * Returns the document range of the query in the block, without the rest of the declaration line if it holds no
	 * query, without the annotations and without trailing blanks.
	 */
	function queryRange(block) {
		let start = 0;
		const firstBreak = block.text.indexOf("\n");
		if (firstBreak >= 0 && /^\s*(\/\/.*)?$/.test(block.text.slice(0, firstBreak))) start = firstBreak + 1;
		let end = block.annotations.length ? block.annotations[0].from : block.text.length;
		// an annotation starts at its name, its indentation still belongs to the line of the annotation
		while (end > start && /\s/.test(block.text.charAt(end - 1))) end--;
		return {from: block.from + start, to: block.from + Math.max(start, end)};
	}

	/** Command that indents the lines of the query of the SPARQL block at the caret by its brackets. */
	function indentBlock(view, pos = view.state.selection.main.head) {
		const block = sparqlBlock(view.state, pos);
		if (!block) return false;
		const {from, to} = queryRange(block);
		const changes = indentRange(view.state, from, to);
		if (!changes.empty) view.dispatch({changes, userEvent: "indent"});
		return true;
	}

	/** Keys of the SPARQL tools, they only act with the caret in a SPARQL block. */
	const keys = [
		{key: "Shift-Alt-i", run: indentBlock},
	];

	// button at the end of the declaration line of the SPARQL block with the caret
	class ToolsWidget extends CodeMirror.view.WidgetType {
		constructor(pos) {
			super();
			this.pos = pos;
		}

		eq(other) {
			return other.pos === this.pos;
		}

		toDOM(view) {
			const tools = document.createElement("span");
			tools.className = "cm-sparql-tools";
			const button = (label, title, command) => {
				const element = document.createElement("button");
				element.type = "button";
				element.textContent = label;
				element.title = title;
				// keeps the focus and the caret in the editor
				element.addEventListener("mousedown", event => event.preventDefault());
				element.addEventListener("click", () => {
					command(view, this.pos);
					view.focus();
				});
				return element;
			};
			tools.append(
				button("Indent", "Indent the SPARQL query by its brackets (Shift-Alt-I)", indentBlock)
			);
			return tools;
		}

		ignoreEvent() {
			return true;
		}
	}

	function toolDecorations(state) {
		const {Decoration} = CodeMirror.view;
		const block = sparqlBlock(state, state.selection.main.head);
		if (!block) return Decoration.none;
		const line = state.doc.lineAt(block.from);
		return Decoration.set([Decoration.widget({widget: new ToolsWidget(block.from), side: 1}).range(line.to)]);
	}

	const toolsField = CodeMirror.state.StateField.define({
		create: toolDecorations,
		update(value, transaction) {
			return transaction.docChanged || transaction.selection ? toolDecorations(transaction.state) : value;
		},
		provide: field => CodeMirror.view.EditorView.decorations.from(field),
	});

	const toolsTheme = CodeMirror.view.EditorView.baseTheme({
		".cm-sparql-tools": {
			marginLeft: "1.5em",
			display: "inline-flex",
			gap: "4px",
			verticalAlign: "middle",
		},
		".cm-sparql-tools button": {
			font: "inherit",
			fontSize: "11px",
			lineHeight: "1.4",
			padding: "0 6px",
			border: "1px solid var(--border-color-default, #e2e2e9)",
			borderRadius: "3px",
			background: "var(--bg-color-secondary, #f6f6f6)",
			color: "var(--syntax-markup, #1a3766)",
			cursor: "pointer",
		},
		".cm-sparql-tools button:hover": {
			background: "var(--bg-color-secondary-shade, #eee)",
		},
	});

	/**
	 * The editor extension with the keys of the SPARQL tools, and their buttons in the page editor. The instant edit
	 * shows the buttons in its tool bar, see KNOWWE.plugin.sparql.codeMirrorEditTool.
	 */
	function tools(options) {
		const keymap = CodeMirror.state.Prec.high(CodeMirror.view.keymap.of(keys));
		return options?.mode === "section" ? [keymap] : [keymap, toolsField, toolsTheme];
	}

	const language = sparql();
	for (const name of markupNames) {
		KnowWEWikiLanguage.registerMarkup(name, {parser: sparqlLanguage.parser, highlight: sparqlHighlightStyle, language});
	}
	// the page editor and the instant edit create their editors through KnowWEEditor, which is a classic script
	if (typeof KnowWEEditor !== "undefined" && KnowWEEditor.registerExtension) KnowWEEditor.registerExtension(tools);

	return {
		markupNames, sparqlLanguage, sparqlHighlightStyle, sparql, sparqlBlock, queryRange, indentBlock,
		tools
	};
})();
