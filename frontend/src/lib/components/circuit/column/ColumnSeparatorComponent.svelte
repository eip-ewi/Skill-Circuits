<script lang="ts">
    import { insertColumn } from "../../../logic/circuit/updates/position_updates.svelte.js";
    import { cubicInOut } from "svelte/easing";

    let { column, columns, height }: { column: number; columns: number; height: number } = $props();
    let showSeparator = $state(false);
    let trailing = $derived(column === columns);
    let gridColumn = $derived(trailing ? columns : column + 1);

    function lineGlassTransition(_element: Element) {
        return {
            duration: 500,
            easing: cubicInOut,
            css: (t: number) => `
                opacity: ${t};
                backdrop-filter: blur(0.5rem) saturate(180%) opacity(${t});
            `,
        };
    }
</script>

<div
    class="separator-anchor"
    data-edge={trailing ? "end" : "start"}
    style:grid-column={gridColumn}
    style:grid-row="1 / span {height}">
    <div class="button-wrapper">
        <button
            class="button"
            aria-label="Add Column"
            onclick={() => insertColumn(column - 1)}
            onmouseenter={() => (showSeparator = true)}
            onmouseleave={() => (showSeparator = false)}
            onfocus={() => (showSeparator = true)}
            onblur={() => (showSeparator = false)}>
            <i class="fa-solid fa-plus"></i>
        </button>
    </div>

    {#if showSeparator}
        <div class="line" aria-hidden="true">
            <div class="line-glass glass" transition:lineGlassTransition></div>
        </div>
    {/if}
</div>

<style>
    .separator-anchor {
        align-self: stretch;
        height: 100%;
        position: relative;
        pointer-events: none;
        width: 0;
    }

    .separator-anchor[data-edge="start"] {
        justify-self: start;
        transform: translateX(calc(-1 * var(--circuit-column-half-gap)));
    }

    .separator-anchor[data-edge="end"] {
        justify-self: end;
        transform: translateX(var(--circuit-column-half-gap));
    }

    .line {
        width: 1.8rem;
        border-radius: var(--column-separator-border-radius);
        height: calc(100% + 4rem);
        left: 50%;
        position: absolute;
        top: -2rem;
        transform: translateX(-50%);

        /* There are buttons underneath it sometimes when you expand blocks. */
        pointer-events: none;
        z-index: 99;
    }

    .line-glass {
        width: 100%;
        height: 100%;
        border-radius: inherit;
    }

    .button-wrapper {
        position: absolute;
        top: -4.5em;
        left: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        pointer-events: auto;
        transform: translateX(-50%);
        z-index: 1;
    }

    .button-wrapper:hover {
        transform: translateX(-50%) scale(1.2);
    }

    .button {
        background-color: var(--group-colour);
        border: none;
        border-radius: var(--column-button-border-radius);
        color: var(--on-glass-colour);
        border: var(--glass-border);
        cursor: pointer;
        place-items: center;
        width: 2.2em;
        height: 2.2em;
        transition: transform 0.2s;
        font-size: 0.8em;
    }
</style>
