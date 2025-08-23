<script lang="ts">

    import type {TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {editTaskLink} from "../../../logic/circuit/updates/task_updates";
    import Select from "../../util/Select.svelte";
    import Option from "../../util/Option.svelte";
    import {TaskIcons} from "../../../dto/task_icons";
    import Button from "../../util/Button.svelte";

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
    <Button square primary aria-label="Edit link" aria-pressed={expanded} onclick={ () => expanded = !expanded }>
        {#if expanded}
            <span class="fa-solid fa-check"></span>
        {:else}
            <span class="fa-solid fa-link"></span>
        {/if}
    </Button>
    <input bind:this={input} name="link" type="text" placeholder="Task link..." onchange={editLink} value={taskInfo.link}/>
</div>


<style>
    .controls {
        position: relative;
    }

    input {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: .5em;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-neutral-surface-colour);
        left: calc(100% + 0.25em);
        min-width: 32em;
        padding: .25em .5em;
        position: absolute;
        top: -1px;
        transform-origin: left;
        transition: transform 150ms ease-in-out;
        z-index: 90;
    }

    .controls[data-direction="left"] input {
        left: initial;
        right: calc(100% + 0.25em);
        transform-origin: right;
    }

    .controls[aria-expanded="false"] input {
        transform: scaleX(0);
    }
</style>