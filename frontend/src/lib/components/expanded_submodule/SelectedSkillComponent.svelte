<script lang="ts">
    import type {SkillBlock} from "../../dto/circuit/module/skill";
    import {addTaskToPath, getItemsOnPath} from "../../logic/edition/active_path.svelte";
    import TasksComponent from "../circuit/item/TasksComponent.svelte";
    import {
        getDraggingItem,
        dragItemEnter,
        dragItemOver,
        dragItemLeave,
        setDraggingItem,
    } from "../../logic/circuit/drag_and_drop_items.svelte";
    import type {TaskItem} from "../../dto/circuit/module/task";

    let { block }: { block: SkillBlock } = $props();
    let itemMap: Map<number, TaskItem> = $derived(new Map(block.items.map(item => [item.id, item as TaskItem])));

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();

        let itemId = parseInt(event.dataTransfer!.getData("skill-circuits/item"));
        let item = itemMap.get(itemId)!;

        await addTaskToPath(item);

        setDraggingItem(false);
    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="content" data-dragging={getDraggingItem()} ondragenter={dragItemEnter} ondragover={dragItemOver} ondragleave={dragItemLeave} ondrop={drop}>
    <h2 class="name">{block.name}</h2>
    <TasksComponent tasks={getItemsOnPath(block)}></TasksComponent>
    <div class="drop-indicator"></div>
</div>

<style>
    .content {
        margin: 0.5em 1em;
        font-size: var(--font-size-300);
        position: relative;
    }

    .name {
        font-size: var(--font-size-600);
        font-weight: 500;
        margin-bottom: 0.5em;
    }

    .drop-indicator {
        background-color: var(--task-drop-indication-colour);
        border: var(--task-drop-indication-border);
        border-radius: var(--task-drop-indication-border-radius);
        inset: -0.5em -1em;
        pointer-events: none;
        position: absolute;
    }

    .content[data-dragging="false"] .drop-indicator {
        display: none;
    }
</style>