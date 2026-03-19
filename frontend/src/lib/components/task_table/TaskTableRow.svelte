<script lang="ts">
    import type {TaskInTaskList} from "../../dto/task_in_task_list";
    import {TaskIcons} from "../../dto/task_icons";
    import Link from "../util/Link.svelte";
    import TaskPathEditComponent from "../circuit/item/TaskPathEditComponent.svelte";
    import type {TaskInfo} from "../../dto/circuit/module/task";

    let { task, taskInfo } : { task: TaskInTaskList, taskInfo: TaskInfo } = $props();
</script>

<tr>
    <th>{taskInfo.name}</th>
    <th> <span class="icon fa-solid fa-{TaskIcons[taskInfo.type]}"></span> </th>
    <th>{taskInfo.time}</th>
    <th>
        {#if task.taskItem.taskType === "regular"}
            No
        {:else}
            Yes
        {/if}
    </th>
    <th>{task.skillName}</th>
    <th>{task.submoduleName}</th>
    <th>{task.moduleName}</th>
    <th>
        {#if taskInfo.link === null}
            -
        {:else}
            <Link href={taskInfo.link} target="_blank">
                <span>{taskInfo.link}</span>
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

<style>
    th {
        padding: 0.6em 1em;
    }

    th:not(:first-child) {
        border-left: 2px solid var(--on-group-colour);
    }
</style>