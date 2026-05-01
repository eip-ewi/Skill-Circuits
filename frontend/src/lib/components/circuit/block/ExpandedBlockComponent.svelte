<script lang="ts">
    import type { Block } from "../../../dto/circuit/block";
    import TasksComponent from "../item/TasksComponent.svelte";
    import { isCompleted } from "../../../logic/circuit/skill_state/completion";
    import { addTaskToPath, getItemsOnPath } from "../../../logic/edition/active_path.svelte";
    import StudentTrayComponent from "../../side_controls/student_tray/StudentTrayComponent.svelte";
    import {
        getDraggingItem,
        dragItemEnter,
        dragItemOver,
        dragItemLeave,
        setDraggingItem,
    } from "../../../logic/circuit/drag_and_drop_items.svelte";
    import { openExpandedBlockTransition } from "../../../logic/transitions";
    import { getItem } from "../../../logic/circuit/circuit.svelte";
    import type { TaskItem } from "../../../dto/circuit/module/task";
    import { cubicInOut } from "svelte/easing";
    import LegendPanelComponent from "../../side_controls/legend/LegendPanelComponent.svelte";

    let { block, open = $bindable() }: { block: Block; open: boolean } = $props();

    let element: HTMLDialogElement | undefined = $state();
    let trayOpen: boolean = $state(false);
    let legendOpen: boolean = $state(false);

    function growHorizontal(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 150,
            easing: cubicInOut,
            css: (t: number) => `transform: scaleX(${t});`,
        };
    }

    let visibleTasks: TaskItem[] = $derived.by(() => {
        //if unchecked task is of type SkillItem|TaskItem
        if (block.blockType !== "skill") return [];

        return getItemsOnPath(block);
    });

    $effect(() => {
        if (element === undefined) {
            return;
        }
        if (open) {
            element.showModal();
        }
    });

    $effect(() => {
        if (isCompleted(block)) {
            open = false;
        }
    });

    function checkForClose(event: MouseEvent | KeyboardEvent) {
        if (event instanceof MouseEvent && event.target === element) {
            open = false;
            return;
        }

        if (event instanceof KeyboardEvent && event.key === "Escape") {
            // prevent default behaviour of instantly closing the dialogue
            event.preventDefault();
            open = false;
        }
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();

        let itemId = parseInt(event.dataTransfer!.getData("skill-circuits/item"));
        let item = getItem(itemId) as TaskItem;

        await addTaskToPath(item);

        setDraggingItem(false);
    }
</script>

{#if open}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
    <dialog bind:this={element} onclick={checkForClose} onkeydown={checkForClose}>
        <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
        <div
            class="expanded-block"
            transition:openExpandedBlockTransition={{ block: block }}
            data-dragging={getDraggingItem()}
            ondragenter={dragItemEnter}
            ondragover={dragItemOver}
            ondragleave={dragItemLeave}
            ondrop={drop}>
            <div class="content">
                <h2 class="name">{block.name}</h2>
                {#if block.blockType === "skill"}
                    <TasksComponent tasks={visibleTasks}></TasksComponent>
                {/if}
                <div class="drop-indicator"></div>
            </div>
        </div>
        <StudentTrayComponent {block} bind:open={trayOpen}></StudentTrayComponent>
        <LegendPanelComponent bind:open={legendOpen}></LegendPanelComponent>
        {#if block.blockType === "skill" && !trayOpen && !legendOpen}
            <!-- svelte-ignore a11y_no_static_element_interactions, a11y_mouse_events_have_key_events -->
            <div class="dialog-controls" in:growHorizontal={{ delay: 150 }} out:growHorizontal={{}}>
                <div class="glass surface" ondragenter={() => (trayOpen = true)}>
                    <button class="button" aria-label="Open tray" onclick={() => (trayOpen = true)}>
                        <span class="fa-solid fa-inbox"></span>
                    </button>
                </div>
                <div class="glass surface">
                    <button
                        class="button"
                        aria-label="Open legend"
                        onclick={() => (legendOpen = true)}>
                        <span class="fa-solid fa-circle-info"></span>
                    </button>
                </div>
            </div>
        {/if}
    </dialog>
{/if}

<style>
    .expanded-block {
        font-size: clamp(0.75rem, calc(16 / 1732 * 100vw), 1rem);

        border: var(--expanded-block-border);
        border-radius: var(--expanded-block-border-radius);
        left: 0;
        outline: none;
        overflow: hidden;
        position: fixed;
        top: 0;
        transform: translate(calc(50vw - 50%), calc(50vh - 50%));
        transform-origin: top left;
    }

    dialog::backdrop {
        background-color: var(--expanded-block-background-colour);
        backdrop-filter: blur(var(--expanded-block-background-blur));
    }

    .content {
        --background-colour: var(--block-colour);
        background-color: var(--block-colour);
        box-shadow: 2rem 2rem 4rem color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        display: grid;
        gap: 1em;
        max-height: calc(100vh - 12em);
        overflow-y: auto;
        padding: 2em;
        place-items: center;
        font-size: var(--font-size-500);
    }

    .dialog-controls {
        font-size: clamp(0.75rem, calc(16 / 1732 * 100vw), 1rem);

        display: grid;
        gap: 1em;
        position: fixed;
        right: 0;
        top: 2em;
        transform-origin: right;
        z-index: 92;
    }

    .dialog-controls .surface {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        display: grid;
    }

    .dialog-controls .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: var(--panel-button-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        display: grid;
        font-size: var(--font-size-600);
        margin: 0.5em;
        padding: 0.5em;
    }

    .dialog-controls .button:focus-visible,
    .dialog-controls .button:hover {
        background: var(--on-glass-surface-active-colour);
    }

    .dialog-controls .button span {
        text-align: center;
    }

    .name {
        font-size: var(--font-size-700);
        font-weight: 700;
        text-align: center;
    }

    .drop-indicator {
        background-color: var(--task-drop-indication-colour);
        border: var(--task-drop-indication-border);
        border-radius: var(--task-drop-indication-border-radius);
        inset: 1em;
        pointer-events: none;
        position: absolute;
    }
    .expanded-block[data-dragging="false"] .drop-indicator {
        display: none;
    }
</style>
