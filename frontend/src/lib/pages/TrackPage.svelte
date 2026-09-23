<script lang="ts">
    import CircuitComponent from "../components/circuit/CircuitComponent.svelte";
    import WarningsComponent from "../components/WarningsComponent.svelte";
    import type { Warning } from "../data/warning";
    import { TrackLevel } from "../data/level";
    import { circuitFetched, fetchCircuit, getCircuit } from "../logic/circuit/circuit.svelte";
    import { setLevel } from "../logic/circuit/level.svelte";
    import { fetchEdition } from "../logic/edition/edition.svelte";
    import PageLayout from "./PageLayout.svelte";

    let { editionId }: { editionId: number } = $props();

    setLevel(TrackLevel);

    let warnings: Warning[] = $state([]);

    async function load() {
        await fetchEdition(editionId);
        await fetchCircuit(`/api/tracks/${editionId}/circuit`);
    }
</script>

<svelte:head>
    <title>{circuitFetched() ? getCircuit().name : "Track"} - Skill Circuits</title>
</svelte:head>

<PageLayout fullWidth>
    {#await load() then _}
        <WarningsComponent {warnings}></WarningsComponent>
        {#key getCircuit()}
            <CircuitComponent bind:warnings></CircuitComponent>
        {/key}
    {/await}
</PageLayout>
