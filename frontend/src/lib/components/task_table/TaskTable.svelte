<script lang="ts">
    import TaskTableRow from "./TaskTableRow.svelte";
    import type { TaskInTaskList } from "../../dto/task_in_task_list";
    import Button from "../util/Button.svelte";
    import type {
        SearchableTaskTableColumn,
        SortableTaskTableColumn,
        TaskTableColumn,
    } from "../../data/task_table_column";
    import { sortAscByLink, sortAscByString, sortAscByType } from "../../logic/task_table.svelte";
    import { getCircuit } from "../../logic/circuit/circuit.svelte";
    import Select from "../util/Select.svelte";
    import Option from "../util/Option.svelte";

    let { tasks }: { tasks: TaskInTaskList[] } = $props();

    let columns: TaskTableColumn[] = $state([
        {
            name: "Task",
            sortable: true,
            sortStatus: 0,
            sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
                sortAscByString(a.taskInfo.name, b.taskInfo.name),
            searchable: true,
            getAttr: (t: TaskInTaskList) => t.taskInfo.name,
        },
        { name: "Paths", sortable: false, searchable: false },
        { name: "Type", sortable: true, sortStatus: 0, sortAsc: sortAscByType, searchable: false },
        {
            name: "Time",
            sortable: true,
            sortStatus: 0,
            sortAsc: (a: TaskInTaskList, b: TaskInTaskList) => a.taskInfo.time - b.taskInfo.time,
            searchable: false,
        },
        {
            name: "Skill",
            sortable: true,
            sortStatus: 0,
            sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
                sortAscByString(a.skillName, b.skillName),
            searchable: true,
            getAttr: (t: TaskInTaskList) => t.skillName,
        },
        {
            name: "Submodule",
            sortable: true,
            sortStatus: 0,
            sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
                sortAscByString(a.submoduleName, b.submoduleName),
            searchable: true,
            getAttr: (t: TaskInTaskList) => t.submoduleName,
        },
        {
            name: "Module",
            sortable: true,
            sortStatus: 0,
            sortAsc: (a: TaskInTaskList, b: TaskInTaskList) =>
                sortAscByString(a.moduleName, b.moduleName),
            searchable: true,
            getAttr: (t: TaskInTaskList) => t.moduleName,
        },
        {
            name: "Link",
            sortable: true,
            sortStatus: 0,
            sortAsc: sortAscByLink,
            searchable: true,
            getAttr: (t: TaskInTaskList) => t.taskInfo.link ?? "",
        },
    ]);

    let searchColumn: SearchableTaskTableColumn = $state(
        columns.find(c => c.name === "Link") as SearchableTaskTableColumn,
    );
    let searchString: string = $state("");

    let showTasksWithLinks: boolean = $state(true);
    let showTasksWithoutLinks: boolean = $state(true);

    function sortByColumn(column: SortableTaskTableColumn, order: -1 | 1) {
        tasks.sort((a: TaskInTaskList, b: TaskInTaskList) => column.sortAsc(a, b) * order);

        columns.forEach(col => {
            if (col.sortable) {
                if (col === column) {
                    col.sortStatus = order;
                } else {
                    col.sortStatus = 0;
                }
            }
        });
    }

    function isRowVisible(task: TaskInTaskList) {
        const linkFilter =
            ((task.taskInfo.link === null || task.taskInfo.link === "") && showTasksWithoutLinks) ||
            (task.taskInfo.link !== null && task.taskInfo.link !== "" && showTasksWithLinks);
        if (searchColumn === undefined || searchString === "") {
            return linkFilter;
        }
        return (
            linkFilter &&
            searchColumn.getAttr(task).toLowerCase().includes(searchString.toLowerCase())
        );
    }

    function updateSearchColumn(event: Event) {
        const newCol = (event.target as HTMLInputElement).value;
        searchColumn = columns.find(c => c.name === newCol) as SearchableTaskTableColumn;
    }

    function updateLinkFilter(event: Event) {
        let newFilters = Array.from((event.target as HTMLSelectElement).selectedOptions).map(
            option => option.value,
        );
        showTasksWithLinks = newFilters.includes("with links");
        showTasksWithoutLinks = newFilters.includes("without links");
    }
</script>

