<script lang="ts">
    import type { Block } from "../../dto/circuit/block";
    import { cubicInOut } from "svelte/easing";
    import StudentTrayComponent from "./student_tray/StudentTrayComponent.svelte";
    import LegendPanelComponent from "./legend/LegendPanelComponent.svelte";

    let { block }: { block: Block } = $props();

    let trayOpen: boolean = $state(false);
    let legendOpen: boolean = $state(false);

    function growHorizontal(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 150,
            easing: cubicInOut,
            css: (t: number) => `transform: scaleX(${t});`,
        };
    }
</script>

<StudentTrayComponent {block} bind:open={trayOpen}></StudentTrayComponent>
<LegendPanelComponent bind:open={legendOpen}></LegendPanelComponent>
{#if block.blockType === "skill" && !trayOpen && !legendOpen}
    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_mouse_events_have_key_events -->
    <div class="dialog-controls" in:growHorizontal={{ delay: 150 }} out:growHorizontal={{}}>
        <div class="glass surface" ondragenter={() => (trayOpen = true)}>
            <button class="button" aria-label="Open tray" onclick={() => (trayOpen = true)}>
                <span class="fa-solid fa-inbox"></span>
            </button>
        </div>
        <div class="glass surface">
            <button class="button" aria-label="Open legend" onclick={() => (legendOpen = true)}>
                <span class="fa-solid fa-circle-info"></span>
            </button>
        </div>
    </div>
{/if}

<style>
    .dialog-controls {
        font-size: clamp(0.75rem, calc(16 / 1732 * 100vw), 1rem);

        display: grid;
        gap: 1em;
        position: fixed;
        right: 0;
        top: 2em;
        transform-origin: right;
        z-index: 92;
    }

    .dialog-controls .surface {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        display: grid;
    }

    .dialog-controls .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: var(--panel-button-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        display: grid;
        font-size: var(--font-size-600);
        margin: 0.5em;
        padding: 0.5em;
    }

    .dialog-controls .button:focus-visible,
    .dialog-controls .button:hover {
        background: var(--on-glass-surface-active-colour);
    }

    .dialog-controls .button span {
        text-align: center;
    }
</style>
