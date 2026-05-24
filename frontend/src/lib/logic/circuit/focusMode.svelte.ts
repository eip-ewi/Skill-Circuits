import type { Block } from "../../dto/circuit/block";
import { getBlocks, getGraph } from "./circuit.svelte";
import type { Graph } from "./graph";
import { BlockStates } from "../../data/block_state";

let focusModeBlock: Block | null = $state(null);
let maxDepth: number | null = $derived.by(() => {
    if (focusModeBlock === null) return null;
    // Calculate max depth in up and down directions
    const maxRowInCircuit = Math.max(0, ...getBlocks().map(block => block.row ?? 0));
    return Math.max(maxRowInCircuit - (focusModeBlock.row ?? 0), focusModeBlock.row ?? 0);
});
let focusModeDepth: number = $state(defaultDepth());
let focusModeVisibleEdges: { from: Block; to: Block }[] = $derived.by(() => {
    const graph: Graph = getGraph();

    // Safety checks
    if (focusModeBlock === null) {
        return graph.getEdges();
    }
    if (focusModeDepth <= 0) {
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
        if (current.depth + 1 > focusModeDepth || visited.has(current.block.id)) {
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

function defaultDepth() {
    return maxDepth !== null && maxDepth <= 2 ? maxDepth : 2;
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

export function resetFocusMode() {
    focusModeBlock = null;
    safelyUpdateFocusModeDepth(defaultDepth());
}

export function toggleFocusMode(clickedBlock: Block) {
    // Reset depth
    safelyUpdateFocusModeDepth(defaultDepth());

    if (getFocusModeBlock() !== clickedBlock) {
        // Set this block to focus mode
        // States of all blocks are updated within the block component
        focusModeBlock = clickedBlock;
    } else {
        // Stop focus mode
        focusModeBlock = null;

        // Reset state of all blocks
        getBlocks().forEach(other => {
            other.state = BlockStates.Inactive;
        });
    }
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

export function safelyUpdateFocusModeDepth(depth: number) {
    if (depth <= 0 || (maxDepth !== null && depth > maxDepth)) return;
    focusModeDepth = depth;
}

export function getFocusModeDepth(): number {
    return focusModeDepth;
}

export function getMaxDepth(): number {
    return maxDepth!;
}
