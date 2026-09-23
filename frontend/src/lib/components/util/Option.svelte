<script lang="ts">
    import { type Snippet } from "svelte";

    let { value, selected, children }: { value: string; selected?: boolean; children: Snippet } =
        $props();
</script>

<option {value} selected={selected ?? false}>
    {@render children()}
</option>
