import type { Block } from "../../dto/circuit/block";
import { getGraph } from "./circuit.svelte";
import type { Graph } from "./graph";
import { getFocusModeDepth } from "../preferences.svelte";

let focusModeBlock: Block | null = $state(null);
let focusModeEdges: { from: Block; to: Block; visible: boolean }[] = $derived.by(() => {
    const graph: Graph = getGraph();
    const edges: { from: Block; to: Block; visible: boolean }[] = graph
        .getEdges()
        .map(edge => ({ from: edge.from, to: edge.to, visible: false }));

    // Safety checks
    if (focusModeBlock === null) {
        edges.forEach(edge => (edge.visible = true));
        return edges;
    }
    if (getFocusModeDepth() <= 0) {
        return edges;
    }

    // Initialize edges, visited blocks and block queue
    let visited: Set<number> = new Set();
    let queue: { block: Block; depth: number; ascend: boolean }[] = [
        { block: focusModeBlock!, depth: 0, ascend: true },
        { block: focusModeBlock!, depth: 0, ascend: false },
    ];

    // Add first level from initial block
    graph.getParents(focusModeBlock!).forEach(parent => {
        queue.push({ block: parent, depth: 1, ascend: true });
        edges
            .filter(edge => edge.from.id === parent.id && edge.to.id === focusModeBlock!.id)
            .forEach(edge => (edge.visible = true));
    });
    graph.getChildren(focusModeBlock!).forEach(child => {
        queue.push({ block: child, depth: 1, ascend: false });
        edges
            .filter(edge => edge.from.id === focusModeBlock!.id && edge.to.id === child.id)
            .forEach(edge => (edge.visible = true));
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
                edges
                    .filter(edge => edge.from.id === parent.id && edge.to.id === current.block.id)
                    .forEach(edge => (edge.visible = true));
            });
        } else {
            // Get children if descending
            graph.getChildren(current.block).forEach(child => {
                queue.push({ block: child, depth: current.depth + 1, ascend: false });
                edges
                    .filter(edge => edge.from.id === current.block.id && edge.to.id === child.id)
                    .forEach(edge => (edge.visible = true));
            });
        }

        visited.add(current.block.id);
    }

    return edges;
});
let focusModeVisibleBlocks: Set<number> = $derived(
    new Set(
        focusModeEdges.filter(edge => edge.visible).flatMap(edge => [edge.from.id, edge.to.id]),
    ),
);

export function getFocusModeEdges(): { from: Block; to: Block; visible: boolean }[] {
    return focusModeEdges;
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
