<script lang="ts">
    import type { Checkpoint } from "../../../dto/checkpoint";
    import type { RegularSkillBlock, SkillBlock } from "../../../dto/circuit/module/skill";
    import type { TaskItem } from "../../../dto/circuit/module/task";
    import {
        getBlocks,
        getPlacedBlocks,
        getVisibleBlocks,
    } from "../../../logic/circuit/circuit.svelte";
    import moment from "moment";
    import { isCompleted } from "../../../logic/circuit/skill_state/completion";
    import { hasEditorRights } from "../../../logic/authorisation.svelte";
    import {
        getFirstUncompletedPastCheckpoint,
        getNextCheckpoint,
        getVisibleCheckpoints,
    } from "../../../logic/edition/edition.svelte";
    import Link from "../../util/Link.svelte";
    import { getAdditionalIcons } from "../../../logic/preferences.svelte";

    let { checkpoint }: { checkpoint: Checkpoint } = $props();

    let skillsWithCheckpointSet: SkillBlock[] = $derived(
        getVisibleBlocks()
            .filter(block => block.blockType === "skill")
            .filter(block => block.checkpoint === checkpoint.id) as SkillBlock[],
    );
    let lastRow: number = $derived(Math.max(...skillsWithCheckpointSet.map(skill => skill.row!)));
    let firstRow: number = $derived.by(() => {
        const rowNumbers = getVisibleBlocks()
            .filter(block => block.blockType === "skill")
            .filter((skill: any) => skill.checkpoint !== null && skill.checkpoint !== checkpoint.id)
            .map((skill: any) => skill.row!)
            .filter((row: number) => row < lastRow);
        if (rowNumbers.length === 0) {
            return 0;
        }
        return Math.max(...rowNumbers) + 1;
    });
    let skills: SkillBlock[] = $derived(
        getVisibleBlocks()
            .filter(block => block.blockType === "skill")
            .filter(
                (block: any) => block.row! >= firstRow && block.row! <= lastRow,
            ) as SkillBlock[],
    );

    let completed: boolean = $derived(
        !hasEditorRights() && !skills.some(skill => !isCompleted(skill)),
    );
    let passed: boolean = $derived(moment().isAfter(moment(checkpoint.deadline)));
    let focused: boolean = $derived(
        hasEditorRights() || passed || completed || getNextCheckpoint()?.id === checkpoint.id,
    );
    let warn: boolean = $derived(
        !hasEditorRights() &&
            passed &&
            !completed &&
            getFirstUncompletedPastCheckpoint()?.id === checkpoint.id,
    );

    let openWarnDialog: boolean = $state(false);
    let element: HTMLDialogElement | undefined = $state();

    function totalTime(): string {
        const relevantSkills = skills.filter(skill => skill.essential === true);

        const totalMinutes = relevantSkills.reduce((total, skill) => {
            if (!skill.items || skill.items.length === 0) {
                return total;
            }

            const skillTime = skill.items.reduce((taskTotal: number, task: TaskItem) => {
                if (task.taskType === "regular") {
                    return taskTotal + task.time;
                } else if (task.taskType === "choice" && task.tasks) {
                    const sortedTimes = task.tasks
                        .map(choiceTask => choiceTask.time)
                        .sort((a, b) => b - a);
                    const topTimes = sortedTimes.slice(0, task.minTasks);
                    return taskTotal + topTimes.reduce((sum, t) => sum + t, 0);
                }
                return taskTotal;
            }, 0);
            return total + skillTime;
        }, 0);

        const hours = Math.floor(totalMinutes / 60);
        const minutes = totalMinutes % 60;

        if (totalMinutes === 0) {
            return "0m";
        }

        if (hours > 0) {
            return `${hours}h${minutes > 0 ? ` ${minutes}m` : ""}`;
        } else {
            return `${minutes}m`;
        }
    }

    $effect(() => {
        if (element === undefined) {
            return;
        }
        if (openWarnDialog) {
            element.showModal();
        }
    });

    function showWarnDialog() {
        openWarnDialog = true;
    }

    function checkForClose(event: MouseEvent) {
        if (event.target === element) {
            openWarnDialog = false;
        }
    }
</script>