<div class="page_wrapper">
    <h1>{getCircuit().name}</h1>

    <div class="task_table_wrapper">
        <div class="search">
            <span class="search_description">Filter tasks by</span>
            <Select onchange={updateSearchColumn}>
                {#each columns.filter(c => c.searchable) as column}
                    <Option
                        value={column.name}
                        selected={searchColumn !== undefined && searchColumn.name === column.name}>
                        {column.name}
                    </Option>
                {/each}
            </Select>
            <span class="search_description">:</span>

            <input
                name="search"
                type="text"
                placeholder="Search"
                bind:value={searchString}
                class="search_input" />
        </div>

        <table class="task_table">
            <thead class="table_header">
                <tr>
                    {#each columns as column}
                        <th>
                            <div class="cell">
                                {column.name}
                                <div class="buttons">
                                    {#if column.name === "Link"}
                                        <Select onchange={updateLinkFilter} multiple>
                                            {#snippet button(
                                                click: (event: MouseEvent) => void,
                                                focus: () => void,
                                                blur: () => void,
                                            )}
                                                <div class="button">
                                                    <Button
                                                        square
                                                        aria-label="Edit link filter"
                                                        onmousedown={click}
                                                        onfocus={focus}
                                                        onblur={blur}>
                                                        <span class="fa-solid fa-filter"></span>
                                                    </Button>
                                                </div>
                                            {/snippet}
                                            <Option
                                                value="without links"
                                                selected={showTasksWithoutLinks}>
                                                <span class="fa-solid fa-link-slash"></span>
                                            </Option>
                                            <Option
                                                value="with links"
                                                selected={showTasksWithLinks}>
                                                <span class="fa-solid fa-link"></span>
                                            </Option>
                                        </Select>
                                    {/if}

                                    {#if column.sortable}
                                        {#if column.sortStatus === -1}
                                            <Button
                                                aria-label="Sort ascendingly by {column.name}"
                                                onclick={() => {
                                                    sortByColumn(column, 1);
                                                }}
                                                square={true}>
                                                <i class="fa-solid fa-caret-down"></i>
                                            </Button>
                                        {:else if column.sortStatus === 0}
                                            <Button
                                                aria-label="Sort ascendingly by {column.name}"
                                                onclick={() => {
                                                    sortByColumn(column, 1);
                                                }}
                                                square={true}>
                                                <i class="fa-solid fa-sort"></i>
                                            </Button>
                                        {:else if column.sortStatus === 1}
                                            <Button
                                                aria-label="Sort descendingly by {column.name}"
                                                onclick={() => {
                                                    sortByColumn(column, -1);
                                                }}
                                                square={true}>
                                                <i class="fa-solid fa-caret-up"></i>
                                            </Button>
                                        {/if}
                                    {/if}
                                </div>
                            </div>
                        </th>
                    {/each}
                </tr>
            </thead>
            <tbody>
                {#each tasks.filter(t => isRowVisible(t)) as task}
                    <TaskTableRow {task}></TaskTableRow>
                {/each}
            </tbody>
        </table>
    </div>
</div>

<style>
    .page_wrapper {
        font-size: clamp(0.5rem, calc(16 / 1732 * 100vw), 1.5rem);
    }

    h1 {
        color: var(--on-background-colour);
        font-size: calc(var(--font-size-700));
        font-weight: 500;
        text-align: center;
    }

    .task_table_wrapper {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;

        margin: 2em auto 4em;
        padding: 1em;
        width: max-content;

        background-color: var(--group-colour);
        color: var(--on-group-colour-higher-contrast);
        border-radius: var(--group-border-radius);
        border: 0.05em solid var(--group-border-colour);
    }

    .search {
        display: flex;
        flex-direction: row;
        align-items: center;
        align-self: start;
        gap: 0.5em;
        margin-bottom: 1.5em;
        margin-left: 0.5em;
    }

    .search_description {
        font-size: calc(var(--font-size-500));
        font-weight: 500;
    }

    .search_input {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 0.5em;
        color: var(--on-neutral-surface-colour);
        padding: 0.5em 0.7em;
        min-width: 23em;
    }

    .task_table {
        font-size: 1.15em;
        text-align: left;
        overflow: auto;
        border-spacing: 0;
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

    .buttons {
        display: flex;
        margin-left: 1em;
        font-size: var(--font-size-100);
        gap: 0.5em;
    }

    .cell {
        display: flex;
        justify-content: space-between;
    }
</style>
