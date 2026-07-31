<script lang="ts">
    import type { TaskItem } from "../../../dto/circuit/module/task";
    import TaskComponent from "./TaskComponent.svelte";
    import ChoiceTaskComponent from "./ChoiceTaskComponent.svelte";

    let { tasks, hideBookmark }: { tasks: TaskItem[]; hideBookmark?: boolean } = $props();

    function hasDeadline(task: TaskItem): boolean {
        if (task.taskType === "regular") {
            return task.deadline !== null;
        }

        return task.tasks.some(subtask => subtask.deadline !== null);
    }
    let reserveDeadlineSpace: boolean = $derived(tasks.some(hasDeadline));
</script>

<div class="tasks" style="--columns: {hideBookmark === true ? 3 : 4}">
    {#each tasks as task}
        {#if task.taskType === "regular"}
            <TaskComponent {task} {hideBookmark} {reserveDeadlineSpace}></TaskComponent>
        {:else}
            <ChoiceTaskComponent {task} {hideBookmark} {reserveDeadlineSpace}></ChoiceTaskComponent>
        {/if}
    {/each}
</div>

<style>
    .tasks {
        align-items: center;
        column-gap: 0.6em;
        display: grid;
        /*the title takes as much space as possible*/
        grid-template-columns: auto 1fr repeat(calc(var(--columns, 4) - 2), auto);
        justify-content: start;
        row-gap: 0.2em;
    }
</style>
