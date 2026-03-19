import type {TaskItem} from "./circuit/module/task";

export interface TaskInTaskList {
    taskItem: TaskItem;
    skillName: string;
    submoduleName: string;
    moduleName: string;
}

/* export interface RegularTaskInTaskList extends ITaskInTaskList, ITaskInfoInTaskList {
    taskType: "regular";
}

export interface ChoiceTaskChoiceInTaskList extends ITaskInfoInTaskList {
    taskType: "choice";
}

export interface ChoiceTaskInTaskList extends ITaskInTaskList {
    minTasks: number;
    tasks: ChoiceTaskChoiceInTaskList[];

    taskType: "choice";
} */

// export type TaskInTaskList = RegularTaskInTaskList | ChoiceTaskInTaskList;
// export type TaskInfoInTaskList = ITaskInfoInTaskList | ChoiceTaskChoiceInTaskList;