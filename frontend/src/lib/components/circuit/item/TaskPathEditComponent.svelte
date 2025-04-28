<script lang="ts">

    import type {TaskItem} from "../../../dto/circuit/module/task";
    import Select from "../../form/Select.svelte";
    import {editTaskPaths} from "../../../logic/circuit/updates/task_updates";
    import {getPaths} from "../../../logic/edition/edition.svelte";

    let { task }: { task: TaskItem } = $props();

    async function editPath(event: Event) {
        let newPaths = Array.from((event.target as HTMLSelectElement).selectedOptions).map(option => parseInt(option.value));
        await editTaskPaths(task, newPaths);
    }
</script>

<Select onchange={editPath} multiple>
    {#snippet button(click: (event: MouseEvent) => void, focus: () => void, blur: () => void)}
        <button aria-label="Edit path" class="button" onmousedown={click} onfocus={focus} onblur={blur}>
            <span class="fa-solid fa-shoe-prints"></span>
            <span class="counter">{task.paths.length}</span>
        </button>
    {/snippet}
    {#each getPaths() as path}
        <option value={path.id} selected={task.paths.includes(path.id)}>{path.name}</option>
    {/each}
</Select>

<style>
    .button {
        aspect-ratio: 1 / 1;
        background-color: var(--primary-surface-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-primary-surface-colour);
        cursor: pointer;
        display: grid;
        min-width: 2rem;
        place-items: center;
        position: relative;
    }

    .button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }

    .counter {
        aspect-ratio: 1 / 1;
        background-color: var(--primary-surface-colour);
        border-radius: 100%;
        font-size: var(--font-size-100);
        position: absolute;
        right: -.25rem;
        top: -.25rem;
        width: 1.125rem;
    }
</style>