<div
    class="checkpoint"
    style:grid-row={lastRow + 1}
    data-completed={completed}
    data-focused={focused}>
    <div class="content">
        <div class="info">
            <span class="label">{checkpoint.name}</span>
            <span class="deadline">{moment(checkpoint.deadline).format("D MMMM YYYY HH:mm")}</span>
            {#if completed && getAdditionalIcons()}
                <span class="checkmark fa-solid fa-check"></span>
            {/if}
            {#if hasEditorRights()}
                <span class="time-row">
                    <i class="fa-regular fa-clock time-icon"></i>
                    <span class="time-estimate">{totalTime()}</span>
                </span>
            {/if}
        </div>
        {#if warn}
            <button class="warning" onclick={showWarnDialog}>
                <span class="fa-solid fa-triangle-exclamation"></span>
                This checkpoint has passed.
            </button>
            {#if openWarnDialog}
                <dialog bind:this={element} onclick={checkForClose} class="dialog glass">
                    <h2>What to do if you are behind</h2>
                    <p>
                        Missing a checkpoint is no cause for concern, but if you are very far
                        behind, we advise you talk to a teaching assistant, academic counsellor, or
                        lecturer. You can find more information about how to reach the academic
                        counsellors
                        <Link
                            target="_blank"
                            href="https://www.tudelft.nl/en/student/eemcs-student-portal/organisation/academic-counsellors">
                            here
                        </Link>.
                    </p>
                </dialog>
            {/if}
        {/if}
    </div>
</div>

<style>
    .checkpoint {
        align-self: end;
        display: flex;
        grid-column: 1 / -1;
        position: relative;
        transform: translateY(calc(100% + 1em));
        /* Must be above SVG lines */
        z-index: 2;
    }

    .checkpoint[data-focused="false"] {
        opacity: 0.5;
    }

    .checkpoint::after {
        background-color: var(--checkpoint-line-colour);
        border-top: var(--checkpoint-line-border);
        border-radius: var(--checkpoint-line-border-radius);
        top: calc(-1 * var(--checkpoint-line-thickness));
        content: "";
        height: var(--checkpoint-line-thickness);
        inset-inline: 0;
        position: absolute;
    }

    .checkpoint[data-completed="true"]::after {
        background-color: var(--checkpoint-completed-line-colour);
    }

    .content {
        align-items: flex-start;
        display: flex;
        gap: 1em;
        margin-top: 0.5em;
        position: absolute;
    }

    .info {
        backdrop-filter: blur(0.25rem);
        background-color: var(--checkpoint-surface-colour);
        border: var(--checkpoint-surface-border);
        border-radius: var(--checkpoint-surface-border-radius);
        color: var(--on-checkpoint-surface-colour);
        display: grid;
        padding: 0.5em 1em;
    }

    .checkpoint[data-completed="true"] .info {
        background-color: var(--checkpoint-completed-surface-colour);
        color: var(--on-checkpoint-completed-surface-colour);
    }

    .label {
        white-space: nowrap;
        grid-column: 1;
    }

    .deadline {
        font-size: var(--font-size-200);
        white-space: nowrap;
        grid-column: 1;
    }

    .checkmark {
        grid-column: 2;
        margin-left: 1em;
        align-self: end;
    }

    .time-estimate {
        font-size: var(--font-size-200);
        white-space: nowrap;
    }

    .time-row {
        display: flex;
        align-items: center;
        gap: 0.3em;
    }

    .time-icon {
        font-size: var(--font-size-100, 0.75em);
    }

    .warning {
        align-items: center;
        backdrop-filter: blur(0.25rem);
        background-color: var(--warning-banner-colour);
        border: var(--warning-banner-border);
        border-radius: var(--warning-banner-border-radius);
        color: var(--on-warning-banner-colour);
        display: flex;
        gap: 1em;
        padding: 0.5em 1em;
        cursor: pointer;
        transition: transform ease-in-out 150ms;
    }

    .warning:hover {
        transform: scale(1.05);
    }

    .dialog {
        border-radius: var(--dialog-border-radius);
        position: fixed;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        max-width: 43em;
        padding: 2em;
        border: none;
    }

    .dialog::backdrop {
        backdrop-filter: blur(0.15rem);
    }

    .dialog::before {
        backdrop-filter: blur(1rem) saturate(180%);
    }

    .dialog > h2 {
        font-size: 1.5em;
        font-weight: bold;
    }

    .dialog > p {
        margin-top: 1em;
        font-size: 1.1em;
    }

    .warning .fa-solid {
        font-size: var(--font-size-500);
    }
</style>
