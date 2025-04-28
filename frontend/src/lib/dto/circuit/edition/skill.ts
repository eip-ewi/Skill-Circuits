import type {IItem} from "../item";

export interface SkillItem extends IItem {
    essential: boolean;

    itemType: "skill";
}