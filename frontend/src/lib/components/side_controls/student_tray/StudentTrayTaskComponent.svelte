<script lang="ts">

    import type {TaskItem} from "../../../dto/circuit/module/task";

    let { task }: { task: TaskItem } = $props();

    let draggable: boolean = $state(false);

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/item", task.id.toString());
    }

    function dragEnd(event: DragEvent) {
    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="task" draggable={draggable} ondragstart={dragStart} ondragend={dragEnd}>
    <div role="button" tabindex="0" aria-label="Move task to skill"
         class="grip fa-solid fa-grip-vertical"
         onmouseenter={ () => draggable = true } onmouseleave={ () => setTimeout(() => draggable = false, 200) }></div>
    <span class="name">{task.name === "" && task.taskType === "choice" ? "Choice task" : task.name}</span>
    {#if task.taskType === "regular"}
        <div class="time">
            <span class="fa-solid fa-clock"></span>
            <span>{task.time}</span>
        </div>
    {/if}
</div>

<style>
    .task {
        align-items: center;
        display: flex;
        gap: .5em;
    }

    .name {
        font-size: var(--font-size-400);
    }
    
    .grip {
        cursor: grab;
        opacity: 0.5;
    }
</style>