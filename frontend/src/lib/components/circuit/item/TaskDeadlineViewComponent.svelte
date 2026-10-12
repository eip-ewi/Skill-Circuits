<script lang="ts">
    import { formatPreferredDeadline } from "../../../logic/deadlines";
    import DeadlineTimeZoneRowsComponent from "../../util/DeadlineTimeZoneRowsComponent.svelte";

    let {
        deadline,
        reserveSpace = false,
    }: {
        deadline: string | null;
        reserveSpace?: boolean;
    } = $props();
</script>

{#if deadline}
    <span class="deadline">
        <span class="fa-solid fa-calendar"></span>
        <span class="value-wrapper">
            <span class="value">{formatPreferredDeadline(deadline, "readable")}</span>
            <DeadlineTimeZoneRowsComponent {deadline} />
        </span>
    </span>
{:else if reserveSpace}
    <span class="deadline deadline-placeholder" aria-hidden="true">
        <span class="fa-solid fa-calendar"></span>
        <span class="value-wrapper">
            <span class="value">00 Xxx 0000, 00:00</span>
            <span class="info fa-solid fa-circle-info"></span>
        </span>
    </span>
{/if}

<style>
    .deadline {
        align-items: center;
        display: flex;
        font-variant-numeric: tabular-nums;
        gap: 0.5em;
        margin-right: 1em;
    }

    .value-wrapper {
        display: flex;
        align-items: flex-start;
        gap: 0.25rem;
    }

    .value {
        white-space: nowrap;
    }

    .deadline-placeholder {
        visibility: hidden;
    }
</style>
