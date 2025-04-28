<script lang="ts">

    import type {TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {editTaskLink} from "../../../logic/circuit/updates/task_updates";
    import Select from "../../form/Select.svelte";
    import Option from "../../form/Option.svelte";
    import {TaskIcons} from "../../../dto/task_icons";

    let { taskInfo }: { taskInfo: TaskInfo } = $props();

    let expanded: boolean = $state(false);
    let input: HTMLInputElement | undefined = $state();

    async function editLink(event: Event) {
        const newLink = (event.target as HTMLInputElement).value;
        await editTaskLink(taskInfo, newLink);
        expanded = false;
    }
</script>

<div class="controls" aria-expanded={expanded} data-direction={(input?.getBoundingClientRect?.()?.left ?? 0) + 32*16 > window.innerWidth ? "left" : "right"}>
    <button aria-label="Edit link" class="button" aria-pressed={expanded} onclick={ () => expanded = !expanded }>
        {#if expanded}
            <span class="fa-solid fa-check"></span>
        {:else}
            <span class="fa-solid fa-link"></span>
        {/if}
    </button>
    <input bind:this={input} type="text" placeholder="Task link..." onchange={editLink} value={taskInfo.link}/>
</div>


<style>
    .controls {
        position: relative;
    }

    .button {
        aspect-ratio: 1 / 1;
        background-color: var(--primary-surface-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-primary-surface-colour);
        cursor: pointer;
        display: grid;
        min-width: 2rem;
        place-items: center;
    }

    .button:where(:hover, :focus-visible, [aria-pressed="true"]) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }

    input {
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 8px;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        left: calc(100% + 0.25rem);
        min-width: 32rem;
        padding: .25rem .5rem;
        position: absolute;
        top: -1px;
        transform-origin: left;
        transition: transform 150ms ease-in-out;
        z-index: 90;
    }

    .controls[data-direction="left"] input {
        left: initial;
        right: calc(100% + 0.25rem);
        transform-origin: right;
    }

    .controls[aria-expanded="false"] input {
        transform: scaleX(0);
    }
</style>