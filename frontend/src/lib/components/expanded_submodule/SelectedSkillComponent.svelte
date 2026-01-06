<script lang="ts">
    import type {SkillBlock} from "../../dto/circuit/module/skill";
    import {isCompleted} from "../../logic/circuit/skill_state/completion";
    import {addTaskToPath, getItemsOnPath} from "../../logic/edition/active_path.svelte";
    import TasksComponent from "../circuit/item/TasksComponent.svelte";
    import StudentTrayComponent from "../side_controls/student_tray/StudentTrayComponent.svelte";
    import {getItem} from "../../logic/circuit/circuit.svelte";
    import type {TaskItem} from "../../dto/circuit/module/task";

    let { block }: { block: SkillBlock } = $props();

    let dragging: boolean = $state(false);

    // TODO: move duplicated code

    function dragEnter(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();
        dragging = true;
    }

    function dragLeave() {
        dragging = false;
    }

    function dragOver(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();
        dragging = true;
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
            return;
        }
        event.preventDefault();

        let itemId = parseInt(event.dataTransfer!.getData("skill-circuits/item"));
        let item = getItem(itemId) as TaskItem;

        await addTaskToPath(item);

        dragging = false;
    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="content" data-dragging={dragging} ondragenter={dragEnter} ondragover={dragOver} ondragleave={dragLeave} ondrop={drop}>
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