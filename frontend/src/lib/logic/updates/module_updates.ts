import type {Checkpoint} from "../../dto/checkpoint";
import {withCsrf} from "../csrf";
import moment from "moment";
import {getCheckpoints, getEdition, getModules, getSortedCheckpoints} from "../edition/edition.svelte";
import type {Module} from "../../dto/module";

export async function createModule(): Promise<Module | undefined> {
    let response = await fetch(`/api/modules`, withCsrf({
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: "New module",
            edition: {
                id: getEdition().id,
            },
        }),
    }));

    if (response.ok) {
        let module = await response.json();
        getModules().unshift(module);
        return getModules()[0]!;
    }
    return undefined;
}

export async function editModuleName(module: Module, newName: string) {
    let oldName = module.name;
    module.name = newName;

    let response = await fetch(`/api/modules/${module.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: newName,
        }),
    }));

    if (!response.ok) {
        module.name = oldName;
    }
}

export async function deleteModule(module: Module) {
    let response = await fetch(`/api/modules/${module.id}`, withCsrf({
        method: "DELETE",
    }));

    if (response.ok) {
        getModules().splice(getModules().findIndex(c => c.id === module.id)!, 1);
    }
}
