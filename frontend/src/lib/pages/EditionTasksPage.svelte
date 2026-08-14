<script lang="ts">
    import { setLevel } from "../logic/circuit/level.svelte";
    import { EditionLevel } from "../data/level";
    import { fetchCircuit, getCircuit } from "../logic/circuit/circuit.svelte";
    import { canEditCircuit, toggleViewMode } from "../logic/authorisation.svelte";
    import { fetchDevMode } from "../logic/dev_mode.svelte";
    import { fetchActivePath, fetchPathCustomisation } from "../logic/edition/active_path.svelte";
    import { fetchEdition, getEdition } from "../logic/edition/edition.svelte";
    import PageLayout from "./PageLayout.svelte";
    import { getDevMode } from "../logic/dev_mode.svelte.js";
    import PageTabs from "../components/util/PageTabs.svelte";
    import Tab from "../components/util/Tab.svelte";
    import TaskTable from "../components/task_table/TaskTable.svelte";
    import type { TaskInTaskList } from "../dto/task_in_task_list";
    import { loadPage } from "../logic/routing.svelte";

    let { editionId }: { editionId: number } = $props();

    let tasks: TaskInTaskList[] = $state([]);

    setLevel(EditionLevel);

    async function load() {
        await fetchEdition(editionId);
        await fetchCircuit(`/api/editions/${editionId}/circuit`);
        await fetchActivePath();
        await fetchPathCustomisation();
        await fetchDevMode();
        await fetchEditionTasks();
    }

    $effect(() => {
        if (getCircuit() !== undefined && !canEditCircuit()) {
            loadPage(`/editions/${editionId}`, true);
        }
    });

    async function fetchEditionTasks() {
        const response = await fetch(`/api/editions/${editionId}/tasks`);
        tasks = await response.json();
    }
</script>

<svelte:window
    onkeydown={e => {
        if (getDevMode() && e.altKey && e.key === "t") {
            toggleViewMode();
        }
    }} />

<svelte:head>
    {#if getCircuit() === undefined}
        <title>Skill Circuits</title>
    {:else}
        <title>{getEdition().course.name} - {getEdition().name} - Skill Circuits</title>
    {/if}
</svelte:head>

{#await load() then _}
    {#if canEditCircuit()}
        <PageLayout fullWidth>
            <PageTabs>
                <Tab page={`/editions/${editionId}`}>Circuit</Tab>
                <Tab page={`/editions/tasks/${editionId}`}>Task list</Tab>
            </PageTabs>

            <TaskTable {tasks}></TaskTable>
        </PageLayout>
    {/if}
{/await}
