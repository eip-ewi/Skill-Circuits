<script lang="ts">
    import type {CircuitModel} from "../model/circuit";
    import {onMount} from "svelte";
    import type {CheckpointData} from "../data/checkpoint";
    import type {CircuitUpdates} from "../model/circuit_updates";

    let { checkpoint, module, updates }: { checkpoint: CheckpointData, module: CircuitModel, updates: CircuitUpdates } = $props();

    let row = $state(Math.max(...checkpoint.skills.map(s => module.getBlock(s)!.row)));

    let checkpointDate = new Date(checkpoint.deadline);
    let options: Intl.DateTimeFormatOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    let formattedCheckpointDate = new Intl.DateTimeFormat('en-US', options).format(checkpointDate);

    onMount(() => {
        updates.subscribe("blockRowChange", rowChange => {
            if (checkpoint.skills.includes(rowChange.block.id)) {
                row = Math.max(...checkpoint.skills.map(s => module.getBlock(s)!.row));
            }
        });
    });
</script>

<div class="checkpoint" style:grid-row="{row + 1}">
    <span>{checkpoint.name}</span>
    <span id="deadline">{formattedCheckpointDate}</span>
</div>

<style>
    .checkpoint {
        grid-column: 1 / -1;
        position: relative;
        bottom: -5.2rem;
        display: flex;
        gap: .5rem;
    }

    .checkpoint > span {
        /*background-color: var(--background-colour);*/
        font-size: 1.125rem;
        align-self: flex-end;
        z-index: 3;
    }

    .checkpoint > #deadline{
        font-size: smaller;
        color: var(--on-background-colour);
    }

    .checkpoint::after {
        content: "";
        border: 2px dashed var(--on-background-colour);
        border-radius: 2px;
        inset-inline: 0;
        position: absolute;
        align-self: flex-end;
        bottom: -.3rem;
    }
</style>
