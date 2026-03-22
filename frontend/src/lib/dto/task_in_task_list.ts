import type {ITaskInfo, ITaskItem, TaskInfo, TaskItem} from "./circuit/module/task";

export interface TaskInTaskList {
    taskItem: TaskItem;
    taskInfo: TaskInfo | undefined;
    skillName: string;
    submoduleName: string;
    moduleName: string;
}
