<script lang="ts">
    import { type BlockAction, BlockActions } from "../../../data/block_action";
    import type { Block } from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";
    import { cubicInOut } from "svelte/easing";
    import { getBlocks } from "../../../logic/circuit/circuit.svelte";
    import { fetchAuthorisation, hasEditorRights } from "../../../logic/authorisation.svelte";
    import { BlockStates } from "../../../data/block_state";
    import {
        getFocusModeBlock,
        getFocusModeDepth,
        isInFocusMode,
        setFocusMode,
        setFocusModeDepth,
        visibleInFocusMode,
    } from "../../../logic/circuit/focusMode.svelte";

    let { block, action = $bindable() }: { block: Block; action: BlockAction | undefined } =
        $props();
    let hovering: boolean = $state(false);
    let maxDepth: number = $derived.by(() => {
        const maxRowInCircuit = Math.max(0, ...getBlocks().map(block => block.row ?? 0));
        // Calculate max depth in up and down directions
        return Math.max(maxRowInCircuit - (block.row ?? 0), block.row ?? 0) + 1;
    });

    let placement: "left" | "right" | "top" = $derived.by(() => {
        const widthConst = 256;
        if (hasEditorRights()) {
            const connectionsPos =
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
            return defaultPos === connectionsPos ? "top" : defaultPos;
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

    function toggleFocusMode() {
        // Reset action
        action = undefined;

        // Reset depth
        setFocusModeDepth(2);

        if (getFocusModeBlock() !== block) {
            // Set this block to focus mode
            setFocusMode(block);
        } else {
            // Stop focus mode
            setFocusMode(null);

            // Reset state of all blocks
            getBlocks().forEach(other => {
                other.state = BlockStates.Inactive;
            });
        }
    }

    function mouseEnter() {
        if (block.state === BlockStates.FocusMode) {
            action = BlockActions.StopFocusMode;
        } else {
            action = BlockActions.FocusMode;
        }
    }

    function safelyUpdateFocusModeDepth(depth: number) {
        if (depth <= 0 || depth >= maxDepth) return;
        setFocusModeDepth(depth);
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
        onclick={toggleFocusMode}
        onmouseenter={mouseEnter}
        onmouseleave={() => (action = undefined)}>
        <span
            class="fa-eye"
            class:fa-regular={getFocusModeBlock() !== block}
            class:fa-solid={getFocusModeBlock() === block}>
        </span>
    </Button>

    {#if isInFocusMode() && hovering}
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
        right: 50%;
        transform: translate(50%, -100%);
        top: -0.5em;
        flex-direction: column-reverse;
    }

    .depth-controls {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0.5em;

        border-radius: var(--surface-border-radius);
        background-color: var(--block-colour);
        border: var(--block-border);
        color: var(--on-block-colour);
    }

    .depth-buttons {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 0.6em;
    }
</style>
