<script lang="ts">

    import moment from "moment";
    import type {Checkpoint} from "../../../dto/checkpoint";
    import {deleteCheckpoint, editCheckpointDeadline, editCheckpointName} from "../../../logic/updates/checkpoint_updates";
    import Button from "../../util/Button.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

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
            <Button square aria-label="Stop editing" onclick={stopEditing}>
                <span class="fa-solid fa-check"></span>
            </Button>
        {:else}
            <Button square aria-label="Edit checkpoint" onclick={ () => checkpoint.editing = true }>
                <span class="fa-solid fa-pencil"></span>
            </Button>
            <WithConfirmationDialog onconfirm={ () => deleteCheckpoint(checkpoint) } icon="fa-solid fa-trash" action="Delete">
                {#snippet button(showDialog: () => void) }
                    <Button square type="caution" aria-label="Delete checkpoint" onclick={showDialog}>
                        <span class="fa-solid fa-trash"></span>
                    </Button>
                {/snippet}
                <p>
                    Are you sure you want to delete '{checkpoint.name}'?
                </p>
            </WithConfirmationDialog>
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

    input {
        border: none;
        border-radius: 8px;
        padding: .5rem 1rem;
    }
</style>
