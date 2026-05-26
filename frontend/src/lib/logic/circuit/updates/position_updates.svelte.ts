import type { Block } from "../../../dto/circuit/block";
import { getLevel } from "../level.svelte";
import { withCsrf } from "../../csrf";
import { BlockStates } from "../../../data/block_state";
import { getBlocks, getCircuit } from "../circuit.svelte";
import { setScrollTarget } from "../scroll_target.svelte";

export async function updateBlockPosition(block: Block, newColumn: number) {
    let oldColumn = block.column;
    block.column = newColumn;
    let response = await fetch(
        `/api/${getLevel().blocks}/${block.id}/position?column=${newColumn}`,
        withCsrf({
            method: "PATCH",
        }),
    );
    if (!response.ok) {
        block.column = oldColumn;
        block.state = BlockStates.Inactive;
    } else {
        setScrollTarget({ kind: "block", id: block.id });
    }
}

export async function removeBlockFromCircuit(block: Block) {
    let oldColumn = block.column;
    block.column = null;
    let response = await fetch(
        `/api/${getLevel().blocks}/${block.id}/position`,
        withCsrf({
            method: "DELETE",
        }),
    );
    if (!response.ok) {
        block.column = oldColumn;
        block.state = BlockStates.Inactive;
    }
}

export async function insertColumn(afterIndex: number) {
    getCircuit().width = (getCircuit().width ?? 5) + 1;

    let blocksToShift = getBlocks().filter(block => block.column! > afterIndex);
    const moveToRight: (block: Block) => number = block => block.column! + 1;

    if (!(await updateColumnPositions(blocksToShift, moveToRight)))
        getCircuit().width = (getCircuit().width ?? 5) - 1;
}
export async function removeColumn(afterIndex: number) {
    const oldWidth = getCircuit().width ?? 5;
    if (oldWidth < 2) return;
    getCircuit().width = oldWidth - 1;
    // moving 0th column to the right is the same as moving column 1 to the left
    if (afterIndex == 0) afterIndex++;

    let blocksToShift = getBlocks().filter(block => block.column! >= afterIndex);
    const moveToLeft: (block: Block) => number = block => block.column! - 1;

    if (!(await updateColumnPositions(blocksToShift, moveToLeft))) getCircuit().width = oldWidth;
}

async function updateColumnPositions(blocks: Block[], getNewColumn: (block: Block) => number) {
    if (blocks.length === 0) return true;

    const previous = blocks.map(block => ({ block: block, oldColumn: block.column }));
    blocks.forEach(block => (block.column = getNewColumn(block)));

    const blockIdProperty = getLevel().block;
    const updates = blocks.map(block => ({
        [blockIdProperty]: { id: block.id },
        column: block.column,
    }));
    let response = await fetch(
        `/api/${getLevel().blocks}/positions`,
        withCsrf({
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ updates }),
        }),
    );

    if (!response.ok) {
        previous.forEach(blockStore => (blockStore.block.column = blockStore.oldColumn));
        return false;
    }

    return true;
}
