<script lang="ts">
    import { type BlockAction, BlockActions } from "../../../data/block_action";
    import type { Block } from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";

    let { block, action = $bindable() }: { block: Block; action: BlockAction | undefined } =
        $props();
</script>

<div class="focus-mode-button">
    <Button
        square
        aria-label="Bookmark"
        onmouseenter={() => (action = BlockActions.FocusMode)}
        onmouseleave={() => (action = undefined)}>
        <span class="fa-solid fa-eye"></span>
    </Button>
</div>

<style>
    .focus-mode-button {
        position: absolute;
        right: 2em;
        top: -0.5em;
        transform-origin: bottom left;
    }
</style>
