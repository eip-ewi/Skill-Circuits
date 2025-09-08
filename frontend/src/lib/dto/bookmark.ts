import type {ChoiceTaskItem, TaskItem} from "./circuit/module/task";
import type {RegularSkillBlock, SkillBlock} from "./circuit/module/skill";

export interface IBookmarkList {
    id: number;
    name: string;
    lastModified: string;

    skills: BookmarkListSkill[]
    tasks: BookmarkListTask[],

    editing: boolean | undefined,
    collapsed: boolean | undefined,
}

export interface PersonalBookmarkList extends IBookmarkList {
    skill: null,
}

export interface HiddenSkillBookmarkList extends IBookmarkList {
    skill: number;
}

export type BookmarkList = PersonalBookmarkList | HiddenSkillBookmarkList;

export interface BookmarkListSkill extends RegularSkillBlock {
    id: number;
    name: string;
    qualifiedName: string;

    items: TaskItem[];
}

export interface IBookmarkListTask {
    name: string;
    qualifiedName: string;
}

export type BookmarkListTask = TaskItem & IBookmarkListTask;
