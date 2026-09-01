<script lang="ts">
    import TrayComponent from "./tray/TrayComponent.svelte";
    import { cubicInOut } from "svelte/easing";
    import CheckpointsPanelComponent from "./checkpoints/CheckpointsPanelComponent.svelte";
    import PathsPanelComponent from "./paths/PathsPanelComponent.svelte";
    import ModulesPanelComponent from "./modules/ModulesPanelComponent.svelte";
    import { hasEditorRights } from "../../logic/authorisation.svelte.js";
    import ConfigPanelComponent from "./config/ConfigPanelComponent.svelte";
    import { getBlocks } from "../../logic/circuit/circuit.svelte";
    import { BlockStates } from "../../data/block_state";
    import { isLevel } from "../../logic/circuit/level.svelte";
    import { EditionLevel } from "../../data/level";
    import BookmarksPanelComponent from "./bookmarks/BookmarksPanelComponent.svelte";
    import LegendPanelComponent from "./legend/LegendPanelComponent.svelte";
    import ResearchPanelComponent from "./research/ResearchPanelComponent.svelte";
    import { getResearchInfo } from "../../logic/research.svelte";

    let openPanel:
        | "bookmarks"
        | "tray"
        | "checkpoints"
        | "paths"
        | "modules"
        | "config"
        | "legend"
        | "research"
        | undefined = $state();

    let bookmarksOpen: boolean = $state(false);
    let trayOpen: boolean = $state(false);
    let checkpointsOpen: boolean = $state(false);
    let pathsOpen: boolean = $state(false);
    let modulesOpen: boolean = $state(false);
    let configOpen: boolean = $state(false);
    let legendOpen: boolean = $state(false);
    let researchOpen: boolean = $state(false);

    $effect(() => {
        if (bookmarksOpen) {
            openPanel = "bookmarks";
        } else if (trayOpen) {
            openPanel = "tray";
        } else if (checkpointsOpen) {
            openPanel = "checkpoints";
        } else if (pathsOpen) {
            openPanel = "paths";
        } else if (modulesOpen) {
            openPanel = "modules";
        } else if (configOpen) {
            openPanel = "config";
        } else if (legendOpen) {
            openPanel = "legend";
        } else if (researchOpen) {
            openPanel = "research";
        } else {
            openPanel = undefined;
        }
    });

    function growHorizontal(_node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 150,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scaleX(${t});
            `,
        };
    }
</script>

<div class="panels">
    <BookmarksPanelComponent bind:open={bookmarksOpen}></BookmarksPanelComponent>
    <TrayComponent bind:open={trayOpen}></TrayComponent>
    <CheckpointsPanelComponent bind:open={checkpointsOpen}></CheckpointsPanelComponent>
    <PathsPanelComponent bind:open={pathsOpen}></PathsPanelComponent>
    {#if isLevel(EditionLevel)}
        <ModulesPanelComponent bind:open={modulesOpen}></ModulesPanelComponent>
        <ConfigPanelComponent bind:open={configOpen}></ConfigPanelComponent>
    {/if}
    {#if !isLevel(EditionLevel)}
        <LegendPanelComponent bind:open={legendOpen}></LegendPanelComponent>
    {/if}
    <ResearchPanelComponent bind:open={researchOpen}></ResearchPanelComponent>
</div>

{#if openPanel === undefined}
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div class="controls" in:growHorizontal={{ delay: 150 }} out:growHorizontal={{}}>
        <div class="glass surface">
            <button
                class="button"
                aria-label="Open bookmarks panel"
                onclick={() => (bookmarksOpen = true)}>
                <span class="fa-solid fa-bookmark"></span>
            </button>
        </div>

        {#if !isLevel(EditionLevel)}
            <div class="glass surface">
                <button
                    class="button"
                    aria-label="Open legend panel"
                    onclick={() => (legendOpen = true)}>
                    <span class="fa-solid fa-circle-info"></span>
                </button>
            </div>
        {/if}

        {#if hasEditorRights()}
            <div class="glass surface" ondragenter={() => (trayOpen = true)}>
                <button class="button" aria-label="Open tray" onclick={() => (trayOpen = true)}>
                    <span class="fa-solid fa-inbox"></span>
                </button>
            </div>

            <div class="glass surface">
                <button
                    class="button"
                    aria-label="Open checkpoints panel"
                    onclick={() => (checkpointsOpen = true)}>
                    <span class="fa-solid fa-calendar-days"></span>
                </button>
            </div>

            <div class="glass surface">
                <button
                    class="button"
                    aria-label="Open paths panel"
                    onclick={() => (pathsOpen = true)}>
                    <span
                        class="fa-solid fa-shoe-prints"
                        data-active={getBlocks().some(
                            block => block.state === BlockStates.AssigningPaths,
                        )}>
                    </span>
                </button>
            </div>

            {#if isLevel(EditionLevel)}
                <div class="glass surface">
                    <button
                        class="button"
                        aria-label="Open modules panel"
                        onclick={() => (modulesOpen = true)}>
                        <span class="fa-solid fa-boxes-stacked"></span>
                    </button>
                </div>

                <div class="glass surface">
                    <button
                        class="button"
                        aria-label="Open config panel"
                        onclick={() => (configOpen = true)}>
                        <span class="fa-solid fa-cog"></span>
                    </button>
                </div>
            {/if}
        {/if}

        {#if hasEditorRights() || getResearchInfo().active}
            <div class="glass surface">
                <button
                    class="button"
                    aria-label="Open research panel"
                    onclick={() => (researchOpen = true)}>
                    <span class="fa-solid fa-flask"></span>
                </button>
            </div>
        {/if}
    </div>
{/if}

<style>
    .panels {
        font-size: clamp(0.75rem, calc(16 / 1732 * 100vw), 1rem);
    }

    .controls {
        font-size: clamp(0.75rem, calc(16 / 1732 * 100vw), 1rem);

        position: fixed;
        display: grid;
        gap: 1em;
        top: 2em;
        right: 0;
        transform-origin: right;
        z-index: 90;
    }

    .surface {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        display: grid;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: var(--panel-button-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        display: grid;
        font-size: var(--font-size-600);
        margin: 0.5em;
        padding: 0.5em;
    }
    .button:focus-visible,
    .button:hover {
        background: var(--on-glass-surface-active-colour);
        outline: 1px solid var(--default-border-color);
    }

    .button span {
        text-align: center;
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
