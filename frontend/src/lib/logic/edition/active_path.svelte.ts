import { getEdition } from "./edition.svelte";
import type { Path } from "../../dto/path";
import { withCsrf } from "../csrf";
import type { Block } from "../../dto/circuit/block";
import type { Item } from "../../dto/circuit/item";
import { getLevel } from "../circuit/level.svelte";
import { ModuleLevel } from "../../data/level";
import { hasEditorRights, getAuthorisation } from "../authorisation.svelte";
import type { TaskItem } from "../../dto/circuit/module/task";

export const pathState: { activePath: Path | null; tasksAdded: number[]; tasksRemoved: number[] } =
    $state({
        activePath: null,
        tasksAdded: [],
        tasksRemoved: [],
    });

function removeTaskId(id: number, tasks: number[]): void {
    let index = tasks.indexOf(id);
    while (index !== -1) {
        tasks.splice(index, 1);
        index = tasks.indexOf(id);
    }
}

function addTaskId(id: number, tasks: number[]): void {
    if (!tasks.includes(id)) tasks.push(id);
}

function isTaskOnActivePathByDefault(task: TaskItem): boolean {
    //for courses that don't have paths
    if (pathState.activePath == null) return true;

    return task.paths.includes(pathState.activePath.id);
}

export function getActivePath(): Path | null {
    return pathState.activePath;
}

export function getItemsOnPath<B extends Block>(block: B): B["items"] {
    if (block.blockType !== "skill" || hasEditorRights()) {
        return block.items;
    }
    return block.items.filter(item => isTaskOnPath(item));
}

export function isTaskOnPath(task: TaskItem): boolean {
    if (pathState.activePath === null) {
        return !pathState.tasksRemoved.includes(task.id);
    }
    return (
        !pathState.tasksRemoved.includes(task.id) &&
        (task.paths.includes(pathState.activePath!.id) || pathState.tasksAdded.includes(task.id))
    );
}

export async function selectPath(path: Path) {
    let response = await fetch(
        `/api/paths/active?edition=${getEdition().id}&path=${path.id}`,
        withCsrf({
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
        }),
    );

    if (response.ok) {
        pathState.activePath = path;
    }
}

export async function fetchActivePath() {
    let response = await fetch(`/api/paths/active?edition=${getEdition().id}`);
    let body = await response.text();
    if (body === "") {
        pathState.activePath = null;
    } else {
        pathState.activePath = JSON.parse(body);
    }
}

export async function fetchPathCustomisation() {
    let response = await fetch(`/api/paths/customisation?edition=${getEdition().id}`);
    let customisation: { tasksAdded: number[]; tasksRemoved: number[] } = await response.json();
    pathState.tasksAdded = customisation.tasksAdded;
    pathState.tasksRemoved = customisation.tasksRemoved;
}

export async function addTaskToPath(task: TaskItem) {
    if (isTaskOnPath(task)) return;

    let response = await fetch(
        `/api/paths/tasks/${task.id}`,
        withCsrf({
            method: "POST",
        }),
    );

    if (response.ok) {
        // the task being in tasksAdded or tasksRemoved is relative to its default state on that path
        if (isTaskOnActivePathByDefault(task)) {
            removeTaskId(task.id, pathState.tasksRemoved);
            removeTaskId(task.id, pathState.tasksAdded);
        } else {
            addTaskId(task.id, pathState.tasksAdded);
            removeTaskId(task.id, pathState.tasksRemoved);
        }
    }
}

export async function removeTaskFromPath(task: TaskItem) {
    if (!isTaskOnPath(task)) return;

    let response = await fetch(
        `/api/paths/tasks/${task.id}`,
        withCsrf({
            method: "DELETE",
        }),
    );

    if (response.ok) {
        // the task being in tasksAdded or tasksRemoved is relative to its default state on that path
        if (isTaskOnActivePathByDefault(task)) {
            removeTaskId(task.id, pathState.tasksAdded);
            addTaskId(task.id, pathState.tasksRemoved);
        } else {
            removeTaskId(task.id, pathState.tasksAdded);
            removeTaskId(task.id, pathState.tasksRemoved);
        }
    }
}
