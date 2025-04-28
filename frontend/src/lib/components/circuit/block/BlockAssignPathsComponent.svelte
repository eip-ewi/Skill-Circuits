<script lang="ts">

    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import {getCheckpoint, getPaths, getSortedCheckpoints} from "../../../logic/edition/edition.svelte";
    import {TaskIcons} from "../../../dto/task_icons";
    import type {TaskItem} from "../../../dto/circuit/module/task";
    import type {Path} from "../../../dto/path";
    import {editTaskPaths} from "../../../logic/circuit/updates/task_updates";
    import {canEditCircuit} from "../../../logic/authorisation.svelte";

    let { skill }: { skill: SkillBlock } = $props();

    async function updatePath(event: Event, task: TaskItem, path: Path) {
        let included = (event.target as HTMLInputElement).checked;
        let newPaths = [...task.paths];
        if (included) {
            newPaths.push(path.id);
        } else {
            newPaths.splice(newPaths.indexOf(path.id)!, 1);
        }
        await editTaskPaths(task, newPaths);
    }

</script>

<div class="block">
    <div class="heading">
        <span class="name">{skill.name}</span>
    </div>
    <table>
        <thead>
            <tr>
                <th></th>
                {#each getPaths() as path}
                    <th style="width: 1%;">{path.name}</th>
                {/each}
            </tr>
        </thead>
        <tbody>
            {#each skill.items as task}
                <tr>
                    <td>
                        {#if task.taskType === "regular"}
                            <span class="task fa-solid fa-{TaskIcons[task.type]}"></span>
                        {:else}
                            <span class="task fa-solid fa-shapes"></span>
                        {/if}
                        <span>{task.name}</span>
                    </td>
                    {#each getPaths() as path}
                        <td>
                            <div class="cell">
                                <input type="checkbox" checked={task.paths.includes(path.id)} onchange={ e => updatePath(e, task, path) }/>
                            </div>
                        </td>
                    {/each}
                </tr>
            {/each}
        </tbody>
    </table>
</div>

<style>
    .block {
        display: grid;
        gap: 1rem;
    }

    .heading {
        display: grid;
        gap: 0;
    }

    .name {
        font-size: 1.25rem;
        font-weight: 700;
    }

    table {
        border-collapse: collapse;
    }

    th {
        text-align: start;
        white-space: nowrap;
        writing-mode: sideways-lr;
    }
    th:not(:first-child) {
        border-block: 1px solid var(--on-block-divider-colour);
        padding-inline: .375rem;
    }

    td:first-child {
        border-block: 1px solid var(--on-block-divider-colour);
        padding-inline: .375rem;
    }
    td:not(:first-child) {
        border: 1px solid var(--on-block-divider-colour);
    }

    .cell {
        display: grid;
        place-items: center;
        padding: .375rem;
    }
</style>