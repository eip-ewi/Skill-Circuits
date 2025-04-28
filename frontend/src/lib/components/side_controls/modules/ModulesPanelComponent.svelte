<script lang="ts">

    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import ModuleListItemComponent from "./ModuleListItemComponent.svelte";
    import {createCheckpoint} from "../../../logic/updates/checkpoint_updates";
    import {getModules, getSortedCheckpoints} from "../../../logic/edition/edition.svelte";
    import {createModule} from "../../../logic/updates/module_updates";

    let { open = $bindable() }: { open: boolean } = $props();

    async function addModule() {
        let module = await createModule();
        if (module !== undefined) {
            module.editing = true;
        }
    }
</script>

{#if canEditCircuit()}
    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
    <div class="panel" aria-expanded={open}>
        <div class="heading">
            <h2>Modules</h2>
            <div class="controls">
                <button class="button" aria-label="Add module" onclick={addModule}>
                    <span class="fa-solid fa-plus"></span>
                </button>
                <button class="button" aria-label="Close panel" onclick={ () => open = false }>
                    <span class="fa-solid fa-arrow-right"></span>
                </button>
            </div>
        </div>
        <div class="modules">
            {#each getModules() as module}
                <ModuleListItemComponent {module}></ModuleListItemComponent>
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

    .modules {
        display: grid;
        gap: 2rem;
        padding: 0 2rem 2rem 2rem;
    }

    .button {
        background: none;
        border: none;
        border-radius: 8px;
        color: var(--on-header-colour);
        cursor: pointer;
        display: grid;
        justify-items: center;
        padding: 0.5rem;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: color-mix(in srgb, color-mix(in oklab, var(--primary-colour) 40%, white) 25%, transparent);
    }
</style>
