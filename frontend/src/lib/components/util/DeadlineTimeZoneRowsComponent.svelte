<script lang="ts">
    import {
        deadlineTimeZoneDiffersFromBrowser,
        formatLocalDeadline,
        formatUniversityDeadline,
        UNIVERSITY_TIME_ZONE_LABEL,
    } from "../../logic/deadlines";
    import Dropdown from "./Dropdown.svelte";

    let { deadline }: { deadline: string } = $props();

    let tooltipOpen: boolean = $state(false);
    let closeTimeout: ReturnType<typeof setTimeout> | undefined;
    let showTooltip: boolean = $derived(deadlineTimeZoneDiffersFromBrowser(deadline));

    function openTooltip() {
        clearTimeout(closeTimeout);
        tooltipOpen = true;
    }

    function closeTooltip() {
        closeTimeout = setTimeout(() => (tooltipOpen = false), 100);
    }
</script>

{#if showTooltip}
    <Dropdown bind:open={tooltipOpen}>
        <button
            class="info"
            type="button"
            aria-label="Show deadline in local and Delft time"
            onmouseenter={openTooltip}
            onfocus={openTooltip}
            onmouseleave={closeTooltip}
            onblur={closeTooltip}>
            <span class="fa-solid fa-circle-info"></span>
        </button>

        {#snippet dropdown()}
            <div
                class="tooltip"
                role="tooltip"
                onmouseenter={openTooltip}
                onmouseleave={closeTooltip}>
                <span class="deadline-timezone-rows">
                    <span class="zone">Local</span>
                    <span class="date">{formatLocalDeadline(deadline, "readable")}</span>
                    <span class="zone">{UNIVERSITY_TIME_ZONE_LABEL}</span>
                    <span class="date">{formatUniversityDeadline(deadline, "readable")}</span>
                </span>
            </div>
        {/snippet}
    </Dropdown>
{/if}

<style>
    .info {
        all: unset;
        color: inherit;
        cursor: help;
        opacity: 0.75;
    }

    .info:where(:hover, :focus-visible) {
        opacity: 1;
        outline: none;
    }

    .deadline-timezone-rows {
        align-items: baseline;
        column-gap: 0.75em;
        display: grid;
        font-variant-numeric: tabular-nums;
        grid-template-columns: auto auto;
        row-gap: 0.15em;
        white-space: nowrap;
    }

    .zone {
        letter-spacing: 0.02em;
        opacity: 0.75;
        text-align: end;
    }
</style>
