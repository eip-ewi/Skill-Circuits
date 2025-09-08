<script lang="ts">
    import {onMount, type Snippet} from "svelte";
    import type {ClassValue} from "svelte/elements";

    let { value, selected, children }: { value: string, selected?: boolean, children: Snippet } = $props();
</script>

<option {value} selected={selected ?? false}>
    {@render children()}
</option>