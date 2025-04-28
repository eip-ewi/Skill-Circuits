<script lang="ts">

    import type {TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {withCsrf} from "../../../logic/csrf";
    import {TaskIcons} from "../../../dto/task_icons";
    import {toggleTaskCompletion} from "../../../logic/circuit/updates/task_updates";

    let { task }: { task: TaskInfo } = $props();

    async function toggleComplete() {
        await toggleTaskCompletion(task);
    }
</script>

<button class="checkbox" aria-label="Complete task" aria-pressed={task.completed} onclick={toggleComplete}>
    <span>{"\u2713"}</span>
</button>
<span class="description">
    <span class="icon fa-solid fa-{TaskIcons[task.type]}"></span>
    {#if task.link === null}
        <span class="name">{task.name}</span>
    {:else}
        <a class="name" href={task.link} target="_blank">{task.name}</a>
    {/if}
</span>
<span class="time">
    <span class="fa-solid fa-clock"></span>
    <span>{task.time}</span>
</span>

<style>
    .checkbox {
        aspect-ratio: 1 / 1;
        background: none;
        border: 2px solid var(--on-block-divider-colour);
        border-radius: 4px;
        cursor: pointer;
        outline: none;
        position: relative;
        width: 1.5rem;
    }
    .checkbox[aria-pressed="true"] {
        background-color: var(--checkbox-checked-colour);
        border: none;
    }

    .checkbox span {
        content: '\2713';
        color: var(--on-checkbox-checked-colour);
        left: 50%;
        position: absolute;
        transition: transform ease-in 150ms;
        translate: -50% -50%;
        transform-origin: center;
        top: 50%;
    }
    .checkbox[aria-pressed="false"] span {
        transform: scale(0);
    }

    .description {
        align-items: center;
        display: flex;
        gap: .25rem;
    }

    a.name {
        color: var(--link-colour);
        text-decoration: none;
    }
    a.name:hover, a.name:focus-visible {
        color: var(--link-active-colour);
    }

    .time {
        align-items: center;
        display: flex;
        gap: .25rem;
    }
</style>