<script lang="ts">

    import type {Checkpoint} from "../../../dto/checkpoint";
    import type {RegularSkillBlock, SkillBlock} from "../../../dto/circuit/module/skill";
    import {getBlocks, getPlacedBlocks, getVisibleBlocks} from "../../../logic/circuit/circuit.svelte";
    import moment from "moment";
    import {isCompleted} from "../../../logic/circuit/skill_state/completion";
    import {canEditCircuit} from "../../../logic/authorisation.svelte";
    import {getFirstUncompletedPastCheckpoint, getNextCheckpoint, getVisibleCheckpoints} from "../../../logic/edition/edition.svelte";

    let { checkpoint }: { checkpoint: Checkpoint } = $props();

    let skills: SkillBlock[] = $derived(getVisibleBlocks().filter(block => block.blockType === "skill").filter(block => block.checkpoint === checkpoint.id));

    let completed: boolean = $derived(!canEditCircuit() && !skills.some(skill => !isCompleted(skill)));
    let passed: boolean = $derived(moment().isAfter(moment(checkpoint.deadline)));
    let focused: boolean = $derived(canEditCircuit() || passed || completed || getNextCheckpoint()?.id === checkpoint.id);
    let warn: boolean = $derived(!canEditCircuit() && passed && !completed && getFirstUncompletedPastCheckpoint()?.id === checkpoint.id);

    let row: number = $derived(Math.max(...skills.map(skill => skill.row!)));

    let openWarnDialog: boolean = $state(false);
    let element: HTMLDialogElement | undefined = $state();

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

<div class="checkpoint" style:grid-row={row + 1} data-completed={completed} data-focused={focused}>
    <div class="content">
        <div class="info">
            <span class="label">{checkpoint.name}</span>
            <span class="deadline">{moment(checkpoint.deadline).format("D MMMM YYYY HH:mm")}</span>
        </div>
        {#if warn}
            <button class="warning" onclick={showWarnDialog}>
                <span class="fa-solid fa-triangle-exclamation"></span>
                This checkpoint has passed.
            </button>
            {#if openWarnDialog}
                <dialog bind:this={element} onclick={checkForClose} class="dialog">
                    <span class="fa-solid fa-triangle-exclamation"></span>
                    <div class="dialog-header">
                        <p>This checkpoint has passed.</p>
                        <p>Some of the tasks in the skills before this point might not be completable anymore.</p>
                    </div>
                    <div class="dialog-content">
                        Missing a checkpoint is no cause for concern, but if you are very far behind, we advise you talk to a teaching assistant, academic counsellor, or lecturer.
                        You can find more information about how to reach the academic counsellors
                        <a target="_blank" href="https://www.tudelft.nl/en/student/eemcs-student-portal/organisation/academic-counsellors">
                            here
                        </a>
                        <span class="fa-solid fa-arrow-up-right-from-square"></span>.
                    </div>
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
        cursor: pointer;
        transition: transform ease-in-out 150ms;
    }

    .warning:hover {
        transform: scale(1.05);
    }

    .dialog {
        border-radius: var(--warning-banner-border-radius);
        background-color: var(--warning-pop-up-color);
        color: var(--on-warning-banner-colour);
        position: fixed;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        max-width: 45rem;
        padding: 1.1em;
        border: none;
        display: grid;
        grid-template-columns: repeat(2, auto);
        grid-template-rows: repeat(2, auto);
        gap: 1em 1.6em;
    }

    .dialog::backdrop {
        backdrop-filter: blur(.15rem);
    }

    .dialog a {
        color: var(--on-warning-banner-colour);
    }

    .dialog > span:first-child {
        font-size: 3em;
        grid-row: 1;
        grid-column: 1;
        display: flex;
        align-items: center;
    }

    .dialog-header {
        grid-row: 1;
        grid-column: 2;
        display: flex;
        flex-direction: column;
        justify-content: center;
    }

    .dialog-header > p:first-child {
        font-size: 1.5em;
        font-weight: bold;
    }

    .dialog-header > p:last-child {
        font-size: 1em;
    }

    .dialog-content {
        grid-row: 2;
        grid-column: 2;
    }

    .dialog-content > span {
        font-size: 0.6em;
    }

    .warning .fa-solid {
        font-size: var(--font-size-500);
    }
</style>