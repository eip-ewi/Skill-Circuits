import {getItem} from "./circuit.svelte";
import type {TaskItem} from "../../dto/circuit/module/task";
import {addTaskToPath} from "../edition/active_path.svelte";

let dragging: boolean = $state(false);

export function dragEnter(event: DragEvent) {
    if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
        return;
    }
    event.preventDefault();
    dragging = true;
}

export function dragLeave() {
    dragging = false;
}

export function dragOver(event: DragEvent) {
    if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
        return;
    }
    event.preventDefault();
    dragging = true;
}

export async function drop(event: DragEvent) {
    if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
        return;
    }
    event.preventDefault();

    let itemId = parseInt(event.dataTransfer!.getData("skill-circuits/item"));
    let item = getItem(itemId) as TaskItem;

    await addTaskToPath(item);

    dragging = false;
}

export function getDragging() {
    return dragging;
}