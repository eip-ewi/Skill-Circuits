import type { Block } from "../../../dto/circuit/block";
import { getLevel } from "../level.svelte";
import { withCsrf } from "../../csrf";
import { BlockStates } from "../../../data/block_state";
import { setScrollTarget } from "../scroll_target.svelte";

export async function updateBlockPosition(block: Block, newColumn: number) {
    let oldColumn = block.column;
    block.column = newColumn;
    let response = await fetch(`/api/${getLevel().blocks}/${block.id}/position?column=${newColumn}`, withCsrf({
        method: "PATCH",
    }));
    if (!response.ok) {
        block.column = oldColumn;
        block.state = BlockStates.Inactive;
    }
    else {
        setScrollTarget({ kind: "block", id: block.id });
    }
}

export async function removeBlockFromCircuit(block: Block) {
    let oldColumn = block.column;
    block.column = null;
    let response = await fetch(`/api/${getLevel().blocks}/${block.id}/position`, withCsrf({
        method: "DELETE",
    }));
    if (!response.ok) {
        block.column = oldColumn;
        block.state = BlockStates.Inactive;
    }
}
