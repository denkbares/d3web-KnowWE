// Structural checks shared by the tree tests, the corpus run and the fuzzer.
import {TreeFragment} from "@lezer/common";

/** Returns the offsets of all error nodes in the tree. */
export function errorOffsets(tree) {
    const offsets = [];
    tree.iterate({
        enter(node) {
            if (node.type.isError) offsets.push(node.from);
        },
    });
    return offsets;
}

/** Throws if a node lies outside of its parent or overlaps its previous sibling. */
export function checkNesting(tree) {
    const cursor = tree.cursor();
    const check = (parentFrom, parentTo, parentName) => {
        let last = parentFrom;
        if (!cursor.firstChild()) return;
        do {
            const {name, from, to} = cursor;
            if (from < parentFrom || to > parentTo) {
                throw new Error(`${name} ${from}-${to} outside of ${parentName} ${parentFrom}-${parentTo}`);
            }
            if (from < last) throw new Error(`${name} ${from}-${to} overlaps its previous sibling in ${parentName}`);
            last = to;
            check(from, to, name);
        } while (cursor.nextSibling());
        cursor.parent();
    };
    check(cursor.from, cursor.to, cursor.name);
}

/** Throws if the top node does not span the whole document. */
export function checkCoverage(tree, text) {
    if (tree.length !== text.length) throw new Error(`tree length ${tree.length} differs from text ${text.length}`);
}

/** Serializes the tree with the ranges of all nodes, unlike Tree.toString. */
export function serialize(tree) {
    const parts = [];
    tree.iterate({
        enter(node) {
            parts.push(`${node.name}[${node.from},${node.to}](`);
        },
        leave() {
            parts.push(")");
        },
    });
    return parts.join("");
}

/**
 * Applies the change to the text, reparses incrementally from the fragments of the old tree and throws if the
 * result differs from a fresh parse of the new text.
 */
export function checkIncremental(parser, text, tree, from, to, insert) {
    const changed = text.slice(0, from) + insert + text.slice(to);
    const fragments = TreeFragment.applyChanges(TreeFragment.addTree(tree),
        [{fromA: from, toA: to, fromB: from, toB: from + insert.length}]);
    const incremental = parser.parse(changed, fragments);
    const fresh = parser.parse(changed);
    if (serialize(incremental) !== serialize(fresh)) {
        throw new Error(`incremental parse differs after replacing ${from}-${to} with ${JSON.stringify(insert)}`);
    }
    return {text: changed, tree: incremental};
}
