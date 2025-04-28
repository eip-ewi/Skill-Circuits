<script lang="ts">
    import {getActivePath, selectPath} from "../logic/edition/active_path.svelte";
    import {onMount} from "svelte";
    import {canEditCircuit, getAuthorisation} from "../logic/authorisation.svelte.js";
    import {getPaths} from "../logic/edition/edition.svelte";
    import type {Path} from "../dto/path";
    import {isOnCircuit} from "../logic/circuit/level.svelte";

    let dialog: HTMLDialogElement | undefined = $state();

    $effect(() => {
        if (dialog !== undefined) {
            dialog.showModal();
        }
    });
</script>

{#if getActivePath() === null && !canEditCircuit() && getPaths().length > 0}
    <dialog bind:this={dialog} class="dialog">
        <div class="content">
            <h2 class="title">Which of these best describes you?</h2>
            <div class="paths">
                {#each getPaths() as path}
                    <div class="path">
                        <h3 class="path-name">{path.name}</h3>
                        <p class="path-description">{path.description}</p>
                        <div class="path-select">
                            <button onclick={ () => selectPath(path) }>
                                Select '{path.name}'
                            </button>
                        </div>
                    </div>
                {/each}
            </div>
        </div>
    </dialog>
{/if}

<style>
    .dialog {
        --blur: 0.5rem;

        border: none;
        border-radius: 16px;
        left: 50%;
        outline: none;
        position: fixed;
        top: 50%;
        transform: translate(-50%, -50%);
        transform-origin: top left;
    }

    .dialog::backdrop {
        background: none;
        backdrop-filter: blur(var(--blur));
    }

    .content {
        background: var(--block-colour);
        box-shadow: 2rem 2rem 4rem color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        display: grid;
        gap: 1rem;
        max-height: calc(100vh - 12rem);
        overflow-y: scroll;
        padding: 2rem;
    }

    .title {
        font-size: var(--font-size-700);
        font-weight: 700;
        text-align: center;
    }

    .paths {
        display: grid;
        gap: 2rem;
    }

    .path {
        display: grid;
        gap: .5rem;
    }

    .path-name {
        font-size: var(--font-size-400);
        font-weight: 500;
    }

    .path-select button {
        background-color: var(--primary-surface-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-primary-surface-colour);
        cursor: pointer;
        display: inline-block;
        padding: .5rem 1rem;
    }

    .path-select button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }
</style>