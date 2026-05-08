<script lang="ts">
    import { type BlockAction, BlockActions } from "../../../data/block_action";
    import type { Block } from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";
    import { cubicInOut } from "svelte/easing";
    import { getFocusModeBlock, setFocusMode } from "../../../logic/circuit/circuit.svelte";

    let { block, action = $bindable() }: { block: Block; action: BlockAction | undefined } =
        $props();

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

<div class="focus-mode-button" transition:transition>
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
        right: 1.5em;
        top: -0.8em;
        transform-origin: bottom left;
    }
</style>
