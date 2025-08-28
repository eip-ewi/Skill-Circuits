<script lang="ts">

    import type {Path} from "../../../dto/path";
    import {deletePath, editPathDescription, editPathName} from "../../../logic/updates/path_updates";
    import Button from "../../util/Button.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

    let { path }: { path: Path } = $props();

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editPathName(path, newName);
    }

    async function editDescription(event: Event) {
        const newDescription = (event.target as HTMLInputElement).value;
        await editPathDescription(path, newDescription);
    }

    async function stopEditing() {
        path.editing = false;
    }
</script>

<div class="path">
    {#if path.editing === true }
        <div class="edit">
            <input aria-label="Name" type="text" name="name" value={path.name} onchange={editName}/>
            <textarea aria-label="Description" name="description" rows="4" placeholder="Enter a description..." onchange={editDescription}>{path.description}</textarea>
        </div>
    {:else}
        <div class="info">
            <span>{path.name}</span>
            <p>{path.description}</p>
        </div>
    {/if}
    <div class="controls">
        {#if path.editing === true }
            <Button square aria-label="Stop editing" onclick={stopEditing}>
                <span class="fa-solid fa-check"></span>
            </Button>
        {:else}
            <Button square aria-label="Edit path" onclick={ () => path.editing = true }>
                <span class="fa-solid fa-pencil"></span>
            </Button>
            <WithConfirmationDialog onconfirm={ () => deletePath(path) } icon="fa-solid fa-trash" action="Delete">
                {#snippet button(showDialog: () => void) }
                    <Button square type="caution" aria-label="Delete path" onclick={showDialog}>
                        <span class="fa-solid fa-trash"></span>
                    </Button>
                {/snippet}
                <p>
                    Are you sure you want to delete '{path.name}'?
                </p>
            </WithConfirmationDialog>
        {/if}
    </div>
</div>

<style>
    .path {
        align-items: center;
        display: flex;
        gap: 1rem;
    }

    .path :first-child {
        flex-grow: 1;
    }

    .path .info {
        display: grid;
        gap: 0;
    }

    .path .info :first-child {
        font-size: var(--font-size-400);
        font-weight: 500;
    }

    .path .edit {
        display: grid;
        gap: 0.5rem;
    }

    .path .controls {
        display: flex;
        gap: 0.5rem;
    }

    input, textarea {
        border: none;
        border-radius: 8px;
        padding: .5rem 1rem;
        resize: none;
    }
</style>
