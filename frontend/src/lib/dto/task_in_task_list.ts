export interface ITaskInTaskList {
    paths: number[];
    skillName: string;
    submoduleName: string;
    moduleName: string;

    itemType: "task";
}

export interface ITaskInfoInTaskList {
    infoId: number;
    name: string;
    type: string;
    time: number;
    link: string | null;
}

export interface RegularTaskInTaskList extends ITaskInTaskList, ITaskInfoInTaskList {
    taskType: "regular";
}

export interface ChoiceTaskChoiceInTaskList extends ITaskInfoInTaskList {
    taskType: "choice";
}

export interface ChoiceTaskInTaskList extends ITaskInTaskList {
    minTasks: number;
    tasks: ChoiceTaskChoiceInTaskList[];

    taskType: "choice";
}

export type TaskInTaskList = RegularTaskInTaskList | ChoiceTaskInTaskList;
export type TaskInfoInTaskList = ITaskInfoInTaskList | ChoiceTaskChoiceInTaskList;