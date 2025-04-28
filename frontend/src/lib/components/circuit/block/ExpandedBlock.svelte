<script lang="ts">

    import type {Block} from "../../../dto/circuit/block";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import {cubicIn, cubicInOut, linear} from "svelte/easing";
    import TasksComponent from "../item/TasksComponent.svelte";
    import {isCompleted} from "../../../logic/circuit/display/completion";
    import {getItemsOnPath} from "../../../logic/edition/active_path.svelte";

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

    function transition(element: Element) {
        return {
            duration: 300,
            easing: linear,
            css: (t: number) => {
                return `
                   --blur: ${t * 0.5}rem;
                   left: calc(${(block.boundingRect!().left + block.boundingRect!().width / 2) * (1 - cubicInOut(t))}px + ${50 * cubicInOut(t)}%);
                   opacity: ${cubicInOut(t)};
                   top: calc(${(block.boundingRect!().top + block.boundingRect!().height / 2) * (1 - cubicInOut(t))}px + ${50 * cubicInOut(t)}%);
                   transform: scale(${cubicInOut(t) * 0.9 + 0.1}) translate(-50%, -50%);
                `;
            }
        }
    }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
{#if open}
    <dialog bind:this={element} class="expanded-block" onclick={checkForClose} transition:transition>
        <div class="content">
            <h2 class="name">{block.name}</h2>
            <TasksComponent tasks={getItemsOnPath(block as SkillBlock)}></TasksComponent>
        </div>
    </dialog>
{/if}

<style>
    .expanded-block {
        --blur: 0.5rem;

        border: none;
        border-radius: 16px;
        left: 50%;
        outline: none;
        position: fixed;
        top: 50%;
        transform: translate(-50%, -50%);
        transform-origin: top left;
    }

    .expanded-block::backdrop {
        background: none;
        backdrop-filter: blur(var(--blur));
    }

    .content {
        background: var(--block-colour);
        box-shadow: 2rem 2rem 4rem color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        display: grid;
        gap: 1rem;
        max-height: calc(100vh - 12rem);
        overflow-y: scroll;
        padding: 2rem;
        place-items: center;
    }

    .name {
        font-size: var(--font-size-700);
        font-weight: 700;
        text-align: center;
    }
</style>