<script lang="ts">

    import type {TaskItem} from "../../../dto/circuit/module/task";
    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import {TaskIcons} from "../../../dto/task_icons.js";

    let { task }: { task: TaskItem } = $props();

</script>

{#if task.taskType === "regular"}
    <span class="task fa-solid fa-{TaskIcons[task.type]}" data-completed={task.completed && !canEditCircuit()}></span>
{:else}
    <span class="task fa-solid fa-shapes" data-completed={task.tasks.filter(subtask => subtask.completed).length >= task.minTasks && !canEditCircuit()}></span>
{/if}

<style>
    .task[data-completed="true"] {
        color: var(--on-block-task-completed-colour);
    }
</style>