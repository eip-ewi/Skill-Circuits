import type {Block} from "../../../dto/circuit/block";
import {isCompleted} from "./completion";
import {getGraph} from "../circuit.svelte";
import {getItemsOnPath} from "../../edition/active_path.svelte";

export function isUnlocked(block: Block, recursionCheck: number = 100): boolean {

    if (recursionCheck <= 0) {
        return false;
    }

    let items = getItemsOnPath(block);

    if (block.blockType !== "skill") {
        // any item unlocked
        return items.some(item => !item.locked);
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

    let allEssentialParentsCompleted = getGraph().has(block) && !getGraph().getParents(block)
        .filter(parent => parent.blockType !== "skill" || parent.essential)
        .some(parent => !isCompleted(parent, recursionCheck));

    if (!allEssentialParentsCompleted) {
        return false;
    }

    let allParentsUnlocked = getGraph().has(block) && !getGraph().getParents(block).some(parent => !isUnlocked(parent, recursionCheck - 1));

    if (!allParentsUnlocked) {
        return false;
    }

    // any task completed
    return true;
}