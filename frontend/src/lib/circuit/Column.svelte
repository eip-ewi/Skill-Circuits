<script lang="ts">
    import type {CircuitData} from "../data/circuit";
    import {BlockColumnChange, type CircuitUpdates} from "../model/circuit_updates";
    import type {Level} from "../data/level";
    import type {CircuitModel} from "../model/circuit";
    import {getAuth} from "../data/auth.svelte";

    let { updates, circuit, level, column, height } :
        { updates: CircuitUpdates, circuit: CircuitModel, level: Level, column: number, height: number } = $props();

    let element: HTMLElement;

    function dragOver(event: DragEvent) {
        if (!getAuth().canEditBlocks) return;

        const blockId = parseInt(event.dataTransfer!.types.find(t => t.startsWith("id-"))!.substring(3));
        // chrome security does not allow getData during dragOver, this is a workaround
        let block = circuit.getBlock(blockId)!;
        if (!block || column === block.column) return;

        // dropEffect is broken in chrome, this is a workaround
        let moving = event.dataTransfer!.types.includes("move");
        element.setAttribute("data-dragging", moving.toString());
        if (!moving) return;

        event.preventDefault();
        event.dataTransfer!.dropEffect = "move";
    }

    function dragLeave(event: DragEvent) {
        if (!getAuth().canEditBlocks) return;

        event.preventDefault();
        element.setAttribute("data-dragging", "false");
    }

    function drop(event: DragEvent) {
        if (!getAuth().canEditBlocks) return;

        // dropEffect is broken in chrome, this is a workaround
        let moving = event.dataTransfer!.types.includes("move");
        document.querySelectorAll(".column").forEach(column => column.setAttribute("data-dragging", "false"));
        if (!moving) return;

        let block = circuit.getBlock(parseInt(event.dataTransfer!.getData("text/plain")!))!;
        if (column === block.column) return;

        event.preventDefault();

        updates.update(new BlockColumnChange(block, level, block.column, column));
    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="column" bind:this={element} style:grid-column="{column + 1}" style:grid-row="1 / span {height}" ondragover={dragOver} ondragleave={dragLeave} ondrop={drop}></div>

<style>
    .column {
        border-radius: 16px;
        margin: -1rem;
        z-index: 1;
    }

    .column:global(:where([data-dragging="true"])) {
        background: var(--on-background-highlight-colour);
    }
</style>