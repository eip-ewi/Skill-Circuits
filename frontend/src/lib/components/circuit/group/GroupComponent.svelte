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
        width: calc(100% + 4rem);
        height: calc(100% + 4rem);
        margin: -2rem -2rem;
        position: relative;
        z-index: -30;
    }

    .name {
        color: var(--on-group-colour);
        font-size: var(--font-size-400);
        font-weight: 500;
        top: -1.75rem;
        left: -1rem;
        position: absolute;
        text-wrap: nowrap;
        z-index: 1;
    }

    .group[data-connect-top="false"][data-connect-right="false"] {
        border-top-right-radius: 24px;
    }
    .group[data-connect-bottom="false"][data-connect-right="false"] {
        border-bottom-right-radius: 24px;
    }
    .group[data-connect-bottom="false"][data-connect-left="false"] {
        border-bottom-left-radius: 24px;
    }
    .group[data-connect-top="false"][data-connect-left="false"] {
        border-top-left-radius: 24px;
    }

    .connector {
        background: var(--group-colour);
        display: none;
        position: absolute;
    }

    .group[data-connect-top="true"] .connector[data-connect="top"] {
        display: initial;
        height: 2rem;
        top: -2rem;
        width: 100%;
    }
    .group[data-connect-right="true"] .connector[data-connect="right"] {
        display: initial;
        height: 100%;
        right: -1rem;
        width: 1rem;
    }
    .group[data-connect-bottom="true"] .connector[data-connect="bottom"] {
        display: initial;
        height: 2rem;
        bottom: -2rem;
        width: 100%;
    }
    .group[data-connect-left="true"] .connector[data-connect="left"] {
        display: initial;
        height: 100%;
        left: -1rem;
        width: 1rem;
    }

    .group[data-connect-top="true"][data-connect-right="true"][data-connect-top-right="true"] .connector[data-connect="top-right"] {
        display: initial;
        height: 2rem;
        right: -1rem;
        top: -2rem;
        width: 1rem;
    }
    .group[data-connect-bottom="true"][data-connect-right="true"][data-connect-bottom-right="true"] .connector[data-connect="bottom-right"] {
        bottom: -2rem;
        display: initial;
        height: 2rem;
        right: -1rem;
        width: 1rem;
    }
    .group[data-connect-bottom="true"][data-connect-left="true"][data-connect-bottom-left="true"] .connector[data-connect="bottom-left"] {
        bottom: -2rem;
        display: initial;
        height: 2rem;
        left: -1rem;
        width: 1rem;
    }
    .group[data-connect-top="true"][data-connect-left="true"][data-connect-top-left="true"] .connector[data-connect="top-left"] {
        display: initial;
        height: 2rem;
        left: -1rem;
        top: -2rem;
        width: 1rem;
    }

    .outer-corner {
        background-image: radial-gradient(
                circle at 100% 100%,
                transparent 24px,
                var(--group-colour) calc(24px + 1px)
        );
        display: none;
        height: 24px;
        position: absolute;
        width: 24px;
    }

    .group[data-connect-top="true"][data-connect-right="true"][data-connect-top-right="false"] .outer-corner[data-connect="top-right"] {
        display: initial;
        right: -24px;
        top: -24px;
        transform: rotate(270deg);
    }
    .group[data-connect-bottom="true"][data-connect-right="true"][data-connect-bottom-right="false"] .outer-corner[data-connect="bottom-right"] {
        bottom: -24px;
        display: initial;
        right: -24px;
        transform: rotate(0);
    }
    .group[data-connect-bottom="true"][data-connect-left="true"][data-connect-bottom-left="false"] .outer-corner[data-connect="bottom-left"] {
        bottom: -24px;
        display: initial;
        left: -24px;
        transform: rotate(90deg);
    }
    .group[data-connect-top="true"][data-connect-left="true"][data-connect-top-left="false"] .outer-corner[data-connect="top-left"] {
        display: initial;
        left: -24px;
        top: -24px;
        transform: rotate(180deg);
    }
</style>