<script lang="ts">

    import type {Item} from "../../../dto/circuit/item";
    import {deleteItem, editItemName} from "../../../logic/circuit/updates/item_updates";
    import TaskTypeEditComponent from "./TaskTypeEditComponent.svelte";
    import TaskTimeEditComponent from "./TaskTimeEditComponent.svelte";
    import TaskLinkEditComponent from "./TaskLinkEditComponent.svelte";
    import TaskPathEditComponent from "./TaskPathEditComponent.svelte";
    import type {Snippet} from "svelte";
    import Button from "../../util/Button.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

    let { item }: { item: Item } = $props();

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editItemName(item, newName);
    }
</script>

<div class="item">
    <input class="name" name="item-name" value={item.name} onchange={editName}/>
    <WithConfirmationDialog onconfirm={ () => deleteItem(item) } icon="fa-solid fa-trash" action="Delete">
        {#snippet button(showDialog: () => void) }
            <Button square primary type="caution" aria-label="Delete item" onclick={showDialog}>
                <span class="fa-solid fa-trash" ></span>
            </Button>
        {/snippet}
        <p>
            Are you sure you want to delete '{item.name}'?
        </p>
    </WithConfirmationDialog>
</div>

<style>
    .item {
        align-items: center;
        display: flex;
        gap: 0.5em;
    }

    .name {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: .5em;
        color: var(--on-neutral-surface-colour);
        flex-grow: 1;
        padding: .25em .5em;
    }
</style>