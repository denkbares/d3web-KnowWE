// Entry point of the vendored CodeMirror bundle. Each package is exposed as a namespace of the global "CodeMirror"
// object, so plugins can externalize "@codemirror/*" and "@lezer/*" imports and map them to these namespaces.
import * as state from "@codemirror/state";
import * as view from "@codemirror/view";
import * as language from "@codemirror/language";
import * as commands from "@codemirror/commands";
import * as search from "@codemirror/search";
import * as autocomplete from "@codemirror/autocomplete";
import * as lint from "@codemirror/lint";
import * as merge from "@codemirror/merge";
import * as common from "@lezer/common";
import * as highlight from "@lezer/highlight";
import * as lr from "@lezer/lr";

export {state, view, language, commands, search, autocomplete, lint, merge};
export const lezer = {common, highlight, lr};
