<script lang="ts">

    import type {TaskItem} from "../../../dto/circuit/module/task";
    import TaskComponent from "./TaskComponent.svelte";
    import ChoiceTaskComponent from "./ChoiceTaskComponent.svelte";

    let { tasks }: { tasks: TaskItem[] } = $props();

</script>

<div class="tasks">
    {#each tasks as task}
        {#if task.taskType === "regular"}
            <TaskComponent {task}></TaskComponent>
        {:else}
            <ChoiceTaskComponent {task}></ChoiceTaskComponent>
        {/if}
    {/each}
</div>

<style>
    .tasks {
        align-items: center;
        column-gap: 0.75rem;
        display: grid;
        font-size: var(--font-size-500);
        grid-template-columns: auto auto auto;
        justify-content: start;
        row-gap: 0.25rem;
    }
</style>