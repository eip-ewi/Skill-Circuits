import type {Block} from "../../../dto/circuit/block";
import {isCompleted} from "./completion";
import {getGraph} from "../circuit.svelte";
import {getItemsOnPath} from "../../edition/active_path.svelte";
import {getBlurBlocks} from "../../preferences.svelte";
import type {Graph} from "../graph";

export function isUnlocked(block: Block, graph: Graph = getGraph(), recursionCheck: number = 100): boolean {

    if (recursionCheck <= 0) {
        return false;
    }

    let items = getItemsOnPath(block);

    if (block.blockType !== "skill") {
        // any item unlocked
        return items.some(item => !item.locked);
    }

    if (block.column === null) {
        return false;
    }

    let anyTaskCompleted = items.some(item => {
        if (item.itemType !== "task" || item.taskType !== "choice") {
            return item.completed;
        }
        return item.tasks.some(subtask => subtask.completed);
    });

    if (anyTaskCompleted) {
        return true;
    }

    let allEssentialParentsCompleted = graph.has(block) && !graph.getParents(block)
        .filter(parent => parent.blockType !== "skill" || parent.essential)
        .some(parent => !isCompleted(parent, graph, recursionCheck));

    if (!allEssentialParentsCompleted) {
        return false;
    }

    let allParentsUnlocked = graph.has(block) && !graph.getParents(block).some(parent => !isUnlocked(parent, graph, recursionCheck - 1));

    if (!allParentsUnlocked) {
        return false;
    }

    // any task completed
    return true;
}