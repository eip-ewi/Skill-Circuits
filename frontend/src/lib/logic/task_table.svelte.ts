import type { TaskTableColumn } from "../data/task_table_column";
import type { TaskInTaskList } from "../dto/task_in_task_list";

let columns: TaskTableColumn[] = $state([
    {
        name: "Task",
        sortable: true,
        sortStatus: 0,
        sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
            sortAscByString(a.taskInfo.name, b.taskInfo.name),
    },
    { name: "Paths", sortable: false },
    { name: "Type", sortable: true, sortStatus: 0, sortAsc: sortAscByType },
    {
        name: "Time",
        sortable: true,
        sortStatus: 0,
        sortAsc: (a: TaskInTaskList, b: TaskInTaskList) => a.taskInfo.time - b.taskInfo.time,
    },
    {
        name: "Skill",
        sortable: true,
        sortStatus: 0,
        sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
            sortAscByString(a.skillName, b.skillName),
    },
    {
        name: "Submodule",
        sortable: true,
        sortStatus: 0,
        sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
            sortAscByString(a.submoduleName, b.submoduleName),
    },
    {
        name: "Module",
        sortable: true,
        sortStatus: 0,
        sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
            sortAscByString(a.moduleName, b.moduleName),
    },
    { name: "Link", sortable: true, sortStatus: 0, sortAsc: sortAscByLink },
]);

export function getColumns(): TaskTableColumn[] {
    return columns;
}

function sortAscByString(a: string, b: string): -1 | 0 | 1 {
    return a === b ? 0 : a > b ? -1 : 1;
}

function sortAscByType(a: TaskInTaskList, b: TaskInTaskList): number {
    if (a.taskItem.taskType === "regular" && b.taskItem.taskType === "choice") {
        return 1;
    } else if (a.taskItem.taskType === "choice" && b.taskItem.taskType === "regular") {
        return -1;
    }
    return sortAscByString(a.taskInfo.type, b.taskInfo.type);
}

function sortAscByLink(a: TaskInTaskList, b: TaskInTaskList): number {
    if (a.taskInfo.link !== null && b.taskInfo.link !== null) {
        return sortAscByString(a.taskInfo.link, b.taskInfo.link);
    } else if (a.taskInfo.link !== null && b.taskInfo.link === null) {
        return -1;
    } else if (a.taskInfo.link === null && b.taskInfo.link !== null) {
        return 1;
    }
    return 0;
}
