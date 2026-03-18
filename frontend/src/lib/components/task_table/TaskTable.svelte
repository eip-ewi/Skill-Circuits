<script lang="ts">
    import TaskTableRow from "./TaskTableRow.svelte";
    import type {TaskInTaskList} from "../../dto/task_in_task_list";

    let { tasks } : { tasks: TaskInTaskList[] } = $props();
</script>

<table class="task_table">
    <thead class="table_header">
        <tr>
            <th>Task</th>
            <th>Type</th>
            <th>Time</th>
            <th>Skill</th>
            <th>Submodule</th>
            <th>Module</th>
            <th>Link</th>
            <th>Paths</th>
        </tr>
    </thead>
    <tbody>
        {#each tasks as task}
            {#if task.taskType === "regular"}
                <TaskTableRow task={task} taskInfo={task}></TaskTableRow>
            {/if}
            {#if task.taskType === "choice"}
                {#each task.tasks as subtask}
                    <TaskTableRow task={task} taskInfo={subtask}></TaskTableRow>
                {/each}
            {/if}
        {/each}
    </tbody>
</table>

<style>
    .task_table {
        margin-top: 2em;
        margin-left: auto;
        margin-right: auto;
        text-align: left;
        padding: 1em;
        font-size: var(--font-size-300);
        background-color: var(--group-colour);
        color: var(--on-group-colour-higher-contrast);
        border-radius: var(--group-border-radius);
        border: 1px solid var(--group-border-colour);
        border-spacing: 0;
    }

    .table_header {
        font-weight: 500;
    }

    th {
        padding: 0.2em 1em;
        border-bottom: 2px solid var(--on-group-colour);
    }

    th:not(:first-child) {
        border-left: 2px solid var(--on-group-colour);
    }
</style>