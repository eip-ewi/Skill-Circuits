<script lang="ts">
    import {cubicInOut} from "svelte/easing";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import {type BlockState, BlockStates} from "../../../data/block_state";
    import {getLevel, isLevel} from "../../../logic/circuit/level.svelte";
    import {ModuleLevel} from "../../../data/level";
    import type {Block} from "../../../dto/circuit/block";
    import {getBlocks, getCircuit} from "../../../logic/circuit/circuit.svelte";
    import {deleteBlock} from "../../../logic/circuit/updates/block_updates";
    import Button from "../../util/Button.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

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
        <Button square={true} aria-label="Stop editing connections" onclick={stopConnecting} onmouseenter={ () => action = BlockActions.CancelLink } onmouseleave={ () => action = undefined }>
            <span class="fa-solid fa-link-slash"></span>
        </Button>
    {:else if block.state === BlockStates.Editing}
        <Button square={true} aria-label="Stop editing" onclick={stopEditing} onmouseenter={ () => action = BlockActions.StopEdit } onmouseleave={ () => action = undefined }>
            <span class="fa-solid fa-check"></span>
        </Button>
    {:else}
        <Button square={true} aria-label="Move" onclick={stopEditing} style="cursor: grab;"
                onmouseenter={ () => { action = BlockActions.Move; draggable = true; } }
                onmouseleave={ () => { action = undefined; draggable = false; } }>
            <span class="fa-solid fa-up-down-left-right"></span>
        </Button>
        {#if isLevel(ModuleLevel)}
            <Button square={true} onclick={connect} aria-label="Edit connections" onmouseenter={ () => action = BlockActions.Link } onmouseleave={ () => action = undefined }>
                <span class="fa-solid fa-link"></span>
            </Button>
        {/if}
        <Button square={true} aria-label="Edit" onclick={edit} onmouseenter={ () => action = BlockActions.Edit } onmouseleave={ () => action = undefined }>
            <span class="fa-solid fa-pencil"></span>
        </Button>
        <WithConfirmationDialog onconfirm={ () => deleteBlock(block) } icon="fa-solid fa-trash" action="Delete">
            {#snippet button(showDialog: () => void) }
                <Button square={true} type="caution" aria-label="Delete" onclick={showDialog} onmouseenter={ () => action = BlockActions.Delete } onmouseleave={ () => action = undefined }>
                    <span class="fa-solid fa-trash"></span>
                </Button>
            {/snippet}
            <p>
                Are you sure you want to delete '{block.name}'?
            </p>
        </WithConfirmationDialog>
    {/if}
</div>

<style>
    .controls {
        display: grid;
        gap: .5em;
        grid-template-columns: 1fr 1fr;
        right: 0.5em;
        padding: 1.5em 1.5em 1.5em 1em;
        position: absolute;
        top: 50%;
        transform-origin: left;
        translate: 100% -50%;
    }

    .controls[data-side="left"] {
        direction: rtl;
        left: 0.5em;
        right: initial;
        padding: 1.5em 1em 1.5em 1.5em;
        translate: -100% -50%;
        transform-origin: right;
    }

    .controls > * {
        aspect-ratio: 1 / 1;
        border: none;
        border-radius: .5em;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        cursor: pointer;
        display: grid;
        min-width: 2em;
        place-items: center;
    }
</style>