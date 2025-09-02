import type {TaskItem} from "./task";
import type {Block, IBlock} from "../block";

export interface ISkillBlock extends IBlock {
    essential: boolean;
    external: boolean;
    items: TaskItem[];
    checkpoint: number | null;

    blockType: "skill";
}

export interface RegularSkillBlock extends ISkillBlock {
    hidden: boolean;
    external: false;
}

export interface ExternalSkillBlock extends ISkillBlock {
    hidden: undefined;
    external: true;
}

export type SkillBlock = RegularSkillBlock | ExternalSkillBlock;