<script lang="ts">
    import { getBlock, getBlocks } from "../../../logic/circuit/circuit.svelte";
    import {
        removeColumn,
        updateBlockPosition,
    } from "../../../logic/circuit/updates/position_updates.svelte.js";
    import { BlockStates } from "../../../data/block_state";
    import { createBlock } from "../../../logic/circuit/updates/block_updates";
    import { hasEditorRights, getAuthorisation } from "../../../logic/authorisation.svelte";
    import { areColumnsEnabled } from "../../../dto/columns.svelte";
    import { createExternalSkill } from "../../../logic/circuit/updates/skill_updates";
    import { getCircuit } from "../../../logic/circuit/circuit.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

    let { column, height }: { column: number; height: number } = $props();

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

        if (event.dataTransfer!.effectAllowed === "move") {
            let blockId = parseInt(event.dataTransfer!.getData("skill-circuits/block"));
            let block = getBlock(blockId);
            await updateBlockPosition(block, column);
        }

        if (event.dataTransfer!.effectAllowed === "copy") {
            await createBlock(column);
        }

        if (event.dataTransfer!.effectAllowed === "link") {
            let blockId = parseInt(event.dataTransfer!.getData("skill-circuits/block"));
            await createExternalSkill(blockId, column);
        }
    }
</script>

{#if hasEditorRights()}
    <div
        class="column-wrapper"
        style:grid-column={column + 1}
        style:grid-row="1 / span {height}"
        data-dragging={dragging}
        data-interactible={areColumnsEnabled()}>
        {#if (getCircuit().width ?? 0) > 1}
            <div class="glass surface button-wrapper">
                <WithConfirmationDialog
                    icon="fa-solid fa-trash"
                    action="Remove Column"
                    onconfirm={() => removeColumn(column)}>
                    {#snippet button(openConfirmationDialog: () => void)}
                        <button
                            class="button"
                            aria-label="Remove Column"
                            onclick={openConfirmationDialog}>
                            <i class="fa-solid fa-minus"></i>
                        </button>
                    {/snippet}
                    <p>
                        Are you sure you want to remove this column? All content will be moved to
                        the column on the left. If this is the leftmost column the shift will happen
                        to the right.
                    </p>
                </WithConfirmationDialog>
            </div>
        {/if}
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <div
            class="column"
            ondragenter={dragEnter}
            ondragover={dragOver}
            ondragleave={dragLeave}
            ondrop={drop}>
        </div>
        <div class="background"></div>
    </div>
{/if}

<style>
    .button {
        background: var(--group-colour);
        border: none;
        border-radius: var(--column-button-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        place-items: center;
        width: 2.2em;
        height: 2.2em;
        transition: transform 0.2s;
        font-size: 0.8em;
    }
    .button-wrapper {
        border-radius: var(--column-button-border-radius);
        align-items: center;
        justify-content: center;
        position: absolute;
        top: -4.5rem;
        left: 50%;
        pointer-events: auto;
        transform: translateX(-50%);
        z-index: 1;
    }
    .button-wrapper:hover {
        transform: translateX(-50%) scale(1.2);
    }
    .background {
        border-radius: inherit;
        height: calc(100% + 4rem);
        top: 2rem;
        transform: translateY(-4rem);
        width: 100%;
        position: absolute;
        opacity: 0;
        pointer-events: none;
        z-index: 99;
        transition:
            opacity 0.5s ease-in-out,
            background-color 0.5s ease-in-out;
    }

    .button-wrapper:hover ~ .background {
        background-color: var(--column-background-color-removal);
        opacity: 1;
    }

    .column-wrapper[data-dragging="true"] .background {
        background-color: var(--column-colour);
        opacity: 1;
    }

    .column-wrapper {
        border-radius: var(--column-border-radius);
        height: 100%;
        position: relative;
        min-width: 6rem;
        width: 100%;
    }

    .column {
        border-radius: inherit;
        height: calc(100% + 8rem);
        transform: translateY(-4rem);
    }

    .column-wrapper[data-interactible="false"] {
        pointer-events: none;
    }
</style>
