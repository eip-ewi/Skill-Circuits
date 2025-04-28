<script lang="ts">

    import type {Block} from "../../../dto/circuit/block";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import type {TaskItem} from "../../../dto/circuit/module/task";
    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import {getLevel} from "../../../logic/circuit/level.svelte";
    import {ModuleLevel} from "../../../data/level";
    import TaskIconsComponent from "../item/TaskIconsComponent.svelte";
    import {getItemsOnPath} from "../../../logic/edition/active_path.svelte";

    let { block }: { block: Block } = $props();

</script>

<div class="heading">
    {#if block.blockType === "skill" && block.external}
        <span class="label">External</span>
    {/if}
    {#if block.blockType === "skill" && !block.essential}
        <span class="label">Optional</span>
    {/if}
    <span class="name">{block.name}</span>
</div>

{#if block.blockType === "skill"}
    <TaskIconsComponent tasks={getItemsOnPath(block)}></TaskIconsComponent>
{:else if canEditCircuit()}
    <span>{block.items.length} {getLevel().items}</span>
{:else}
    <span>{block.items.filter(item => item.completed).length}/{block.items.length} completed</span>
{/if}

<style>
    .heading {
        display: flex;
        gap: 0;
        flex-direction: column;
    }

    .name {
        font-size: 1.25rem;
        font-weight: 700;
    }

    .label {
        font-style: italic;
        opacity: 35%;
        margin-top: -.25rem;
    }
</style>