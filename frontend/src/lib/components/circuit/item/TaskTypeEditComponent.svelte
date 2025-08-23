<script lang="ts">

    import type {TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {editTaskType} from "../../../logic/circuit/updates/task_updates";
    import Select from "../../util/Select.svelte";
    import Option from "../../util/Option.svelte";
    import {TaskIcons} from "../../../dto/task_icons";

    let { taskInfo }: { taskInfo: TaskInfo } = $props();

    async function editType(event: Event) {
        const newType = (event.target as HTMLInputElement).value;
        await editTaskType(taskInfo, newType);
    }
</script>

{#key taskInfo}
    <Select style="min-width: 3.8em; padding: 0.25em 0.5em;" onchange={editType}>
        {#each Object.keys(TaskIcons) as type}
            <Option value={type} selected={taskInfo.type === type}>
                <span class="fa-solid fa-{TaskIcons[type]}"></span>
            </Option>
        {/each}
    </Select>
{/key}

<style>
</style>