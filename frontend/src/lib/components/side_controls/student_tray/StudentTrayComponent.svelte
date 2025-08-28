<script lang="ts">

    import {cubicInOut} from "svelte/easing";
    import type {Block} from "../../../dto/circuit/block";
    import StudentTrayTaskComponent from "./StudentTrayTaskComponent.svelte";
    import {getItem} from "../../../logic/circuit/circuit.svelte";
    import type {TaskItem} from "../../../dto/circuit/module/task";
    import {addTaskToPath, isTaskOnPath, removeTaskFromPath} from "../../../logic/edition/active_path.svelte";

    let { block }: { block: Block } = $props();

    let open: boolean = $state(false);

    function dragEnter(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();
    }

    function dragLeave() {
    }

    function dragOver(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();

        let itemId = parseInt(event.dataTransfer!.getData("skill-circuits/item"));
        let item = getItem(itemId) as TaskItem;

        await removeTaskFromPath(item);
    }

    function growHorizontal(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 150,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scaleX(${t});
            `,
        }
    }
</script>

{#if !open}

    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events, a11y_mouse_events_have_key_events -->
    <div class="scrollable side glass open-button" ondragenter={ () => open = true } in:growHorizontal={{ delay: 150 }} out:growHorizontal={{}}>
        <button class="button" aria-label="Open tray" onclick={ () => open = true }>
            <span class="fa-solid fa-inbox"></span>
        </button>
    </div>

{/if}

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="scrollable glass side panel" aria-expanded={open}
     ondragenter={dragEnter} ondragover={dragOver} ondragleave={dragLeave} ondrop={drop}>
    <div class="heading">
        <h2>Tray</h2>
        <div class="controls">
            <button class="button" aria-label="Close panel" onclick={ () => open = false }>
                <span class="fa-solid fa-arrow-right"></span>
            </button>
        </div>
    </div>
    <div class="content">
        <div class="tasks">
            <div class="banner">
                <span class="fa-solid fa-info-circle"></span>
                <p>
                    Here you can find tasks that might not right be for you, but if you want to do them anyway, you can drag them to the skill.
                    Similarly, you can drag tasks from the skill here that you do not want to do.
                </p>
            </div>
            {#if block.blockType === "skill"}
                {@const availableTasks = block.items.filter(task => !isTaskOnPath(task))}
                {#if availableTasks.length === 0}
                    <p>
                        There are no available tasks.
                    </p>
                {/if}
                {#each availableTasks as task}
                    <StudentTrayTaskComponent {task}></StudentTrayTaskComponent>
                {/each}
            {/if}
        </div>
    </div>
</div>

<style>
    .side {
        font-size: clamp(.75rem, calc(16 / 1732 * 100vw), 1rem);

        position: fixed;
        right: 0;
        top: 2em;
        transform-origin: right;
    }

    .open-button {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        display: grid;
    }
    .open-button button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: var(--panel-button-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        display: grid;
        font-size: var(--font-size-600);
        margin: .5em;
        padding: .5em;
    }
    .open-button button:where(:focus-visible, :hover) {
        background: var(--on-glass-surface-active-colour);
    }
    .open-button button span {
        text-align: center;
    }

    .panel {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        display: flex;
        flex-direction: column;
        inset-block: 2em;
        max-width: 32em;
        overflow-y: auto;
        overscroll-behavior: contain;
        transition: transform ease-in-out 150ms;
        z-index: 91;
    }

    .panel[aria-expanded="false"] {
        transform: scaleX(0);
    }
    .panel[aria-expanded="true"] {
        transition-delay: 150ms;
    }

    .heading {
        align-items: center;
        display: flex;
        justify-content: space-between;
        gap: 2rem;
        padding: 2em 2em 1em 2em;
    }

    .heading h2 {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    .controls {
        display: flex;
        gap: .25em;
    }

    .content {
        display: grid;
        gap: 2em;
        padding: 0 2em 2em 2em;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: .5em;
        cursor: pointer;
        display: grid;
        justify-items: center;
        padding: 0.5em;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: var(--on-glass-surface-active-colour);
    }

    .tasks {
        display: grid;
        gap: .5em;
    }

    .banner {
        align-items: center;
        background-color: var(--info-banner-colour);
        border: var(--info-banner-border);
        border-radius: var(--info-banner-border-radius);
        color: var(--on-info-banner-colour);
        display: flex;
        gap: 1em;
        margin-bottom: 1em;
        padding: .5em 1em;
    }
    .banner .fa-solid {
        font-size: var(--font-size-500);
    }
</style>
