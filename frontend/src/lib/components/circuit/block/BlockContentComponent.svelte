<script lang="ts">
    import type { Block } from "../../../dto/circuit/block";
    import { hasEditorRights } from "../../../logic/authorisation.svelte";
    import { getLevel } from "../../../logic/circuit/level.svelte";
    import TaskIconsComponent from "../item/TaskIconsComponent.svelte";
    import { getItemsOnPath } from "../../../logic/edition/active_path.svelte";
    import { isSkillItemRevealed } from "../../../logic/circuit/unlocked_skills.svelte";
    import type { SkillItem } from "../../../dto/circuit/edition/skill";

    let { block }: { block: Block } = $props();

    function isSkillItemVisible(item: SkillItem) {
        return item.column !== null && (!item.hidden || isSkillItemRevealed(item));
    }

    function getNumCompletedItems(skillType: "essential" | "optional") {
        return block.items.filter(item => {
            if (item.itemType === "skill") {
                return (
                    item.completed &&
                    item.essential === (skillType === "essential") &&
                    isSkillItemVisible(item)
                );
            }
            return item.completed;
        }).length;
    }

    function getNumTotalItems(skillType: "essential" | "optional") {
        return block.items.filter(item => {
            if (item.itemType === "skill") {
                return item.essential === (skillType === "essential") && isSkillItemVisible(item);
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
    {@const completed = getNumCompletedItems("essential")}
    {@const total = getNumTotalItems("essential")}
    {@const completedOpt = getNumCompletedItems("optional")}
    {@const totalOpt = getNumTotalItems("optional")}

    <div class="completion-counters">
        {#if total > 0}
            <span>
                {completed}/{total} completed
            </span>
        {/if}
        {#if block.blockType === "submodule" && totalOpt > 0}
            <span class="optional-counter">
                {completedOpt}/{totalOpt} optional
            </span>
        {/if}
    </div>
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

    .completion-counters {
        display: flex;
        flex-direction: column;
    }

    .optional-counter {
        font-style: italic;
        opacity: 35%;
    }
</style>
