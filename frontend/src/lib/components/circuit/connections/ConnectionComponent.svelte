<script lang="ts">
    import type {LineSegments} from "../../../data/path";
    import {generatePathString} from "../../../logic/line_segments";
    import type {Block} from "../../../dto/circuit/block";
    import {createConnectionPath} from "../../../logic/circuit/connection.svelte";
    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import {isUnlocked} from "../../../logic/circuit/display/unlock";
    import {isCompleted} from "../../../logic/circuit/display/completion";
    import {getCircuit} from "../../../logic/circuit/circuit.svelte";

    let { from, to }: { from: Block, to: Block } = $props();

    let locked: boolean = $derived(!canEditCircuit() && !(isCompleted(from) || (isUnlocked(from) && from.blockType === "skill" && !from.essential)));
    let animated: boolean = $state(false);

    let element: SVGPathElement | undefined = $state();

    $effect(() => {
        if (element !== undefined && !locked && !canEditCircuit()) {
            animate();
        }
    });

    function animate() {
        let length = element!.getTotalLength();
        element!.style.setProperty("--length", length.toString());
        animated = true;
        setTimeout(() => {
            animated = false;
        }, 2200);
    }
</script>

{#if getCircuit().boundingRect !== undefined && from.boundingRect !== undefined && to.boundingRect !== undefined}
    <path xmlns="http://www.w3.org/2000/svg" class="line" d={generatePathString(createConnectionPath(from, to), 16)}
          data-locked={locked} data-preview={to.preview === true} bind:this={element} data-animate={animated}/>
{/if}

<style>
    path {
        fill: none;
        padding: .25rem;
        stroke: #575757;
        stroke-width: 4px;
        transition: filter ease-in-out 150ms, opacity ease-in-out 150ms;
        z-index: 10;
    }

    path:hover {
        stroke: color-mix(in oklab, var(--secondary-colour) 80%, var(--background-colour));
        stroke-width: 6px;
    }

    path[data-animate="true"] {
        animation: draw 2s linear forwards;
        stroke-dasharray: var(--length);
        stroke-dashoffset: var(--length);
    }

    path[data-locked="true"] {
        opacity: 0;
        filter: blur(.2rem);
        stroke: #c0c0c0;
    }

    path[data-preview="true"] {
        opacity: 1;
    }

    @keyframes draw {
        to {
            stroke-dashoffset: 0;
        }
    }
</style>
