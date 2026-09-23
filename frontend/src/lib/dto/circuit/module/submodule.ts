import type { RegularSkillBlock } from "./skill";
import type { IGroup } from "../group";

export interface SubmoduleGroup extends IGroup {
    blocks: RegularSkillBlock[];

    groupType: "submodule";
}
