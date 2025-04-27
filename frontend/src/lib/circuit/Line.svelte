<script lang="ts">
    import type {Point} from "../util/point";
    import {getAuth} from "../data/auth.svelte";

    let { from, to, locked = false, animated = false }: { from: Point, to: Point, locked: boolean, animated: boolean } = $props();

    let radius = 16;

    function animate(path: SVGPathElement, animate: boolean) {
        if (!animate) {
            return;
        }
        let length = path.getTotalLength();
        path.style.setProperty("--length", length.toString());
    }
</script>

{#if Math.abs(from.x - to.x) < 0.5}
    <path xmlns="http://www.w3.org/2000/svg" class="line" use:animate={animated} data-animate="{animated}" data-locked="{locked}" d="M {from.x} {from.y} L {to.x} {to.y}"/>
{:else}
    {@const xOffset = from.x < to.x ? radius : -radius}
    {@const x1 = from.x}
    {@const y1 = from.y + 64 - radius}
    {@const x2 = from.x + xOffset}
    {@const y2 = from.y + 64}
    {@const x3 = to.x - xOffset}
    {@const y3 = from.y + 64}
    {@const x4 = to.x}
    {@const y4 = from.y + 64 + radius}
    <path xmlns="http://www.w3.org/2000/svg" class="line" use:animate={animated} data-animate="{animated}" data-locked="{locked && !getAuth().canEditBlocks}"
          d="M {from.x} {from.y} L {x1} {y1} S {x1} {y2}, {x2} {y2} L {x3} {y3} S {x4} {y3}, {x4} {y4} L {to.x} {to.y}"/>
{/if}

<style>
    .line {
        fill: transparent;
        stroke: #575757;
        stroke-width: 4px;
    }
    .line[data-animate="true"] {
        animation: draw 2s linear forwards;
        stroke-dasharray: var(--length);
        stroke-dashoffset: var(--length);
    }
    .line[data-locked="true"] {
        filter: blur(.2rem);
        stroke: #c0c0c0;
        animation: showLocked 150ms ease-in-out forwards;
    }

    @keyframes draw {
        to {
            stroke-dashoffset: 0;
        }
    }

    @keyframes showLocked {
        from {
            opacity: 0;
        }
        to {
            opacity: 1;
        }
    }
</style>
