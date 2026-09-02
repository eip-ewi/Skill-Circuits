<script lang="ts">
    import { onMount } from "svelte";
    import { getCsrfToken } from "../logic/csrf";

    let csrfToken: string = $state("");

    onMount(async () => {
        csrfToken = getCsrfToken().token;
    });
</script>

<input type="hidden" name="_csrf" value={csrfToken} />
