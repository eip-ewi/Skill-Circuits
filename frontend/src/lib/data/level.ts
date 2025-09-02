export const ModuleLevel = {
    circuit: "module",
    circuits: "modules",
    group: "submodule",
    groups: "submodules",
    block: "skill",
    blocks: "skills",
    item: "task",
    items: "tasks",
}

export const EditionLevel = {
    circuit: "edition",
    circuits: "editions",
    group: "module",
    groups: "modules",
    block: "submodule",
    blocks: "submodules",
    item: "skill",
    items: "skills",
}

export const TrackLevel = {
    circuit: "track",
    circuits: "tracks",
    group: "edition",
    groups: "editions",
    block: "module",
    blocks: "modules",
    item: "submodule",
    items: "submodules",
}

export const ProgrammeLevel = {
    circuit: "programme",
    circuits: "programmes",
    group: "track",
    groups: "tracks",
    block: "edition",
    blocks: "editions",
    item: "module",
    items: "modules",
}

export type Level = typeof ModuleLevel | typeof EditionLevel | typeof TrackLevel | typeof ProgrammeLevel;
