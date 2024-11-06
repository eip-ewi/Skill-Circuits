export const ModuleLevel = {
    circuit: "module",
    group: "submodule",
    block: "skill",
    item: "task",
}

export const EditionLevel = {
    circuit: "edition",
    group: "module",
    block: "submodule",
    item: "skill",
}

export type Level = typeof ModuleLevel | typeof EditionLevel;
