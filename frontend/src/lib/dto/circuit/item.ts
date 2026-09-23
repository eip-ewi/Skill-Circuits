import type { SkillItem } from "./edition/skill";
import type { TaskItem } from "./module/task";
import type { TrackSubmoduleItem } from "./track";

export interface IItem {
    id: number;
    name: string;
    completed: boolean;
    locked: boolean;
}

export type Item = SkillItem | TaskItem | TrackSubmoduleItem;
