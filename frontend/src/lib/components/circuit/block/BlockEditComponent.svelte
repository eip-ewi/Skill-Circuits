<script lang="ts">

    import type {Block} from "../../../dto/circuit/block";
    import {getLevel} from "../../../logic/circuit/level.svelte";
    import {getCircuit, getGroup, getGroupForBlock} from "../../../logic/circuit/circuit.svelte";
    import ItemEditComponent from "../item/ItemEditComponent.svelte";
    import Select from "../../util/Select.svelte";
    import {editBlockGroup, editBlockName} from "../../../logic/circuit/updates/block_updates";
    import {createItem} from "../../../logic/circuit/updates/item_updates";
    import moment from "moment";
    import type {RegularSkillBlock, SkillBlock} from "../../../dto/circuit/module/skill";
    import {getCheckpoint, getSortedCheckpoints} from "../../../logic/edition/edition.svelte";
    import ItemsEditComponent from "../item/ItemsEditComponent.svelte";
    import {createChoiceTask} from "../../../logic/circuit/updates/task_updates";
    import Button from "../../util/Button.svelte";
    import {editSkillCheckpoint, editSkillEssential, editSkillHidden} from "../../../logic/circuit/updates/skill_updates";

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
        await editSkillCheckpoint(block as SkillBlock, newCheckpoint);
    }

    async function editOptional(event: Event) {
        const newOptional = (event.target as HTMLInputElement).checked;
        await editSkillEssential(block as SkillBlock, !newOptional);
    }

    async function editHidden(event: Event) {
        const newHidden = (event.target as HTMLInputElement).checked;
        await editSkillHidden(block as RegularSkillBlock, newHidden);
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

            <input aria-label="Edit {getLevel().block} name" class="name" name="name" value={block.name} onchange={editName}/>
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
                <input id="optional-{block.id}" name="optional" type="checkbox" checked={!block.essential} disabled={!block.external && block.hidden} onchange={editOptional}>
                <label for="optional-{block.id}">Optional</label>
            </div>
            {#if !block.external}
                <div>
                    <input id="hidden-{block.id}" name="hidden" type="checkbox" checked={block.hidden} onchange={editHidden}>
                    <label for="hidden-{block.id}">Hidden</label>
                </div>
            {/if}
        {/if}
    </div>

    {#if block.blockType !== "skill" || !block.external}
        <div class="divider"></div>

        <div class="content">
            {#if block.items.length > 0}
                <ItemsEditComponent {block}></ItemsEditComponent>
            {/if}
            <div class="new-task">
                <Button primary onclick={addItem}>
                    <span class="fa-solid fa-plus-circle"></span>
                    <span>Create a new {getLevel().item}</span>
                </Button>
                {#if block.blockType === "skill"}
                    <Button primary onclick={addChoiceTask}>
                        <span class="fa-solid fa-list-check"></span>
                        <span>Create a new choice task</span>
                    </Button>
                {/if}
            </div>
        </div>
    {/if}
</div>

<style>
    .edit {
        display: grid;
        gap: 1em;
    }

    .heading {
        display: grid;
        gap: 0.5em;
    }

    span.name {
        font-size: 1.25em;
        font-weight: 700;
    }

    input.name {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: .5em;
        color: var(--on-neutral-surface-colour);
        padding: .5em 1em;
    }

    input[type="checkbox"] {
        aspect-ratio: 1/1;
        width: 0.875em;
    }

    .divider {
        background: var(--on-block-divider-colour);
        height: 1px;
        width: 100%;
    }

    .content {
        display: grid;
        gap: 1em;
    }

    .new-task {
        display: flex;
        gap: .5em;
        flex-wrap: wrap;
    }
</style>