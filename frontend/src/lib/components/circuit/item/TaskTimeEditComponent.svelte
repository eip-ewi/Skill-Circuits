<script lang="ts">

    import type {TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {editTaskTime, editTaskType} from "../../../logic/circuit/updates/task_updates";
    import Select from "../../util/Select.svelte";
    import Option from "../../util/Option.svelte";
    import {TaskIcons} from "../../../dto/task_icons";

    let { taskInfo }: { taskInfo: TaskInfo } = $props();

    async function editTime(event: Event) {
        const newTime = parseInt((event.target as HTMLInputElement).value);
        await editTaskTime(taskInfo, newTime);
    }
</script>

<div class="controls">
    <span class="icon fa-solid fa-clock"></span>
    <input name="time" type="number" min="0" value={taskInfo.time} onchange={editTime}/>
</div>

<style>
    .controls {
        align-items: center;
        display: flex;
        gap: 0.25em;
        position: relative;
    }

    .icon {
        left: .5em;
        opacity: 0.25;
        position: absolute;
    }

    input {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: .5em;
        color: var(--on-neutral-surface-colour);
        max-width: 5.625em;
        padding: .25em .5em .25em 1.75em;
    }
</style>