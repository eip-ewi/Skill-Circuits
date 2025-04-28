import {withCsrf} from "../csrf";
import {getCircuit} from "../circuit/circuit.svelte";
import type {Path} from "../../dto/path";
import {getEdition, getPaths} from "../edition/edition.svelte";

export async function createPath(): Promise<Path | undefined> {
    let response = await fetch(`/api/paths`, withCsrf({
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: "New path",
            description: "",
            edition: {
                id: getEdition().id,
            },
        }),
    }));

    if (response.ok) {
        let path = await response.json();

        getPaths().unshift(path);

        let circuit = getCircuit();
        if (circuit.circuitType === "module") {
            circuit.groups.forEach(group => group.blocks.forEach(block => block.items.forEach(item => item.paths.push(path.id))));
        }

        return getPaths()[0]!;
    }
    return undefined;
}

export async function editPathName(path: Path, newName: string) {
    let oldName = path.name;
    path.name = newName;

    let response = await fetch(`/api/paths/${path.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: newName,
        }),
    }));

    if (!response.ok) {
        path.name = oldName;
    }
}

export async function editPathDescription(path: Path, newDescription: string) {
    let oldDescription = path.description;
    path.description = newDescription;

    let response = await fetch(`/api/paths/${path.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            description: newDescription,
        }),
    }));

    if (!response.ok) {
        path.description = oldDescription;
    }
}

export async function deletePath(path: Path) {
    let response = await fetch(`/api/paths/${path.id}`, withCsrf({
        method: "DELETE",
    }));

    if (response.ok) {
        getPaths().splice(getPaths().findIndex(p => p.id === path.id)!, 1);

        let circuit = getCircuit();
        if (circuit.circuitType === "module") {
            circuit.groups.forEach(group => group.blocks.forEach(block => block.items
                .filter(item => item.paths.includes(path.id))
                .forEach(item => item.paths.splice(item.paths.indexOf(path.id)!, 1))));
        }
    }
}
