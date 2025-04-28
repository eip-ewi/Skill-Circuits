import type {TaskItem} from "./task";
import type {Block, IBlock} from "../block";

export interface ISkillBlock extends IBlock {
    essential: boolean;
    external: boolean;
    items: TaskItem[];

    blockType: "skill";
}

export interface RegularSkillBlock extends ISkillBlock {
    checkpoint: number | null;
    external: false;
}

export interface ExternalSkillBlock extends ISkillBlock {
    checkpoint: null;
    external: true;
}

export type SkillBlock = RegularSkillBlock | ExternalSkillBlock;