import type {TaskInTaskList} from "../dto/task_in_task_list";

export interface TaskTableColumn {
    name: string;
}

export interface SortableTaskTableColumn extends TaskTableColumn {
    sortable: true;
    sortStatus: -1 | 0 | 1 | undefined;
    sortAsc: undefined | ((a: TaskInTaskList, b: TaskInTaskList) => number);
}

export interface UnsortableTaskTableColumn extends TaskTableColumn {
    sortable: false;
}