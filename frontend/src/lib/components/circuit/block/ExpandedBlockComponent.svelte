<script lang="ts">

    import type {Block} from "../../../dto/circuit/block";
    import {cubicInOut, linear} from "svelte/easing";
    import TasksComponent from "../item/TasksComponent.svelte";
    import {isCompleted} from "../../../logic/circuit/skill_state/completion";
    import {addTaskToPath, getItemsOnPath} from "../../../logic/edition/active_path.svelte";
    import type {Point} from "../../../data/point";
    import StudentTrayComponent from "../../side_controls/student_tray/StudentTrayComponent.svelte";
    import {
        getDragging,
        dragEnter,
        dragOver,
        dragLeave,
        setDragging
    } from "../../../logic/circuit/drag_and_drop_items.svelte";
    import {openExpandedBlockTransition} from "../../../logic/transitions";
    import {getItem} from "../../../logic/circuit/circuit.svelte";
    import type {TaskItem} from "../../../dto/circuit/module/task";

    let { block, open = $bindable() }: { block: Block, open: boolean } = $props();

    let element: HTMLDialogElement | undefined = $state();

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

    function checkForClose(event: MouseEvent) {
        if (event.target === element) {
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

        setDragging(false);
    }
</script>

{#if open}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
    <dialog bind:this={element} onclick={checkForClose}>
        <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
        <div class="expanded-block" transition:openExpandedBlockTransition={{ block: block }} data-dragging={getDragging()}
             ondragenter={dragEnter} ondragover={dragOver} ondragleave={dragLeave} ondrop={drop}>
            <div class="content">
                <h2 class="name">{block.name}</h2>
                {#if block.blockType === "skill"}
                    <TasksComponent tasks={getItemsOnPath(block)}></TasksComponent>
                {/if}
                <div class="drop-indicator"></div>
            </div>
        </div>
        <StudentTrayComponent {block}></StudentTrayComponent>
    </dialog>
{/if}

<style>
    .expanded-block {
        font-size: clamp(.75rem, calc(16 / 1732 * 100vw), 1rem);

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