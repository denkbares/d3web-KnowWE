var KnowWEWikiLanguage = (function(exports, common, highlight, language, lr) {
  "use strict";
  const newline = 110, softBreak = 111, itemBreak = 112, rowBreak = 113, text = 114, boldEnd = 115, italicEnd = 116, monospaceEnd = 117, styleEnd = 118, divEnd = 119, boldRestart = 120, italicRestart = 121, BoldToggle = 1, ItalicToggle = 2, MarkupOpen = 3, LineMarkupOpen = 4, InlineMarkupOpen = 5, blockContent = 6, lineContent = 7, inlineContent = 8, MarkupClose = 9, InlineMarkupClose = 10, HeadingMark = 11, ListMark = 12, DefinitionMark = 13, DescriptionMark = 14, HorizontalRule = 15, CellMark = 16, HeaderMark = 17, BoldOpen = 18, BoldClose = 19, ItalicOpen = 20, ItalicClose = 21, MonospaceOpen = 22, MonospaceClose = 23, PreOpen = 24, CodeOpen = 25, CodeText = 26, CodeClose = 27, LinkOpen = 28, FootnoteOpen = 29, FootnoteReferenceOpen = 30, LinkSeparator = 31, LinkClose = 32, LinkText = 33, LinkTarget = 34, URL = 35, AttributeName = 36, attributeEquals = 122, AttributeValue = 37, attributeText = 123, PluginOpen = 38, VariableOpen = 39, AccessRuleOpen = 40, MetadataOpen = 41, PluginClose = 42, InsertKeyword = 43, WhereKeyword = 44, AccessKeyword = 45, SetKeyword = 46, PluginName = 47, ParameterName = 48, MetadataName = 49, parameterEquals = 124, ParameterValue = 50, MetadataValue = 51, VariableName = 52, Permission = 53, Principal = 54, pluginText = 125, pluginSpace = 126, bodyStart = 127, bodyText$1 = 128, StyleOpen = 55, DivOpen = 56, StyleClose = 57, StrayStyleClose = 58, StyleClass = 59, StyleDeclaration = 60, styleBlank = 129, Escape = 61, LineBreak = 62, Entity = 63, Unclosed = 64;
  const FALLBACK_NAME = "[A-Za-z][\\w.\\-]*(?::[\\w.\\-]+)*";
  let blockPattern = null;
  let inlinePattern = null;
  let fallbackPattern = namePattern([FALLBACK_NAME]);
  function namePattern(names) {
    if (names.length === 0) return null;
    const sorted = [...names].sort((a, b) => b.length - a.length);
    return new RegExp("^%%(?:" + sorted.join("|") + ")(?=[ \\t:=]|$)", "i");
  }
  function defineMarkups(markups) {
    if (markups == null) {
      blockPattern = inlinePattern = null;
      return;
    }
    const block = [], inline2 = [];
    for (const markup of markups) {
      const entry = typeof markup === "string" ? { name: markup, inline: false } : markup;
      (entry.inline ? inline2 : block).push(entry.name);
    }
    blockPattern = namePattern(block);
    inlinePattern = namePattern(inline2);
  }
  function markupsDefined() {
    return blockPattern !== null || inlinePattern !== null;
  }
  const BLOCK_REST = /^[ \t]*[:=]?[ \t]*(?:\/\/.*)?$/;
  const OPEN_REST = /^[ \t]*[:=]?/;
  function matchDeclaration(line) {
    let indent = 0;
    while (line.charCodeAt(indent) === 32 || line.charCodeAt(indent) === 9) indent++;
    const declaration = indent ? line.slice(indent) : line;
    const name = markupsDefined() ? blockPattern?.exec(declaration) : fallbackPattern.exec(declaration);
    if (!name) return null;
    const rest = declaration.slice(name[0].length);
    if (BLOCK_REST.test(rest)) {
      return { block: true, length: indent + name[0].length + OPEN_REST.exec(rest)[0].length };
    }
    let i = 0;
    while (rest.charCodeAt(i) === 32 || rest.charCodeAt(i) === 9) i++;
    const separator = rest.charCodeAt(i);
    if (separator === 58 || separator === 61) {
      i++;
    } else if (i === 0 || !markupsDefined()) {
      return null;
    }
    let content = i;
    while (rest.charCodeAt(content) === 32 || rest.charCodeAt(content) === 9) content++;
    if (content >= rest.length || rest.charCodeAt(content) === 47) return null;
    return { block: false, length: indent + name[0].length + i };
  }
  function matchInlineDeclaration(text2) {
    const name = inlinePattern?.exec(text2);
    if (!name) return -1;
    return name[0].length + OPEN_REST.exec(text2.slice(name[0].length))[0].length;
  }
  const NEWLINE$1 = 10, RETURN$1 = 13, SPACE$1 = 32, TAB$1 = 9;
  const EXCLAMATION = 33, QUOTE = 34, HASH = 35, DOLLAR = 36, PERCENT = 37, AMPERSAND = 38, APOSTROPHE = 39;
  const OPEN_PAREN = 40, CLOSE_PAREN = 41, STAR = 42, DASH = 45, DOT = 46, SLASH$1 = 47, COLON$1 = 58, SEMICOLON = 59;
  const EQUALS$1 = 61, OPEN_BRACKET = 91, BACKSLASH = 92, CLOSE_BRACKET = 93, UNDERSCORE = 95, OPEN_BRACE = 123;
  const PIPE = 124, CLOSE_BRACE = 125, TILDE = 126;
  const SPECIAL = /* @__PURE__ */ new Set([
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
  const ESCAPABLE = /* @__PURE__ */ new Set([
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
  const EXTERNAL = new RegExp("^(?:" + [
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
  const PARAGRAPH = 0, LIST = 1, TABLE = 2, TERM = 3, OTHER = 4;
  const DIV = 1, STYLE = 2, MONOSPACE = 3, BOLD = 4, ITALIC = 5, LINK = 6, PLUGIN = 7, CODE = 8;
  const END = [-1, divEnd, styleEnd, monospaceEnd, boldEnd, italicEnd];
  const MAX_DEPTH = 48;
  class Frame {
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
  }
  const DANGLING_BOLD = 1, DANGLING_ITALIC = 2, PENDING_BOLD = 4, PENDING_ITALIC = 8, TAINTED = 16;
  const DANGLING = DANGLING_BOLD | DANGLING_ITALIC, PENDING = PENDING_BOLD | PENDING_ITALIC;
  class Context {
    constructor(kind, frame, flags) {
      this.kind = kind;
      this.frame = frame;
      this.flags = flags;
      this.hash = flags ? NaN : frame.hash ^ Math.imul(kind + 1, 668265261) | 1;
    }
  }
  const ROOT = new Frame(0, null);
  function context(kind, frame, flags = 0) {
    if (flags & (DANGLING | PENDING)) flags |= TAINTED;
    return frame.contexts[kind * 32 + flags] ??= new Context(kind, frame, flags);
  }
  function startBlock(cx, kind) {
    return context(kind, cx.frame, cx.flags & TAINTED);
  }
  function paragraphBreak(cx, input, offset) {
    const restart = (cx.flags & DANGLING | (cx.flags & PENDING) >> 2) << 2;
    const ch = input.peek(offset);
    const block = ch === STAR || ch === HASH || ch === EXCLAMATION || ch === SEMICOLON || ch === PIPE || matches(input, offset, "{{{") || matches(input, offset, "----") || matches(input, offset, "%%") || isBlank$1(ch) && declarationAt(input, offset);
    return context(lineKind(input, offset), cx.frame, (block ? 0 : restart) | cx.flags & TAINTED);
  }
  function openElement(cx, types) {
    for (let frame = cx.frame; frame !== ROOT; frame = frame.parent) {
      if (types.includes(frame.type)) return frame;
      if (frame.type === DIV) return null;
    }
    return null;
  }
  function openInline(cx) {
    const type = cx.frame.type;
    return type >= STYLE && type <= ITALIC ? cx.frame : null;
  }
  function lineKind(input, offset) {
    const ch = input.peek(offset);
    if ((ch === PERCENT || isBlank$1(ch)) && declarationAt(input, offset)) return OTHER;
    switch (ch) {
      case EXCLAMATION:
        return OTHER;
      case STAR:
      case HASH:
        return LIST;
      case PIPE:
        return TABLE;
      case SEMICOLON:
        return TERM;
      default:
        return PARAGRAPH;
    }
  }
  const lineContext = new lr.ContextTracker({
    start: context(PARAGRAPH, ROOT),
    shift(cx, term, stack, input) {
      switch (term) {
        case newline: {
          const next = afterLineBreak(input, 0);
          if (atLineStart(input)) return paragraphBreak(cx, input, next);
          const kind = lineKind(input, next);
          return kind === TABLE ? startBlock(cx, TABLE) : context(kind, cx.frame, cx.flags);
        }
        case HorizontalRule:
          return startBlock(cx, PARAGRAPH);
        case PreOpen:
          return context(PARAGRAPH, cx.frame.push(CODE), cx.flags & TAINTED);
        case ListMark:
          return startBlock(cx, LIST);
        case itemBreak:
          return context(LIST, cx.frame, cx.flags);
        case CellMark:
        case HeaderMark:
        case rowBreak:
          return context(TABLE, cx.frame, cx.flags);
        case DefinitionMark:
          return startBlock(cx, TERM);
        case MarkupOpen:
        case LineMarkupOpen:
          return startBlock(cx, OTHER);
        case HeadingMark:
        case DescriptionMark:
          return context(OTHER, cx.frame, cx.flags);
        case DivOpen:
          return context(PARAGRAPH, cx.frame.push(DIV), cx.flags & TAINTED);
        case StyleOpen:
          return context(cx.kind, cx.frame.push(STYLE), cx.flags);
        case MonospaceOpen:
          return context(cx.kind, cx.frame.push(MONOSPACE), cx.flags);
        case BoldOpen:
          return context(cx.kind, cx.frame.push(BOLD), cx.flags);
        case ItalicOpen:
          return context(cx.kind, cx.frame.push(ITALIC), cx.flags);
        case boldRestart:
          return context(cx.kind, cx.frame.push(BOLD), cx.flags & ~PENDING_BOLD);
        case italicRestart:
          return context(cx.kind, cx.frame.push(ITALIC), cx.flags & ~PENDING_ITALIC);
        case BoldToggle:
          return context(cx.kind, cx.frame, cx.flags & ~DANGLING_BOLD);
        case ItalicToggle:
          return context(cx.kind, cx.frame, cx.flags & ~DANGLING_ITALIC);
        case boldEnd:
          return context(cx.kind, cx.frame.parent, cx.flags | DANGLING_BOLD);
        case italicEnd:
          return context(cx.kind, cx.frame.parent, cx.flags | DANGLING_ITALIC);
        case LinkOpen:
        case FootnoteOpen:
        case FootnoteReferenceOpen:
          return context(cx.kind, cx.frame.push(LINK), cx.flags);
        case PluginOpen:
        case VariableOpen:
        case AccessRuleOpen:
        case MetadataOpen:
          return context(cx.kind, cx.frame.push(PLUGIN), cx.flags);
        case CodeOpen:
          return context(cx.kind, cx.frame.push(CODE), cx.flags);
        case StyleClose:
        case MonospaceClose:
        case BoldClose:
        case ItalicClose:
        case divEnd:
        case styleEnd:
        case monospaceEnd:
        case LinkClose:
        case PluginClose:
        case CodeClose:
          return context(cx.frame.type === DIV ? PARAGRAPH : cx.kind, cx.frame.parent, cx.flags);
        default:
          return cx;
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
    return ch >= 192 && new RegExp("\\p{L}", "u").test(String.fromCharCode(ch));
  }
  function atLineStart(input, offset = 0) {
    return input.pos + offset === 0 || input.peek(offset - 1) === NEWLINE$1;
  }
  function matches(input, offset, string) {
    for (let i = 0; i < string.length; i++) {
      if (input.peek(offset + i) !== string.charCodeAt(i)) return false;
    }
    return true;
  }
  function countWhile(input, offset, predicate) {
    let i = offset;
    while (predicate(input.peek(i))) i++;
    return i - offset;
  }
  function lineText(input, offset) {
    let text2 = "";
    for (let i = offset; text2.length < 4096; i++) {
      const ch = input.peek(i);
      if (isLineEnd(ch)) break;
      text2 += String.fromCharCode(ch);
    }
    return text2;
  }
  function afterLineBreak(input, offset) {
    return input.peek(offset) === RETURN$1 && input.peek(offset + 1) === NEWLINE$1 ? offset + 2 : offset + 1;
  }
  function declarationAt(input, offset) {
    const indent = countWhile(input, offset, isBlank$1);
    if (input.peek(offset + indent) !== PERCENT || input.peek(offset + indent + 1) !== PERCENT) return null;
    return matchDeclaration(lineText(input, offset));
  }
  function closingLineLength(input, offset) {
    let i = offset + countWhile(input, offset, isBlank$1);
    if (input.peek(i) === SLASH$1) i++;
    if (input.peek(i) !== PERCENT) return -1;
    i++;
    i += countWhile(input, i, isBlank$1);
    return isLineEnd(input.peek(i)) ? i - offset : -1;
  }
  function styleSpecLength(input, offset) {
    let i = offset;
    if (isLetter(input.peek(i))) i += classLength(input, i);
    if (input.peek(i) === OPEN_PAREN) i += declarationLength(input, i);
    return i - offset;
  }
  function classLength(input, offset) {
    let i = offset;
    for (; ; ) {
      const ch = input.peek(i);
      if (ch === BACKSLASH && !isLineEnd(input.peek(i + 1))) {
        i += 2;
      } else if (ch === OPEN_PAREN || isBlank$1(ch) || isLineEnd(ch)) {
        return i - offset;
      } else {
        i++;
      }
    }
  }
  function declarationLength(input, offset) {
    let depth = 0, i = offset;
    for (; ; ) {
      const ch = input.peek(i);
      if (ch < 0) return i - offset;
      i++;
      if (ch === OPEN_PAREN) depth++;
      else if (ch === CLOSE_PAREN && --depth === 0) return i - offset;
    }
  }
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
  function codeEnd(input, offset) {
    for (let i = offset; ; i++) {
      const ch = input.peek(i);
      if (ch < 0) return -1;
      if (ch === TILDE && matches(input, i + 1, "}}}")) i += 3;
      else if (ch === CLOSE_BRACE && matches(input, i + 1, "}}")) return i;
    }
  }
  function scanBracket(input, offset, plugin2) {
    let depth = 1;
    for (let i = offset; ; ) {
      const ch = input.peek(i);
      if (ch < 0) return -1;
      if (ch === NEWLINE$1) {
        i++;
        if (declarationAt(input, i)) return -1;
      } else if (!plugin2) {
        if (ch === CLOSE_BRACKET) return i + 1;
        i++;
      } else if (ch === OPEN_BRACKET && input.peek(i + 1) === OPEN_BRACE) {
        depth++;
        i += 2;
      } else if (ch === CLOSE_BRACE && input.peek(i + 1) === CLOSE_BRACKET) {
        i += 2;
        if (--depth === 0) return i;
      } else {
        i++;
      }
    }
  }
  const EMPTY = 0, BLOCK = 1, INDENTED = 2, PLAIN = 3, LIST_LINE = 4, TABLE_LINE = 5;
  function nextLineKind(input, offset) {
    const ch = input.peek(offset);
    if (ch < 0 || ch === NEWLINE$1 || ch === RETURN$1 && input.peek(offset + 1) === NEWLINE$1) return EMPTY;
    if ((ch === PERCENT || isBlank$1(ch)) && declarationAt(input, offset)) return BLOCK;
    switch (ch) {
      case SPACE$1:
        return INDENTED;
      case STAR:
      case HASH:
        return LIST_LINE;
      case PIPE:
        return TABLE_LINE;
      case EXCLAMATION:
      case SEMICOLON:
        return BLOCK;
      case DASH:
        return matches(input, offset, "----") ? BLOCK : PLAIN;
      case OPEN_BRACE:
        return matches(input, offset, "{{{") ? BLOCK : PLAIN;
      case PERCENT:
        return input.peek(offset + 1) === PERCENT && opensStyle(input, offset + 2) && isStyleBlock(input, offset) ? BLOCK : PLAIN;
      default:
        return PLAIN;
    }
  }
  function lineBreak(input, stack) {
    const length = input.next === RETURN$1 ? 2 : 1, cx = stack.context;
    let term = newline;
    if (!atLineStart(input)) {
      const next = nextLineKind(input, length);
      const inline2 = openInline(cx) !== null;
      if (next === LIST_LINE && cx.kind === LIST && (inline2 || stack.canShift(itemBreak))) {
        term = itemBreak;
      } else if (next === TABLE_LINE && cx.kind === TABLE && (inline2 || stack.canShift(rowBreak))) {
        term = rowBreak;
      } else if (next === INDENTED || next === PLAIN && cx.kind === PARAGRAPH) {
        if ((cx.kind === PARAGRAPH || cx.kind === LIST) && stack.canShift(softBreak)) {
          return input.acceptToken(softBreak, length);
        }
      }
    }
    const element = openInline(cx);
    if (element) return input.acceptToken(END[element.type], 0);
    input.acceptToken(term, length);
  }
  function close(input, stack, element, term, length) {
    const top = stack.context.frame;
    if (top !== element) return input.acceptToken(END[top.type], 0);
    input.acceptToken(stack.canShift(term) ? term : text, length);
  }
  function mayNest(stack, monospace) {
    const frame = stack.context.frame;
    return (monospace ? frame.monospace : frame.styles) < MAX_DEPTH;
  }
  function lineStart(input, stack) {
    const ch = input.next, flags = stack.context.flags;
    if (flags & PENDING && !isLineEnd(ch) && !declarationAt(input, 0)) {
      const restart = flags & PENDING_ITALIC ? italicRestart : boldRestart;
      if (stack.canShift(restart)) return input.acceptToken(restart, 0), true;
    }
    if ((ch === PERCENT || isBlank$1(ch)) && stack.canShift(MarkupOpen)) {
      const declaration = declarationAt(input, 0);
      if (declaration) {
        input.acceptToken(declaration.block ? MarkupOpen : LineMarkupOpen, declaration.length);
        return true;
      }
    }
    let term = -1, length = 0;
    switch (ch) {
      case EXCLAMATION:
        term = HeadingMark;
        length = Math.min(3, countWhile(input, 0, (c) => c === EXCLAMATION));
        break;
      case STAR:
      case HASH:
        term = ListMark;
        length = countWhile(input, 0, (c) => c === STAR || c === HASH);
        length += countWhile(input, length, (c) => c === SPACE$1);
        break;
      case SEMICOLON:
        term = DefinitionMark;
        length = 1;
        break;
      case DASH:
        length = countWhile(input, 0, (c) => c === DASH);
        if (length >= 4) term = HorizontalRule;
        break;
      case PIPE:
        term = input.peek(1) === PIPE ? HeaderMark : CellMark;
        length = term === HeaderMark ? 2 : 1;
        break;
      case OPEN_BRACE:
        if (matches(input, 0, "{{{") && codeEnd(input, 3) >= 0) {
          term = PreOpen;
          length = 3;
        }
        break;
      case PERCENT:
        if (input.peek(1) === PERCENT && opensStyle(input, 2) && isStyleBlock(input, 0) && mayNest(stack, false)) {
          term = DivOpen;
          length = 2;
        }
        break;
    }
    if (term < 0 || !stack.canShift(term)) return false;
    input.acceptToken(term, length);
    return true;
  }
  function markupContent(input, stack) {
    let length = 0;
    if (stack.canShift(blockContent)) {
      for (; ; ) {
        let ch = input.peek(length);
        while (!isLineEnd(ch)) ch = input.peek(++length);
        if (ch < 0) break;
        length = afterLineBreak(input, length);
        if (input.peek(length) < 0 || closingLineLength(input, length) >= 0) break;
      }
      if (length > 0) input.acceptToken(blockContent, length);
    } else if (stack.canShift(lineContent)) {
      while (!isLineEnd(input.peek(length))) length++;
      if (length > 0) input.acceptToken(lineContent, length);
    } else {
      length = inlineMarkupEnd(input, 0);
      if (length > 0) input.acceptToken(inlineContent, length);
      else input.acceptToken(InlineMarkupClose, 2);
    }
  }
  function inlineMarkupEnd(input, offset) {
    for (let i = offset; ; i++) {
      const ch = input.peek(i);
      if (ch < 0) return -1;
      if ((ch === PERCENT || ch === SLASH$1) && input.peek(i + 1) === PERCENT) return i;
      if (ch === NEWLINE$1 && declarationAt(input, i + 1)) return -1;
    }
  }
  function code(input) {
    const end = codeEnd(input, 0);
    if (end > 0) input.acceptToken(CodeText, end);
    else input.acceptToken(CodeClose, 3);
  }
  function link(input, stack) {
    const ch = input.next;
    if (ch === CLOSE_BRACKET) return input.acceptToken(LinkClose, 1);
    if (ch === PIPE && stack.canShift(LinkSeparator)) return input.acceptToken(LinkSeparator, 1);
    if (stack.canShift(attributeText) || stack.canShift(attributeEquals)) {
      if (isWordChar$1(ch) && stack.canShift(AttributeName)) {
        const name = countWhile(input, 0, isWordChar$1);
        const blanks = countWhile(input, name, isBlank$1);
        if (input.peek(name + blanks) === EQUALS$1) return input.acceptToken(AttributeName, name);
      }
      if (stack.canShift(attributeEquals)) {
        const blanks = countWhile(input, 0, isBlank$1);
        if (input.peek(blanks) === EQUALS$1) {
          return input.acceptToken(attributeEquals, blanks + 1 + countWhile(input, blanks + 1, isBlank$1));
        }
      }
      if (ch === APOSTROPHE && stack.canShift(AttributeValue)) {
        let i = 1;
        while (input.peek(i) !== APOSTROPHE && input.peek(i) !== CLOSE_BRACKET) i++;
        return input.acceptToken(AttributeValue, input.peek(i) === APOSTROPHE ? i + 1 : i);
      }
      const length2 = Math.max(1, countWhile(input, 0, (c) => c !== CLOSE_BRACKET && c !== APOSTROPHE && c !== EQUALS$1 && !isWordChar$1(c)));
      return input.acceptToken(attributeText, length2);
    }
    let length = 0, text2 = "";
    for (let c = ch; c !== PIPE && c !== CLOSE_BRACKET; c = input.peek(++length)) text2 += String.fromCharCode(c);
    if (input.peek(length) === PIPE && stack.canShift(LinkText)) return input.acceptToken(LinkText, length);
    input.acceptToken(EXTERNAL.test(text2.trim()) ? URL : LinkTarget, length);
  }
  function openLink(input) {
    const end = scanBracket(input, 1, false);
    if (end < 0) return input.acceptToken(Unclosed, 1);
    let content = "";
    for (let i = 1; i < end - 1; i++) content += String.fromCharCode(input.peek(i));
    const parts = content.split("|");
    const target = parts.length === 1 ? parts[0] : parts[1].trim();
    if (target.startsWith("#")) return input.acceptToken(FootnoteOpen, 1);
    if (/^-?\d+$/.test(target)) return input.acceptToken(FootnoteReferenceOpen, 1);
    input.acceptToken(LinkOpen, 1);
  }
  function openPlugin(input, unclosed) {
    if (scanBracket(input, 2, true) < 0) return input.acceptToken(unclosed, 2);
    if (input.peek(2) === DOLLAR) return input.acceptToken(VariableOpen, 3);
    if (matches(input, 2, "ALLOW") || matches(input, 2, "DENY")) return input.acceptToken(AccessRuleOpen, 2);
    if (matches(input, 2, "SET")) return input.acceptToken(MetadataOpen, 2);
    input.acceptToken(PluginOpen, 2);
  }
  function quotedLength(input, quote) {
    let depth = 0;
    for (let i = 1; ; i++) {
      const ch = input.peek(i);
      if (ch < 0) return i;
      if (ch === BACKSLASH && input.peek(i + 1) >= 0) {
        i++;
      } else if (ch === OPEN_BRACKET && input.peek(i + 1) === OPEN_BRACE) {
        depth++;
        i++;
      } else if (ch === CLOSE_BRACE && input.peek(i + 1) === CLOSE_BRACKET) {
        if (depth === 0) return i;
        depth--;
        i++;
      } else if (ch === quote && depth === 0) {
        return i + 1;
      }
    }
  }
  function atPluginEnd(input, offset) {
    return input.peek(offset) === CLOSE_BRACE && input.peek(offset + 1) === CLOSE_BRACKET;
  }
  function atPluginStart(input, offset) {
    return input.peek(offset) === OPEN_BRACKET && input.peek(offset + 1) === OPEN_BRACE;
  }
  function atPluginBracket(input, offset) {
    return atPluginEnd(input, offset) || atPluginStart(input, offset);
  }
  function wordLength(input, offset) {
    let i = offset;
    while (!isBlank$1(input.peek(i)) && !isLineEnd(input.peek(i)) && !atPluginBracket(input, i)) i++;
    return i - offset;
  }
  function plugin(input, stack) {
    const ch = input.next;
    if (atPluginEnd(input, 0)) return input.acceptToken(PluginClose, 2);
    if (atPluginStart(input, 0)) return openPlugin(input, stack.canShift(bodyText$1) ? bodyText$1 : pluginText);
    if (stack.canShift(bodyText$1)) {
      let i = 0;
      while (input.peek(i) >= 0 && !atPluginEnd(input, i) && !atPluginStart(input, i)) i++;
      return input.acceptToken(bodyText$1, i);
    }
    if (ch === NEWLINE$1 || ch === RETURN$1) {
      if (stack.canShift(bodyStart)) {
        let i = afterLineBreak(input, 0), blankLines = 0;
        for (; ; ) {
          const blanks = countWhile(input, i, isBlank$1);
          const end = input.peek(i + blanks);
          if (end !== NEWLINE$1 && end !== RETURN$1) break;
          i = afterLineBreak(input, i + blanks);
          blankLines++;
        }
        if (blankLines > 0) return input.acceptToken(bodyStart, i);
      }
      return input.acceptToken(pluginSpace, afterLineBreak(input, 0));
    }
    if (isBlank$1(ch)) return input.acceptToken(pluginSpace, countWhile(input, 0, isBlank$1));
    if (stack.canShift(VariableName)) return input.acceptToken(VariableName, wordLength(input, 0));
    if (stack.canShift(AccessKeyword)) return input.acceptToken(AccessKeyword, matches(input, 0, "DENY") ? 4 : 5);
    if (stack.canShift(SetKeyword)) return input.acceptToken(SetKeyword, 3);
    if (stack.canShift(Permission)) return input.acceptToken(Permission, wordLength(input, 0));
    if (stack.canShift(Principal)) {
      if (ch === 44) return input.acceptToken(pluginText, 1);
      let i = 0, end = 0;
      for (let c = ch; c !== 44 && !isLineEnd(c) && !atPluginBracket(input, i); c = input.peek(++i)) {
        if (!isBlank$1(c)) end = i + 1;
      }
      return input.acceptToken(Principal, end);
    }
    if (stack.canShift(MetadataName)) {
      let i = 0, end = 0;
      for (let c = ch; c !== EQUALS$1 && c >= 0 && !atPluginBracket(input, i); c = input.peek(++i)) {
        if (!isBlank$1(c) && !isLineEnd(c)) end = i + 1;
      }
      if (input.peek(i) === EQUALS$1 && end > 0) return input.acceptToken(MetadataName, end);
      return input.acceptToken(pluginText, wordLength(input, 0));
    }
    if (stack.canShift(MetadataValue)) {
      let i = 0, end = 0;
      for (let c = ch; c >= 0 && !atPluginBracket(input, i); c = input.peek(++i)) {
        if (!isBlank$1(c) && !isLineEnd(c)) end = i + 1;
      }
      return input.acceptToken(MetadataValue, end);
    }
    if (ch === EQUALS$1 && stack.canShift(parameterEquals)) return input.acceptToken(parameterEquals, 1);
    if (stack.canShift(ParameterValue)) {
      if (ch === APOSTROPHE || ch === QUOTE) return input.acceptToken(ParameterValue, quotedLength(input, ch));
      return input.acceptToken(ParameterValue, wordLength(input, 0));
    }
    if (isWordChar$1(ch)) {
      const word = countWhile(input, 0, (c) => isWordChar$1(c) || c === DOT);
      if (stack.canShift(PluginName)) {
        if (word === 6 && matches(input, 0, "INSERT") && stack.canShift(InsertKeyword)) {
          return input.acceptToken(InsertKeyword, 6);
        }
        return input.acceptToken(PluginName, word);
      }
      if (word === 5 && matches(input, 0, "WHERE") && stack.canShift(WhereKeyword)) {
        return input.acceptToken(WhereKeyword, 5);
      }
      const name = countWhile(input, 0, (c) => isWordChar$1(c) || c === DOT || c === DASH);
      if (input.peek(name + countWhile(input, name, isBlank$1)) === EQUALS$1 && stack.canShift(ParameterName)) {
        return input.acceptToken(ParameterName, name);
      }
      return input.acceptToken(pluginText, word);
    }
    input.acceptToken(pluginText, 1);
  }
  function styleSpec(input, stack) {
    const ch = input.next;
    if (isLetter(ch) && stack.canShift(StyleClass)) {
      return input.acceptToken(StyleClass, classLength(input, 0)), true;
    }
    if (ch === OPEN_PAREN && stack.canShift(StyleDeclaration)) {
      return input.acceptToken(StyleDeclaration, declarationLength(input, 0)), true;
    }
    if (isBlank$1(ch) && stack.canShift(styleBlank)) {
      const length = countWhile(input, 0, isBlank$1);
      if (isLineEnd(input.peek(length))) return input.acceptToken(styleBlank, length), true;
    }
    return false;
  }
  function escape(input) {
    const next = input.peek(1);
    if (next === SPACE$1) return input.acceptToken(Escape, 2);
    if (ESCAPABLE.has(next)) return input.acceptToken(Escape, 1 + countWhile(input, 1, (c) => c === next));
    input.acceptToken(text, 1);
  }
  function unitMark(input, stack, term, length) {
    const element = openInline(stack.context);
    if (element) return input.acceptToken(END[element.type], 0);
    input.acceptToken(stack.canShift(term) ? term : text, length);
  }
  function inline(input, stack) {
    const ch = input.next, next = input.peek(1);
    switch (ch) {
      case UNDERSCORE:
        if (next === UNDERSCORE) {
          const bold = openElement(stack.context, [BOLD]);
          if (bold) return close(input, stack, bold, BoldClose, 2);
          return input.acceptToken(stack.context.flags & DANGLING_BOLD ? BoldToggle : BoldOpen, 2);
        }
        break;
      case APOSTROPHE:
        if (next === APOSTROPHE) {
          const italic = openElement(stack.context, [ITALIC]);
          if (italic) return close(input, stack, italic, ItalicClose, 2);
          return input.acceptToken(stack.context.flags & DANGLING_ITALIC ? ItalicToggle : ItalicOpen, 2);
        }
        break;
      case OPEN_BRACE:
        if (next === OPEN_BRACE) {
          if (input.peek(2) !== OPEN_BRACE) {
            return input.acceptToken(mayNest(stack, true) ? MonospaceOpen : text, 2);
          }
          return input.acceptToken(codeEnd(input, 3) >= 0 ? CodeOpen : Unclosed, 3);
        }
        break;
      case CLOSE_BRACE:
        if (next === CLOSE_BRACE) {
          if (input.peek(2) === CLOSE_BRACE) return input.acceptToken(text, 3);
          const monospace = openElement(stack.context, [MONOSPACE]);
          return monospace ? close(input, stack, monospace, MonospaceClose, 2) : input.acceptToken(text, 2);
        }
        break;
      case OPEN_BRACKET:
        if (next === OPEN_BRACKET) {
          return input.acceptToken(Escape, countWhile(input, 0, (c) => c === OPEN_BRACKET));
        }
        if (next === OPEN_BRACE) return openPlugin(input, Unclosed);
        return openLink(input);
      case TILDE:
        return escape(input);
      case BACKSLASH:
        if (next === BACKSLASH) return input.acceptToken(LineBreak, input.peek(2) === BACKSLASH ? 3 : 2);
        break;
      case PERCENT:
        if (next === PERCENT) {
          const inlineMarkup = matchInlineDeclaration(lineText(input, 0));
          if (inlineMarkup > 0 && inlineMarkupEnd(input, inlineMarkup) >= 0) {
            return input.acceptToken(InlineMarkupOpen, inlineMarkup);
          }
          if (opensStyle(input, 2)) return input.acceptToken(mayNest(stack, false) ? StyleOpen : text, 2);
          const style = openElement(stack.context, [STYLE, DIV]);
          return style ? close(input, stack, style, StyleClose, 2) : input.acceptToken(StrayStyleClose, 2);
        }
        break;
      case SLASH$1:
        if (next === PERCENT) {
          const style = openElement(stack.context, [STYLE, DIV]);
          if (!style) break;
          if (opensStyle(input, 2) && mayNest(stack, false)) return input.acceptToken(StyleOpen, 2);
          return close(input, stack, style, StyleClose, 2);
        }
        break;
      case AMPERSAND: {
        const length = countWhile(input, 1, (c) => isWordChar$1(c) || c === HASH);
        if (length > 0 && input.peek(1 + length) === SEMICOLON) return input.acceptToken(Entity, length + 2);
        break;
      }
      case COLON$1:
        if (stack.context.kind === TERM) return unitMark(input, stack, DescriptionMark, 1);
        break;
      case PIPE:
        if (stack.context.kind === TABLE) {
          return next === PIPE ? unitMark(input, stack, HeaderMark, 2) : unitMark(input, stack, CellMark, 1);
        }
        break;
      default: {
        let length = 1;
        while (!SPECIAL.has(input.peek(length)) && input.peek(length) >= 0) length++;
        return input.acceptToken(text, length);
      }
    }
    input.acceptToken(text, 1);
  }
  function raw(input, stack) {
    if (stack.canShift(blockContent) || stack.canShift(lineContent) || stack.canShift(InlineMarkupClose)) {
      markupContent(input, stack);
    } else if (stack.canShift(MarkupClose)) {
      const length = closingLineLength(input, 0);
      if (length < 0) return false;
      input.acceptToken(MarkupClose, length);
    } else {
      return styleSpec(input, stack);
    }
    return true;
  }
  const wikiTokens = new lr.ExternalTokenizer((input, stack) => {
    if (input.next < 0) {
      const top = stack.context.frame;
      if (top !== ROOT) input.acceptToken(END[top.type], 0);
      return;
    }
    switch (stack.context.frame.type) {
      case LINK:
        return link(input, stack);
      case PLUGIN:
        return plugin(input, stack);
      case CODE:
        return code(input);
    }
    if (!stack.canShift(text) && raw(input, stack)) return;
    const ch = input.next;
    if ((ch === OPEN_PAREN || input.peek(-1) === PERCENT && isLetter(ch)) && styleSpec(input, stack)) return;
    if (atLineStart(input) && lineStart(input, stack)) return;
    if (input.next === NEWLINE$1 || input.next === RETURN$1 && input.peek(1) === NEWLINE$1) return lineBreak(input, stack);
    inline(input, stack);
  }, { contextual: true });
  const parser$2 = lr.LRParser.deserialize({
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
  const DeclarationComment = 1, bodyText = 8, AnnotationName = 2, AnnotationValue = 3, space = 9;
  const NEWLINE = 10, RETURN = 13, SPACE = 32, TAB = 9, AT = 64, SLASH = 47, COLON = 58, EQUALS = 61;
  function isBlank(ch) {
    return ch === SPACE || ch === TAB;
  }
  function isSpace(ch) {
    return isBlank(ch) || ch === NEWLINE || ch === RETURN;
  }
  function isWordChar(ch) {
    return ch >= 48 && ch <= 57 || ch >= 65 && ch <= 90 || ch >= 97 && ch <= 122 || ch === 95 || ch >= 192;
  }
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
  function isAnnotationLine(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    return annotationNameLength(input, i) > 0;
  }
  function nextAnnotationLine(input, offset) {
    for (let i = offset; ; i++) {
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
  function inAnnotationIndent(input, offset) {
    let i = offset - 1;
    while (isBlank(input.peek(i))) i--;
    return input.peek(i) === NEWLINE && isAnnotationLine(input, offset);
  }
  const bodyTokens = new lr.ExternalTokenizer((input, stack) => {
    const ch = input.next;
    if (ch < 0) return;
    if (inAnnotationIndent(input, 0)) {
      const indent = countBlanks(input, 0);
      if (indent > 0) return input.acceptToken(space, indent);
      return input.acceptToken(AnnotationName, annotationNameLength(input, 0));
    }
    if (stack.canShift(DeclarationComment)) {
      const indent = countBlanks(input, 0);
      if (input.peek(indent) === SLASH && input.peek(indent + 1) === SLASH) {
        if (indent > 0) return input.acceptToken(space, indent);
        let i = 0;
        while (input.peek(i) >= 0 && input.peek(i) !== NEWLINE && input.peek(i) !== RETURN) i++;
        return input.acceptToken(DeclarationComment, i);
      }
    }
    const end = nextAnnotationLine(input, 0);
    let blank = 0;
    while (blank < end && isSpace(input.peek(blank))) blank++;
    if (stack.canShift(bodyText)) return input.acceptToken(bodyText, end);
    if (blank > 0) return input.acceptToken(space, blank);
    input.acceptToken(AnnotationValue, lastNonSpace(input, 0, end));
  }, { contextual: true });
  const parser$1 = lr.LRParser.deserialize({
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
  const registry = /* @__PURE__ */ new Map();
  function registerLanguage(markupName2, parser2) {
    registry.set(markupName2.toLowerCase(), parser2);
  }
  function unregisterLanguage(markupName2) {
    registry.delete(markupName2.toLowerCase());
  }
  function getLanguage(markupName2) {
    return registry.get(markupName2.toLowerCase()) ?? null;
  }
  function markupName(open, input) {
    return input.read(open.from, open.to).trim().replace(/^%%/, "").replace(/[\s:=]*$/, "");
  }
  const markupContentParser = parser$1.configure({
    props: [
      highlight.styleTags({
        DeclarationComment: highlight.tags.lineComment,
        AnnotationName: highlight.tags.annotation,
        AnnotationValue: highlight.tags.attributeValue
      })
    ]
  });
  function foldBlock(node, state) {
    const first = state.doc.lineAt(node.from);
    const last = node.lastChild;
    const closed = last && (last.name === "MarkupClose" || last.name === "StyleMark" || last.name === "CodeMark");
    const end = closed ? state.doc.lineAt(last.from).from - 1 : node.to;
    return end > first.to ? { from: first.to, to: end } : null;
  }
  const parser = parser$2.configure({
    props: [
      highlight.styleTags({
        "Heading/...": highlight.tags.heading,
        "HeadingMark ListMark DefinitionMark DescriptionMark": highlight.tags.list,
        "BoldMark ItalicMark MonospaceMark CodeMark": highlight.tags.processingInstruction,
        "Bold/...": highlight.tags.strong,
        "Italic/...": highlight.tags.emphasis,
        "Monospace/... InlineCode/... Preformatted/...": highlight.tags.monospace,
        "TableHeader/...": highlight.tags.strong,
        "CellMark HeaderMark": highlight.tags.punctuation,
        "Link/... Footnote/... FootnoteReference/...": highlight.tags.link,
        URL: highlight.tags.url,
        LinkMark: highlight.tags.punctuation,
        "LinkAttributes/...": highlight.tags.meta,
        "Plugin/... Variable/... AccessRule/... Metadata/...": highlight.tags.processingInstruction,
        PluginName: highlight.tags.macroName,
        PluginMark: highlight.tags.punctuation,
        "StyleMark StyleDeclaration": highlight.tags.meta,
        StyleClass: highlight.tags.className,
        Escape: highlight.tags.escape,
        Entity: highlight.tags.character,
        "LineBreak HorizontalRule": highlight.tags.contentSeparator,
        "MarkupOpen MarkupClose": highlight.tags.tagName,
        Unclosed: highlight.tags.invalid
      }),
      language.foldNodeProp.add({
        "Markup StyleBlock Preformatted": foldBlock,
        Plugin(node, state) {
          const first = state.doc.lineAt(node.from);
          return node.to - 2 > first.to ? { from: first.to, to: node.to - 2 } : null;
        }
      })
    ],
    wrap: common.parseMixed((node, input) => {
      if (node.name !== "MarkupContent") return null;
      const open = node.node.prevSibling;
      if (!open || open.name !== "MarkupOpen") return null;
      return { parser: getLanguage(markupName(open, input)) ?? markupContentParser };
    })
  });
  const wikiLanguage = language.LRLanguage.define({
    name: "jspwiki",
    parser,
    languageData: {
      // apostrophes and quotes are prose in wiki text, so only brackets close automatically
      closeBrackets: { brackets: ["(", "[", "{"] }
    }
  });
  function wiki() {
    return new language.LanguageSupport(wikiLanguage);
  }
  exports.defineMarkups = defineMarkups;
  exports.getLanguage = getLanguage;
  exports.markupContentParser = markupContentParser;
  exports.markupName = markupName;
  exports.parser = parser;
  exports.registerLanguage = registerLanguage;
  exports.unregisterLanguage = unregisterLanguage;
  exports.wiki = wiki;
  exports.wikiLanguage = wikiLanguage;
  Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
  return exports;
})({}, CodeMirror.lezer.common, CodeMirror.lezer.highlight, CodeMirror.language, CodeMirror.lezer.lr);
