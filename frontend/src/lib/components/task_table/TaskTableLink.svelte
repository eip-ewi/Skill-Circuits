<script lang="ts">
    import { editTaskLink } from "../../logic/circuit/updates/task_updates";
    import type { TaskInfo } from "../../dto/circuit/module/task";

    let { taskInfo }: { taskInfo: TaskInfo } = $props();

    async function editLink(event: Event) {
        const newLink = (event.target as HTMLInputElement).value;
        await editTaskLink(taskInfo, newLink);
    }
</script>

<input
    name="link"
    type="text"
    placeholder="Task link"
    onchange={editLink}
    value={taskInfo.link ?? ""} />

<style>
    input {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 0.5em;
        color: var(--on-neutral-surface-colour);
        padding: 0.4em 0.5em;
        width: 100%;
        font-size: 80%;
    }
</style>
