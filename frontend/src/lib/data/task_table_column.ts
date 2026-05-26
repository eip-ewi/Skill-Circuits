import type { TaskInTaskList } from "../dto/task_in_task_list";

export interface ITaskTableColumn {
    name: string;
}

export interface SortableTaskTableColumn extends ITaskTableColumn {
    sortable: true;
    sortStatus: -1 | 0 | 1;
    sortAsc: (a: TaskInTaskList, b: TaskInTaskList) => number;
}

export interface UnsortableTaskTableColumn extends ITaskTableColumn {
    sortable: false;
}

export type TaskTableColumn = SortableTaskTableColumn | UnsortableTaskTableColumn;
