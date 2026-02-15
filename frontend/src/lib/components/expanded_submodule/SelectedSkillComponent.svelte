<script lang="ts">
    import type {SkillBlock} from "../../dto/circuit/module/skill";
    import {getItemsOnPath} from "../../logic/edition/active_path.svelte";
    import TasksComponent from "../circuit/item/TasksComponent.svelte";
    import {getDragging, dragEnter, dragOver, dragLeave, drop} from "../../logic/circuit/drag_and_drop_items.svelte";
    import {getGroupForItem, getItem} from "../../logic/circuit/circuit.svelte";
    import {isCompleted} from "../../logic/circuit/skill_state/completion";
    import type {SkillItem} from "../../dto/circuit/edition/skill";
    import type {ModuleGroup} from "../../dto/circuit/edition/module";

    let { block }: { block: SkillBlock } = $props();
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="content" data-dragging={getDragging()} ondragenter={dragEnter} ondragover={dragOver} ondragleave={dragLeave} ondrop={drop}>
    <h2 class="name">{block.name}</h2>
    <TasksComponent tasks={getItemsOnPath(block)}></TasksComponent>
    <div class="drop-indicator"></div>
</div>

<style>
    .content {
        margin-left: 1.5em;
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