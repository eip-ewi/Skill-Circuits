<script lang="ts">
    import {cubicInOut} from "svelte/easing";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import {type BlockState, BlockStates} from "../../../data/block_state";
    import {getLevel, isLevel} from "../../../logic/circuit/level.svelte";
    import {ModuleLevel} from "../../../data/level";
    import type {Block} from "../../../dto/circuit/block";
    import {getBlocks, getCircuit} from "../../../logic/circuit/circuit.svelte";
    import {deleteBlock} from "../../../logic/circuit/updates/block_updates";

    let { block, action = $bindable(), draggable = $bindable() }: { block: Block, action: BlockAction | undefined, draggable: boolean } = $props();

    function transition(element: HTMLElement) {
        return {
            duration: 100,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scaleX(${t});
            `,
        };
    }

    function edit() {
        block.state = BlockStates.Editing;
        action = undefined;
    }

    function stopEditing() {
        block.state = BlockStates.Inactive;
    }

    function connect() {
        block.state = BlockStates.Connecting;
        getBlocks().filter(other => other.id !== block.id).forEach(other => {
            other.state = BlockStates.WaitingForConnection;
        });
        action = undefined;
    }

    function stopConnecting() {
        getBlocks().forEach(block => {
            block.state = BlockStates.Inactive;
        });
    }

    async function remove() {
        await deleteBlock(block);
    }
</script>

<div class="controls" data-side={(block.boundingRect === undefined ? 0 : block.boundingRect!().right) + 128 > window.innerWidth ? "left" : "right"} transition:transition>
    {#if block.state === BlockStates.Connecting}
        <button onclick={stopConnecting} aria-label="Stop editing connections" class="fa-solid fa-link-slash" onmouseenter={ () => action = BlockActions.CancelLink } onmouseleave={ () => action = undefined }></button>
    {:else if block.state === BlockStates.Editing}
        <button aria-label="Stop editing" class="fa-solid fa-check" onclick={stopEditing} onmouseenter={ () => action = BlockActions.StopEdit } onmouseleave={ () => action = undefined }></button>
    {:else}
        <div aria-label="Move" class="drag" tabindex="0" role="button"
             onmouseenter={ () => { action = BlockActions.Move; draggable = true; } }
             onmouseleave={ () => { action = undefined; draggable = false } }>
            <span class="fa-solid fa-up-down-left-right"></span>
        </div>
        {#if isLevel(ModuleLevel)}
            <button onclick={connect} aria-label="Edit connections" class="fa-solid fa-link" onmouseenter={ () => action = BlockActions.Link } onmouseleave={ () => action = undefined }></button>
        {/if}
        <button aria-label="Edit" class="fa-solid fa-pencil" onclick={edit} onmouseenter={ () => action = BlockActions.Edit } onmouseleave={ () => action = undefined }></button>
        <button aria-label="Delete" class="danger fa-solid fa-trash" onclick={remove} onmouseenter={ () => action = BlockActions.Delete } onmouseleave={ () => action = undefined }></button>
    {/if}
</div>

<style>
    .controls {
        display: grid;
        gap: .5rem;
        grid-template-columns: 1fr 1fr;
        right: 0.5rem;
        padding: 1.5rem 1.5rem 1.5rem 1rem;
        position: absolute;
        top: 50%;
        transform-origin: left;
        translate: 100% -50%;
    }

    .controls[data-side="left"] {
        direction: rtl;
        left: 0.5rem;
        right: initial;
        padding: 1.5rem 1rem 1.5rem 1.5rem;
        translate: -100% -50%;
        transform-origin: right;
    }

    .controls > * {
        aspect-ratio: 1 / 1;
        background-color: var(--block-colour);
        border: none;
        border-radius: 8px;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        cursor: pointer;
        display: grid;
        min-width: 2rem;
        place-items: center;
    }

    .controls > *:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }

    .controls > *.danger:where(:hover, :focus-visible) {
        background-color: var(--primary-error-active-colour);
        color: var(--on-error-surface-colour);
    }

    .controls > *.drag {
        cursor: grab;
    }
</style>