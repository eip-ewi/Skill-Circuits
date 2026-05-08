<script lang="ts">
    import { type BlockAction, BlockActions } from "../../../data/block_action";
    import type { Block } from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";
    import { cubicInOut } from "svelte/easing";
    import { getFocusModeBlock, setFocusMode } from "../../../logic/circuit/circuit.svelte";
    import { hasEditorRights } from "../../../logic/authorisation.svelte";

    let { block, action = $bindable() }: { block: Block; action: BlockAction | undefined } =
        $props();

    let placement: "left" | "right" | "top" = $derived.by(() => {
        if (hasEditorRights()) {
            const connectionsPos =
                (block.boundingRect === undefined ? 0 : block.boundingRect!().right) + 128 >
                window.innerWidth
                    ? "left"
                    : "right";
            const defaultPos =
                (block.boundingRect === undefined ? 64 : block.boundingRect!().left) - 64 < 0
                    ? "right"
                    : "left";
            return defaultPos === connectionsPos ? "top" : defaultPos;
        }
        return (block.boundingRect === undefined ? 0 : block.boundingRect!().right) + 64 >
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
        if (getFocusModeBlock() !== block) {
            setFocusMode(block);
        } else {
            setFocusMode(null);
        }
    }
</script>

<div class="focus-mode-button" transition:transition data-placement={placement}>
    <Button
        square
        aria-label="Focus mode"
        onclick={toggleFocusMode}
        onmouseenter={() => (action = BlockActions.FocusMode)}
        onmouseleave={() => (action = undefined)}>
        <span
            class="fa-eye"
            class:fa-regular={getFocusModeBlock() !== block}
            class:fa-solid={getFocusModeBlock() === block}>
        </span>
    </Button>
</div>

<style>
    .focus-mode-button {
        position: absolute;
        top: 50%;
    }

    .focus-mode-button[data-placement="right"] {
        right: 0.5em;
        padding: 1.5em 1.5em 1.5em 1em;
        top: 50%;
        transform-origin: left;
        translate: 100% -50%;
    }

    .focus-mode-button[data-placement="left"] {
        left: 0.5em;
        right: initial;
        padding: 1.5em 1em 1.5em 1.5em;
        translate: -100% -50%;
        transform-origin: right;
    }

    .focus-mode-button[data-placement="top"] {
        right: auto;
        left: -0.5em;
        top: -0.7em;
        transform-origin: bottom right;
    }
</style>
