<script lang="ts">
    import {getBlock, getBlocks} from "../../../logic/circuit/circuit.svelte";
    import {updateBlockPosition} from "../../../logic/circuit/updates/position_updates.svelte.js";
    import {BlockStates} from "../../../data/block_state";
    import {createBlock} from "../../../logic/circuit/updates/block_updates";
    import {getAuthorisation} from "../../../logic/authorisation.svelte";
    import {areColumnsEnabled} from "../../../dto/columns.svelte";

    let { column, height }: { column: number, height: number } = $props();

    let dragging: boolean = $state(false);

    function dragEnter(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/block")) {
            return;
        }
        event.preventDefault();
        dragging = true;
    }

    function dragLeave() {
        dragging = false;
    }

    function dragOver(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/block")) {
            return;
        }
        event.preventDefault();
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/block")) {
            return;
        }
        event.preventDefault();

        dragging = false;

        if (event.dataTransfer!.dropEffect === "move") {
            let blockId = parseInt(event.dataTransfer!.getData("skill-circuits/block"));
            let block = getBlock(blockId);
            await updateBlockPosition(block, column);
        }

        if (event.dataTransfer!.dropEffect === "copy") {
            await createBlock(column);
        }
    }
</script>

{#if areColumnsEnabled()}
    <div class="column-wrapper" style:grid-column={column + 1} style:grid-row="1 / span {height}" data-dragging={dragging}>
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <div class="column"
             ondragenter={dragEnter} ondragover={dragOver} ondragleave={dragLeave} ondrop={drop}></div>
        <div class="background"></div>
    </div>
{/if}

<style>
    .column-wrapper {
        border-radius: 16px;
        height: 100%;
        position: relative;
    }

    .column {
        border-radius: inherit;
        height: calc(100% + 8rem);
        transform: translateY(-4rem);
    }

    .background {
        border-radius: inherit;
        content: "";
        height: calc(100% + 8rem);
        top: 0;
        transform: translateY(-4rem);
        width: 100%;
        position: absolute;
        z-index: -1;
    }

    .column-wrapper[data-dragging="true"] .background {
        background-color: var(--on-background-highlight-colour);
    }
</style>