<script lang="ts">
    import type {CircuitModel} from "../model/circuit";
    import {onMount} from "svelte";
    import type {CheckpointData} from "../data/checkpoint";
    import Checkpoint from "./Checkpoint.svelte";
    import type {CircuitUpdates} from "../model/circuit_updates";

    let { module, updates }: { module: CircuitModel, updates: CircuitUpdates } = $props();

    let checkpoints: CheckpointData[] | undefined = $state(undefined);

    onMount(async () => {
        let response = await fetch(`/api/module/${module.id}/checkpoints`);
        checkpoints = await response.json();
    });
</script>

{#if checkpoints !== undefined}
    {#each checkpoints as checkpoint}
        <Checkpoint checkpoint={checkpoint} module={module} updates={updates}/>
    {/each}
{/if}
