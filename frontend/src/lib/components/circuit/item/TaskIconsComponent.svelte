<script lang="ts">

    import type {TaskItem} from "../../../dto/circuit/module/task";
    import TaskIconComponent from "./TaskIconComponent.svelte";

    let { tasks }: { tasks: TaskItem[] } = $props();

</script>

<div class="tasks">
    {#each tasks as task}
        <TaskIconComponent {task}></TaskIconComponent>
    {/each}
</div>

<style>
    .tasks {
        display: flex;
        gap: .25em;
    }
</style>