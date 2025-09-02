<script lang="ts">

    import type {Block} from "../../../dto/circuit/block";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import {cubicIn, cubicInOut, linear} from "svelte/easing";
    import TasksComponent from "../item/TasksComponent.svelte";
    import {isCompleted} from "../../../logic/circuit/skill_state/completion";
    import {addTaskToPath, getItemsOnPath, removeTaskFromPath} from "../../../logic/edition/active_path.svelte";
    import type {Point} from "../../../data/point";
    import SideControlsComponent from "../../side_controls/SideControlsComponent.svelte";
    import StudentTrayComponent from "../../side_controls/student_tray/StudentTrayComponent.svelte";
    import {getBlockForItem, getItem} from "../../../logic/circuit/circuit.svelte";
    import type {TaskItem} from "../../../dto/circuit/module/task";
    import {editTaskIndex, moveTask} from "../../../logic/circuit/updates/task_updates";

    let { block, open = $bindable() }: { block: Block, open: boolean } = $props();

    let element: HTMLDialogElement | undefined = $state();

    let dragging: boolean = $state(false);

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

    function dragEnter(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();
        dragging = true;
    }

    function dragLeave() {
        dragging = false;
    }

    function dragOver(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();
        dragging = true;
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();

        let itemId = parseInt(event.dataTransfer!.getData("skill-circuits/item"));
        let item = getItem(itemId) as TaskItem;

        await addTaskToPath(item);

        dragging = false;
    }

    function transition(element: Element) {
        return {
            duration: 300,
            easing: linear,
            css: (t: number) => {
                let t3 = cubicInOut(t);
                let start: Point = {
                    x: block.boundingRect!().left + block.boundingRect!().width / 2,
                    y: block.boundingRect!().top + block.boundingRect!().height / 2,
                };
                let position = {
                    x: `calc(${start.x * (1 - t3)}px + ${50 * t3}vw - ${50 * t3}%)`,
                    y: `calc(${start.y * (1 - t3)}px + ${50 * t3}vh - ${50 * t3}%)`,
                }
                return `
                   --blur: ${t * 0.5}rem;
                   opacity: ${t3};
                   transform: translate(${position.x}, ${position.y}) scale(${t3 * 0.9 + 0.1}) ;
                `;
            }
        }
    }
</script>

{#if open}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
    <dialog bind:this={element} onclick={checkForClose}>
        <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
        <div class="expanded-block" transition:transition data-dragging={dragging}
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