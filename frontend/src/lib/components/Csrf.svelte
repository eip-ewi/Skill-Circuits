<script lang="ts">
    import {onMount} from "svelte";

    let csrfToken: string = $state("");

    onMount(async () => {
        csrfToken = document.querySelector("meta[name='csrf-token']")!.getAttribute("value")!;
    });
</script>

<input type="hidden" name="_csrf" value="{csrfToken}">