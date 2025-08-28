import type { Block } from "../../../dto/circuit/block";
import {getLevel} from "../level.svelte";
import {withCsrf} from "../../csrf";
import type {SkillBlock} from "../../../dto/circuit/module/skill";
import type {Group} from "../../../dto/circuit/group";
import {getBlockForItem, getBlocks, getCircuit, getGroup, getGroupForBlock} from "../circuit.svelte";
import {BlockStates} from "../../../data/block_state";
import type {Item} from "../../../dto/circuit/item";

export async function createItem(block: Block) {
    let create: any = {
        name: `New ${getLevel().item}`,
    };
    create[getLevel().block] = { id: block.id };

    let response = await fetch(`/api/${getLevel().items}`, withCsrf({
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(create),
    }));

    if (response.ok) {
        let newTask: Item = await response.json();
        // @ts-ignore
        newTask.itemType = getLevel().item;
        (block.items as Item[]).push(newTask);
    }
}

export async function editItemName(item: Item, newName: string) {
    let oldName = item.name;
    item.name = newName;

    let response = await fetch(`/api/${getLevel().items}/${item.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: newName,
        }),
    }));

    if (!response.ok) {
        item.name = oldName;
    }
}

export async function deleteItem(item: Item) {
    let response = await fetch(`/api/${getLevel().items}/${item.id}`, withCsrf({
        method: "DELETE",
    }));

    if (response.ok) {
        let block = getBlockForItem(item);
        block.items.splice(block.items.findIndex(i => i.id === item.id), 1);
    }
}
