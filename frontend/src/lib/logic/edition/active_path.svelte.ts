import {getEdition} from "./edition.svelte";
import type {Path} from "../../dto/path";
import {withCsrf} from "../csrf";
import type {Block} from "../../dto/circuit/block";
import {getAuthorisation, hasEditorRights, isTeacherForCircuit} from "../authorisation.svelte";
import type {TaskItem} from "../../dto/circuit/module/task";

let activePath: Path | null = $state(null);
let tasksAdded: number[] = $state([]);
let tasksRemoved: number[] = $state([]);

export function getActivePath(): Path | null {
    return activePath;
}

function isTeacherPreviewMode(): boolean {
    return getAuthorisation().viewMode === "VIEWER" && isTeacherForCircuit();
}

export function getItemsOnPath<B extends Block>(block: B): B["items"] {
    // In teacher preview mode we show the same full task list as editor mode.
    if (block.blockType !== "skill" || hasEditorRights() || isTeacherPreviewMode()) {
        return block.items;
    }
    return block.items.filter(item => isTaskOnPath(item));
}

export function isTaskOnPath(task: TaskItem): boolean {
    const teacherPreview = isTeacherPreviewMode();

    // Teacher preview should not be affected by teacher-specific customisation.
    if (!teacherPreview && tasksRemoved.includes(task.id)) {
        return false;
    }
    if (!teacherPreview && tasksAdded.includes(task.id)) {
        return true;
    }

    if (activePath === null) {
        return true;
    }

    return task.paths.length === 0 || task.paths.includes(activePath.id);
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