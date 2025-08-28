<script lang="ts">

    import type {TaskItem} from "../../../dto/circuit/module/task";
    import Select from "../../util/Select.svelte";
    import {editTaskPaths} from "../../../logic/circuit/updates/task_updates";
    import {getPaths} from "../../../logic/edition/edition.svelte";
    import Button from "../../util/Button.svelte";

    let { task }: { task: TaskItem } = $props();

    async function editPath(event: Event) {
        let newPaths = Array.from((event.target as HTMLSelectElement).selectedOptions).map(option => parseInt(option.value));
        await editTaskPaths(task, newPaths);
    }
</script>

<Select onchange={editPath} multiple>
    {#snippet button(click: (event: MouseEvent) => void, focus: () => void, blur: () => void)}
        <div class="button">
            <Button square primary aria-label="Edit path" onmousedown={click} onfocus={focus} onblur={blur}>
                <span class="fa-solid fa-shoe-prints"></span>
                <div class="counter">
                    <span>{task.paths.length}</span>
                </div>
            </Button>
        </div>
    {/snippet}
    {#each getPaths() as path}
        <option value={path.id} selected={task.paths.includes(path.id)}>{path.name}</option>
    {/each}
</Select>

<style>
    .button {
        position: relative;
    }

    .counter {
        aspect-ratio: 1 / 1;
        background-color: var(--primary-surface-colour);
        border-radius: 100%;
        color: var(--on-primary-surface-colour);
        display: grid;
        font-size: var(--font-size-100);
        outline: var(--primary-surface-border);
        place-items: center;
        position: absolute;
        right: -.5em;
        top: -.5em;
        width: 1.5em;
    }
</style>