<script lang="ts">
    import { type BlockAction, BlockActions } from "../../../data/block_action";
    import type { Block } from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";
    import { cubicInOut } from "svelte/easing";
    import {
        getFocusModeBlock,
        resetFocusMode,
        toggleFocusMode
    } from "../../../logic/circuit/focusMode.svelte";
    import {
        FocusModeBlockStates,
    } from "../../../data/focus_mode_block_state";
    import {onDestroy} from "svelte";
    import {getBlocks} from "../../../logic/circuit/circuit.svelte";

    let { block, action = $bindable() }: { block: Block; action: BlockAction | undefined } =
        $props();

    let placement: "left" | "right" = $derived.by(() => {
        return (block.boundingRect === undefined ? 0 : block.boundingRect!().right) + 64 >
            window.innerWidth
            ? "left"
            : "right";
    });

    onDestroy(async () => {
        // This means that the page changed or the user switched to editor mode
        if (getFocusModeBlock() === block) {
            resetFocusMode();

            // Reset focus mode block states
            getBlocks().forEach(other => {
                other.focusModeState = FocusModeBlockStates.NotInFocusMode;
            });
        }
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

    function clickFocusModeButton() {
        // Toggle focus mode
        toggleFocusMode(block);

        // Set action
        if (getFocusModeBlock() === block) {
            action = BlockActions.StopFocusMode;
        } else {
            action = BlockActions.FocusMode;
        }
    }

    function mouseEnter() {
        if (block.focusModeState === FocusModeBlockStates.FocusOnBlock) {
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
    tabindex="0">
    <Button
        square
        style="height: min-content;"
        aria-label="Focus mode"
        onclick={clickFocusModeButton}
        onmouseenter={mouseEnter}
        onmouseleave={() => (action = undefined)}>
        <span
            class="fa-solid"
            class:fa-eye={getFocusModeBlock() !== block}
            class:fa-eye-slash={getFocusModeBlock() === block}>
        </span>
    </Button>
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
</style>
