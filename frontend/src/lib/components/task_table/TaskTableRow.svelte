<script lang="ts">
    import type {TaskInTaskList} from "../../dto/task_in_task_list";

    let { task } : { task: TaskInTaskList } = $props();
</script>

<tr>
    <th>{task.name}</th>
    <th>{task.skillName}</th>
    <th>{task.submoduleName}</th>
    <th>{task.link ?? "-"}</th>
</tr>

<style>
    th {
        padding: 0.2em 1em;
    }

    th:not(:first-child) {
        border-left: 2px solid var(--on-group-colour);
    }
</style>