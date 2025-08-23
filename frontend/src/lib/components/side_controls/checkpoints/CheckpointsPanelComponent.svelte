<script lang="ts">

    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import CheckpointListItemComponent from "./CheckpointListItemComponent.svelte";
    import {createCheckpoint} from "../../../logic/updates/checkpoint_updates";
    import {getSortedCheckpoints} from "../../../logic/edition/edition.svelte";

    let { open = $bindable() }: { open: boolean } = $props();

    async function addCheckpoint() {
        let checkpoint = await createCheckpoint();
        if (checkpoint !== undefined) {
            checkpoint.editing = true;
        }
    }
</script>

{#if canEditCircuit()}
    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
    <div class="scrollable glass panel" aria-expanded={open}>
        <div class="heading">
            <h2>Checkpoints</h2>
            <div class="controls">
                <button class="button" aria-label="Add checkpoint" onclick={addCheckpoint}>
                    <span class="fa-solid fa-plus"></span>
                </button>
                <button class="button" aria-label="Close panel" onclick={ () => open = false }>
                    <span class="fa-solid fa-arrow-right"></span>
                </button>
            </div>
        </div>
        <div class="checkpoints">
            {#each getSortedCheckpoints() as checkpoint}
                <CheckpointListItemComponent {checkpoint}></CheckpointListItemComponent>
            {/each}
        </div>
    </div>
{/if}

<style>
    .panel {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
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

    .checkpoints {
        display: grid;
        gap: 2rem;
        padding: 0 2rem 2rem 2rem;
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
