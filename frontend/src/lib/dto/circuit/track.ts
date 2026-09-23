import type { IBlock } from "./block";
import type { ICircuit } from "./circuit";
import type { IGroup } from "./group";
import type { IItem } from "./item";

export interface TrackSubmoduleItem extends IItem {
    itemType: "submodule";
}

export interface TrackModuleBlock extends IBlock {
    items: TrackSubmoduleItem[];
    blockType: "module";
}

export interface TrackEditionGroup extends IGroup {
    blocks: TrackModuleBlock[];
    groupType: "edition";
}

export interface TrackCircuit extends ICircuit {
    groups: TrackEditionGroup[];
    circuitType: "track";
}
