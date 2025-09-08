<script lang="ts">
    import ConnectionComponent from "./ConnectionComponent.svelte";
    import type {Block} from "../../../dto/circuit/block";
    import {Graph} from "../../../logic/circuit/graph";
    import {onMount, tick} from "svelte";
    import {getGraph} from "../../../logic/circuit/circuit.svelte";
    import {areColumnsEnabled} from "../../../dto/columns.svelte";

    let visible: boolean = $state(false);

    onMount(async () => {
        await tick();
        visible = true;
    })
</script>

<svg data-interactible={!areColumnsEnabled()}>
    {#if visible}
        {#each getGraph().getEdges() as edge}
            <ConnectionComponent from={edge.from} to={edge.to}></ConnectionComponent>
        {/each}
    {/if}
</svg>

<style>
    svg {
        filter: drop-shadow(.75rem 1.25rem 1rem color-mix(in srgb, var(--shadow-colour) 20%, transparent));
        height: 100%;
        position: absolute;
        overflow: visible;
        width: 100%;
    }

    svg[data-interactible="false"] {
        pointer-events: none;
    }
</style>