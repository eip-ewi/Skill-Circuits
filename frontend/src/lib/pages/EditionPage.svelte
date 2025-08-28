<script lang="ts">
    import CircuitComponent from "../components/circuit/CircuitComponent.svelte";
    import WarningsComponent from "../components/WarningsComponent.svelte";
    import type {Warning} from "../data/warning";
    import {setLevel} from "../logic/circuit/level.svelte";
    import {EditionLevel} from "../data/level";
    import {fetchCircuit, getCircuit} from "../logic/circuit/circuit.svelte";
    import {fetchAuthorisation, getAuthorisation, toggleViewMode} from "../logic/authorisation.svelte";
    import {fetchDevMode} from "../logic/dev_mode.svelte";
    import HeaderComponent from "../components/HeaderComponent.svelte";
    import TrayComponent from "../components/side_controls/tray/TrayComponent.svelte";
    import SideControlsComponent from "../components/side_controls/SideControlsComponent.svelte";
    import ChoosePathComponent from "../components/ChoosePathComponent.svelte";
    import {fetchActivePath, fetchPathCustomisation} from "../logic/edition/active_path.svelte";
    import {fetchEdition, getEdition} from "../logic/edition/edition.svelte";
    import PageLayout from "./PageLayout.svelte";

    let { editionId }: { editionId: number } = $props();

    setLevel(EditionLevel);

    let warnings: Warning[] = $state([]);

    async function load() {
        await fetchEdition(editionId);
        await fetchCircuit(`/api/editions/${editionId}/circuit`);
        await fetchActivePath();
        await fetchPathCustomisation();
        await fetchDevMode();
    }
</script>

<!-- TODO remove this-->
<svelte:window onkeydown={ e => { if (e.altKey && e.key === "t") { toggleViewMode(); } } }></svelte:window>

<svelte:head>
    {#if getCircuit() === undefined}
        <title>Skill Circuits</title>
    {:else}
        <title>{getEdition().course.name} - {getEdition().name} - Skill Circuits</title>
    {/if}
</svelte:head>

<PageLayout fullWidth>

    {#await load() then _}

        <WarningsComponent {warnings}></WarningsComponent>
        {#key getCircuit()}
            <CircuitComponent bind:warnings={warnings}></CircuitComponent>
        {/key}
        <SideControlsComponent></SideControlsComponent>
        <ChoosePathComponent></ChoosePathComponent>

    {/await}

</PageLayout>
