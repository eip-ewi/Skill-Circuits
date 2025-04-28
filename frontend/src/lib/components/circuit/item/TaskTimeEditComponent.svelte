<script lang="ts">

    import type {TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {editTaskTime, editTaskType} from "../../../logic/circuit/updates/task_updates";
    import Select from "../../form/Select.svelte";
    import Option from "../../form/Option.svelte";
    import {TaskIcons} from "../../../dto/task_icons";

    let { taskInfo }: { taskInfo: TaskInfo } = $props();

    async function editTime(event: Event) {
        const newTime = parseInt((event.target as HTMLInputElement).value);
        await editTaskTime(taskInfo, newTime);
    }
</script>

<div class="controls">
    <span class="icon fa-solid fa-clock"></span>
    <input type="number" min="0" value={taskInfo.time} onchange={editTime}/>
</div>

<style>
    .controls {
        align-items: center;
        display: flex;
        gap: 0.25rem;
        position: relative;
    }

    .icon {
        left: .5rem;
        opacity: 0.25;
        position: absolute;
    }

    input {
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 8px;
        max-width: 5.625rem;
        padding: .25rem .5rem .25rem 1.75rem;
    }
</style>