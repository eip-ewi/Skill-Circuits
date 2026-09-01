import type { SkillItem } from "./skill";
import type { IBlock } from "../block";

export interface SubmoduleBlock extends IBlock {
    items: SkillItem[];

    blockType: "submodule";
}
