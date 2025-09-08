<script lang="ts">

   import type {Group} from "../../../dto/circuit/group";
   import type {Blob} from "../../../data/blob";

   let { blob }: { blob: Blob } = $props();

</script>

{#each blob.allocations as alloc}
    <div class="group-wrapper" style:grid-column={alloc.point.x + 1} style:grid-row={alloc.point.y + 1}>
        {#if alloc.showName}
            <span class="name">{blob.group.name}</span>
        {/if}

        <div class="group"
             data-connect-top={alloc.neighbours.top}
             data-connect-right={alloc.neighbours.right}
             data-connect-bottom={alloc.neighbours.bottom}
             data-connect-left={alloc.neighbours.left}
             data-connect-top-right={alloc.neighbours.topRight}
             data-connect-bottom-right={alloc.neighbours.bottomRight}
             data-connect-bottom-left={alloc.neighbours.bottomLeft}
             data-connect-top-left={alloc.neighbours.topLeft}
        >
            <div class="connector" data-connect="top"></div>
            <div class="connector" data-connect="right"></div>
            <div class="connector" data-connect="bottom"></div>
            <div class="connector" data-connect="left"></div>

            <div class="connector" data-connect="top-right"></div>
            <div class="connector" data-connect="bottom-right"></div>
            <div class="connector" data-connect="bottom-left"></div>
            <div class="connector" data-connect="top-left"></div>

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
        width: calc(100% + 4em);
        height: calc(100% + 4em);
        margin: -2em -2em;
        position: relative;
        z-index: -30;
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
        border-top: none;
    }
    .group[data-connect-right="true"] {
        border-right: none;
    }
    .group[data-connect-bottom="true"] {
        border-bottom: none;
    }
    .group[data-connect-left="true"] {
        border-left: none;
    }

    .connector {
        background: var(--group-colour);
        border: 1px solid var(--group-border-colour);
        display: none;
        position: absolute;
    }

    .group[data-connect-top="true"] .connector[data-connect="top"] {
        border-bottom: none;
        border-top: none;
        display: initial;
        left: -1px;
        height: 2em;
        top: -2em;
        width: calc(100% + 2px);
    }
    .group[data-connect-right="true"] .connector[data-connect="right"] {
        border-left: none;
        border-right: none;
        display: initial;
        height: calc(100% + 2px);
        right: -1em;
        top: -1px;
        width: 1em;
    }
    .group[data-connect-bottom="true"] .connector[data-connect="bottom"] {
        border-bottom: none;
        border-top: none;
        display: initial;
        left: -1px;
        height: 2em;
        bottom: -2em;
        width: calc(100% + 2px);
    }
    .group[data-connect-left="true"] .connector[data-connect="left"] {
        border-left: none;
        border-right: none;
        display: initial;
        height: calc(100% + 2px);
        left: -1em;
        top: -1px;
        width: 1em;
    }

    .group[data-connect-top="true"][data-connect-right="true"][data-connect-top-right="true"] .connector[data-connect="top-right"] {
        border: none;
        display: initial;
        height: 2em;
        right: -1em;
        top: -2em;
        width: 1em;
    }
    .group[data-connect-bottom="true"][data-connect-right="true"][data-connect-bottom-right="true"] .connector[data-connect="bottom-right"] {
        border: none;
        bottom: -2em;
        display: initial;
        height: 2em;
        right: -1em;
        width: 1em;
    }
    .group[data-connect-bottom="true"][data-connect-left="true"][data-connect-bottom-left="true"] .connector[data-connect="bottom-left"] {
        border: none;
        bottom: -2em;
        display: initial;
        height: 2em;
        left: -1em;
        width: 1em;
    }
    .group[data-connect-top="true"][data-connect-left="true"][data-connect-top-left="true"] .connector[data-connect="top-left"] {
        border: none;
        display: initial;
        height: 2em;
        left: -1em;
        top: -2em;
        width: 1em;
    }

    .outer-corner {
        background-image: radial-gradient(
                circle at 100% 100%,
                transparent var(--group-border-radius),
                var(--group-colour) calc(var(--group-border-radius) + 1px)
        );
        display: none;
        height: var(--group-border-radius);
        position: absolute;
        width: var(--group-border-radius);
    }

    .group[data-connect-top="true"][data-connect-right="true"][data-connect-top-right="false"] .outer-corner[data-connect="top-right"] {
        display: initial;
        right: calc(-1 * var(--group-border-radius));
        top: calc(-1 * var(--group-border-radius));
        transform: rotate(270deg);
    }
    .group[data-connect-bottom="true"][data-connect-right="true"][data-connect-bottom-right="false"] .outer-corner[data-connect="bottom-right"] {
        bottom: calc(-1 * var(--group-border-radius));
        display: initial;
        right: calc(-1 * var(--group-border-radius));
        transform: rotate(0);
    }
    .group[data-connect-bottom="true"][data-connect-left="true"][data-connect-bottom-left="false"] .outer-corner[data-connect="bottom-left"] {
        bottom: calc(-1 * var(--group-border-radius));
        display: initial;
        left: calc(-1 * var(--group-border-radius));
        transform: rotate(90deg);
    }
    .group[data-connect-top="true"][data-connect-left="true"][data-connect-top-left="false"] .outer-corner[data-connect="top-left"] {
        display: initial;
        left: calc(-1 * var(--group-border-radius));
        top: calc(-1 * var(--group-border-radius));
        transform: rotate(180deg);
    }
</style>