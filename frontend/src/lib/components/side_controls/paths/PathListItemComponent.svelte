<script lang="ts">

    import type {Path} from "../../../dto/path";
    import {deletePath, editPathDescription, editPathName} from "../../../logic/updates/path_updates";

    let { path }: { path: Path } = $props();

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editPathName(path, newName);
    }

    async function editDescription(event: Event) {
        const newDescription = (event.target as HTMLInputElement).value;
        await editPathDescription(path, newDescription);
    }

    async function remove() {
        await deletePath(path);
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
            <button aria-label="Stop editing" class="button" onclick={stopEditing}>
                <span class="fa-solid fa-check"></span>
            </button>
        {:else}
            <button aria-label="Edit path" class="button" onclick={ () => path.editing = true }>
                <span class="fa-solid fa-pencil"></span>
            </button>
            <button aria-label="Delete path" class="danger button" onclick={remove}>
                <span class="fa-solid fa-trash"></span>
            </button>
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

    .button {
        aspect-ratio: 1 / 1;
        background-color: var(--block-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-block-colour);
        cursor: pointer;
        display: grid;
        min-width: 2rem;
        place-items: center;
    }

    .button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }

    .button.danger:where(:hover, :focus-visible) {
        background-color: var(--primary-error-active-colour);
        color: var(--on-error-surface-colour);
    }

    input, textarea {
        border: none;
        border-radius: 8px;
        padding: .25rem .5rem;
        resize: none;
    }
</style>
