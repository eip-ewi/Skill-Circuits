<script lang="ts">

    import type {TaskItem} from "../../../dto/circuit/module/task";
    import TaskComponent from "./TaskComponent.svelte";
    import ChoiceTaskComponent from "./ChoiceTaskComponent.svelte";

    let { tasks, hideBookmark }: { tasks: TaskItem[], hideBookmark?: boolean } = $props();

</script>

<div class="tasks" style="--columns: {hideBookmark === true ? 3 : 4}">
    {#each tasks as task}
        {#if task.taskType === "regular"}
            <TaskComponent {task} {hideBookmark}></TaskComponent>
        {:else}
            <ChoiceTaskComponent {task}></ChoiceTaskComponent>
        {/if}
    {/each}
</div>

<style>
    .tasks {
        align-items: center;
        column-gap: 0.6em;
        display: grid;
        font-size: var(--font-size-500);
        grid-template-columns: repeat(var(--columns, 4), auto);
        justify-content: start;
        row-gap: 0.2em;
    }
</style>