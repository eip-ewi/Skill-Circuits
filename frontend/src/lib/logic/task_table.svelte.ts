import type { TaskInTaskList } from "../dto/task_in_task_list";

export function sortAscByString(a: string, b: string): -1 | 0 | 1 {
    return a === b ? 0 : a > b ? 1 : -1;
}

export function sortAscByType(a: TaskInTaskList, b: TaskInTaskList): number {
    if (a.taskItem.taskType === "regular" && b.taskItem.taskType === "choice") {
        return -1;
    } else if (a.taskItem.taskType === "choice" && b.taskItem.taskType === "regular") {
        return 1;
    }
    return sortAscByString(a.taskInfo.type, b.taskInfo.type);
}

export function sortAscByLink(a: TaskInTaskList, b: TaskInTaskList): number {
    if (a.taskInfo.link !== null && b.taskInfo.link !== null) {
        return sortAscByString(a.taskInfo.link, b.taskInfo.link);
    } else if (a.taskInfo.link !== null && b.taskInfo.link === null) {
        return 1;
    } else if (a.taskInfo.link === null && b.taskInfo.link !== null) {
        return -1;
    }
    return 0;
}
