<script lang="ts">
    import TaskTableRow from "./TaskTableRow.svelte";
    import type { TaskInTaskList } from "../../dto/task_in_task_list";
    import Button from "../util/Button.svelte";
    import { getColumns } from "../../logic/task_table.svelte";
    import type { SortableTaskTableColumn } from "../../data/task_table_column";

    let { tasks }: { tasks: TaskInTaskList[] } = $props();

    function sortByColumn(column: SortableTaskTableColumn, order: -1 | 1) {
        tasks.sort((a: TaskInTaskList, b: TaskInTaskList) => column.sortAsc(a, b) * order);

        getColumns().forEach(col => {
            if (col.sortable) {
                if (col === column) {
                    col.sortStatus = order;
                } else {
                    col.sortStatus = 0;
                }
            }
        });
    }
</script>

<table class="task_table">
    <thead class="table_header">
        <tr>
            {#each getColumns() as column}
                <th>
                    <div class="cell-wrapper">
                        {column.name}
                        {#if column.sortable}
                            {#if column.sortStatus === -1}
                                <Button
                                    aria-label="Sort ascendingly by {column.name}"
                                    onclick={() => {
                                        sortByColumn(column, 1);
                                    }}
                                    square={true}
                                    style="margin-left: 1em; font-size: var(--font-size-100)">
                                    <i class="fa-solid fa-caret-down"></i>
                                </Button>
                            {:else if column.sortStatus === 0}
                                <Button
                                    aria-label="Sort by {column.name}"
                                    onclick={() => {
                                        sortByColumn(column, -1);
                                    }}
                                    square={true}
                                    style="margin-left: 1em; font-size: var(--font-size-100)">
                                    <i class="fa-solid fa-sort"></i>
                                </Button>
                            {:else if column.sortStatus === 1}
                                <Button
                                    aria-label="Sort descendingly by {column.name}"
                                    onclick={() => {
                                        sortByColumn(column, -1);
                                    }}
                                    square={true}
                                    style="margin-left: 1em; font-size: var(--font-size-100)">
                                    <i class="fa-solid fa-caret-up"></i>
                                </Button>
                            {/if}
                        {/if}
                    </div>
                </th>
            {/each}
        </tr>
    </thead>
    <tbody>
        {#each tasks as task}
            <TaskTableRow {task}></TaskTableRow>
        {/each}
    </tbody>
</table>

<style>
    .task_table {
        font-size: clamp(0.5rem, calc(16 / 1732 * 100vw * 1.2), 1.5rem);
        margin: 2em auto 4em;
        text-align: left;
        padding: 1em;
        background-color: var(--group-colour);
        color: var(--on-group-colour-higher-contrast);
        border-radius: var(--group-border-radius);
        border: 0.05em solid var(--group-border-colour);
        border-spacing: 0;
        overflow: auto;
    }

    .table_header {
        font-weight: 500;
    }

    th {
        padding: 0.3em 0.5em;
        border-bottom: 0.18em solid var(--on-group-colour);
    }

    th:not(:first-child) {
        border-left: 0.18em solid var(--on-group-colour);
    }

    .cell-wrapper {
        display: flex;
        justify-content: space-between;
    }
</style>
