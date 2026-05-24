<script lang="ts">
    import { type BlockAction, BlockActions } from "../../../data/block_action";
    import type { Block } from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";
    import { cubicInOut } from "svelte/easing";
    import { hasEditorRights } from "../../../logic/authorisation.svelte";
    import { BlockStates } from "../../../data/block_state";
    import {
        getFocusModeBlock,
        getFocusModeDepth,
        getMaxDepth,
        isInFocusMode,
        resetFocusMode,
        safelyUpdateFocusModeDepth,
        toggleFocusMode,
    } from "../../../logic/circuit/focusMode.svelte";
    import { onDestroy, onMount } from "svelte";
    import { getBlocks } from "../../../logic/circuit/circuit.svelte";

    let { block, action = $bindable() }: { block: Block; action: BlockAction | undefined } =
        $props();
    let hovering: boolean = $state(false);

    let placement: "left" | "right" | "top" = $derived.by(() => {
        const widthConst = 256;
        if (hasEditorRights()) {
            const controlsPos =
                (block.boundingRect === undefined ? 0 : block.boundingRect!().right) + 128 >
                window.innerWidth
                    ? "left"
                    : "right";
            const defaultPos =
                (block.boundingRect === undefined ? widthConst : block.boundingRect!().left) -
                    widthConst <
                0
                    ? "right"
                    : "left";
            return defaultPos === controlsPos ? "top" : defaultPos;
        }
        return (block.boundingRect === undefined ? 0 : block.boundingRect!().right) + widthConst >
            window.innerWidth
            ? "left"
            : "right";
    });

    function transition(element: Element) {
        return {
            duration: 100,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scale(${t});
            `,
        };
    }

    onDestroy(async () => {
        resetFocusMode();

        // Reset block states
        getBlocks().forEach(other => {
            if (
                other.state === BlockStates.FocusMode ||
                other.state === BlockStates.VisibleInFocusMode ||
                other.state === BlockStates.DisabledInFocusMode
            ) {
                other.state = BlockStates.Inactive;
            }
        });
    });

    function clickFocusModeButton() {
        // Reset action
        action = undefined;

        // Toggle focus mode
        toggleFocusMode(block);
    }

    function mouseEnter() {
        if (block.state === BlockStates.FocusMode) {
            action = BlockActions.StopFocusMode;
        } else {
            action = BlockActions.FocusMode;
        }
    }
</script>

<div
    class="focus-mode-buttons"
    transition:transition
    data-placement={placement}
    role="button"
    tabindex="0"
    onmouseenter={() => (hovering = true)}
    onmouseleave={() => (hovering = false)}>
    <Button
        square
        style="height: min-content;"
        aria-label="Focus mode"
        onclick={clickFocusModeButton}
        onmouseenter={mouseEnter}
        onmouseleave={() => (action = undefined)}>
        <span
            class="fa-eye"
            class:fa-regular={getFocusModeBlock() !== block}
            class:fa-solid={getFocusModeBlock() === block}>
        </span>
    </Button>

    {#if isInFocusMode() && hovering && getMaxDepth() > 1}
        <div class="depth-controls" transition:transition>
            Depth
            <div class="depth-buttons">
                <Button
                    primary
                    square
                    aria-label="Decrease depth"
                    onclick={() => safelyUpdateFocusModeDepth(getFocusModeDepth() - 1)}>
                    <span class="fa-solid fa-minus"></span>
                </Button>
                {getFocusModeDepth()}
                <Button
                    primary
                    square
                    aria-label="Increase depth"
                    onclick={() => safelyUpdateFocusModeDepth(getFocusModeDepth() + 1)}>
                    <span class="fa-solid fa-plus"></span>
                </Button>
            </div>
        </div>
    {/if}
</div>

<style>
    .focus-mode-buttons {
        position: absolute;
        top: 50%;
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 0.6em;
        z-index: 1;
    }

    .focus-mode-buttons[data-placement="right"] {
        right: 0.5em;
        padding-left: 1em;
        top: 50%;
        transform-origin: left;
        translate: 100% -50%;
    }

    .focus-mode-buttons[data-placement="left"] {
        left: 0.5em;
        right: initial;
        padding-right: 1em;
        translate: -100% -50%;
        transform-origin: right;
        flex-direction: row-reverse;
    }

    .focus-mode-buttons[data-placement="top"] {
        translate: -50% -100%;
        flex-direction: column-reverse;
        top: 0;
        left: 50%;
        padding: 0.5em 1em;
        position: absolute;
        transform-origin: bottom;
    }

    .depth-controls {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0.5em;

        border: var(--neutral-surface-border);
        border-radius: var(--surface-border-radius);
        background-color: var(--neutral-surface-colour);
        color: var(--on-neutral-surface-colour);
    }

    .depth-buttons {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 0.6em;
    }
</style>
