import type { TaskInfo, TaskItem } from "./circuit/module/task";

export interface TaskInTaskList {
    taskItem: TaskItem;
    taskInfo: TaskInfo;
    skillName: string;
    submoduleName: string;
    moduleName: string;
}
