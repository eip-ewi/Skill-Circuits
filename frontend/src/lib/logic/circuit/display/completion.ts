import type {Block} from "../../../dto/circuit/block";
import {isUnlocked} from "./unlock";
import {getItemsOnPath} from "../../edition/active_path.svelte";
import type {SkillBlock} from "../../../dto/circuit/module/skill";

export function isCompleted(block: Block, recursionCheck: number = 100): boolean {

    if (recursionCheck <= 0) {
        return false;
    }

    let items = getItemsOnPath(block);

    let allEssentialItemsCompleted = !items
        .filter(item => item.itemType !== "skill" || item.essential)
        .some(item => !item.completed);

    if (!allEssentialItemsCompleted) {
        return false;
    }

    let allItemsUnlocked = !items.some(item => item.locked)

    if (!allItemsUnlocked) {
        return false;
    }

    let isEmpty = items.length === 0;

    if (isEmpty) {
        return isUnlocked(block, recursionCheck);
    }

    return true;

}
