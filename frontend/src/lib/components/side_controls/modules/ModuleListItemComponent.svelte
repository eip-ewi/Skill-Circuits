<script lang="ts">

    import moment from "moment";
    import type {Checkpoint} from "../../../dto/checkpoint";
    import {deleteCheckpoint, editCheckpointDeadline, editCheckpointName} from "../../../logic/updates/checkpoint_updates";
    import type {Module} from "../../../dto/module";
    import {deleteModule, editModuleName} from "../../../logic/updates/module_updates";

    let { module }: { module: Module } = $props();

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editModuleName(module, newName);
    }

    async function remove() {
        await deleteModule(module);
    }

    async function stopEditing() {
        module.editing = false;
    }
</script>

<div class="module">
    {#if module.editing === true }
        <div class="edit">
            <input aria-label="Name" type="text" name="name" value={module.name} onchange={editName}/>
        </div>
    {:else}
        <div class="info">
            <a href="/page/modules/{module.id}">{module.name}</a>
        </div>
    {/if}
    <div class="controls">
        {#if module.editing === true }
            <button aria-label="Stop editing" class="button" onclick={stopEditing}>
                <span class="fa-solid fa-check"></span>
            </button>
        {:else}
            <button aria-label="Edit checkpoint" class="button" onclick={ () => module.editing = true }>
                <span class="fa-solid fa-pencil"></span>
            </button>
            <button aria-label="Delete checkpoint" class="danger button" onclick={remove}>
                <span class="fa-solid fa-trash"></span>
            </button>
        {/if}
    </div>
</div>

<style>
    .module {
        align-items: center;
        display: flex;
        gap: 1rem;
    }

    .module :first-child {
        flex-grow: 1;
    }

    .module .info {
        display: grid;
        gap: 0;
    }

    .module .info :first-child {
        color: var(--link-colour);
        font-size: var(--font-size-400);
        font-weight: 500;
        text-decoration: none;
    }

    .module .info :first-child:where(:hover, :focus-visible) {
        color: var(--link-active-colour);
    }

    .module .edit {
        display: grid;
        gap: 0.25rem;
    }

    .module .controls {
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

    input {
        border: none;
        border-radius: 8px;
        padding: .25rem .5rem;
    }
</style>
