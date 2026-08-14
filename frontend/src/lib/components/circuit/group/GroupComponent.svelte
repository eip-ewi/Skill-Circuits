<script lang="ts">
    import type { Blob } from "../../../data/blob";

    let { blob }: { blob: Blob } = $props();
</script>

{#each blob.allocations as alloc (`${alloc.point.x}:${alloc.point.y}`)}
    <div
        class="group-wrapper"
        style:grid-column={alloc.point.x + 1}
        style:grid-row={alloc.point.y + 1}>
        {#if alloc.showName}
            <span class="name">{blob.group.name}</span>
        {/if}

        <div
            class="group"
            data-connect-top={alloc.neighbours.top}
            data-connect-right={alloc.neighbours.right}
            data-connect-bottom={alloc.neighbours.bottom}
            data-connect-left={alloc.neighbours.left}
            data-connect-top-right={alloc.neighbours.topRight}
            data-connect-bottom-right={alloc.neighbours.bottomRight}
            data-connect-bottom-left={alloc.neighbours.bottomLeft}
            data-connect-top-left={alloc.neighbours.topLeft}>
            <div class="connector" data-connect="top"></div>
            <div class="connector" data-connect="right"></div>
            <div class="connector" data-connect="bottom"></div>
            <div class="connector" data-connect="left"></div>

            <div class="connector" data-connect="top-right"></div>
            <div class="connector" data-connect="bottom-right"></div>
            <div class="connector" data-connect="bottom-left"></div>
            <div class="connector" data-connect="top-left"></div>
        </div>
        <div
            class="outer-corners"
            data-connect-top={alloc.neighbours.top}
            data-connect-right={alloc.neighbours.right}
            data-connect-bottom={alloc.neighbours.bottom}
            data-connect-left={alloc.neighbours.left}
            data-connect-top-right={alloc.neighbours.topRight}
            data-connect-bottom-right={alloc.neighbours.bottomRight}
            data-connect-bottom-left={alloc.neighbours.bottomLeft}
            data-connect-top-left={alloc.neighbours.topLeft}>
            <div class="outer-corner" data-connect="top-right"></div>
            <div class="outer-corner" data-connect="bottom-right"></div>
            <div class="outer-corner" data-connect="bottom-left"></div>
            <div class="outer-corner" data-connect="top-left"></div>
        </div>
    </div>
{/each}

<style>
    .group-wrapper {
        height: 100%;
        position: relative;
        pointer-events: none;
        width: 100%;
    }

    .group {
        background-color: var(--group-colour);
        border: 1px solid var(--group-border-colour);
        width: calc(100% + 4em + 2px);
        height: calc(100% + 4em + 2px);
        margin: -2em -2em;
        position: relative;
        /*Justification: non iteractable should be lower than the SVG lines*/
        z-index: -2;
    }

    .outer-corners {
        width: calc(100% + 4em + 2px);
        height: calc(100% + 4em + 2px);
        position: relative;
        top: calc(-1 * 100% - 2px - 2em);
        left: calc(-2em);
        /*Justification: non iteractable should be lower than the SVG lines*/
        z-index: -1;
    }

    .name {
        color: var(--on-group-colour);
        font-size: var(--font-size-400);
        font-weight: 500;
        top: -1.75em;
        left: -1em;
        position: absolute;
        text-wrap: nowrap;
        z-index: 1;
    }

    .group[data-connect-top="false"][data-connect-right="false"] {
        border-top-right-radius: var(--group-border-radius);
    }
    .group[data-connect-bottom="false"][data-connect-right="false"] {
        border-bottom-right-radius: var(--group-border-radius);
    }
    .group[data-connect-bottom="false"][data-connect-left="false"] {
        border-bottom-left-radius: var(--group-border-radius);
    }
    .group[data-connect-top="false"][data-connect-left="false"] {
        border-top-left-radius: var(--group-border-radius);
    }

    .group[data-connect-top="true"] {
        border-top-color: var(--group-colour);
    }
    .group[data-connect-right="true"] {
        border-right-color: var(--group-colour);
    }
    .group[data-connect-bottom="true"] {
        border-bottom-color: var(--group-colour);
    }
    .group[data-connect-left="true"] {
        border-left-color: var(--group-colour);
    }

    .connector {
        background: var(--group-colour);
        border: 1px solid var(--group-colour);
        display: none;
        position: absolute;
    }

    .group[data-connect-top="true"] .connector[data-connect="top"] {
        border-left-color: var(--group-border-colour);
        border-right-color: var(--group-border-colour);
        display: initial;
        left: -1px;
        width: calc(100% + 2px);
        height: calc(2em + 2px);
        top: calc(-2em - 1px);
    }
    .group[data-connect-right="true"] .connector[data-connect="right"] {
        border-bottom-color: var(--group-border-colour);
        border-top-color: var(--group-border-colour);
        display: initial;
        height: calc(100% + 2px);
        right: calc(-1em - 1px);
        top: -1px;
        width: calc(1em + 2px);
    }
    .group[data-connect-bottom="true"] .connector[data-connect="bottom"] {
        border-left-color: var(--group-border-colour);
        border-right-color: var(--group-border-colour);
        display: initial;
        left: -1px;
        height: calc(2em + 2px);
        bottom: calc(-2em - 1px);
        width: calc(100% + 2px);
    }
    .group[data-connect-left="true"] .connector[data-connect="left"] {
        border-bottom-color: var(--group-border-colour);
        border-top-color: var(--group-border-colour);
        display: initial;
        height: calc(100% + 2px);
        left: calc(-1em - 1px);
        top: -1px;
        width: calc(1em + 2px);
    }

    .group[data-connect-top="true"][data-connect-right="true"][data-connect-top-right="true"]
        .connector[data-connect="top-right"] {
        display: initial;
        height: calc(2em + 2px);
        right: calc(-1em - 1px);
        top: calc(-2em - 1px);
        width: calc(1em + 2px);
    }
    .group[data-connect-bottom="true"][data-connect-right="true"][data-connect-bottom-right="true"]
        .connector[data-connect="bottom-right"] {
        bottom: calc(-2em - 1px);
        display: initial;
        height: calc(2em + 2px);
        right: calc(-1em - 1px);
        width: calc(1em + 2px);
    }
    .group[data-connect-bottom="true"][data-connect-left="true"][data-connect-bottom-left="true"]
        .connector[data-connect="bottom-left"] {
        bottom: calc(-2em - 1px);
        display: initial;
        height: calc(2em + 2px);
        left: calc(-1em - 1px);
        width: calc(1em + 2px);
    }
    .group[data-connect-top="true"][data-connect-left="true"][data-connect-top-left="true"]
        .connector[data-connect="top-left"] {
        display: initial;
        height: calc(2em + 2px);
        left: calc(-1em - 1px);
        top: calc(-2em - 1px);
        width: calc(1em + 2px);
    }

    .outer-corner {
        display: none;
        position: absolute;
        height: calc(var(--group-border-radius));
        width: calc(var(--group-border-radius));
        background-color: var(--group-colour);
        border-bottom-right-radius: calc(var(--group-border-radius));
        border-top: 2px solid var(--group-colour);
        border-left: 2px solid var(--group-colour);
    }

    .outer-corner::after {
        position: absolute;
        content: "";
        height: calc(var(--group-border-radius));
        width: calc(var(--group-border-radius));
        border-top-left-radius: calc(var(--group-border-radius));
        background-color: var(--background-colour);
        border: 1px solid var(--default-border-color);
        border-right: none;
        border-bottom: none;
    }

    .outer-corners[data-connect-top="true"][data-connect-right="true"][data-connect-top-right="false"]
        .outer-corner[data-connect="top-right"] {
        display: initial;
        right: calc(-1 * var(--group-border-radius) + 2px);
        top: calc(-1 * var(--group-border-radius) + 2px);
        transform: rotate(270deg);
    }
    .outer-corners[data-connect-bottom="true"][data-connect-right="true"][data-connect-bottom-right="false"]
        .outer-corner[data-connect="bottom-right"] {
        bottom: calc(-1 * var(--group-border-radius) + 2px);
        display: initial;
        right: calc(-1 * var(--group-border-radius) + 2px);
        transform: rotate(0);
    }
    .outer-corners[data-connect-bottom="true"][data-connect-left="true"][data-connect-bottom-left="false"]
        .outer-corner[data-connect="bottom-left"] {
        bottom: calc(-1 * var(--group-border-radius) + 2px);
        display: initial;
        left: calc(-1 * var(--group-border-radius) + 2px);
        transform: rotate(90deg);
    }
    .outer-corners[data-connect-top="true"][data-connect-left="true"][data-connect-top-left="false"]
        .outer-corner[data-connect="top-left"] {
        display: initial;
        left: calc(-1 * var(--group-border-radius) + 2px);
        top: calc(-1 * var(--group-border-radius) + 2px);
        transform: rotate(180deg);
    }
</style>
