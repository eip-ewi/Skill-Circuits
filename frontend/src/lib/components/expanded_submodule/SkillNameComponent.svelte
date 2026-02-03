<script lang="ts">
    import type {SkillBlock} from "../../dto/circuit/module/skill";
    import {isCompleted} from "../../logic/circuit/skill_state/completion";
    import type {SkillItem} from "../../dto/circuit/edition/skill";
    import {getGroupForItem, getItem} from "../../logic/circuit/circuit.svelte";
    import type {ModuleGroup} from "../../dto/circuit/edition/module";

    let { block, selectedSkill = $bindable() }: { block: SkillBlock, selectedSkill: SkillBlock } = $props();
    let item: SkillItem = $derived(getItem(block.id) as SkillItem);
    let module: ModuleGroup = $derived(getGroupForItem(item) as ModuleGroup);
    let completed : boolean = $derived(isCompleted(block, module.moduleGraph));
</script>

<button class="skill-name" class:completed={completed} class:selected={selectedSkill === block} onclick={() => {selectedSkill = block}} aria-label="Select skill">
    {#if completed}
        <span class="icon fa-solid fa-check"></span>
    {/if}
    <span>{block.name}</span>
</button>

<style>
.skill-name {
    background: none;
    border: none;
    border-radius: var(--submodule-overview-selection-border-radius);
    padding: 0.3em 0.5em;
    color: var(--on-block-colour);
    cursor: pointer;
    /* TODO: text wrapping
    text-overflow: ellipsis;
    white-space: nowrap; */
}

.skill-name:hover {
    background-color: color-mix(in srgb, var(--column-colour) 50%, transparent);
}

.completed {
    color: var(--submodule-overview-skill-completed-colour);
}

.selected {
    background-color: var(--column-colour);
}
</style>