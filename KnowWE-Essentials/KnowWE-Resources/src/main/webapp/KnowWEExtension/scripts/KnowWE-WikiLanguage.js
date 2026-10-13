var KnowWEWikiLanguage = (function(exports, _codemirror_state, _lezer_common, _lezer_highlight, _codemirror_language, _lezer_lr) {
	Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
	//#region src/markups.js
	var FALLBACK_NAME = "[A-Za-z][\\w.\\-]*(?::[\\w.\\-]+)*";
	var blockPattern = null;
	var inlinePattern = null;
	var fallbackPattern = namePattern([FALLBACK_NAME]);
	function namePattern(names) {
		if (names.length === 0) return null;
		const sorted = [...names].sort((a, b) => b.length - a.length);
		return new RegExp("^%%(?:" + sorted.join("|") + ")(?=[ \\t:=]|$)", "i");
	}
	/**
	* Defines the markups of the wiki. Each entry has the markup name, which KnowWE treats as a case insensitive regular
	* expression, and whether the markup is inline. Passing null restores the default recognition.
	*/
	function defineMarkups(markups) {
		if (markups == null) {
			blockPattern = inlinePattern = null;
			return;
		}
		const block = [], inline = [];
		for (const markup of markups) {
			const entry = typeof markup === "string" ? {
				name: markup,
				inline: false
			} : markup;
			(entry.inline ? inline : block).push(entry.name);
		}
		blockPattern = namePattern(block);
		inlinePattern = namePattern(inline);
	}
	/** Returns whether markups were defined, so declarations are matched against known names only. */
	function markupsDefined() {
		return blockPattern !== null || inlinePattern !== null;
	}
	var BLOCK_REST = /^[ \t]*[:=]?[ \t]*(?:\/\/.*)?$/;
	var OPEN_REST = /^[ \t]*[:=]?/;
	/**
	* Matches a markup declaration against a whole line. Returns the length of the opening "%%Name" including the
	* separator and whether the markup is a block running to its closing line or a single line markup, or null.
	*/
	function matchDeclaration(line) {
		let indent = 0;
		while (line.charCodeAt(indent) === 32 || line.charCodeAt(indent) === 9) indent++;
		const declaration = indent ? line.slice(indent) : line;
		const name = markupsDefined() ? blockPattern?.exec(declaration) : fallbackPattern.exec(declaration);
		if (!name) return null;
		const rest = declaration.slice(name[0].length);
		if (BLOCK_REST.test(rest)) return {
			block: true,
			length: indent + name[0].length + OPEN_REST.exec(rest)[0].length
		};
		let i = 0;
		while (rest.charCodeAt(i) === 32 || rest.charCodeAt(i) === 9) i++;
		const separator = rest.charCodeAt(i);
		if (separator === 58 || separator === 61) i++;
		else if (i === 0 || !markupsDefined()) return null;
		let content = i;
		while (rest.charCodeAt(content) === 32 || rest.charCodeAt(content) === 9) content++;
		if (content >= rest.length || rest.charCodeAt(content) === 47) return null;
		return {
			block: false,
			length: indent + name[0].length + i
		};
	}
	/**
	* Matches an inline markup at the start of the given text. Returns the length of the opening "%%Name" including the
	* separator, or -1. The caller checks that a closing "%%" or "/%" follows.
	*/
	function matchInlineDeclaration(text) {
		const name = inlinePattern?.exec(text);
		if (!name) return -1;
		return name[0].length + OPEN_REST.exec(text.slice(name[0].length))[0].length;
	}
	//#endregion
	//#region src/tokens.js
	var NEWLINE$1 = 10;
	var RETURN$1 = 13;
	var SPACE$1 = 32;
	var TAB$1 = 9;
	var EXCLAMATION = 33;
	var QUOTE = 34;
	var HASH = 35;
	var DOLLAR = 36;
	var PERCENT = 37;
	var AMPERSAND = 38;
	var APOSTROPHE = 39;
	var OPEN_PAREN = 40;
	var CLOSE_PAREN = 41;
	var STAR = 42;
	var DASH = 45;
	var DOT = 46;
	var SLASH$1 = 47;
	var COLON$1 = 58;
	var SEMICOLON = 59;
	var EQUALS$1 = 61;
	var OPEN_BRACKET = 91;
	var BACKSLASH = 92;
	var CLOSE_BRACKET = 93;
	var UNDERSCORE = 95;
	var OPEN_BRACE = 123;
	var PIPE = 124;
	var CLOSE_BRACE = 125;
	var TILDE = 126;
	var SPECIAL = /* @__PURE__ */ new Set([
		NEWLINE$1,
		RETURN$1,
		UNDERSCORE,
		APOSTROPHE,
		OPEN_BRACE,
		CLOSE_BRACE,
		OPEN_BRACKET,
		CLOSE_BRACKET,
		TILDE,
		BACKSLASH,
		PERCENT,
		SLASH$1,
		AMPERSAND,
		COLON$1,
		PIPE
	]);
	var ESCAPABLE = /* @__PURE__ */ new Set([
		PIPE,
		TILDE,
		BACKSLASH,
		STAR,
		HASH,
		DASH,
		EXCLAMATION,
		APOSTROPHE,
		UNDERSCORE,
		OPEN_BRACKET,
		OPEN_BRACE,
		CLOSE_BRACKET,
		CLOSE_BRACE,
		PERCENT
	]);
	var EXTERNAL = new RegExp("^(?:" + [
		"https?:",
		"ftp:",
		"mailto:",
		"news:",
		"file:",
		"rtsp:",
		"mms:",
		"ldap:",
		"gopher:",
		"nntp:",
		"telnet:",
		"wais:",
		"prospero:",
		"z39\\.50[sr]",
		"vemmi:",
		"imap:",
		"nfs:",
		"acap:",
		"tip:",
		"pop:",
		"dav:",
		"opaquelocktoken:",
		"sips?:",
		"tel:",
		"fax:",
		"modem:",
		"soap\\.beeps?",
		"xmlrpc\\.beeps?",
		"urn:",
		"go:",
		"h323:",
		"ipp:",
		"tftp:",
		"mupdate:",
		"pres:",
		"im:",
		"mtqp",
		"smb:"
	].join("|") + ")");
	var PARAGRAPH = 0;
	var LIST = 1;
	var TABLE = 2;
	var TERM = 3;
	var OTHER = 4;
	var DIV = 1;
	var STYLE = 2;
	var MONOSPACE = 3;
	var BOLD = 4;
	var ITALIC = 5;
	var LINK = 6;
	var PLUGIN = 7;
	var CODE = 8;
	var END = [
		-1,
		119,
		118,
		117,
		115,
		116
	];
	var MAX_DEPTH = 48;
	var Frame = class Frame {
		constructor(type, parent) {
			this.type = type;
			this.parent = parent;
			this.monospace = (parent?.monospace ?? 0) + (type === MONOSPACE ? 1 : 0);
			this.styles = (parent?.styles ?? 0) + (type === STYLE || type === DIV ? 1 : 0);
			this.hash = Math.imul((parent?.hash ?? 0) ^ type * 73244475, 2654435761) ^ (parent ? parent.hash >>> 15 : 0);
			this.children = [];
			this.contexts = [];
		}
		push(type) {
			return this.children[type] ??= new Frame(type, this);
		}
	};
	var DANGLING_BOLD = 1;
	var DANGLING_ITALIC = 2;
	var PENDING_ITALIC = 8;
	var TAINTED = 16;
	var DANGLING = 3;
	var PENDING = 12;
	var Context = class {
		constructor(kind, frame, flags) {
			this.kind = kind;
			this.frame = frame;
			this.flags = flags;
			this.hash = flags ? NaN : frame.hash ^ Math.imul(kind + 1, 668265261) | 1;
		}
	};
	var ROOT = new Frame(0, null);
	function context(kind, frame, flags = 0) {
		if (flags & 15) flags |= TAINTED;
		return frame.contexts[kind * 32 + flags] ??= new Context(kind, frame, flags);
	}
	/** Ends bold and italic, like JSPWiki at the start of a block level element. */
	function startBlock(cx, kind) {
		return context(kind, cx.frame, cx.flags & TAINTED);
	}
	/**
	* Returns the context after a paragraph break, which ends bold and italic and continues them in the next paragraph,
	* unless the next line starts a block that cannot be inside of a paragraph.
	*/
	function paragraphBreak(cx, input, offset) {
		const restart = (cx.flags & DANGLING | (cx.flags & PENDING) >> 2) << 2;
		const ch = input.peek(offset);
		const block = ch === STAR || ch === HASH || ch === EXCLAMATION || ch === SEMICOLON || ch === PIPE || matches(input, offset, "{{{") || matches(input, offset, "----") || matches(input, offset, "%%") || isBlank$1(ch) && declarationAt(input, offset);
		return context(lineKind(input, offset), cx.frame, (block ? 0 : restart) | cx.flags & TAINTED);
	}
	/** Returns the innermost open element of one of the given types, inline elements are searched above blocks only. */
	function openElement(cx, types) {
		for (let frame = cx.frame; frame !== ROOT; frame = frame.parent) {
			if (types.includes(frame.type)) return frame;
			if (frame.type === DIV) return null;
		}
		return null;
	}
	/** Returns the innermost element if it is inline, these end with their paragraph like unit. */
	function openInline(cx) {
		const type = cx.frame.type;
		return type >= STYLE && type <= ITALIC ? cx.frame : null;
	}
	/** Returns the kind of the unit that the line at the offset starts after a line break. */
	function lineKind(input, offset) {
		const ch = input.peek(offset);
		if ((ch === PERCENT || isBlank$1(ch)) && declarationAt(input, offset)) return OTHER;
		switch (ch) {
			case EXCLAMATION: return OTHER;
			case STAR:
			case HASH: return LIST;
			case PIPE: return TABLE;
			case SEMICOLON: return TERM;
			default: return PARAGRAPH;
		}
	}
	/**
	* Tracks the kind of the paragraph like unit and the open elements. Line breaks continue paragraphs on every line
	* that does not start a block, and list items on lines starting with a space.
	*/
	var lineContext = new _lezer_lr.ContextTracker({
		start: context(PARAGRAPH, ROOT),
		shift(cx, term, stack, input) {
			switch (term) {
				case 110: {
					const next = afterLineBreak(input, 0);
					if (atLineStart(input)) return paragraphBreak(cx, input, next);
					const kind = lineKind(input, next);
					return kind === TABLE ? startBlock(cx, TABLE) : context(kind, cx.frame, cx.flags);
				}
				case 15: return startBlock(cx, PARAGRAPH);
				case 24: return context(PARAGRAPH, cx.frame.push(CODE), cx.flags & TAINTED);
				case 12: return startBlock(cx, LIST);
				case 112: return context(LIST, cx.frame, cx.flags);
				case 16:
				case 17:
				case 113: return context(TABLE, cx.frame, cx.flags);
				case 13: return startBlock(cx, TERM);
				case 3:
				case 4: return startBlock(cx, OTHER);
				case 11:
				case 14: return context(OTHER, cx.frame, cx.flags);
				case 56: return context(PARAGRAPH, cx.frame.push(DIV), cx.flags & TAINTED);
				case 55: return context(cx.kind, cx.frame.push(STYLE), cx.flags);
				case 22: return context(cx.kind, cx.frame.push(MONOSPACE), cx.flags);
				case 18: return context(cx.kind, cx.frame.push(BOLD), cx.flags);
				case 20: return context(cx.kind, cx.frame.push(ITALIC), cx.flags);
				case 120: return context(cx.kind, cx.frame.push(BOLD), cx.flags & -5);
				case 121: return context(cx.kind, cx.frame.push(ITALIC), cx.flags & -9);
				case 1: return context(cx.kind, cx.frame, cx.flags & -2);
				case 2: return context(cx.kind, cx.frame, cx.flags & -3);
				case 115: return context(cx.kind, cx.frame.parent, cx.flags | DANGLING_BOLD);
				case 116: return context(cx.kind, cx.frame.parent, cx.flags | DANGLING_ITALIC);
				case 28:
				case 29:
				case 30: return context(cx.kind, cx.frame.push(LINK), cx.flags);
				case 38:
				case 39:
				case 40:
				case 41: return context(cx.kind, cx.frame.push(PLUGIN), cx.flags);
				case 25: return context(cx.kind, cx.frame.push(CODE), cx.flags);
				case 57:
				case 23:
				case 19:
				case 21:
				case 119:
				case 118:
				case 117:
				case 32:
				case 42:
				case 27: return context(cx.frame.type === DIV ? PARAGRAPH : cx.kind, cx.frame.parent, cx.flags);
				default: return cx;
			}
		},
		hash: (cx) => cx.hash
	});
	function isBlank$1(ch) {
		return ch === SPACE$1 || ch === TAB$1;
	}
	function isLineEnd(ch) {
		return ch === NEWLINE$1 || ch === RETURN$1 || ch < 0;
	}
	function isWordChar$1(ch) {
		return ch >= 48 && ch <= 57 || ch >= 65 && ch <= 90 || ch >= 97 && ch <= 122 || ch === UNDERSCORE || ch >= 192;
	}
	function isLetter(ch) {
		if (ch >= 65 && ch <= 90 || ch >= 97 && ch <= 122) return true;
		return ch >= 192 && /\p{L}/u.test(String.fromCharCode(ch));
	}
	function atLineStart(input, offset = 0) {
		return input.pos + offset === 0 || input.peek(offset - 1) === NEWLINE$1;
	}
	function matches(input, offset, string) {
		for (let i = 0; i < string.length; i++) if (input.peek(offset + i) !== string.charCodeAt(i)) return false;
		return true;
	}
	function countWhile(input, offset, predicate) {
		let i = offset;
		while (predicate(input.peek(i))) i++;
		return i - offset;
	}
	function lineText(input, offset) {
		let text = "";
		for (let i = offset; text.length < 4096; i++) {
			const ch = input.peek(i);
			if (isLineEnd(ch)) break;
			text += String.fromCharCode(ch);
		}
		return text;
	}
	/** Returns the offset of the next line start after the line break at the given offset. */
	function afterLineBreak(input, offset) {
		return input.peek(offset) === RETURN$1 && input.peek(offset + 1) === NEWLINE$1 ? offset + 2 : offset + 1;
	}
	/** Returns the markup declaration of the line at the given offset, or null. */
	function declarationAt(input, offset) {
		const indent = countWhile(input, offset, isBlank$1);
		if (input.peek(offset + indent) !== PERCENT || input.peek(offset + indent + 1) !== PERCENT) return null;
		return matchDeclaration(lineText(input, offset));
	}
	/** Returns the length of a markup closing line "/%" or "%" at the given offset, without the line break, or -1. */
	function closingLineLength(input, offset) {
		let i = offset + countWhile(input, offset, isBlank$1);
		if (input.peek(i) === SLASH$1) i++;
		if (input.peek(i) !== PERCENT) return -1;
		i++;
		i += countWhile(input, i, isBlank$1);
		return isLineEnd(input.peek(i)) ? i - offset : -1;
	}
	/**
	* Returns the length of the class and style of a JSPWiki style, starting after "%%" or "/%". The class runs up to a
	* blank, line end or opening parenthesis and a backslash escapes the next character. The style is in balanced
	* parentheses and may span lines.
	*/
	function styleSpecLength(input, offset) {
		let i = offset;
		if (isLetter(input.peek(i))) i += classLength(input, i);
		if (input.peek(i) === OPEN_PAREN) i += declarationLength(input, i);
		return i - offset;
	}
	function classLength(input, offset) {
		let i = offset;
		for (;;) {
			const ch = input.peek(i);
			if (ch === BACKSLASH && !isLineEnd(input.peek(i + 1))) i += 2;
			else if (ch === OPEN_PAREN || isBlank$1(ch) || isLineEnd(ch)) return i - offset;
			else i++;
		}
	}
	function declarationLength(input, offset) {
		let depth = 0, i = offset;
		for (;;) {
			const ch = input.peek(i);
			if (ch < 0) return i - offset;
			i++;
			if (ch === OPEN_PAREN) depth++;
			else if (ch === CLOSE_PAREN && --depth === 0) return i - offset;
		}
	}
	/** Returns whether a style opening at the given offset is a block, since nothing follows its class and style. */
	function isStyleBlock(input, offset) {
		let i = offset + 2;
		i += styleSpecLength(input, i);
		i += countWhile(input, i, isBlank$1);
		return isLineEnd(input.peek(i));
	}
	function opensStyle(input, offset) {
		const ch = input.peek(offset);
		return ch === OPEN_PAREN || isLetter(ch);
	}
	/** Returns the offset of the "}}}" that ends preformatted text, or -1. A tilde in front escapes it. */
	function codeEnd(input, offset) {
		for (let i = offset;; i++) {
			const ch = input.peek(i);
			if (ch < 0) return -1;
			if (ch === TILDE && matches(input, i + 1, "}}}")) i += 3;
			else if (ch === CLOSE_BRACE && matches(input, i + 1, "}}")) return i;
		}
	}
	/**
	* Scans to the given end of a bracket construct like JSPWiki, across line breaks. Returns the offset after the end,
	* or -1 if the input ends or a markup declaration line comes first.
	*/
	function scanBracket(input, offset, plugin) {
		let depth = 1;
		for (let i = offset;;) {
			const ch = input.peek(i);
			if (ch < 0) return -1;
			if (ch === NEWLINE$1) {
				i++;
				if (declarationAt(input, i)) return -1;
			} else if (!plugin) {
				if (ch === CLOSE_BRACKET) return i + 1;
				i++;
			} else if (ch === OPEN_BRACKET && input.peek(i + 1) === OPEN_BRACE) {
				depth++;
				i += 2;
			} else if (ch === CLOSE_BRACE && input.peek(i + 1) === CLOSE_BRACKET) {
				i += 2;
				if (--depth === 0) return i;
			} else i++;
		}
	}
	var EMPTY = 0;
	var BLOCK = 1;
	var INDENTED = 2;
	var PLAIN = 3;
	var LIST_LINE = 4;
	var TABLE_LINE = 5;
	function nextLineKind(input, offset) {
		const ch = input.peek(offset);
		if (ch < 0 || ch === NEWLINE$1 || ch === RETURN$1 && input.peek(offset + 1) === NEWLINE$1) return EMPTY;
		if ((ch === PERCENT || isBlank$1(ch)) && declarationAt(input, offset)) return BLOCK;
		switch (ch) {
			case SPACE$1: return INDENTED;
			case STAR:
			case HASH: return LIST_LINE;
			case PIPE: return TABLE_LINE;
			case EXCLAMATION:
			case SEMICOLON: return BLOCK;
			case DASH: return matches(input, offset, "----") ? BLOCK : PLAIN;
			case OPEN_BRACE: return matches(input, offset, "{{{") ? BLOCK : PLAIN;
			case PERCENT: return input.peek(offset + 1) === PERCENT && opensStyle(input, offset + 2) && isStyleBlock(input, offset) ? BLOCK : PLAIN;
			default: return PLAIN;
		}
	}
	function lineBreak(input, stack) {
		const length = input.next === RETURN$1 ? 2 : 1, cx = stack.context;
		let term = 110;
		if (!atLineStart(input)) {
			const next = nextLineKind(input, length);
			const inline = openInline(cx) !== null;
			if (next === LIST_LINE && cx.kind === LIST && (inline || stack.canShift(112))) term = 112;
			else if (next === TABLE_LINE && cx.kind === TABLE && (inline || stack.canShift(113))) term = 113;
			else if (next === INDENTED || next === PLAIN && cx.kind === PARAGRAPH) {
				if ((cx.kind === PARAGRAPH || cx.kind === LIST) && stack.canShift(111)) return input.acceptToken(111, length);
			}
		}
		const element = openInline(cx);
		if (element) return input.acceptToken(END[element.type], 0);
		input.acceptToken(term, length);
	}
	/**
	* Accepts the closing mark of the given element, or the end token of the innermost element if that is nested in it,
	* so that every element in between is closed first like the popElement of JSPWiki.
	*/
	function close(input, stack, element, term, length) {
		const top = stack.context.frame;
		if (top !== element) return input.acceptToken(END[top.type], 0);
		input.acceptToken(stack.canShift(term) ? term : 114, length);
	}
	function mayNest(stack, monospace) {
		const frame = stack.context.frame;
		return (monospace ? frame.monospace : frame.styles) < MAX_DEPTH;
	}
	/** Produces the tokens that are only valid in column zero. Returns whether a token was accepted. */
	function lineStart(input, stack) {
		const ch = input.next, flags = stack.context.flags;
		if (flags & PENDING && !isLineEnd(ch) && !declarationAt(input, 0)) {
			const restart = flags & PENDING_ITALIC ? 121 : 120;
			if (stack.canShift(restart)) return input.acceptToken(restart, 0), true;
		}
		if ((ch === PERCENT || isBlank$1(ch)) && stack.canShift(3)) {
			const declaration = declarationAt(input, 0);
			if (declaration) {
				input.acceptToken(declaration.block ? 3 : 4, declaration.length);
				return true;
			}
		}
		let term = -1, length = 0;
		switch (ch) {
			case EXCLAMATION:
				term = 11;
				length = Math.min(3, countWhile(input, 0, (c) => c === EXCLAMATION));
				break;
			case STAR:
			case HASH:
				term = 12;
				length = countWhile(input, 0, (c) => c === STAR || c === HASH);
				length += countWhile(input, length, (c) => c === SPACE$1);
				break;
			case SEMICOLON:
				term = 13;
				length = 1;
				break;
			case DASH:
				length = countWhile(input, 0, (c) => c === DASH);
				if (length >= 4) term = 15;
				break;
			case PIPE:
				term = input.peek(1) === PIPE ? 17 : 16;
				length = term === 17 ? 2 : 1;
				break;
			case OPEN_BRACE:
				if (matches(input, 0, "{{{") && codeEnd(input, 3) >= 0) {
					term = 24;
					length = 3;
				}
				break;
			case PERCENT: if (input.peek(1) === PERCENT && opensStyle(input, 2) && isStyleBlock(input, 0) && mayNest(stack, false)) {
				term = 56;
				length = 2;
			}
		}
		if (term < 0 || !stack.canShift(term)) return false;
		input.acceptToken(term, length);
		return true;
	}
	function markupContent(input, stack) {
		let length = 0;
		if (stack.canShift(6)) {
			for (;;) {
				let ch = input.peek(length);
				while (!isLineEnd(ch)) ch = input.peek(++length);
				if (ch < 0) break;
				length = afterLineBreak(input, length);
				if (input.peek(length) < 0 || closingLineLength(input, length) >= 0) break;
			}
			if (length > 0) input.acceptToken(6, length);
		} else if (stack.canShift(7)) {
			while (!isLineEnd(input.peek(length))) length++;
			if (length > 0) input.acceptToken(7, length);
		} else {
			length = inlineMarkupEnd(input, 0);
			if (length > 0) input.acceptToken(8, length);
			else input.acceptToken(10, 2);
		}
	}
	/** Returns the offset of the "%%" or "/%" that closes an inline markup, or -1. */
	function inlineMarkupEnd(input, offset) {
		for (let i = offset;; i++) {
			const ch = input.peek(i);
			if (ch < 0) return -1;
			if ((ch === PERCENT || ch === SLASH$1) && input.peek(i + 1) === PERCENT) return i;
			if (ch === NEWLINE$1 && declarationAt(input, i + 1)) return -1;
		}
	}
	function code(input) {
		const end = codeEnd(input, 0);
		if (end > 0) input.acceptToken(26, end);
		else input.acceptToken(27, 3);
	}
	function link(input, stack) {
		const ch = input.next;
		if (ch === CLOSE_BRACKET) return input.acceptToken(32, 1);
		if (ch === PIPE && stack.canShift(31)) return input.acceptToken(31, 1);
		if (stack.canShift(123) || stack.canShift(122)) {
			if (isWordChar$1(ch) && stack.canShift(36)) {
				const name = countWhile(input, 0, isWordChar$1);
				const blanks = countWhile(input, name, isBlank$1);
				if (input.peek(name + blanks) === EQUALS$1) return input.acceptToken(36, name);
			}
			if (stack.canShift(122)) {
				const blanks = countWhile(input, 0, isBlank$1);
				if (input.peek(blanks) === EQUALS$1) return input.acceptToken(122, blanks + 1 + countWhile(input, blanks + 1, isBlank$1));
			}
			if (ch === APOSTROPHE && stack.canShift(37)) {
				let i = 1;
				while (input.peek(i) !== APOSTROPHE && input.peek(i) !== CLOSE_BRACKET) i++;
				return input.acceptToken(37, input.peek(i) === APOSTROPHE ? i + 1 : i);
			}
			const length = Math.max(1, countWhile(input, 0, (c) => c !== CLOSE_BRACKET && c !== APOSTROPHE && c !== EQUALS$1 && !isWordChar$1(c)));
			return input.acceptToken(123, length);
		}
		let length = 0, text = "";
		for (let c = ch; c !== PIPE && c !== CLOSE_BRACKET; c = input.peek(++length)) text += String.fromCharCode(c);
		if (input.peek(length) === PIPE && stack.canShift(33)) return input.acceptToken(33, length);
		input.acceptToken(EXTERNAL.test(text.trim()) ? 35 : 34, length);
	}
	/** Accepts the opening of a link, footnote or footnote reference at "[", if its end follows. */
	function openLink(input) {
		const end = scanBracket(input, 1, false);
		if (end < 0) return input.acceptToken(64, 1);
		let content = "";
		for (let i = 1; i < end - 1; i++) content += String.fromCharCode(input.peek(i));
		const parts = content.split("|");
		const target = parts.length === 1 ? parts[0] : parts[1].trim();
		if (target.startsWith("#")) return input.acceptToken(29, 1);
		if (/^-?\d+$/.test(target)) return input.acceptToken(30, 1);
		input.acceptToken(28, 1);
	}
	/** Accepts the opening of a plugin, variable, access rule or metadata at "[{", if its end follows. */
	function openPlugin(input, unclosed) {
		if (scanBracket(input, 2, true) < 0) return input.acceptToken(unclosed, 2);
		if (input.peek(2) === DOLLAR) return input.acceptToken(39, 3);
		if (matches(input, 2, "ALLOW") || matches(input, 2, "DENY")) return input.acceptToken(40, 2);
		if (matches(input, 2, "SET")) return input.acceptToken(41, 2);
		input.acceptToken(38, 2);
	}
	/** Returns the length of a quoted parameter value, which nested plugins do not end, unlike the end of its plugin. */
	function quotedLength(input, quote) {
		let depth = 0;
		for (let i = 1;; i++) {
			const ch = input.peek(i);
			if (ch < 0) return i;
			if (ch === BACKSLASH && input.peek(i + 1) >= 0) i++;
			else if (ch === OPEN_BRACKET && input.peek(i + 1) === OPEN_BRACE) {
				depth++;
				i++;
			} else if (ch === CLOSE_BRACE && input.peek(i + 1) === CLOSE_BRACKET) {
				if (depth === 0) return i;
				depth--;
				i++;
			} else if (ch === quote && depth === 0) return i + 1;
		}
	}
	function atPluginEnd(input, offset) {
		return input.peek(offset) === CLOSE_BRACE && input.peek(offset + 1) === CLOSE_BRACKET;
	}
	function atPluginStart(input, offset) {
		return input.peek(offset) === OPEN_BRACKET && input.peek(offset + 1) === OPEN_BRACE;
	}
	/** Returns whether a plugin or a nested plugin ends or starts at the offset. */
	function atPluginBracket(input, offset) {
		return atPluginEnd(input, offset) || atPluginStart(input, offset);
	}
	/** Returns the length of a run up to a blank, a line end or a plugin bracket. */
	function wordLength(input, offset) {
		let i = offset;
		while (!isBlank$1(input.peek(i)) && !isLineEnd(input.peek(i)) && !atPluginBracket(input, i)) i++;
		return i - offset;
	}
	function plugin(input, stack) {
		const ch = input.next;
		if (atPluginEnd(input, 0)) return input.acceptToken(42, 2);
		if (atPluginStart(input, 0)) return openPlugin(input, stack.canShift(128) ? 128 : 125);
		if (stack.canShift(128)) {
			let i = 0;
			while (input.peek(i) >= 0 && !atPluginEnd(input, i) && !atPluginStart(input, i)) i++;
			return input.acceptToken(128, i);
		}
		if (ch === NEWLINE$1 || ch === RETURN$1) {
			if (stack.canShift(127)) {
				let i = afterLineBreak(input, 0), blankLines = 0;
				for (;;) {
					const blanks = countWhile(input, i, isBlank$1);
					const end = input.peek(i + blanks);
					if (end !== NEWLINE$1 && end !== RETURN$1) break;
					i = afterLineBreak(input, i + blanks);
					blankLines++;
				}
				if (blankLines > 0) return input.acceptToken(127, i);
			}
			return input.acceptToken(126, afterLineBreak(input, 0));
		}
		if (isBlank$1(ch)) return input.acceptToken(126, countWhile(input, 0, isBlank$1));
		if (stack.canShift(52)) return input.acceptToken(52, wordLength(input, 0));
		if (stack.canShift(45)) return input.acceptToken(45, matches(input, 0, "DENY") ? 4 : 5);
		if (stack.canShift(46)) return input.acceptToken(46, 3);
		if (stack.canShift(53)) return input.acceptToken(53, wordLength(input, 0));
		if (stack.canShift(54)) {
			if (ch === 44) return input.acceptToken(125, 1);
			let i = 0, end = 0;
			for (let c = ch; c !== 44 && !isLineEnd(c) && !atPluginBracket(input, i); c = input.peek(++i)) if (!isBlank$1(c)) end = i + 1;
			return input.acceptToken(54, end);
		}
		if (stack.canShift(49)) {
			let i = 0, end = 0;
			for (let c = ch; c !== EQUALS$1 && c >= 0 && !atPluginBracket(input, i); c = input.peek(++i)) if (!isBlank$1(c) && !isLineEnd(c)) end = i + 1;
			if (input.peek(i) === EQUALS$1 && end > 0) return input.acceptToken(49, end);
			return input.acceptToken(125, wordLength(input, 0));
		}
		if (stack.canShift(51)) {
			let i = 0, end = 0;
			for (let c = ch; c >= 0 && !atPluginBracket(input, i); c = input.peek(++i)) if (!isBlank$1(c) && !isLineEnd(c)) end = i + 1;
			return input.acceptToken(51, end);
		}
		if (ch === EQUALS$1 && stack.canShift(124)) return input.acceptToken(124, 1);
		if (stack.canShift(50)) {
			if (ch === APOSTROPHE || ch === QUOTE) return input.acceptToken(50, quotedLength(input, ch));
			return input.acceptToken(50, wordLength(input, 0));
		}
		if (isWordChar$1(ch)) {
			const word = countWhile(input, 0, (c) => isWordChar$1(c) || c === DOT);
			if (stack.canShift(47)) {
				if (word === 6 && matches(input, 0, "INSERT") && stack.canShift(43)) return input.acceptToken(43, 6);
				return input.acceptToken(47, word);
			}
			if (word === 5 && matches(input, 0, "WHERE") && stack.canShift(44)) return input.acceptToken(44, 5);
			const name = countWhile(input, 0, (c) => isWordChar$1(c) || c === DOT || c === DASH);
			if (input.peek(name + countWhile(input, name, isBlank$1)) === EQUALS$1 && stack.canShift(48)) return input.acceptToken(48, name);
			return input.acceptToken(125, word);
		}
		input.acceptToken(125, 1);
	}
	function styleSpec(input, stack) {
		const ch = input.next;
		if (isLetter(ch) && stack.canShift(59)) return input.acceptToken(59, classLength(input, 0)), true;
		if (ch === OPEN_PAREN && stack.canShift(60)) return input.acceptToken(60, declarationLength(input, 0)), true;
		if (isBlank$1(ch) && stack.canShift(129)) {
			const length = countWhile(input, 0, isBlank$1);
			if (isLineEnd(input.peek(length))) return input.acceptToken(129, length), true;
		}
		return false;
	}
	function escape(input) {
		const next = input.peek(1);
		if (next === SPACE$1) return input.acceptToken(61, 2);
		if (ESCAPABLE.has(next)) return input.acceptToken(61, 1 + countWhile(input, 1, (c) => c === next));
		input.acceptToken(114, 1);
	}
	/** Accepts a mark that ends the current table cell or definition term, after the end of its inline elements. */
	function unitMark(input, stack, term, length) {
		const element = openInline(stack.context);
		if (element) return input.acceptToken(END[element.type], 0);
		input.acceptToken(stack.canShift(term) ? term : 114, length);
	}
	function inline(input, stack) {
		const ch = input.next, next = input.peek(1);
		switch (ch) {
			case UNDERSCORE:
				if (next === UNDERSCORE) {
					const bold = openElement(stack.context, [BOLD]);
					if (bold) return close(input, stack, bold, 19, 2);
					return input.acceptToken(stack.context.flags & DANGLING_BOLD ? 1 : 18, 2);
				}
				break;
			case APOSTROPHE:
				if (next === APOSTROPHE) {
					const italic = openElement(stack.context, [ITALIC]);
					if (italic) return close(input, stack, italic, 21, 2);
					return input.acceptToken(stack.context.flags & DANGLING_ITALIC ? 2 : 20, 2);
				}
				break;
			case OPEN_BRACE:
				if (next === OPEN_BRACE) {
					if (input.peek(2) !== OPEN_BRACE) return input.acceptToken(mayNest(stack, true) ? 22 : 114, 2);
					return input.acceptToken(codeEnd(input, 3) >= 0 ? 25 : 64, 3);
				}
				break;
			case CLOSE_BRACE:
				if (next === CLOSE_BRACE) {
					if (input.peek(2) === CLOSE_BRACE) return input.acceptToken(114, 3);
					const monospace = openElement(stack.context, [MONOSPACE]);
					return monospace ? close(input, stack, monospace, 23, 2) : input.acceptToken(114, 2);
				}
				break;
			case OPEN_BRACKET:
				if (next === OPEN_BRACKET) return input.acceptToken(61, countWhile(input, 0, (c) => c === OPEN_BRACKET));
				if (next === OPEN_BRACE) return openPlugin(input, 64);
				return openLink(input);
			case TILDE: return escape(input);
			case BACKSLASH:
				if (next === BACKSLASH) return input.acceptToken(62, input.peek(2) === BACKSLASH ? 3 : 2);
				break;
			case PERCENT:
				if (next === PERCENT) {
					const inlineMarkup = matchInlineDeclaration(lineText(input, 0));
					if (inlineMarkup > 0 && inlineMarkupEnd(input, inlineMarkup) >= 0) return input.acceptToken(5, inlineMarkup);
					if (opensStyle(input, 2)) return input.acceptToken(mayNest(stack, false) ? 55 : 114, 2);
					const style = openElement(stack.context, [STYLE, DIV]);
					return style ? close(input, stack, style, 57, 2) : input.acceptToken(58, 2);
				}
				break;
			case SLASH$1:
				if (next === PERCENT) {
					const style = openElement(stack.context, [STYLE, DIV]);
					if (!style) break;
					if (opensStyle(input, 2) && mayNest(stack, false)) return input.acceptToken(55, 2);
					return close(input, stack, style, 57, 2);
				}
				break;
			case AMPERSAND: {
				const length = countWhile(input, 1, (c) => isWordChar$1(c) || c === HASH);
				if (length > 0 && input.peek(1 + length) === SEMICOLON) return input.acceptToken(63, length + 2);
				break;
			}
			case COLON$1:
				if (stack.context.kind === TERM) return unitMark(input, stack, 14, 1);
				break;
			case PIPE:
				if (stack.context.kind === TABLE) return next === PIPE ? unitMark(input, stack, 17, 2) : unitMark(input, stack, 16, 1);
				break;
			default: {
				let length = 1;
				while (!SPECIAL.has(input.peek(length)) && input.peek(length) >= 0) length++;
				return input.acceptToken(114, length);
			}
		}
		input.acceptToken(114, 1);
	}
	/** Produces the tokens of markup bodies and of the class of style blocks. Returns whether a token was accepted. */
	function raw(input, stack) {
		if (stack.canShift(6) || stack.canShift(7) || stack.canShift(10)) markupContent(input, stack);
		else if (stack.canShift(9)) {
			const length = closingLineLength(input, 0);
			if (length < 0) return false;
			input.acceptToken(9, length);
		} else return styleSpec(input, stack);
		return true;
	}
	var wikiTokens = new _lezer_lr.ExternalTokenizer((input, stack) => {
		if (input.next < 0) {
			const top = stack.context.frame;
			if (top !== ROOT) input.acceptToken(END[top.type], 0);
			return;
		}
		switch (stack.context.frame.type) {
			case LINK: return link(input, stack);
			case PLUGIN: return plugin(input, stack);
			case CODE: return code(input);
		}
		if (!stack.canShift(114) && raw(input, stack)) return;
		const ch = input.next;
		if ((ch === OPEN_PAREN || input.peek(-1) === PERCENT && isLetter(ch)) && styleSpec(input, stack)) return;
		if (atLineStart(input) && lineStart(input, stack)) return;
		if (input.next === NEWLINE$1 || input.next === RETURN$1 && input.peek(1) === NEWLINE$1) return lineBreak(input, stack);
		inline(input, stack);
	}, { contextual: true });
	//#endregion
	//#region src/wiki.grammar
	var parser$2 = _lezer_lr.LRParser.deserialize({
		version: 14,
		states: "@dO#`QROOO%WQQO'#DqO%bQQO'#DrO%lQQO'#DsO%vQQO'#DtO&OQQO'#DuO'xQQO'#DvO(QQQO'#DwO(QQQO'#DzO(QQQO'#D{O(zQQO'#D|O)mQQO'#EPO)wQQO'#EQO)|QQO'#EROOQQ'#F['#F[OOQQ'#FV'#FVO*RQRO'#DpO*]QRO'#ESO*gQRO'#EUO*tQRO'#ETO,mQRO'#EVO,wQRO'#E[O-[QRO'#E]OOQQ'#Ei'#EiO-oQRO'#EZO.QQRO'#EYO.]QQO'#E^O.eQRO'#E_O.sQRO'#E_O/OQQO'#E`OOQQ'#FU'#FUO1QQRO'#FUO1XQRO'#ElO1cQRO'#FTO1cQRO'#FTQOQQOOOOQQ'#FW'#FWOOQQ'#Ea'#EaO1kQQO,5:]OOQQ,5:],5:]O1uQQO,5:^OOQQ,5:^,5:^O2PQQO,5:_OOQQ,5:_,5:_OOQQ,5:`,5:`O2ZQQO,5:`OOQQ'#FX'#FXO2`QQO'#FXO4`QQO,5:aOOQQ,5:b,5:bO4jQQO,5:bOOQQ'#FZ'#FZOOQQ'#FY'#FYO4oQQO'#FYO4}QQO'#FYO5SQQO,5:cO5XQQO,5:fO5^QQO,5:gO5cQQO'#D}OOQQ'#F]'#F]OOQQ'#Ec'#EcO5hQQO,5:hOOQQ,5:h,5:hO6TQQO,5:hO5hQQO,5:hO6[QQO,5:hOOQQ'#Ee'#EeO6aQQO,5:kOOQQ,5:k,5:kO6aQQO,5:kO6|QQO,5:lO7WQQO,5:mOOQQ'#Eg'#EgO7bQRO,5:[O7lQRO,5:nO7vQRO,5:pO8TQQO'#EhO8YQRO,5:oO8eQRO'#EWO:`QRO,5:qO:gQRO,5:qO:rQRO,5:vO;VQRO,5:wOOQQ-E8g-E8gO;jQQO'#EjO;rQRO,5:tOOQQ,5:x,5:xO;}QQO,5:xOOQQ,5:y,5:yO<SQRO,5:yO<_QQO,5:zOOQQ,5;p,5;pOOQQ,5;W,5;WOOQQ-E8j-E8jO<mQRO,5;oOOQQ-E8_-E8_OOQQ1G/w1G/wOOQQ1G/x1G/xOOQQ1G/y1G/yOOQQ1G/z1G/zOOQQ,5;s,5;sO<uQQO1G/{OOQQ1G/{1G/{OOQQ1G/|1G/|O=PQQO,5;tO=[QQO,5;tO=dQQO,5;tOOQQ1G/}1G/}OOQQ1G0Q1G0QOOQQ1G0R1G0RO=rQQO,5:iOOQQ-E8a-E8aOOQQ1G0S1G0SO>uQQO1G0SOOQQ'#Ed'#EdO>|QQO'#EOO?TQQO1G0SO?YQQO1G0SO?YQQO1G0SOOQQ-E8c-E8cOOQQ1G0V1G0VO?dQQO1G0VOOQQ'#Ef'#EfO?kQQO1G0WOOQQ1G0W1G0WO?kQQO1G0WO?rQQO1G0XOOQQ1G0X1G0XO?yQQO1G0XOOQQ-E8e-E8eOOQQ,5;S,5;SOOQQ-E8f-E8fO@OQRO,5:rO@]QRO'#EXOOQQ1G0]1G0]OBTQRO1G0]OOQQ,5;U,5;UOOQQ-E8h-E8hOOQQ1G0d1G0dOOQQ1G0e1G0eOCuQQO'#EkODrQQO1G0fOD}QRO1G0fOOQQ1G0f1G0fODrQQO1G0fOOQQ7+%g7+%gOEXQQO'#DyOOQQ'#Eb'#EbOE^QQO'#DxOOQQ1G1`1G1`OEiQQO1G1`OEtQQO1G1`OOQQ1G0T1G0TOOQQ7+%n7+%nOE|QQO7+%nOOQQ-E8b-E8bOFRQQO7+%nOFYQQO7+%nOOQQ7+%q7+%qOOQQ-E8d-E8dOOQQ7+%r7+%rOFdQQO7+%rOOQQ7+%s7+%sOFkQQO7+%sOFuQRO,5:sOOQQ7+%w7+%wOGPQQO'#DpOG^QQO'#ESOGkQQO'#EUOG{QQO'#ETOHZQQO'#EVOHhQQO'#E[OIOQQO'#E]OIfQQO'#EZOIzQQO'#EYOJYQQO'#E_OJkQQO'#E_OOQQ,5;V,5;VOJyQQO'#FUOOQQ-E8i-E8iOKTQRO7+&QOOQQ7+&Q7+&QOK_QRO7+&QOKiQQO7+&QOKtQQO,5:eOOQQ-E8`-E8`OOQQ7+&z7+&zOLSQQO7+&zOOQQ<<IY<<IYOL_QQO<<IYOLdQQO<<IYOOQQ<<I^<<I^OLkQQO<<I_OOQQ<<I_<<I_OLkQQO<<I_OLrQQO,5:[OMPQQO,5:nOM^QQO,5:pOMnQQO,5:oOM|QQO'#EWON^QQO,5:qONhQQO,5:qONvQQO,5:vO! ^QQO,5:wO! tQQO,5:tO!!SQQO,5:yO!!bQRO<<IlO!!bQRO<<IlO!!lQRO<<IlOOQQ<<Il<<IlOOQQ1G0P1G0POOQQ<<Jf<<JfOOQQAN>tAN>tO!!vQQOAN>tOOQQAN>yAN>yO!!{QQOAN>yO!#SQQO,5:rO!#dQQO'#EXO!#qQQO1G0]O!#{QQO1G0fO!$YQROAN?WO!$YQROAN?WOOQQG24`G24`OOQQG24eG24eO!$dQQO,5:sO!$qQQO7+&QO!%OQQO7+&QO!%]QROG24rO!%gQQO<<IlO!%gQQO<<IlO!%tQQO<<IlO!&RQQOAN?WO!&RQQOAN?WO!&`QQOG24rO!&mQQO'#EhO!&rQQO'#EjO!&zQQO,5:zO!'YQQO1G0fO!'YQQO1G0fO!'eQQO7+&QO/OQQO'#E`",
		stateData: "!'p~O#rOS~OP_OQ_ORkOSlOTUOZaO[bO]dO_oO`eOafObPOdQOfROhjOiSOlVOmWOnXOvYOwZOx[Oy]O!XTO!YmO![_O!__O!`_O!a_O!b_O#f_O#lPO#mQO~O#bpO#a#wP~PVOP_OQ_OTUObPOdQOfROiSOlVOmWOnXOvYOwZOx[Oy]O!XTO![_O!__O!`_O!a_O!b_O#ctO#f_O#lPO#mQO~OcwO#gwO~P#jOeyO#hyO~P#jOg{O#i{O~P#jOj}Ok|O~O!]!PO!^!OOP#{PQ#{PT#{Pb#{Pd#{Pf#{Pi#{Pl#{Pm#{Pn#{Pv#{Pw#{Px#{Py#{P!X#{P!Z#{P![#{P!_#{P!`#{P!a#{P!b#{P#c#{P#f#{P#j#{P#l#{P#m#{P~OW!SOY!RO~Oo!VOq!WOr!TOs!TOp#|P~OvYOwZOx[Oy]O|!]O!Q![O#q!]O~Oz!`O{!cO!P!bO#s!aO~P(cOvYOwZOx[Oy]O#q!dO~Oz!fO!U!gO~P)[O}!hO~O!O!iO~O#a!dX#b!dX~P#jO#a!vX#b!vX~P#jO#a!xX#b!xX#d!xX~P#jO#d!nO#a!wX#b!wX~OP_OQ_OTUObPOdQOfROiSOlVOmWOnXOvYOwZOx[Oy]O!XTO![_O!__O!`_O!a_O!b_O#f_O#lPO#mQO#b!yX~O^!qO#a!yX~P+PO`#OXa#OX#a#OX#b#OX#e#OX~P#jO`#PXa#PX#a#PX#b#PX#e#PX~P#jO`eOafO#a!}X#b!}X#e!}X~O#e!vO#a!|X#b!|X~Oj!yOk!xO~OU!{OX!zO#a#RX#b#RX~OV!zO#a#RX#b#RX~O!]!PO!^!OO!Z#{P#b#{P#k#{P#u#{P~OP_OQ_OTUObPOdQOfROiSOlVOmWOnXOvYOwZOx[Oy]O!XTO![_O!__O!`_O!a_O!b_O#f_O#lPO#mQO#b#xX~O#a#xX~P/dO#a#`X#b#`X~PVO#bpO#a#wX~Oc#SO#g#SO~P#jOe#TO#h#TO~P#jOg#UO#i#UO~P#jOk#VO~O!^#WOP#{XQ#{XT#{Xb#{Xd#{Xf#{Xi#{Xl#{Xm#{Xn#{Xv#{Xw#{Xx#{Xy#{X!X#{X!Z#{X![#{X!_#{X!`#{X!a#{X!b#{X#c#{X#f#{X#j#{X#l#{X#m#{X#b#{X#k#{X#u#{X~O!Z#YO#j#YO~P#jOY#ZO~Oo#[Or!TOs!TOp#|X~Oo#^O~Op#_O~Op#`O~Op#aO~O#p#bO~Oz#dO#s#eO~P(cOvYOwZOx[Oy]Oz#dO~O#t#fO~P5rO!P#jO~Oz#lO~P)[OvYOwZOx[Oy]O!W#nO#q#nO~Oz#pO!V#qO~P6hOz#sO!R#tO~P)[O#a!da#b!da~P#jO#a!va#b!va~P#jO#a!xa#b!xa#d!xa~P#jO[bO~O#d!nO#a!wa#b!wa~O^!zX#a!zX#b!zX~P#jOP_OQ_OTUObPOdQOfROiSOlVOmWOnXOvYOwZOx[Oy]O!XTO![_O!__O!`_O!a_O!b_O#f_O#lPO#mQO#b!ya~O#a!ya~P8rO^#{O#a!ya#b!ya~O`#Oaa#Oa#a#Oa#b#Oa#e#Oa~P#jO`#Paa#Pa#a#Pa#b#Pa#e#Pa~P#jO`eOafO~O#e!vO#a!|a#b!|a~Ok$OO~OX$PO#a#Ra#b#Ra~O!Z$SO#b$QO#k$TO#u$UO~O#bpO#a#wa~O!Z$VO#j$VO~P#jOt$WO#o$XOp#|a~Oo$[Op#|a~Oo$[Or!TOs!TOp#|a~O!S$^Ov!qaw!qax!qay!qaz!qa|!qa!Q!qa#q!qa#s!qa~OvYOwZOx[Oy]O#t#fO~Oz$_O~P>dOz!rX~P>dOz$_O~Oz$_O#s$bO~P(cOz$dO~P)[Oz$fO~P6hOz$hO~P)[O#p$iO~O^!za#a!za#b!za~P#jO#a!{X#b!{X~P#jOP_OQ_OTUObPOdQOfROiSOlVOmWOnXOvYOwZOx[Oy]O!XTO![_O!__O!`_O!a_O!b_O#f_O#lPO#mQO#b!yi~O#a!yi~P@gOP_OQ_OTUObPOdQOfROiSOlVOmWOnXOvYOwZOx[Oy]O!XTO![_O!__O!`_O!a_O!b_O#f_O#lPO#mQO~OR$uOS$vOZ$mO[$nO]$pO_$xO`$qOa$rOhjO!Y&YO!Z#_X#b#_X#k#_X~PB[O!Z$zO#b$QO#k${O~O#a#Si#b#Si~PB[O#n%OO~Ot$WO#o$XOp!lX~Ot$WO#o$XOp#|i~Oo%ROp#|i~Oz%SO~Oz%SO~P>dOz%SO#s%UO~P(cOz%VO~P6hOz%XO!T%YO~P)[O#a!{a#b!{a~P#jO!Z!dX#b!dX#k!dX~P#jO!Z!vX#b!vX#k!vX~P#jO!Z!xX#b!xX#d!xX#k!xX~P#jO#d&SO!Z!wX#b!wX#k!wX~O^%`O!Z!yX#k!yX~P+PO`#OXa#OX!Z#OX#b#OX#e#OX#k#OX~P#jO`#PXa#PX!Z#PX#b#PX#e#PX#k#PX~P#jO`$qOa$rO!Z!}X#b!}X#e!}X#k!}X~O#e&TO!Z!|X#b!|X#k!|X~OU%eOX!zO!Z#RX#b#RX#k#RX~OV!zO!Z#RX#b#RX#k#RX~O!Z#xX#k#xX~P/dO#a#Sq#b#Sq~PB[O#a#Sq#b#Sq~P#jO!Z%hO#b$QO#k%iO~Ou%jOp!mat!ma#o!ma~Ot$WO#o$XOp#|q~Oz%lO~Oz%lO~P>dOz%nO~P)[O!Z!da#b!da#k!da~P#jO!Z!va#b!va#k!va~P#jO!Z!xa#b!xa#d!xa#k!xa~P#jO#d&SO!Z!wa#b!wa#k!wa~O^!zX!Z!zX#b!zX#k!zX~P#jO!Z!ya#k!ya~P8rO^%rO!Z!ya#b!ya#k!ya~O`#Oaa#Oa!Z#Oa#b#Oa#e#Oa#k#Oa~P#jO`#Paa#Pa!Z#Pa#b#Pa#e#Pa#k#Pa~P#jO#e&TO!Z!|a#b!|a#k!|a~OX$PO!Z#Ra#b#Ra#k#Ra~O#a#Sy#b#Sy~P#jO#a#Sy#b#Sy~PB[Oz%vO~Oz%wO~P)[O^!za!Z!za#b!za#k!za~P#jO!Z!{X#b!{X#k!{X~P#jO!Z!yi#k!yi~P@gO!Z#Si#b#Si#k#Si~PB[O#a#S!R#b#S!R~P#jO!Z!{a#b!{a#k!{a~P#jO!Z#Sq#b#Sq#k#Sq~PB[O!Z#Sq#b#Sq#k#Sq~P#jO#a#S!Z#b#S!Z~P#jO!Z#Sy#b#Sy#k#Sy~P#jO!Z#Sy#b#Sy#k#Sy~PB[O!Z#S!R#b#S!R#k#S!R~P#jO!Z#S!Z#b#S!Z#k#S!Z~P#jO[$nO~O`$qOa$rO~O!Z%sO#b$QO#k$TO#u&WO~O!Z%yO#b$QO#k${O~O!Z&OO#b$QO#k%iO~O",
		goto: "4{$QPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP$R$[$[$[$[$[$[$[%q%z$[$[&Q(Q(Y&Q&Q&Q(f(f(k(f(v(|(f)U)a)a)j(f(f)q*T*]*k*t+[+f-[-f-s-}.aPPPPPPPPPPPPPPPPPPPPPP.k.n.w0v1|2V2`2k4sUnOp$QT!}o$x#w_OPQR`abdefopvxz!Q!k!l!m!p!q!s!t#X#x#y#{$Q$S$j$l$m$n$p$q$r$x$z$|%Z%[%]%_%`%b%c%f%g%h%p%q%r%s%t%u%x%y%z%{%|%}&O&P&Q&RQ$Z#[Q%Q$[R%k%RX$X#[$Y$[%R${^OPQRYZ`abdefopvxz!Q!_!a!b!e!g!h!i!k!l!m!p!q!s!t#X#e#g#i#j#m#o#q#r#x#y#{$Q$S$b$c$g$i$j$l$m$n$p$q$r$x$z$|%U%W%Y%Z%[%]%_%`%b%c%f%g%h%o%p%q%r%s%t%u%x%y%z%{%|%}&O&P&Q&R]!]Y!_!b#i#j$cQ#h!aQ$`#eQ%T$bR%m%UVnOp$QScOpS#v!n&SR$o$QQ!rdR%a$pS#z!q%`T$k#{%rSiOpS#|!v&TR$t$Q_gOhp!v$Q$s&TSoOpR$x$QQvPQxQQzRW#Rvxz#XR#X!QU$Y#[$[%RR%P$YQ!_YU#c!_#i$cQ#i!bR$c#jW#g!a#e$b%UR$a#gQ!eZY#k!e#m#r%W%oQ#m!gQ#r!iQ%W$iR%o%YQ#o!hS$e#o$gR$g#qQ!k`Q!laQ!mbQ!seQ!tfx#u!k!l!m!s!t#x$j%Z%[%]%b%c%g%p%t%x%{%}&P&RQ#x!pQ$j#yQ%Z$lQ%[$mQ%]$nQ%b$qQ%c$rQ%g$|Q%p%_Q%t%fQ%x%qQ%{%uQ%}%zQ&P%|R&R&QQ!ocS#w!o%^R%^$oUhOp!vS!uh$sT$s$Q&TQ!wiS#}!w%dR%d$tQ$R!|W$y$R$}&V&XQ$}$UQ&V&UR&X&WQqOS#Pq#QR#QrRsOQrOQ#OpR$w$QU`Oop#StPQR`abefvxz!Q!k!l!m!p!s!t#X#x#y$j$l$m$n$q$r$|%Z%[%]%_%b%c%f%g%p%q%t%u%x%z%{%|%}&P&Q&RQ!pdS#y!q#{S$l$Q$xQ$|$SQ%_$pQ%f$zS%q%`%rQ%u%hQ%z%sQ%|%yR&Q&O`uPQRvxz!Q#X!s!j`abef!k!l!m!p!s!t#x#y$j$l$m$n$q$r$|%Z%[%]%_%b%c%f%g%p%q%t%u%x%z%{%|%}&P&Q&RQ!QTQ!|mR&U&YQ!XVQ!YWR!ZXU!UVWXQ#]!VR$]#^#v_OPQR`abdefopvxz!Q!k!l!m!p!q!s!t#X#x#y#{$Q$S$j$l$m$n$p$q$r$x$z$|%Z%[%]%_%`%b%c%f%g%h%p%q%r%s%t%u%x%y%z%{%|%}&O&P&Q&R[!]Y!_!b#i#j$cd!dZ!e!g!i#m#r$i%W%Y%oY#f!a#e#g$b%UX#n!h#o#q$g]!^Y!_!b#i#j$c",
		nodeNames: "⚠ BoldMark ItalicMark MarkupOpen MarkupOpen MarkupOpen MarkupContent MarkupContent MarkupContent MarkupClose MarkupClose HeadingMark ListMark DefinitionMark DescriptionMark HorizontalRule CellMark HeaderMark BoldMark BoldMark ItalicMark ItalicMark MonospaceMark MonospaceMark CodeMark CodeMark CodeText CodeMark LinkMark LinkMark LinkMark LinkMark LinkMark LinkText LinkTarget URL AttributeName AttributeValue PluginMark PluginMark PluginMark PluginMark PluginMark PluginKeyword PluginKeyword PluginKeyword PluginKeyword PluginName ParameterName ParameterName ParameterValue ParameterValue VariableName Permission Principal StyleMark StyleMark StyleMark StyleMark StyleClass StyleDeclaration Escape LineBreak Entity Unclosed Document Paragraph Bold Italic Monospace InlineCode Style InlineMarkup Link LinkAttributes Attribute Footnote FootnoteReference Plugin Parameter PluginBody Variable AccessRule Metadata Heading List ListItem Definition DefinitionTerm DefinitionDescription Table TableRow TableCell TableHeader Preformatted Markup StyleBlock",
		maxTerm: 139,
		context: lineContext,
		skippedNodes: [0],
		repeatNodeCount: 12,
		tokenData: "RORO",
		tokenizers: [0, wikiTokens],
		topRules: { "Document": [0, 65] },
		tokenPrec: 0
	});
	//#endregion
	//#region src/content-tokens.js
	var NEWLINE = 10;
	var RETURN = 13;
	var SPACE = 32;
	var TAB = 9;
	var AT = 64;
	var SLASH = 47;
	var COLON = 58;
	var EQUALS = 61;
	function isBlank(ch) {
		return ch === SPACE || ch === TAB;
	}
	function isSpace(ch) {
		return isBlank(ch) || ch === NEWLINE || ch === RETURN;
	}
	function isWordChar(ch) {
		return ch >= 48 && ch <= 57 || ch >= 65 && ch <= 90 || ch >= 97 && ch <= 122 || ch === 95 || ch >= 192;
	}
	/** Returns the length of the annotation name "@name" with an optional ":" or "=" at the offset, or -1. */
	function annotationNameLength(input, offset) {
		if (input.peek(offset) !== AT || !isWordChar(input.peek(offset + 1))) return -1;
		let i = offset + 2;
		while (isWordChar(input.peek(i))) i++;
		const ch = input.peek(i);
		if (ch >= 0 && ch !== COLON && ch !== EQUALS && !isSpace(ch)) return -1;
		let j = i;
		while (isBlank(input.peek(j))) j++;
		if (input.peek(j) === COLON || input.peek(j) === EQUALS) i = j + 1;
		return i - offset;
	}
	/** Returns whether the line starting at the offset is an annotation line. */
	function isAnnotationLine(input, offset) {
		let i = offset;
		while (isBlank(input.peek(i))) i++;
		return annotationNameLength(input, i) > 0;
	}
	/** Returns the offset of the next annotation line start after the offset, or the end of the input. */
	function nextAnnotationLine(input, offset) {
		for (let i = offset;; i++) {
			const ch = input.peek(i);
			if (ch < 0) return i;
			if (ch === NEWLINE && isAnnotationLine(input, i + 1)) return i + 1;
		}
	}
	function countBlanks(input, offset) {
		let i = offset;
		while (isBlank(input.peek(i))) i++;
		return i - offset;
	}
	function lastNonSpace(input, from, to) {
		let end = to;
		while (end > from && isSpace(input.peek(end - 1))) end--;
		return end;
	}
	/** Returns whether the offset is in the indentation of an annotation line, which ends at its name. */
	function inAnnotationIndent(input, offset) {
		let i = offset - 1;
		while (isBlank(input.peek(i))) i--;
		return input.peek(i) === NEWLINE && isAnnotationLine(input, offset);
	}
	var bodyTokens = new _lezer_lr.ExternalTokenizer((input, stack) => {
		if (input.next < 0) return;
		if (inAnnotationIndent(input, 0)) {
			const indent = countBlanks(input, 0);
			if (indent > 0) return input.acceptToken(9, indent);
			return input.acceptToken(2, annotationNameLength(input, 0));
		}
		if (stack.canShift(1)) {
			const indent = countBlanks(input, 0);
			if (input.peek(indent) === SLASH && input.peek(indent + 1) === SLASH) {
				if (indent > 0) return input.acceptToken(9, indent);
				let i = 0;
				while (input.peek(i) >= 0 && input.peek(i) !== NEWLINE && input.peek(i) !== RETURN) i++;
				return input.acceptToken(1, i);
			}
		}
		const end = nextAnnotationLine(input, 0);
		let blank = 0;
		while (blank < end && isSpace(input.peek(blank))) blank++;
		if (stack.canShift(8)) return input.acceptToken(8, end);
		if (blank > 0) return input.acceptToken(9, blank);
		input.acceptToken(3, lastNonSpace(input, 0, end));
	}, { contextual: true });
	//#endregion
	//#region src/content.grammar
	var parser$1 = _lezer_lr.LRParser.deserialize({
		version: 14,
		states: "!QQVQQOOObQRO'#CaOOQQ'#Cb'#CbQmQQOOQmQQOOQYQQOOOOQQ,58{,58{OOQQ-E6`-E6`",
		stateData: "r~OXOS~OPTOQPOWSO~ORUOQTXVTX~OQPO~O",
		goto: "fVPPPPPW^XQORSTUROSTRVR",
		nodeNames: "⚠ DeclarationComment AnnotationName AnnotationValue MarkupBody Annotation",
		maxTerm: 10,
		skippedNodes: [0],
		repeatNodeCount: 1,
		tokenData: "RORO",
		tokenizers: [0, bodyTokens],
		topRules: { "MarkupBody": [0, 4] },
		tokenPrec: 0
	});
	//#endregion
	//#region src/registry.js
	/** Contributions of markups to the editor, keyed by the markup name in lower case. */
	var markups = /* @__PURE__ */ new Map();
	/**
	* Registers what the markup with the given name contributes to the editor. The name is matched case insensitively
	* against the name in the `%%Name` declaration. All fields are optional.
	*
	* The `parser` is the Lezer parser for the body of the markup. It receives the text between the declaration and the
	* closing line, starting with the rest of the declaration line, or the content of a single line or inline markup.
	* Markups without a parser use the default body parser with annotation highlighting.
	*
	* The function `complete(block, context)` offers completions in the body of the markup. It returns a
	* `CompletionResult`, null or a promise of either. The block describes the markup at the caret as `markupBlock`
	* does. The context holds `pos`, `explicit`, `aborted`, `addEventListener` and `matchBefore` like a
	* `CompletionContext`. Positions in the context and in the result are relative to the start of the body. Only the
	* `apply` functions of the options get document positions, since they act on the editor.
	*
	* The `highlight` style colors the nodes of the parser in place of the editor's style. Scope it to the language
	* (`HighlightStyle.define(specs, {scope: language})`) and register `language.parser`, which marks the embedded trees
	* with that language. Properties the style leaves out still come from the editor's style.
	*
	* Call this at the top level of a module script declared in the plugin.xml of the markup. Module scripts run before
	* the document is loaded, and editors are created on that event or later, so every registration precedes the first
	* parse. A second registration for the same name replaces the first.
	*/
	function registerMarkup(name, { parser = null, complete = null, highlight = null } = {}) {
		markups.set(name.toLowerCase(), {
			parser,
			complete,
			highlight
		});
	}
	function unregisterMarkup(name) {
		markups.delete(name.toLowerCase());
	}
	/** Returns the contribution registered for the markup name, or null. */
	function getMarkup(name) {
		return markups.get(name.toLowerCase()) ?? null;
	}
	/** Returns the distinct highlight styles of all registered markups. */
	function markupHighlightStyles() {
		return [...new Set([...markups.values()].map((markup) => markup.highlight).filter(Boolean))];
	}
	/** Returns the name of the markup declared by the given opening, without "%%" and separator. */
	function markupName(open, input) {
		return input.read(open.from, open.to).trim().replace(/^%%/, "").replace(/[\s:=]*$/, "");
	}
	//#endregion
	//#region src/block.js
	/** The title of the article the edited text belongs to, null if unknown. */
	var articleTitle = _codemirror_state.Facet.define({ combine: (values) => values.length ? values[0] : null });
	function annotations(body, text) {
		const result = [];
		if (!body || body.type.name !== "MarkupBody") body = parser$1.parse(text);
		for (let node = body.topNode.firstChild; node; node = node.nextSibling) {
			if (node.name !== "Annotation") continue;
			const name = node.getChild("AnnotationName");
			const value = node.getChild("AnnotationValue");
			result.push({
				name: text.slice(name.from, name.to).replace(/^@/, "").replace(/[\s:=]*$/, ""),
				value: value ? text.slice(value.from, value.to) : "",
				from: node.from,
				to: node.to,
				valueFrom: value ? value.from : node.to,
				valueTo: value ? value.to : node.to
			});
		}
		return result;
	}
	/**
	* Describes the markup block whose body contains the position, or returns null. The description holds the markup
	* `name` as declared, the body `text` with its document range `from` and `to`, the `annotations` of the body (name,
	* value and the ranges `from`, `to`, `valueFrom`, `valueTo`), the Lezer `tree` of the body if the markup registered
	* a parser, and the `article` title of the editor. Positions of annotations and tree are relative to the body.
	*/
	function markupBlock(state, pos) {
		let markup = (0, _codemirror_language.syntaxTree)(state).resolveInner(pos, -1);
		while (markup && markup.name !== "Markup" && markup.name !== "InlineMarkup") markup = markup.parent;
		if (!markup) return null;
		const cursor = markup.cursor(_lezer_common.IterMode.IgnoreMounts);
		if (!cursor.firstChild() || cursor.name !== "MarkupOpen") return null;
		const name = markupName(cursor, { read: (from, to) => state.sliceDoc(from, to) });
		let from = cursor.to, to = markup.to, mounted = null;
		while (cursor.nextSibling()) if (cursor.name === "MarkupContent") {
			from = cursor.from;
			to = cursor.to;
			mounted = cursor.tree?.prop(_lezer_common.NodeProp.mounted)?.tree ?? null;
		} else if (cursor.name === "MarkupClose") to = Math.min(to, cursor.from);
		if (pos < from || pos > to) return null;
		const text = state.sliceDoc(from, to);
		const parser = getMarkup(name)?.parser;
		return {
			name,
			text,
			from,
			to,
			annotations: annotations(mounted, text),
			tree: parser && mounted ? mounted : null,
			article: state.facet(articleTitle)
		};
	}
	function shiftResult(result, offset) {
		if (!result) return null;
		const { map, ...shifted } = result;
		shifted.from = result.from + offset;
		if (result.to != null) shifted.to = result.to + offset;
		if (typeof result.validFor === "function") {
			const validFor = result.validFor;
			shifted.validFor = (text, from, to, state) => validFor(text, from - offset, to - offset, state);
		}
		return shifted;
	}
	function anchorAtEnd(expr) {
		return expr.source.endsWith("$") ? expr : new RegExp("(?:" + expr.source + ")$", expr.flags);
	}
	/**
	* Completion source that offers the completions of the markup at the caret. It hands the markup block and a body
	* relative context to the `complete` function the markup registered and moves the result into the document.
	*/
	function completeMarkup(context) {
		const block = markupBlock(context.state, context.pos);
		if (!block) return null;
		const complete = getMarkup(block.name)?.complete;
		if (!complete) return null;
		const offset = block.from;
		const result = complete(block, {
			pos: context.pos - offset,
			explicit: context.explicit,
			get aborted() {
				return context.aborted;
			},
			addEventListener: (type, listener, options) => context.addEventListener(type, listener, options),
			matchBefore(expr) {
				const line = context.state.doc.lineAt(context.pos);
				const start = Math.max(line.from, offset, context.pos - 250);
				const text = context.state.sliceDoc(start, context.pos);
				const found = text.search(anchorAtEnd(expr));
				return found < 0 ? null : {
					from: start + found - offset,
					to: context.pos - offset,
					text: text.slice(found)
				};
			}
		});
		if (result && typeof result.then === "function") return result.then((resolved) => context.aborted ? null : shiftResult(resolved, offset));
		return shiftResult(result, offset);
	}
	//#endregion
	//#region src/index.js
	/** Parser for the default body of a markup block with its content, a declaration comment and annotations. */
	var markupContentParser = parser$1.configure({ props: [(0, _lezer_highlight.styleTags)({
		DeclarationComment: _lezer_highlight.tags.lineComment,
		AnnotationName: _lezer_highlight.tags.annotation,
		AnnotationValue: _lezer_highlight.tags.attributeValue
	})] });
	function foldBlock(node, state) {
		const first = state.doc.lineAt(node.from);
		const last = node.lastChild;
		const end = last && (last.name === "MarkupClose" || last.name === "StyleMark" || last.name === "CodeMark") ? state.doc.lineAt(last.from).from - 1 : node.to;
		return end > first.to ? {
			from: first.to,
			to: end
		} : null;
	}
	/** Parser for JSPWiki markup, embedding registered parsers into the bodies of markup blocks. */
	var parser = parser$2.configure({
		props: [(0, _lezer_highlight.styleTags)({
			"Heading/...": _lezer_highlight.tags.heading,
			"HeadingMark ListMark DefinitionMark DescriptionMark": _lezer_highlight.tags.list,
			"BoldMark ItalicMark MonospaceMark CodeMark": _lezer_highlight.tags.processingInstruction,
			"Bold/...": _lezer_highlight.tags.strong,
			"Italic/...": _lezer_highlight.tags.emphasis,
			"Monospace/... InlineCode/... Preformatted/...": _lezer_highlight.tags.monospace,
			"TableHeader/...": _lezer_highlight.tags.strong,
			"CellMark HeaderMark": _lezer_highlight.tags.punctuation,
			"Link/... Footnote/... FootnoteReference/...": _lezer_highlight.tags.link,
			URL: _lezer_highlight.tags.url,
			LinkMark: _lezer_highlight.tags.punctuation,
			"LinkAttributes/...": _lezer_highlight.tags.meta,
			"Plugin/... Variable/... AccessRule/... Metadata/...": _lezer_highlight.tags.processingInstruction,
			PluginName: _lezer_highlight.tags.macroName,
			PluginMark: _lezer_highlight.tags.punctuation,
			"StyleMark StyleDeclaration": _lezer_highlight.tags.meta,
			StyleClass: _lezer_highlight.tags.className,
			Escape: _lezer_highlight.tags.escape,
			Entity: _lezer_highlight.tags.character,
			"LineBreak HorizontalRule": _lezer_highlight.tags.contentSeparator,
			"MarkupOpen MarkupClose": _lezer_highlight.tags.tagName,
			Unclosed: _lezer_highlight.tags.invalid
		}), _codemirror_language.foldNodeProp.add({
			"Markup StyleBlock Preformatted": foldBlock,
			Plugin(node, state) {
				const first = state.doc.lineAt(node.from);
				return node.to - 2 > first.to ? {
					from: first.to,
					to: node.to - 2
				} : null;
			}
		})],
		wrap: (0, _lezer_common.parseMixed)((node, input) => {
			if (node.name !== "MarkupContent") return null;
			const open = node.node.prevSibling;
			if (!open || open.name !== "MarkupOpen") return null;
			return { parser: getMarkup(markupName(open, input))?.parser ?? markupContentParser };
		})
	});
	var wikiLanguage = _codemirror_language.LRLanguage.define({
		name: "jspwiki",
		parser,
		languageData: { closeBrackets: { brackets: [
			"(",
			"[",
			"{"
		] } }
	});
	/** Returns the level of the heading node, 3 for the largest heading "!!!" and 1 for the smallest "!". */
	function headingLevel(heading) {
		const mark = heading.getChild("HeadingMark");
		return mark ? mark.to - mark.from : 1;
	}
	/**
	* Returns the end of the section of the heading node, the start of the next heading of the same or a larger level in
	* the same block, or the end of that block. Returns -1 if the section may continue beyond the parsed text.
	*/
	function sectionEnd(heading, treeLength, docLength) {
		const level = headingLevel(heading);
		for (let next = heading.nextSibling; next; next = next.nextSibling) {
			if (next.name === "Heading" && headingLevel(next) >= level) return next.from;
			if (next.name === "StyleMark") return next.from;
		}
		const end = heading.parent.to;
		return end >= treeLength && treeLength < docLength ? -1 : end;
	}
	function foldSection(state, lineStart, lineEnd) {
		const tree = (0, _codemirror_language.syntaxTree)(state);
		let heading = tree.resolve(lineStart, 1);
		while (heading && heading.name !== "Heading") heading = heading.parent;
		if (!heading || heading.from !== lineStart) return null;
		let end = sectionEnd(heading, tree.length, state.doc.length);
		if (end < 0) {
			const mark = "!".repeat(headingLevel(heading));
			const first = state.doc.lineAt(tree.length);
			let pos = first.from;
			for (const lines = state.doc.iterLines(first.number); !lines.next().done;) {
				if (lines.value.startsWith(mark)) break;
				pos += lines.value.length + 1;
			}
			end = Math.min(pos, state.doc.length);
		}
		const from = Math.max(lineEnd, state.doc.lineAt(heading.to).to);
		while (end > from && /\s/.test(state.doc.sliceString(end - 1, end))) end--;
		return end > from ? {
			from,
			to: end
		} : null;
	}
	/** The CodeMirror language support for JSPWiki pages. */
	function wiki() {
		const styles = markupHighlightStyles().map((style) => _codemirror_state.Prec.high((0, _codemirror_language.syntaxHighlighting)(style)));
		return new _codemirror_language.LanguageSupport(wikiLanguage, [_codemirror_language.foldService.of(foldSection), ...styles]);
	}
	//#endregion
	exports.articleTitle = articleTitle;
	exports.completeMarkup = completeMarkup;
	exports.defineMarkups = defineMarkups;
	exports.getMarkup = getMarkup;
	exports.headingLevel = headingLevel;
	exports.markupBlock = markupBlock;
	exports.markupContentParser = markupContentParser;
	exports.markupName = markupName;
	exports.parser = parser;
	exports.registerMarkup = registerMarkup;
	exports.sectionEnd = sectionEnd;
	exports.unregisterMarkup = unregisterMarkup;
	exports.wiki = wiki;
	exports.wikiLanguage = wikiLanguage;
	return exports;
})({}, CodeMirror.state, CodeMirror.lezer.common, CodeMirror.lezer.highlight, CodeMirror.language, CodeMirror.lezer.lr);
