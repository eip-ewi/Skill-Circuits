<script lang="ts">

    import type {Block} from "../../../dto/circuit/block";
    import {getLevel} from "../../../logic/circuit/level.svelte";
    import {getCircuit, getGroup, getGroupForBlock} from "../../../logic/circuit/circuit.svelte";
    import ItemEditComponent from "../item/ItemEditComponent.svelte";
    import Select from "../../form/Select.svelte";
    import {editBlockCheckpoint, editBlockEssential, editBlockGroup, editBlockName} from "../../../logic/circuit/updates/block_updates";
    import {createItem} from "../../../logic/circuit/updates/item_updates";
    import moment from "moment";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import {getCheckpoint, getSortedCheckpoints} from "../../../logic/edition/edition.svelte";
    import ItemsEditComponent from "../item/ItemsEditComponent.svelte";
    import {createChoiceTask} from "../../../logic/circuit/updates/task_updates";

    let { block }: { block: Block } = $props();

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editBlockName(block, newName);
    }

    async function editGroup(event: Event) {
        const newGroup = getGroup(parseInt((event.target as HTMLSelectElement).value));
        await editBlockGroup(block, newGroup);
    }

    async function editCheckpoint(event: Event) {
        const value = (event.target as HTMLSelectElement).value;
        const newCheckpoint = value === "" ? null : getCheckpoint(parseInt(value));
        await editBlockCheckpoint(block as SkillBlock, newCheckpoint);
    }

    async function editEssential(event: Event) {
        const newEssential = (event.target as HTMLInputElement).checked;
        await editBlockEssential(block as SkillBlock, !newEssential);
    }

    async function addItem() {
        await createItem(block)
    }

    async function addChoiceTask() {
        await createChoiceTask(block as SkillBlock);
    }

</script>

<div class="edit">
    <div class="heading">
        {#if block.blockType !== "skill" || !block.external}

            <input class="name" value={block.name} onchange={editName}/>
            <Select onchange={editGroup}>
                {#each getCircuit().groups as group}
                    {@const currentGroup = getGroupForBlock(block)}
                    <option selected={group.id === currentGroup.id} value={group.id}>{group.name}</option>
                {/each}
            </Select>
            {#if block.blockType === "skill"}
                <Select onchange={editCheckpoint}>
                    <option selected={block.checkpoint === null} value="">No checkpoint</option>
                    {#each getSortedCheckpoints() as checkpoint}
                        {@const currentCheckpoint = block.checkpoint === null ? null : getCheckpoint(block.checkpoint)}
                        <option selected={checkpoint.id === currentCheckpoint?.id} value={checkpoint.id}>{checkpoint.name} ({moment(checkpoint.deadline).format("D MMMM YYYY HH:mm")})</option>
                    {/each}
                </Select>
            {/if}

        {:else}
            <span class="name">{block.name}</span>
        {/if}

        {#if block.blockType === "skill"}
            <div>
                <input id="essential-{block.id}" type="checkbox" checked={!block.essential} onchange={editEssential}>
                <label for="essential-{block.id}">Optional</label>
            </div>
        {/if}
    </div>

    {#if block.blockType !== "skill" || !block.external}
        <div class="divider"></div>

        <div class="content">
            {#if block.items.length > 0}
                <ItemsEditComponent {block}></ItemsEditComponent>
            {/if}
            <div class="new-task">
                <button class="button" onclick={addItem}>
                    <span class="fa-solid fa-plus-circle"></span>
                    <span>Create a new {getLevel().item}</span>
                </button>
                {#if block.blockType === "skill"}
                    <button class="button" onclick={addChoiceTask}>
                        <span class="fa-solid fa-list-check"></span>
                        <span>Create a new choice task</span>
                    </button>
                {/if}
            </div>
        </div>
    {/if}
</div>

<style>
    .edit {
        display: grid;
        gap: 1rem;
    }

    .heading {
        display: grid;
        gap: 0.5rem;
    }

    span.name {
        font-size: 1.25rem;
        font-weight: 700;
    }

    input.name {
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 8px;
        padding: .5rem 1rem;
    }

    .divider {
        background: var(--on-block-divider-colour);
        height: 1px;
        width: 100%;
    }

    .content {
        display: grid;
        gap: 1rem;
    }

    .button {
        background-color: var(--primary-surface-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-primary-surface-colour);
        cursor: pointer;
        display: inline-block;
        padding: .5rem 1rem;
    }

    .button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }
</style>