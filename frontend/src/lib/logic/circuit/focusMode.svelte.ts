import type { Block } from "../../dto/circuit/block";
import { getGraph } from "./circuit.svelte";
import type { Graph } from "./graph";
import { getFocusModeDepth } from "../preferences.svelte";

let focusModeBlock: Block | null = $state(null);
let focusModeVisibleEdges: { from: Block; to: Block }[] = $derived.by(() => {
    const graph: Graph = getGraph();

    // Safety checks
    if (focusModeBlock === null) {
        return graph.getEdges();
    }
    if (getFocusModeDepth() <= 0) {
        return [];
    }

    // Initialize edges, visited blocks and block queue
    let edges: { from: Block; to: Block }[] = [];
    let visited: Set<number> = new Set();
    let queue: { block: Block; depth: number; ascend: boolean }[] = [
        { block: focusModeBlock!, depth: 0, ascend: true },
        { block: focusModeBlock!, depth: 0, ascend: false },
    ];

    // Add first level from initial block
    graph.getParents(focusModeBlock!).forEach(parent => {
        queue.push({ block: parent, depth: 1, ascend: true });
        edges.push({ from: parent, to: focusModeBlock! });
    });
    graph.getChildren(focusModeBlock!).forEach(child => {
        queue.push({ block: child, depth: 1, ascend: false });
        edges.push({ from: focusModeBlock!, to: child });
    });
    visited.add(focusModeBlock!.id);

    // Traverse the graph, ascending and descending depending on direction
    while (queue.length > 0) {
        let current: { block: Block; depth: number; ascend: boolean } = queue.shift()!;

        // Stop if visited or above max depth
        if (current.depth + 1 > getFocusModeDepth() || visited.has(current.block.id)) {
            continue;
        }

        if (current.ascend) {
            // Get parents if ascending
            graph.getParents(current.block).forEach(parent => {
                queue.push({ block: parent, depth: current.depth + 1, ascend: true });
                edges.push({ from: parent, to: current.block });
            });
        } else {
            // Get children if descending
            graph.getChildren(current.block).forEach(child => {
                queue.push({ block: child, depth: current.depth + 1, ascend: false });
                edges.push({ from: current.block, to: child });
            });
        }

        visited.add(current.block.id);
    }

    return edges;
});
let focusModeVisibleBlocks: Set<number> = $derived(
    new Set(focusModeVisibleEdges.flatMap(edge => [edge.from.id, edge.to.id])),
);

export function getFocusModeVisibleEdges(): { from: Block; to: Block }[] {
    return focusModeVisibleEdges;
}

export function setFocusMode(block: Block | null) {
    const graph: Graph = getGraph();

    // Validation before setting focus mode block
    if (
        block !== null &&
        (graph === undefined || !graph.has(block) || graph.getNode(block.id) !== block)
    ) {
        return;
    }

    focusModeBlock = block;
}

export function getFocusModeBlock() {
    return focusModeBlock;
}

export function isInFocusMode(): boolean {
    return focusModeBlock !== null;
}

export function visibleInFocusMode(block: Block): boolean {
    return focusModeVisibleBlocks.has(block.id);
}
