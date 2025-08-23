<script lang="ts">

    import type {Circuit} from "../../dto/circuit/circuit.js";
    import BlockComponent from "./block/BlockComponent.svelte";
    import ConnectionsComponent from "./connections/ConnectionsComponent.svelte";
    import type {Block} from "../../dto/circuit/block";
    import {onMount, tick} from "svelte";
    import {placeBlocks, placeBlocksWithCheckpoints} from "../../logic/circuit/block_placement";
    import {Graph} from "../../logic/circuit/graph";
    import type {Warning} from "../../data/warning";
    import {hasCycle} from "../../logic/diagnostics/detect_cycles";
    import {getBlocks, getCircuit, getGraph, getVisibleBlocks} from "../../logic/circuit/circuit.svelte";
    import GroupComponent from "./group/GroupComponent.svelte";
    import GroupsComponent from "./group/GroupsComponent.svelte";
    import {canEditCircuit, getAuthorisation} from "../../logic/authorisation.svelte";
    import ColumnComponent from "./column/ColumnComponent.svelte";
    import {getPlacedBlocks} from "../../logic/circuit/circuit.svelte.js";
    import {ModuleLevel} from "../../data/level";
    import {getLevel, isLevel} from "../../logic/circuit/level.svelte";
    import CheckpointComponent from "./checkpoint/CheckpointComponent.svelte";
    import { fade } from "svelte/transition";
    import {getVisibleCheckpoints} from "../../logic/edition/edition.svelte";

    let { warnings = $bindable() }: { warnings: Warning[] } = $props();

    $effect(() => {
        if (isLevel(ModuleLevel)) {
            placeBlocksWithCheckpoints(getGraph(), getVisibleCheckpoints());
        } else {
            placeBlocks(getGraph());
        }
    });

    $effect(() => {
        if (hasCycle(getGraph())) {
            warnings = [ {
                type: "cycles",
                message: "Your circuit contains cycles! While Skill Circuit will continue to work, it is strongly recommended to remove all cycles."
            } ];
        }
    });

    let element: HTMLElement;

    onMount(async () => {
        getCircuit().width = Math.max(0, ...getPlacedBlocks().map(block => block.column!)) + 1;
        await tick();
        recalculateBounds();
    });

    $effect(() => {
        canEditCircuit();
        getCircuit().width;
        getPlacedBlocks().forEach(block => block.column = Math.min(getCircuit().width! - 1, block.column!))
        recalculateBounds();
    });

    function recalculateBounds() {
        getCircuit().boundingRect = () => element.getBoundingClientRect();
    }

</script>

<svelte:window onresize={recalculateBounds}></svelte:window>

<div transition:fade|global bind:this={element} class="circuit" style="--columns: {getCircuit().width ?? 5}">

    <h1>{getCircuit().name}</h1>

    <ConnectionsComponent></ConnectionsComponent>

    <div class="grid">

        <GroupsComponent groups={getCircuit().groups}></GroupsComponent>

        {#if isLevel(ModuleLevel)}
            {#each getVisibleCheckpoints() as checkpoint}
                <CheckpointComponent {checkpoint}></CheckpointComponent>
            {/each}
        {/if}

        {#each {length: getCircuit().width ?? 5} as _, column}
            {@const height = Math.max(0, ...getBlocks().map(block => block.row ?? 0)) + 1}
            <ColumnComponent {column} {height}></ColumnComponent>
        {/each}

        {#each getVisibleBlocks() as block}
            <BlockComponent {block}></BlockComponent>
        {/each}

    </div>

</div>

{#if canEditCircuit()}
    <div>
        <button onclick={() => getCircuit().width = (getCircuit().width ?? 0) + 1 }>+Column</button>
        <button onclick={() => getCircuit().width = (getCircuit().width ?? 0) - 1 }>-Column</button>
    </div>
{/if}

<style>

    .circuit {
        font-size: clamp(.2rem, calc(16 / 1732 * 100vw), 1rem);

        align-items: start;
        display: grid;
        gap: 4em;
        justify-content: center;
        padding-bottom: 12em;
        padding-inline: 2em;
        position: relative;

    }

    h1 {
        color: var(--on-background-colour);
        font-size: var(--font-size-700);
        font-weight: 500;
        text-align: center;
    }

    .grid {
        align-items: start;
        display: grid;
        column-gap: 6em;
        grid-template-columns: repeat(var(--columns, 5), 1fr);
        row-gap: 8em;
    }

</style>