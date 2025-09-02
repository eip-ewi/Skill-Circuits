<script lang="ts">

    import {getAuthorisation} from "../../../logic/authorisation.svelte";
    import {cubicInOut, cubicIn, cubicOut} from "svelte/easing";
    import NewBlockComponent from "./NewBlockComponent.svelte";
    import {getBlock, getBlocks, getPlacableBlocks} from "../../../logic/circuit/circuit.svelte";
    import PlacableBlockComponent from "./PlacableBlockComponent.svelte";
    import {removeBlockFromCircuit, updateBlockPosition} from "../../../logic/circuit/updates/position_updates.svelte";
    import {createBlock} from "../../../logic/circuit/updates/block_updates";
    import {canEditCircuit} from "../../../logic/authorisation.svelte.js";

    let { open = $bindable() }: { open: boolean } = $props();

    let dragCounter: number = 0;

    function dragEnter(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/block") || event.dataTransfer!.dropEffect !== "move") {
            return;
        }
        event.preventDefault();
        dragCounter++;
        open = true;
    }

    function dragLeave(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/block")) {
            return;
        }
        dragCounter--;
        open = dragCounter > 0;
    }

    function dragOver(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/block") || event.dataTransfer!.dropEffect !== "move") {
            return;
        }
        event.preventDefault();
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/block") || event.dataTransfer!.dropEffect !== "move") {
            return;
        }
        event.preventDefault();

        open = false;

        let blockId = parseInt(event.dataTransfer!.getData("skill-circuits/block"));
        let block = getBlock(blockId);
        await removeBlockFromCircuit(block);
    }
</script>

{#if canEditCircuit()}
    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
    <div class="scrollable glass panel" aria-expanded={open}
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
            <NewBlockComponent></NewBlockComponent>
            <div class="blocks">
                {#each getPlacableBlocks() as block}
                    <PlacableBlockComponent {block}></PlacableBlockComponent>
                {/each}
            </div>
        </div>
    </div>
{/if}

<style>
    .panel {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        display: flex;
        flex-direction: column;
        inset-block: 2rem;
        max-width: 32rem;
        overflow-y: auto;
        overscroll-behavior: contain;
        position: fixed;
        right: 0;
        top: 2rem;
        transform-origin: right;
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
        padding: 2rem 2rem 1rem 2rem;
    }

    .heading h2 {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    .controls {
        display: flex;
        gap: .25rem;
    }

    .content {
        display: grid;
        gap: 2rem;
        padding: 0 2rem 2rem 2rem;
    }

    .blocks {
        display: grid;
        gap: 1rem;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: 8px;
        cursor: pointer;
        display: grid;
        justify-items: center;
        padding: 0.5rem;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: var(--on-glass-surface-active-colour);
    }
</style>
