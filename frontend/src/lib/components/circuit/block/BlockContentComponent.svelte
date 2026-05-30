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
    import { getCheckpoint, getVisibleCheckpoints } from "../../../logic/edition/edition.svelte";
    import { BlockStates } from "../../../data/block_state";
    import { FocusModeBlockStates } from "../../../data/focus_mode_block_state";

    let { block }: { block: Block } = $props();
</script>

<div class="heading">
    {#if block.blockType === "skill" && block.external && block.focusModeState !== FocusModeBlockStates.DisabledInFocusMode}
        <span class="label">External</span>
    {/if}
    {#if block.blockType === "skill" && !block.essential && block.focusModeState !== FocusModeBlockStates.DisabledInFocusMode}
        <span class="label">Optional</span>
    {/if}
    <span class="name">{block.name}</span>
</div>

<div
    style={block.focusModeState === FocusModeBlockStates.DisabledInFocusMode
        ? "visibility: hidden"
        : ""}>
    {#if block.blockType === "skill"}
        <TaskIconsComponent tasks={getItemsOnPath(block)}></TaskIconsComponent>
    {:else if hasEditorRights()}
        <span>{block.items.length} {getLevel().items}</span>
    {:else}
        <span>
            {block.items.filter(
                item => item.completed && (item.itemType !== "skill" || item.column !== null),
            ).length}/{block.items.filter(
                item =>
                    item.itemType !== "skill" ||
                    (item.column !== null && (!item.hidden || isSkillItemRevealed(item))),
            ).length} completed
        </span>
    {/if}
</div>

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
