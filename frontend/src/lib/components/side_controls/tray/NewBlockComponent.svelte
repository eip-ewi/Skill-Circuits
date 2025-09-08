<script lang="ts">
    import {getLevel} from "../../../logic/circuit/level.svelte";
    import {disableColumns, enableColumns} from "../../../dto/columns.svelte";

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "copy";
        event.dataTransfer!.setData("skill-circuits/block", "new");
        enableColumns();
    }

    function dragEnd(event: DragEvent) {
        disableColumns();
    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="block" draggable="true" ondragstart={dragStart} ondragend={dragEnd}>
    <h2 class="title">New {getLevel().block}</h2>
    <p class="description">
        Drag this block onto the circuit to create a new {getLevel().block}.
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

    .title {
        font-size: 1.25rem;
        font-weight: 700;
    }

    .description {
        font-style: italic;
        opacity: 35%;
    }
</style>