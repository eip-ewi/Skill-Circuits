<script lang="ts">
    import type {CircuitModel} from "../model/circuit";
    import {onMount} from "svelte";
    import type {CheckpointData} from "../data/checkpoint";
    import type {CircuitUpdates} from "../model/circuit_updates";

    let { checkpoint, module, updates }: { checkpoint: CheckpointData, module: CircuitModel, updates: CircuitUpdates } = $props();

    let row = $state(Math.max(...checkpoint.skills.map(s => module.getBlock(s)!.row)));

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
</div>

<style>
    .checkpoint {
        grid-column: 1 / -1;
        position: relative;
        /*height: 0;*/
        z-index: 2;
    }

    .checkpoint > span {
        position: absolute;
        bottom: -4.5rem;
        left: -2rem;
        background-color: var(--background-colour);
        font-size: 1.125rem;
        padding-inline: .5rem;
        z-index: 3;
    }

    .checkpoint::after {
        content: "";
        border: 2px dashed var(--on-background-colour);
        border-radius: 2px;
        inset-inline: 0;
        position: absolute;
        bottom: -4rem;
    }
</style>
