<script lang="ts">
    import type {TaskInTaskList} from "../../dto/task_in_task_list";
    import {TaskIcons} from "../../dto/task_icons";
    import Link from "../util/Link.svelte";
    import TaskPathEditComponent from "../circuit/item/TaskPathEditComponent.svelte";

    let { task } : { task: TaskInTaskList } = $props();
</script>

{#if task.taskInfo !== undefined}
    <tr>
        <th>{task.taskInfo.name}</th>
        <th>
            <span class="icon fa-solid fa-{TaskIcons[task.taskInfo.type]}"></span>
            {#if task.taskItem.taskType === "choice"}
                {' '}&#8712; <span class="icon fa-solid fa-shapes"></span>
            {/if}
        </th>
        <th>{task.taskInfo.time}</th>
        <th>{task.skillName}</th>
        <th>{task.submoduleName}</th>
        <th>{task.moduleName}</th>
        <th>
            {#if task.taskInfo.link === null}
                -
            {:else}
                <Link href={task.taskInfo.link} target="_blank">
                    <span>{task.taskInfo.link}</span>
                </Link>
            {/if}
        </th>
        <th>
            {#if task.taskItem.taskType === "regular"}
                <TaskPathEditComponent task={task.taskItem}></TaskPathEditComponent>
            {:else}
                -
            {/if}
        </th>
    </tr>
{/if}

<style>
    th {
        padding: 0.6em 1em;
    }

    th:not(:first-child) {
        border-left: 2px solid var(--on-group-colour);
    }
</style>