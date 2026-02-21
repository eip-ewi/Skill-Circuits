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

export function getDragging() {
    return dragging;
}

export function setDragging(value: boolean) {
    dragging = value;
}