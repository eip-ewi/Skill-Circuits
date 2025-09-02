<script lang="ts">

    import type {Checkpoint} from "../../../dto/checkpoint";
    import type {RegularSkillBlock, SkillBlock} from "../../../dto/circuit/module/skill";
    import {getBlocks, getPlacedBlocks, getVisibleBlocks} from "../../../logic/circuit/circuit.svelte";
    import moment from "moment";
    import {isCompleted} from "../../../logic/circuit/skill_state/completion";
    import {canEditCircuit} from "../../../logic/authorisation.svelte";
    import {getFirstUncompletedPastCheckpoint, getNextCheckpoint, getVisibleCheckpoints} from "../../../logic/edition/edition.svelte";

    let { checkpoint }: { checkpoint: Checkpoint } = $props();

    let skills: RegularSkillBlock[] = $derived(getVisibleBlocks().filter(block => block.blockType === "skill").filter(block => block.checkpoint === checkpoint.id));

    let completed: boolean = $derived(!canEditCircuit() && !skills.some(skill => !isCompleted(skill)));
    let passed: boolean = $derived(moment().isAfter(moment(checkpoint.deadline)));
    let focused: boolean = $derived(canEditCircuit() || passed || completed || getNextCheckpoint()?.id === checkpoint.id);
    let warn: boolean = $derived(!canEditCircuit() && passed && !completed && getFirstUncompletedPastCheckpoint()?.id === checkpoint.id);

    let row: number = $derived(Math.max(...skills.map(skill => skill.row!)));

</script>

<div class="checkpoint" style:grid-row={row + 1} data-completed={completed} data-focused={focused}>
    <div class="content">
        <div class="info">
            <span class="label">{checkpoint.name}</span>
            <span class="deadline">{moment(checkpoint.deadline).format("D MMMM YYYY HH:mm")}</span>
        </div>
        {#if warn}
            <div class="warning">
                <span class="fa-solid fa-triangle-exclamation"></span>
                <p>
                    This checkpoint has passed. Some of the tasks in the skills before this point might not be completable anymore.
                    Missing a checkpoint is no cause for concern, but if you are very far behind, we advice you talk to a teaching assistant, academic counselor, or lecturer.
                </p>
            </div>
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
        margin-top: .5em;
        position: absolute;
    }

    .info {
        backdrop-filter: blur(.25rem);
        background-color: var(--checkpoint-surface-colour);
        border: var(--checkpoint-surface-border);
        border-radius: var(--checkpoint-surface-border-radius);
        color: var(--on-checkpoint-surface-colour);
        display: grid;
        padding: .5em 1em;
    }

    .checkpoint[data-completed="true"] .info {
        background-color: var(--checkpoint-completed-surface-colour);
        color: var(--on-checkpoint-completed-surface-colour);
    }

    .label {
        white-space: nowrap;
    }

    .deadline {
        font-size: var(--font-size-200);
        white-space: nowrap;
    }

    .warning {
        align-items: center;
        backdrop-filter: blur(.25rem);
        background-color: var(--warning-banner-colour);
        border: var(--warning-banner-border);
        border-radius: var(--warning-banner-border-radius);
        color: var(--on-warning-banner-colour);
        display: flex;
        gap: 1em;
        padding: .5em 1em;
    }

    .warning .fa-solid {
        font-size: var(--font-size-500);
    }
</style>