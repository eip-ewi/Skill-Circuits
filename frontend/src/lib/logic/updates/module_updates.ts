import { withCsrf } from "../csrf";
import { getEdition, getModules } from "../edition/edition.svelte";
import type { Module } from "../../dto/module";
import { getCircuit } from "../circuit/circuit.svelte";
import { isLevel } from "../circuit/level.svelte";
import { EditionLevel } from "../../data/level";

export async function createModule(): Promise<Module | undefined> {
    const response = await fetch(
        `/api/modules`,
        withCsrf({
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
        }),
    );

    if (response.ok) {
        const module = await response.json();
        if (isLevel(EditionLevel)) {
            getCircuit().groups.push(module);
        }
        getModules().unshift(module);
        return getModules()[0]!;
    }
    return undefined;
}

export async function editModuleName(module: Module, newName: string) {
    const oldName = module.name;
    module.name = newName;
    if (isLevel(EditionLevel)) {
        getCircuit().groups.find(g => g.id === module.id)!.name = newName;
    }

    const response = await fetch(
        `/api/modules/${module.id}`,
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
        if (isLevel(EditionLevel)) {
            getCircuit().groups.find(g => g.id === module.id)!.name = oldName;
        }
        module.name = oldName;
    }
}

export async function deleteModule(module: Module) {
    const response = await fetch(
        `/api/modules/${module.id}`,
        withCsrf({
            method: "DELETE",
        }),
    );

    if (response.ok) {
        if (isLevel(EditionLevel)) {
            getCircuit().groups.splice(
                getCircuit().groups.findIndex(g => g.id === module.id),
                1,
            );
        }
        getModules().splice(getModules().findIndex(m => m.id === module.id)!, 1);
    }
}
