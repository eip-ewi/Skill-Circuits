import type { Block } from "../../../dto/circuit/block";
import { getLevel } from "../level.svelte";
import { withCsrf } from "../../csrf";
import { getBlockForItem } from "../circuit.svelte";
import type { Item } from "../../../dto/circuit/item";

export async function createItem(block: Block) {
    const create: Record<string, unknown> = {
        name: `New ${getLevel().item}`,
    };
    create[getLevel().block] = { id: block.id };

    const response = await fetch(
        `/api/${getLevel().items}`,
        withCsrf({
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(create),
        }),
    );

    if (response.ok) {
        const newTask: Item = await response.json();
        // @ts-expect-error -- The response DTO omits this client-only discriminant.
        newTask.itemType = getLevel().item;
        (block.items as Item[]).push(newTask);
    }
}

export async function editItemName(item: Item, newName: string) {
    const oldName = item.name;
    item.name = newName;

    const response = await fetch(
        `/api/${getLevel().items}/${item.id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                name: newName,
            }),
        }),
    );

    if (!response.ok) {
        item.name = oldName;
    }
}

export async function deleteItem(item: Item) {
    const response = await fetch(
        `/api/${getLevel().items}/${item.id}`,
        withCsrf({
            method: "DELETE",
        }),
    );

    if (response.ok) {
        const block = getBlockForItem(item);
        block.items.splice(
            block.items.findIndex(i => i.id === item.id),
            1,
        );
    }
}
