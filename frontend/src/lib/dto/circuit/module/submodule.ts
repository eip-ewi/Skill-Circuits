import type {RegularSkillBlock, SkillBlock} from "./skill";
import type {Group, IGroup} from "../group";

export interface SubmoduleGroup extends IGroup {
    blocks: RegularSkillBlock[];

    groupType: "submodule";
}