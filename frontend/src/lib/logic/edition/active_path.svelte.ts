import {getEdition} from "./edition.svelte";
import type {Path} from "../../dto/path";
import {withCsrf} from "../csrf";
import type {Block} from "../../dto/circuit/block";
import type {Item} from "../../dto/circuit/item";
import {getLevel} from "../circuit/level.svelte";
import {ModuleLevel} from "../../data/level";
import {canEditCircuit, getAuthorisation} from "../authorisation.svelte";

let activePath: Path | null = $state(null);

export function getActivePath(): Path | null {
    return activePath;
}

export function getItemsOnPath<B extends Block>(block: B): B["items"] {
    if (activePath === null || block.blockType !== "skill" || canEditCircuit()) {
        return block.items;
    }
    return block.items.filter(item => item.paths.includes(activePath!.id));
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

export function resetActivePath() {
    activePath = null;
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