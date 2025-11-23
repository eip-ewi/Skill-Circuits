<script lang="ts">
    import type {LineSegments} from "../../../data/path";
    import {generatePathString} from "../../../logic/line_segments";
    import type {Block} from "../../../dto/circuit/block";
    import {createConnectionPath} from "../../../logic/circuit/connection.svelte";
    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import {isUnlocked} from "../../../logic/circuit/skill_state/unlock";
    import {isCompleted} from "../../../logic/circuit/skill_state/completion";
    import {getCircuit} from "../../../logic/circuit/circuit.svelte";
    import {onMount, tick} from "svelte";
    import {getBlurBlocks} from "../../../logic/preferences.svelte";

    let { from, to }: { from: Block, to: Block } = $props();

    let locked: boolean = $derived(!canEditCircuit() && !(isCompleted(from) || (isUnlocked(from) && from.blockType === "skill" && !from.essential)));
    let animated: boolean = $state(false);

    let element: SVGPathElement | undefined = $state();

    let radius: number = $state(getDefinedRadius());

    onMount(() => {
        radius = Math.max(3.2, Math.min(16 * window.innerWidth / 1732, 16));
    });

    $effect(() => {
        if (element !== undefined && !locked && !canEditCircuit()) {
            animate();
        }
    });

    $effect(() => {
        // TODO: check if get theme is needed here
        tick().then(() => {
            radius = getDefinedRadius();
        });
    })

    function getDefinedRadius() {
        return parseInt(getComputedStyle(document.documentElement).getPropertyValue("--connection-radius"));
    }

    function animate() {
        let length = element!.getTotalLength();
        element!.style.setProperty("--length", length.toString());
        animated = true;
        setTimeout(() => {
            animated = false;
        }, 2200);
    }
</script>

<svelte:window onresize={ () => radius = Math.max(3.2, Math.min(getDefinedRadius() * window.innerWidth / 1732, getDefinedRadius())) }></svelte:window>

{#if getCircuit().boundingRect !== undefined && from.boundingRect !== undefined && to.boundingRect !== undefined}
    {@const path = createConnectionPath(from, to)}
    {#if path !== undefined}
        <path xmlns="http://www.w3.org/2000/svg" class="line" d={generatePathString(path, radius)}
              data-locked={locked && getBlurBlocks()} data-preview={to.preview === true && locked} bind:this={element} data-animate={animated}/>
    {/if}
{/if}

<style>
    path {
        fill: none;
        padding: .25rem;
        stroke: var(--connection-colour);
        stroke-width: var(--connection-width);
        transition: filter ease-in-out 150ms, opacity ease-in-out 150ms;
        z-index: 10;
    }

    path:hover {
        stroke: var(--connection-highlighted-colour);
        stroke-width: var(--connection-highlighted-width);
    }

    path[data-animate="true"] {
        animation: draw 2s linear forwards;
        stroke-dasharray: var(--length);
        stroke-dashoffset: var(--length);
    }

    path[data-locked="true"] {
        opacity: 0;
        filter: blur(.2em);
    }

    path[data-preview="true"] {
        opacity: 0.3;
    }

    @keyframes draw {
        to {
            stroke-dashoffset: 0;
        }
    }
</style>
