<script lang="ts">
    import type { TaskInfo } from "../../../dto/circuit/module/task";
    import { editTaskDeadline } from "../../../logic/circuit/updates/task_updates";
    import Button from "../../util/Button.svelte";
    import Dropdown from "../../util/Dropdown.svelte";

    let { taskInfo }: { taskInfo: TaskInfo } = $props();

    let expanded: boolean = $state(false);

    async function editDeadline(event: Event) {
        const newDeadline = (event.target as HTMLInputElement).value;
        await editTaskDeadline(taskInfo, newDeadline === "" ? null : newDeadline);
    }

    async function clearDeadline() {
        await editTaskDeadline(taskInfo, null);
    }

    function toggle() {
        expanded = !expanded;
    }
</script>

<Dropdown bind:open={expanded}>
    <Button
        primary
        square
        aria-label={`${expanded ? "Close" : "Open"} deadline picker`}
        aria-pressed={expanded}
        onclick={toggle}>
        {#if taskInfo.deadline}
            <span class="fa-solid fa-calendar"></span>
        {:else}
            <span class="fa-regular fa-calendar"></span>
        {/if}
    </Button>

    {#snippet dropdown()}
        <div class="editor">
            <input
                class="deadline-input"
                name="deadline"
                type="datetime-local"
                value={taskInfo.deadline ?? ""}
                onchange={editDeadline}
                aria-label="Task deadline" />
            <button
                class="deadline-action"
                type="button"
                aria-label="Clear deadline"
                onclick={clearDeadline}>
                <span class="fa-solid fa-trash"></span>
                <span>Clear deadline</span>
            </button>
        </div>
    {/snippet}
</Dropdown>

<style>
    .editor {
        display: grid;
        gap: 0.5em;
        white-space: nowrap;
    }

    .deadline-input,
    .deadline-action {
        align-items: center;
        background: none;
        border: none;
        border-radius: var(--option-border-radius);
        color: var(--on-glass-colour);
        font-family: inherit;
        padding: 0.5em 1em;
    }

    .deadline-input {
        min-width: 16em;
    }

    .deadline-action {
        cursor: pointer;
        display: flex;
        gap: 0.5em;
        white-space: nowrap;
    }

    .deadline-input:where(:hover, :focus-visible),
    .deadline-action:where(:hover, :focus-visible) {
        background-color: var(--option-active-colour);
        color: var(--on-option-active-colour);
        outline: none;
    }
</style>
