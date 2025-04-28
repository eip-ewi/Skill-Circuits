<script lang="ts">

    import TrayComponent from "./tray/TrayComponent.svelte";
    import {cubicInOut} from "svelte/easing";
    import CheckpointsPanelComponent from "./checkpoints/CheckpointsPanelComponent.svelte";
    import {getAuthorisation} from "../../logic/authorisation.svelte";
    import PathsPanelComponent from "./paths/PathsPanelComponent.svelte";
    import ModulesPanelComponent from "./modules/ModulesPanelComponent.svelte";
    import {canEditCircuit} from "../../logic/authorisation.svelte.js";
    import ConfigPanelComponent from "./config/ConfigPanelComponent.svelte";
    import {getBlocks} from "../../logic/circuit/circuit.svelte";
    import {BlockStates} from "../../data/block_state";
    import {isLevel} from "../../logic/circuit/level.svelte";
    import {EditionLevel} from "../../data/level";

    let openPanel: "tray" | "checkpoints" | "paths" | "modules" | "config" | undefined = $state();

    let trayOpen: boolean = $state(false);
    let checkpointsOpen: boolean = $state(false);
    let pathsOpen: boolean = $state(false);
    let modulesOpen: boolean = $state(false);
    let configOpen: boolean = $state(false);

    $effect(() => {
        if (trayOpen) {
            openPanel = "tray";
        } else if (checkpointsOpen) {
            openPanel = "checkpoints";
        } else if (pathsOpen) {
            openPanel = "paths";
        } else if (modulesOpen) {
            openPanel = "modules";
        } else if (configOpen) {
            openPanel = "config";
        } else {
            openPanel = undefined;
        }
    })

    function growHorizontal(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 150,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scaleX(${t});
            `,
        }
    }
</script>

<TrayComponent bind:open={trayOpen}></TrayComponent>
<CheckpointsPanelComponent bind:open={checkpointsOpen}></CheckpointsPanelComponent>
<PathsPanelComponent bind:open={pathsOpen}></PathsPanelComponent>
{#if isLevel(EditionLevel)}
    <ModulesPanelComponent bind:open={modulesOpen}></ModulesPanelComponent>
    <ConfigPanelComponent bind:open={configOpen}></ConfigPanelComponent>
{/if}

{#if openPanel === undefined}

    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events, a11y_mouse_events_have_key_events -->
    <div class="controls" in:growHorizontal={{ delay: 150 }} out:growHorizontal={{}}>
        {#if canEditCircuit()}
            <div class="surface" ondragenter={ () => trayOpen = true }>
                <button class="button" aria-label="Open tray" onclick={ () => trayOpen = true }>
                    <span class="fa-solid fa-inbox"></span>
                </button>
            </div>

            <div class="surface">
                <button class="button" aria-label="Open checkpoints panel" onclick={ () => checkpointsOpen = true }>
                    <span class="fa-solid fa-calendar-days"></span>
                </button>
            </div>

            <div class="surface">
                <button class="button" aria-label="Open paths panel" onclick={ () => pathsOpen = true }>
                    <span class="fa-solid fa-shoe-prints" data-active={getBlocks().some(block => block.state === BlockStates.AssigningPaths)}></span>
                </button>
            </div>

            {#if isLevel(EditionLevel)}
                <div class="surface">
                    <button class="button" aria-label="Open modules panel" onclick={ () => modulesOpen = true }>
                        <span class="fa-solid fa-boxes-stacked"></span>
                    </button>
                </div>

                <div class="surface">
                    <button class="button" aria-label="Open config panel" onclick={ () => configOpen = true }>
                        <span class="fa-solid fa-cog"></span>
                    </button>
                </div>
            {/if}
        {/if}
    </div>

{/if}

<style>
    .controls {
        position: fixed;
        display: grid;
        gap: 1rem;
        top: 2rem;
        right: 0;
        transform-origin: right;
        z-index: 90;
    }

    .surface {
        backdrop-filter: blur(.5rem) saturate(180%);
        background: color-mix(in srgb, white 25%, transparent);
        border-radius: 24px 0 0 24px;
        box-shadow:
                .75rem 1.25rem 1.9rem 0 color-mix(in srgb, var(--shadow-colour) 4%, transparent),
                inset 0.125rem 0.125rem 0.0625rem 0 rgba(255 255 255 / 0.6),
                inset -0.0625rem -0.0625rem 0.0625rem rgba(255 255 255 / 0.4);
        display: grid;
        place-items: center;
    }

    .button {
        background: none;
        border: none;
        border-radius: 16px;
        color: var(--on-header-colour);
        cursor: pointer;
        display: grid;
        font-size: var(--font-size-600);
        margin: .5rem;
        padding: 1rem;
        place-items: center;
    }
    .button:focus-visible, .button:hover {
        background: color-mix(in srgb, color-mix(in oklab, var(--primary-colour) 40%, white) 25%, transparent);
    }

    .button span[data-active="true"] {
        animation: wiggle 400ms infinite;
    }

    @keyframes wiggle {
        0% {
            transform: rotate(0deg);
        }
        25% {
            transform: rotate(-5deg);
        }
        75% {
            transform: rotate(5deg);
        }
        100% {
            transform: rotate(0deg);
        }
    }
</style>
