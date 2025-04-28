import type {SkillItem} from "./skill";
import type {Block, IBlock} from "../block";

export interface SubmoduleBlock extends IBlock {
    items: SkillItem[];

    blockType: "submodule";
}