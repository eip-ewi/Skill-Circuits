import type { Block } from "./block";
import type { ModuleGroup } from "./edition/module";
import type { SubmoduleGroup } from "./module/submodule";
import type { TrackEditionGroup } from "./track";

export interface IGroup {
    id: number;
    name: string;
    blocks: Block[];
}

export type Group = ModuleGroup | SubmoduleGroup | TrackEditionGroup;
