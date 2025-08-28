import type {IItem} from "../item";

export interface SkillItem extends IItem {
    essential: boolean;
    hidden: boolean;

    itemType: "skill";
}