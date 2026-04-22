<script lang="ts">
    import type { Block } from "../../../dto/circuit/block";
    import type { SkillBlock } from "../../../dto/circuit/module/skill";
    import type { TaskItem } from "../../../dto/circuit/module/task";
    import { hasEditorRights, getAuthorisation } from "../../../logic/authorisation.svelte";
    import { getLevel } from "../../../logic/circuit/level.svelte";
    import { ModuleLevel } from "../../../data/level";
    import TaskIconsComponent from "../item/TaskIconsComponent.svelte";
    import { getItemsOnPath } from "../../../logic/edition/active_path.svelte";
    import { isSkillItemRevealed } from "../../../logic/circuit/unlocked_skills.svelte";

    let { block }: { block: Block } = $props();

    function getNumCompletedItems() {
        return block.items.filter(item => {
            if (item.itemType == "skill") {
                return item.completed && item.column !== null && item.essential;
            }
            return item.completed;
        }).length;
    }

    function getNumTotalItems() {
        return block.items.filter(item => {
            if (item.itemType == "skill") {
                return (
                    item.column !== null &&
                    item.essential &&
                    (!item.hidden || isSkillItemRevealed(item))
                );
            }
            return true;
        }).length;
    }
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
{:else if hasEditorRights()}
    <span>{block.items.length} {getLevel().items}</span>
{:else}
    <span>
        {getNumCompletedItems()}/{getNumTotalItems()} completed
    </span>
{/if}

<style>
    .heading {
        display: flex;
        gap: 0;
        flex-direction: column;
    }

    .name {
        font-size: 1.25em;
        font-weight: 700;
    }

    .label {
        font-style: italic;
        opacity: 35%;
        margin-top: -0.25em;
    }
</style>
