<script lang="ts">

    import type {Item} from "../../../dto/circuit/item";
    import {deleteItem, editItemName} from "../../../logic/circuit/updates/item_updates";
    import TaskTypeEditComponent from "./TaskTypeEditComponent.svelte";
    import TaskTimeEditComponent from "./TaskTimeEditComponent.svelte";
    import TaskLinkEditComponent from "./TaskLinkEditComponent.svelte";
    import TaskPathEditComponent from "./TaskPathEditComponent.svelte";
    import type {Snippet} from "svelte";

    let { item }: { item: Item } = $props();

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editItemName(item, newName);
    }

    async function remove() {
        await deleteItem(item);
    }
</script>

<div class="item">
    <input class="name" value={item.name} onchange={editName}/>
    <button aria-label="Remove item" class="button danger fa-solid fa-trash" onclick={remove}></button>
</div>

<style>
    .item {
        align-items: center;
        display: flex;
        gap: 0.5rem;
    }

    .name {
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 8px;
        flex-grow: 1;
        padding: .25rem .5rem;
    }

    .button {
        aspect-ratio: 1 / 1;
        background-color: var(--primary-surface-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-primary-surface-colour);
        cursor: pointer;
        display: grid;
        min-width: 2rem;
        place-items: center;
    }

    .button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }

    .button.danger {
        background-color: var(--primary-error-colour);
        color: var(--on-error-surface-colour);
    }
    .button.danger:where(:hover, :focus-visible) {
        background-color: var(--primary-error-active-colour);
        color: var(--on-error-surface-colour);
    }
</style>