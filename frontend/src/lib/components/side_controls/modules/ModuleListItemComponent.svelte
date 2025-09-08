<script lang="ts">

    import moment from "moment";
    import type {Checkpoint} from "../../../dto/checkpoint";
    import {deleteCheckpoint, editCheckpointDeadline, editCheckpointName} from "../../../logic/updates/checkpoint_updates";
    import type {Module} from "../../../dto/module";
    import {deleteModule, editModuleName} from "../../../logic/updates/module_updates";
    import Button from "../../util/Button.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

    let { module }: { module: Module } = $props();

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editModuleName(module, newName);
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
            <Button square aria-label="Stop editing" onclick={stopEditing}>
                <span class="fa-solid fa-check"></span>
            </Button>
        {:else}
            <Button square aria-label="Edit checkpoint" onclick={ () => module.editing = true }>
                <span class="fa-solid fa-pencil"></span>
            </Button>
            <WithConfirmationDialog onconfirm={ () => deleteModule(module) } icon="fa-solid fa-trash" action="Delete">
                {#snippet button(showDialog: () => void) }
                    <Button square type="caution" aria-label="Delete checkpoint" onclick={showDialog}>
                        <span class="fa-solid fa-trash"></span>
                    </Button>
                {/snippet}
                <p>
                    Are you sure you want to delete '{module.name}'?
                </p>
            </WithConfirmationDialog>
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

    input {
        border: none;
        border-radius: 8px;
        padding: .5rem 1rem;
    }
</style>
