<script lang="ts">
    import type { SkillBlock } from "../../dto/circuit/module/skill";
    import { isCompleted } from "../../logic/circuit/skill_state/completion";
    import { isUnlocked } from "../../logic/circuit/skill_state/unlock";
    import type { Graph } from "../../logic/circuit/graph";
    import { getBlurBlocks } from "../../logic/preferences.svelte";

    let {
        block,
        moduleGraph,
        selectedSkill = $bindable(),
    }: { block: SkillBlock; moduleGraph: Graph; selectedSkill: SkillBlock } = $props();
    let completed: boolean = $derived(isCompleted(block, moduleGraph));
    let locked: boolean = $derived(!isUnlocked(block, moduleGraph));
</script>

<button
    class="skill-name"
    data-locked={locked && getBlurBlocks()}
    data-completed={completed}
    data-selected={selectedSkill === block}
    onclick={() => {
        selectedSkill = block;
    }}
    aria-label="Select skill">
    {#if completed}
        <span class="icon fa-solid fa-check"></span>
    {/if}
    <span>{block.name}</span>
</button>

<style>
    .skill-name {
        position: relative;
        background: none;
        border: none;
        padding: 0.3em 0.5em;
        margin-top: 0.3em;
        border-radius: var(--submodule-overview-selection-border-radius);
        color: var(--on-block-colour);
        cursor: pointer;
        transition:
            filter ease-in-out 150ms,
            background-color ease-in-out 150ms;
        white-space: nowrap;
    }

    .skill-name[data-completed="true"] {
        color: var(--submodule-overview-skill-completed-colour);
    }

    .skill-name:hover {
        background-color: color-mix(in srgb, var(--column-colour) 50%, transparent);
    }

    .skill-name[data-selected="true"] {
        background-color: var(--column-colour);
    }

    .skill-name[data-locked="true"] {
        filter: blur(0.2em);
    }

    .skill-name:hover[data-locked="true"],
    .skill-name[data-selected="true"][data-locked="true"] {
        /* If the skill is locked and hovered over/selected, do not blur */
        filter: none;
    }
</style>
