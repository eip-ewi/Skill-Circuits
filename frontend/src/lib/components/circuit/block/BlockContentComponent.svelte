<script lang="ts">
    import type { Block } from "../../../dto/circuit/block";
    import { hasEditorRights } from "../../../logic/authorisation.svelte";
    import { getLevel } from "../../../logic/circuit/level.svelte";
    import TaskIconsComponent from "../item/TaskIconsComponent.svelte";
    import { getItemsOnPath } from "../../../logic/edition/active_path.svelte";
    import { isSkillItemRevealed } from "../../../logic/circuit/unlocked_skills.svelte";
    import type { SkillItem } from "../../../dto/circuit/edition/skill";
    import { getAdditionalIcons } from "../../../logic/preferences.svelte";
    import { getCheckpoint, getVisibleCheckpoints } from "../../../logic/edition/edition.svelte";
    import { BlockStates } from "../../../data/block_state";
    import {
        type FocusModeBlockState,
        FocusModeBlockStates,
    } from "../../../data/focus_mode_block_state";
    import { getFocusModeState } from "../../../logic/circuit/focusMode.svelte";

    let { block, completed }: { block: Block; completed: boolean } = $props();

    let focusModeState: FocusModeBlockState = $derived(getFocusModeState(block.id));

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
    {#if block.blockType === "skill" && block.external && focusModeState !== FocusModeBlockStates.DisabledInFocusMode}
        <span class="label">External</span>
    {/if}
    {#if block.blockType === "skill" && !block.essential && focusModeState !== FocusModeBlockStates.DisabledInFocusMode}
        <span class="label">Optional</span>
    {/if}
    <span class="name">
        {block.name}
    </span>
</div>

<div
    style={focusModeState === FocusModeBlockStates.DisabledInFocusMode ? "visibility: hidden" : ""}>
    {#if block.blockType === "skill"}
        <TaskIconsComponent tasks={getItemsOnPath(block)}></TaskIconsComponent>
    {:else if hasEditorRights()}
        <span>{block.items.length} {getLevel().items}</span>
    {:else}
        {@const nrCompleted = getNumCompletedItems("essential")}
        {@const nrTotal = getNumTotalItems("essential")}
        {@const completedOpt = getNumCompletedItems("optional")}
        {@const totalOpt = getNumTotalItems("optional")}

        <div class="completion-counters">
            {#if nrTotal > 0}
                <span>
                    {nrCompleted}/{nrTotal} completed
                </span>
            {/if}
            {#if block.blockType === "submodule" && totalOpt > 0}
                <span class="optional-counter">
                    {completedOpt}/{totalOpt} optional
                </span>
            {/if}
        </div>
    {/if}
</div>

{#if completed && getAdditionalIcons()}
    <span class="checkmark fa-solid fa-check"></span>
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
        opacity: var(--reduced-opacity);
        margin-top: -0.25em;
    }

    .completion-counters {
        display: flex;
        flex-direction: column;
    }

    .optional-counter {
        font-style: italic;
        opacity: var(--reduced-opacity);
    }

    .checkmark {
        position: absolute;
        color: var(--on-block-task-completed-colour);
        bottom: 0.7em;
        right: 1em;
    }
</style>
