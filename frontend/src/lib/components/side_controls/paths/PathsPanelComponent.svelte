<script lang="ts">

    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import PathListItemComponent from "./PathListItemComponent.svelte";
    import {createPath} from "../../../logic/updates/path_updates";
    import {getPaths} from "../../../logic/edition/edition.svelte";
    import {getLevel, isLevel} from "../../../logic/circuit/level.svelte";
    import {ModuleLevel} from "../../../data/level";
    import {getBlocks} from "../../../logic/circuit/circuit.svelte";
    import {BlockStates} from "../../../data/block_state";

    let { open = $bindable() }: { open: boolean } = $props();

    let bulkEditing: boolean = $derived(isLevel(ModuleLevel) && getBlocks().some(block => block.state === BlockStates.AssigningPaths));

    async function addPath() {
        let path = await createPath();
        if (path !== undefined) {
            path.editing = true;
        }
    }

    function bulkEdit() {
        if (bulkEditing) {
            getBlocks().forEach(block => block.state = BlockStates.Inactive);
        } else {
            getBlocks().forEach(block => block.state = BlockStates.AssigningPaths);
            open = false;
        }
    }
</script>

{#if canEditCircuit()}
    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
    <div class="panel" aria-expanded={open}>
        <div class="heading">
            <h2>Paths</h2>
            <div class="controls">
                <button class="button" aria-label="Add path" onclick={addPath}>
                    <span class="fa-solid fa-plus"></span>
                </button>
                <button class="button" aria-label="Close panel" onclick={ () => open = false }>
                    <span class="fa-solid fa-arrow-right"></span>
                </button>
            </div>
        </div>
        {#if isLevel(ModuleLevel)}
            <div class="extra-controls">
                <button class="button" onclick={bulkEdit}>
                    {#if bulkEditing}
                        <span class="fa-solid fa-check"></span>
                        <span>Stop editing</span>
                    {:else}
                        <span class="fa-solid fa-pencil"></span>
                        <span>Edit paths in bulk</span>
                    {/if}
                </button>
            </div>
        {/if}
        <div class="paths">
            {#each getPaths() as path}
                <PathListItemComponent {path}></PathListItemComponent>
            {/each}
        </div>
    </div>
{/if}

<style>
    .panel {
        backdrop-filter: blur(.5rem) saturate(180%);
        background: color-mix(in srgb, white 25%, transparent);
        border-radius: 24px 0 0 24px;
        box-shadow:
                .75rem 1.25rem 1.9rem 0 color-mix(in srgb, var(--shadow-colour) 4%, transparent),
                inset 0.125rem 0.125rem 0.0625rem 0 rgba(255 255 255 / 0.6),
                inset -0.0625rem -0.0625rem 0.0625rem rgba(255 255 255 / 0.4);
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
        color: var(--on-header-colour);
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

    .extra-controls {
        display: grid;
        padding: 0 2rem 1rem 2rem;
    }

    .paths {
        display: grid;
        gap: 2rem;
        padding: 0 2rem 2rem 2rem;
    }

    .button {
        align-items: center;
        background: none;
        border: none;
        border-radius: 8px;
        color: var(--on-header-colour);
        cursor: pointer;
        display: flex;
        padding: 0.5rem;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: color-mix(in srgb, color-mix(in oklab, var(--primary-colour) 40%, white) 25%, transparent);
    }

    .extra-controls .button {
        background-color: var(--block-colour);
        color: var(--on-block-colour);
        gap: 0.5rem;
        padding: 0.5rem 1rem;
    }

    .extra-controls .button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }
</style>
