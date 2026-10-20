// Entry of the KnowWE-Plugin-TextDiff.js bundle, which defines both web components.
import "./components/text-diff.js";
import "./components/file-change.js";

export {preferences} from "./components/text-diff.js";

export {computeDiff} from "./diff/index.js";
export {renderTable} from "./renderer/table.js";
export {createDiffView} from "./renderer/codemirror.js";
