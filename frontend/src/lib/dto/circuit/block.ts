import type {Item} from "./item";
import type {BlockState} from "../../data/block_state";
import type {BlockAction} from "../../data/block_action";
import type {SubmoduleBlock} from "./edition/submodule";
import type {SkillBlock} from "./module/skill";

export interface IBlock {
    id: number;
    name: string;
    column: number | null;
    row: number | undefined;
    parents: number[];
    children: number[];
    items: Item[];

    preview: boolean | undefined;
    boundingRect: (() => DOMRect) | undefined;
    state: BlockState | undefined;
}

export type Block = SubmoduleBlock | SkillBlock;