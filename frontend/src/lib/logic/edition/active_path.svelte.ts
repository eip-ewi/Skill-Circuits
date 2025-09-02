import {getEdition} from "./edition.svelte";
import type {Path} from "../../dto/path";
import {withCsrf} from "../csrf";
import type {Block} from "../../dto/circuit/block";
import type {Item} from "../../dto/circuit/item";
import {getLevel} from "../circuit/level.svelte";
import {ModuleLevel} from "../../data/level";
import {canEditCircuit, getAuthorisation} from "../authorisation.svelte";
import type {TaskItem} from "../../dto/circuit/module/task";

let activePath: Path | null = $state(null);
let tasksAdded: number[] = $state([]);
let tasksRemoved: number[] = $state([]);

export function getActivePath(): Path | null {
    return activePath;
}

export function getItemsOnPath<B extends Block>(block: B): B["items"] {
    if (block.blockType !== "skill" || canEditCircuit()) {
        return block.items;
    }
    return block.items.filter(item => isTaskOnPath(item));
}

export function isTaskOnPath(task: TaskItem): boolean {
    if (activePath === null) {
        return !tasksRemoved.includes(task.id);
    }
    return !tasksRemoved.includes(task.id) && (task.paths.includes(activePath!.id) || tasksAdded.includes(task.id))
}

export async function selectPath(path: Path) {
    let response = await fetch(`/api/paths/active?edition=${getEdition().id}&path=${path.id}`, withCsrf({
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
    }));

    if (response.ok) {
        activePath = path;
    }
}

export async function fetchActivePath() {
    let response = await fetch(`/api/paths/active?edition=${getEdition().id}`);
    let body = await response.text();
    if (body === "") {
        activePath = null;
    } else {
        activePath = JSON.parse(body);
    }
}

export async function fetchPathCustomisation() {
    let response = await fetch(`/api/paths/customisation?edition=${getEdition().id}`);
    let customisation: { tasksAdded: number[], tasksRemoved: number[] } = await response.json();
    tasksAdded = customisation.tasksAdded;
    tasksRemoved = customisation.tasksRemoved;
}

export async function addTaskToPath(task: TaskItem) {
    let response = await fetch(`/api/paths/tasks/${task.id}`, withCsrf({
        method: "POST",
    }));
    if (response.ok) {
        if (tasksRemoved.includes(task.id)) {
            tasksRemoved.splice(tasksRemoved.indexOf(task.id), 1);
        } else {
            tasksAdded.push(task.id);
        }
    }
}

export async function removeTaskFromPath(task: TaskItem) {
    let response = await fetch(`/api/paths/tasks/${task.id}`, withCsrf({
        method: "DELETE",
    }));
    if (response.ok) {
        if (tasksAdded.includes(task.id)) {
            tasksAdded.splice(tasksAdded.indexOf(task.id), 1);
        } else {
            tasksRemoved.push(task.id);
        }
    }
}