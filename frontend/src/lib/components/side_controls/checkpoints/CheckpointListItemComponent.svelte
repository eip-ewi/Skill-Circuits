<script lang="ts">

    import moment from "moment";
    import type {Checkpoint} from "../../../dto/checkpoint";
    import {deleteCheckpoint, editCheckpointDeadline, editCheckpointName} from "../../../logic/updates/checkpoint_updates";

    let { checkpoint }: { checkpoint: Checkpoint } = $props();

    let deadlineToSet: string = checkpoint.deadline;

    $effect(() => {
        deadlineToSet = checkpoint.deadline;
    })

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editCheckpointName(checkpoint, newName);
    }

    async function editDeadline(event: Event) {
        const newDeadline = (event.target as HTMLInputElement).value;
        deadlineToSet = await editCheckpointDeadline(checkpoint, moment(newDeadline, "YYYY-MM-DDTHH:mm"));
    }

    async function remove() {
        await deleteCheckpoint(checkpoint);
    }

    async function stopEditing() {
        checkpoint.editing = false;
        checkpoint.deadline = deadlineToSet;
    }
</script>

<div class="checkpoint">
    {#if checkpoint.editing === true }
        <div class="edit">
            <input aria-label="Name" type="text" name="name" value={checkpoint.name} onchange={editName}/>
            <input aria-label="Deadline" type="datetime-local" name="deadline" value={checkpoint.deadline} onchange={editDeadline}/>
        </div>
    {:else}
        <div class="info">
            <span>{checkpoint.name}</span>
            <span>{moment(checkpoint.deadline).format("D MMMM yyyy HH:mm")}</span>
        </div>
    {/if}
    <div class="controls">
        {#if checkpoint.editing === true }
            <button aria-label="Stop editing" class="button" onclick={stopEditing}>
                <span class="fa-solid fa-check"></span>
            </button>
        {:else}
            <button aria-label="Edit checkpoint" class="button" onclick={ () => checkpoint.editing = true }>
                <span class="fa-solid fa-pencil"></span>
            </button>
            <button aria-label="Delete checkpoint" class="danger button" onclick={remove}>
                <span class="fa-solid fa-trash"></span>
            </button>
        {/if}
    </div>
</div>

<style>
    .checkpoint {
        align-items: center;
        display: flex;
        gap: 1rem;
    }

    .checkpoint :first-child {
        flex-grow: 1;
    }

    .checkpoint .info {
        display: grid;
        gap: 0;
    }

    .checkpoint .info :first-child {
        font-size: var(--font-size-400);
        font-weight: 500;
    }

    .checkpoint .edit {
        display: grid;
        gap: 0.25rem;
    }

    .checkpoint .controls {
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
