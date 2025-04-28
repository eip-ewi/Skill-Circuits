<script lang="ts">

    import type {Checkpoint} from "../../../dto/checkpoint";
    import type {RegularSkillBlock, SkillBlock} from "../../../dto/circuit/module/skill";
    import {getBlocks, getPlacedBlocks} from "../../../logic/circuit/circuit.svelte";
    import moment from "moment";
    import {isCompleted} from "../../../logic/circuit/display/completion";

    let { checkpoint }: { checkpoint: Checkpoint } = $props();

    let skills: RegularSkillBlock[] = $derived(getPlacedBlocks().filter(block => block.blockType === "skill" && !block.external).filter(block => block.checkpoint === checkpoint.id));

    let row: number = $derived(Math.max(...skills.map(skill => skill.row!)));

</script>

<div class="checkpoint" style:grid-row={row + 1}>
    <div class="info">
        <span class="label">{checkpoint.name}</span>
        <span class="deadline">{moment(checkpoint.deadline).format("D MMMM YYYY HH:mm")}</span>
    </div>
</div>

<style>
    .checkpoint {
        align-self: end;
        display: flex;
        grid-column: 1 / -1;
        position: relative;
        transform: translateY(calc(100% + 1rem));
    }

    .checkpoint::after {
        border: 2px solid var(--on-background-colour);
        border-radius: 4px;
        top: -0.25rem;
        content: "";
        height: 1px;
        inset-inline: 0;
        opacity: 0.35;
        position: absolute;
    }

    .info {
        backdrop-filter: blur(.25rem);
        background-color: color-mix(in srgb, white 50%, transparent);
        border-radius: 16px;
        display: grid;
        margin-top: .5rem;
        padding: .5rem 1rem;
        position: absolute;
    }

    .label {
        color: color-mix(in oklab, black 20%, var(--on-background-colour));
        white-space: nowrap;
    }

    .deadline {
        color: color-mix(in oklab, black 20%, var(--on-background-colour));
        font-size: var(--font-size-200);
        white-space: nowrap;
    }

</style>