<script lang="ts">
    import type {EditionCircuit} from "../dto/circuit/edition/edition";
    import CircuitComponent from "../components/circuit/CircuitComponent.svelte";
    import WarningsComponent from "../components/WarningsComponent.svelte";
    import type {Warning} from "../data/warning";
    import {setLevel} from "../logic/circuit/level.svelte";
    import {EditionLevel, ModuleLevel} from "../data/level";
    import {fetchAuthorisation, getAuthorisation, toggleViewMode} from "../logic/authorisation.svelte";
    import type {ModuleCircuit} from "../dto/circuit/module/module";
    import {fetchDevMode, getDevMode} from "../logic/dev_mode.svelte";
    import {circuitFetched, fetchCircuit, getBlocks, getCircuit} from "../logic/circuit/circuit.svelte";
    import HeaderComponent from "../components/HeaderComponent.svelte";
    import TrayComponent from "../components/side_controls/tray/TrayComponent.svelte";
    import SideControlsComponent from "../components/side_controls/SideControlsComponent.svelte";
    import ChoosePathComponent from "../components/ChoosePathComponent.svelte";
    import {fetchActivePath} from "../logic/edition/active_path.svelte";
    import {fetchEdition} from "../logic/edition/edition.svelte";

    let { moduleId }: { moduleId: number } = $props();

    setLevel(ModuleLevel);

    let warnings: Warning[] = $state([]);

    async function load() {
        await fetchCircuit(`/api/modules/${moduleId}/circuit`);
        await fetchEdition((getCircuit() as ModuleCircuit).editionId);
        await fetchActivePath();
        await fetchDevMode();
    }
</script>

<!-- TODO remove this-->
<svelte:window onkeydown={ e => { if (e.altKey && e.key === "t") { toggleViewMode(); } } }></svelte:window>

<svelte:head>
    {#if circuitFetched()}
        <title>{getCircuit().name}- Skill Circuits</title>
    {:else}
        <title>Skill Circuits</title>
    {/if}
</svelte:head>

<main>

    {#await load() then _}

        <WarningsComponent {warnings}></WarningsComponent>
        {#key getCircuit()}
            <CircuitComponent bind:warnings={warnings}></CircuitComponent>
        {/key}
        <SideControlsComponent></SideControlsComponent>
        <ChoosePathComponent></ChoosePathComponent>

    {/await}

</main>

<style>
    main {
        align-content: start;
        display: grid;
        justify-content: center;
        height: 100%;
        padding: 4rem 2rem 2rem 2rem;
    }
</style>