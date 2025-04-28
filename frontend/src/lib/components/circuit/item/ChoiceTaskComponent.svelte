<script lang="ts">

    import type {ChoiceTaskItem, TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {withCsrf} from "../../../logic/csrf";
    import {TaskIcons} from "../../../dto/task_icons";
    import {toggleTaskCompletion} from "../../../logic/circuit/updates/task_updates";
    import TaskComponent from "./TaskComponent.svelte";

    let { task }: { task: ChoiceTaskItem } = $props();

</script>

<div class="task" data-completed={task.tasks.filter(task => task.completed).length >= task.minTasks}>
    <div class="heading">
        {#if task.name.length === 0}
            <span>Do {task.minTasks} out of {task.tasks.length}</span>
        {:else}
            <span>{task.name}</span>
        {/if}
        <span>{task.tasks.filter(task => task.completed).length}/{task.minTasks}</span>
    </div>
    <div class="tasks">
        {#each task.tasks as subtask}
            <TaskComponent task={subtask}></TaskComponent>
        {/each}
    </div>
</div>

<style>
    .task {
        outline: 1px solid var(--on-block-divider-colour);
        border-radius: 16px;
        grid-column: 1 / -1;
        margin-top: 1rem;
        margin-bottom: .5rem;
        padding: 1.25rem 1rem 1rem 1rem;
        position: relative;
    }

    .task[data-completed="true"] {
        outline: 2px solid var(--completed-task-colour);
    }

    .heading {
        display: flex;
        justify-content: space-between;
        left: 0;
        padding-inline: 1rem;
        position: absolute;
        top: 0;
        transform: translateY(-50%);
        width: 100%;
    }

    .heading > * {
        background-color: var(--block-colour);
        padding-inline: .5rem;
    }

    .tasks {
        align-items: center;
        column-gap: 0.75rem;
        display: grid;
        grid-template-columns: auto 1fr auto;
        justify-content: start;
        row-gap: 0.25rem;
    }
</style>