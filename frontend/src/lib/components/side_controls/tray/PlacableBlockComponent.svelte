<script lang="ts">
    import {getLevel} from "../../../logic/circuit/level.svelte";
    import BlockContentComponent from "../../circuit/block/BlockContentComponent.svelte";
    import type {Block} from "../../../dto/circuit/block";
    import {BlockStates} from "../../../data/block_state";
    import {disableColumns, enableColumns} from "../../../dto/columns.svelte";

    let { block }: { block: Block } = $props();

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/block", block.id.toString());
        enableColumns();
    }

    function dragEnd(event: DragEvent) {
        block.state = BlockStates.Inactive;
        disableColumns();
    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="block" draggable="true" ondragstart={dragStart} ondragend={dragEnd}>
    <BlockContentComponent {block}></BlockContentComponent>
    <p class="description">
        Drag this {getLevel().block} onto the circuit to place it.
    </p>
</div>

<style>
    .block {
        background-color: var(--block-colour);
        border: var(--block-border);
        border-radius: var(--block-border-radius);
        box-shadow: 0.25rem 0.25rem 0.5rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        cursor: grab;
        display: flex;
        flex-direction: column;
        gap: .5rem;
        padding: 1rem;
        position: relative;
        transition: filter ease-in-out 150ms, box-shadow ease-in-out 150ms;
    }

    .description {
        font-style: italic;
        opacity: 35%;
    }
</style>