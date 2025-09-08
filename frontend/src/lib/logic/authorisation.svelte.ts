import type {SimpleAuth} from "../dto/auth/simple";
import {getLevel, isLevel} from "./circuit/level.svelte";
import {getCircuit} from "./circuit/circuit.svelte";
import type {Authorisation} from "../dto/auth/authorisation";
import {withCsrf} from "./csrf";
import {getEdition} from "./edition/edition.svelte";
import {EditionLevel, ModuleLevel} from "../data/level";

let authorisation: Authorisation = $state({ viewMode: "VIEWER", isAdmin: false, managedCourses: [], managedEditions: [] });

export function getAuthorisation(): Authorisation {
    return authorisation;
}

export async function fetchAuthorisation() {
    let response = await fetch("/api/auth/authorisation");
    authorisation = await response.json();
}

async function sendNewViewMode(viewMode: "VIEWER" | "EDITOR" | "ADMIN") {
    await fetch(`/api/auth/view-mode?viewMode=${viewMode}`, withCsrf({
        method: "POST",
    }));
}

export async function toggleViewMode() {
    if (authorisation.viewMode === "VIEWER") {
        await setViewMode("EDITOR");
    } else {
        await setViewMode("VIEWER");
    }
}

export async function setViewMode(viewMode: "VIEWER" | "EDITOR" | "ADMIN") {
    authorisation.viewMode = viewMode;
    await sendNewViewMode(viewMode);
}

export function canEditCircuit() {
    if (authorisation.viewMode === "VIEWER") {
        return false;
    }
    if (authorisation.viewMode === "ADMIN") {
        return true;
    }
    return isEditorForCircuit();
}

export function isEditorForCircuit() {
    if (isLevel(ModuleLevel) || isLevel(EditionLevel)) {
        return authorisation.managedEditions.includes(getEdition().id);
    }
    return false;
}

export function isEditorForAny() {
    return authorisation.managedEditions.length > 0;
}
