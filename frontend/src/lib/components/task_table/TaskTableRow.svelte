<script lang="ts">
    import type { TaskInTaskList } from "../../dto/task_in_task_list";
    import { TaskIcons } from "../../dto/task_icons";
    import Link from "../util/Link.svelte";
    import TaskPathEditComponent from "../circuit/item/TaskPathEditComponent.svelte";
    import TaskTableLink from "./TaskTableLink.svelte";

    let { task }: { task: TaskInTaskList } = $props();
</script>

{#if task.taskInfo !== undefined}
    <tr>
        <th style="max-width: 12em">{task.taskInfo.name}</th>
        <th class="path_column" style="max-width: 5em; overflow: visible;">
            {#if task.taskItem.taskType === "regular"}
                <TaskPathEditComponent task={task.taskItem}></TaskPathEditComponent>
            {:else}
                -
            {/if}
        </th>
        <th style="max-width: 7em">
            <span class="icon fa-solid fa-{TaskIcons[task.taskInfo.type]}"></span>
            {#if task.taskItem.taskType === "choice"}
                {" "}in
                <span class="icon fa-solid fa-shapes"></span>
            {/if}
        </th>
        <th style="max-width: 4em">{task.taskInfo.time}</th>
        <th style="max-width: 12em">{task.skillName}</th>
        <th style="max-width: 12em">{task.submoduleName}</th>
        <th style="max-width: 12em">{task.moduleName}</th>
        <th class="link_column">
            <TaskTableLink taskInfo={task.taskInfo}></TaskTableLink>
        </th>
    </tr>
{/if}

<style>
    th {
        padding: 0 0.5em;
        height: 3.2em;
        overflow: auto;
    }

    .link_column {
        min-width: 25em;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    th:not(:first-child) {
        border-left: 0.18em solid var(--on-group-colour);
    }
</style>
