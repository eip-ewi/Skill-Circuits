import type {IItem} from "../item";

export interface ITaskItem extends IItem {
    paths: number[];

    itemType: "task";
}

export interface ITaskInfo {
    infoId: number;
    name: string;
    completed: boolean;
    type: string;
    time: number;
    link: string | null;
}

export interface RegularTaskItem extends ITaskItem, ITaskInfo {
    taskType: "regular";
}

export interface ChoiceTaskChoice extends ITaskInfo {
    taskType: "choice";
}

export interface ChoiceTaskItem extends ITaskItem {
    minTasks: number;
    tasks: ChoiceTaskChoice[];

    taskType: "choice";
}

export type TaskItem = RegularTaskItem | ChoiceTaskItem;
export type TaskInfo = RegularTaskItem | ChoiceTaskChoice;