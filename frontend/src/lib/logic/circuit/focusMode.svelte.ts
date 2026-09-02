import type { Block } from "../../dto/circuit/block";
import { getGraph } from "./circuit.svelte";
import type { Graph } from "./graph";
import { type FocusModeBlockState, FocusModeBlockStates } from "../../data/focus_mode_block_state";
import { SvelteSet } from "svelte/reactivity";

let focusModeBlock: Block | null = $state(null);
const focusModeVisibleBlocks: Set<number> = $derived.by(() => {
    const graph: Graph = getGraph();
    const blocks: SvelteSet<number> = new SvelteSet();

    // Safety check
    if (focusModeBlock === null) {
        return blocks;
    }

    // Initialize visited blocks and block queue
    const visited: SvelteSet<number> = new SvelteSet();
    const queue: { block: Block; depth: number; ascend: boolean }[] = [
        { block: focusModeBlock!, depth: 0, ascend: true },
        { block: focusModeBlock!, depth: 0, ascend: false },
    ];
    blocks.add(focusModeBlock.id);

    // Add first level from initial block
    graph.getParents(focusModeBlock!).forEach(parent => {
        queue.push({ block: parent, depth: 1, ascend: true });
        blocks.add(parent.id);
    });
    graph.getChildren(focusModeBlock!).forEach(child => {
        queue.push({ block: child, depth: 1, ascend: false });
        blocks.add(child.id);
    });
    visited.add(focusModeBlock!.id);

    // Traverse the graph, ascending and descending depending on direction
    while (queue.length > 0) {
        const current: { block: Block; depth: number; ascend: boolean } = queue.shift()!;

        // Stop if visited or above max depth
        if (stopTraversal(current) || visited.has(current.block.id)) {
            continue;
        }

        if (current.ascend) {
            // Get parents if ascending
            graph.getParents(current.block).forEach(parent => {
                queue.push({ block: parent, depth: current.depth + 1, ascend: true });
                blocks.add(parent.id);
            });
        } else {
            // Get children if descending
            graph.getChildren(current.block).forEach(child => {
                queue.push({ block: child, depth: current.depth + 1, ascend: false });
                blocks.add(child.id);
            });
        }

        visited.add(current.block.id);
    }

    return blocks;
});

function stopTraversal(current: { block: Block; depth: number; ascend: boolean }) {
    // Only include one level of descendants
    return !current.ascend && current.depth >= 1;
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
}

export function toggleFocusMode(clickedBlock: Block) {
    // States of all blocks are updated within the block component

    if (getFocusModeBlock() !== clickedBlock) {
        // Set this block to focus mode
        focusModeBlock = clickedBlock;
    } else {
        // Stop focus mode
        focusModeBlock = null;
    }
}

export function getFocusModeBlock() {
    return focusModeBlock;
}

export function isInFocusMode(): boolean {
    return focusModeBlock !== null;
}

export function getFocusModeState(id: number): FocusModeBlockState {
    if (isInFocusMode()) {
        if (getFocusModeBlock()!.id === id) {
            return FocusModeBlockStates.FocusOnBlock;
        } else if (visibleInFocusMode(id)) {
            return FocusModeBlockStates.VisibleInFocusMode;
        } else {
            return FocusModeBlockStates.DisabledInFocusMode;
        }
    } else {
        return FocusModeBlockStates.NotInFocusMode;
    }
}

export function visibleInFocusMode(id: number): boolean {
    return focusModeVisibleBlocks.has(id);
}
