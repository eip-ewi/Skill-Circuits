<script lang="ts">
    import type { TaskInfo } from "../../../dto/circuit/module/task";
    import { editTaskLink } from "../../../logic/circuit/updates/task_updates";
    import Button from "../../util/Button.svelte";
    import Dropdown from "../../util/Dropdown.svelte";

    let { taskInfo }: { taskInfo: TaskInfo } = $props();

    let open: boolean = $state(false);

    async function editLink(event: Event) {
        const newLink = (event.target as HTMLInputElement).value;
        await editTaskLink(taskInfo, newLink);
        open = false;
    }
</script>

<Dropdown bind:open>
    <Button square primary aria-label="Edit link" aria-pressed={open} onclick={() => (open = !open)}>
        <span
            class="fa-solid"
            class:fa-link={taskInfo.link !== null}
            class:fa-link-slash={taskInfo.link === null}>
        </span>
    </Button>

    {#snippet dropdown()}
        <div class="link-edit">
            <input
                name="link"
                type="text"
                placeholder="Task link..."
                onchange={editLink}
                value={taskInfo.link ?? ""}
                autofocus />
        </div>
    {/snippet}
</Dropdown>

<style>
    .link-edit {
        padding: 0.25em;
    }

    input {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 0.5em;
        color: var(--on-neutral-surface-colour);
        min-width: 24em;
        padding: 0.25em 0.5em;
    }
</style>
