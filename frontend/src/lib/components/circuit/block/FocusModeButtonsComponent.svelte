<script lang="ts">
    import { type BlockAction, BlockActions } from "../../../data/block_action";
    import type { Block } from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";
    import { cubicInOut } from "svelte/easing";
    import { hasEditorRights } from "../../../logic/authorisation.svelte";
    import { getFocusModeBlock, toggleFocusMode } from "../../../logic/circuit/focusMode.svelte";
    import {
        FocusModeBlockStates,
        isVisibleAndInFocusMode,
    } from "../../../data/focus_mode_block_state";

    let { block, action = $bindable() }: { block: Block; action: BlockAction | undefined } =
        $props();

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
