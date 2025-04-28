<script lang="ts">
    import {loadPage} from "../logic/routing.svelte";

    let { skillId }: { skillId: number } = $props();

    async function load() {
        const response = await fetch(`/api/skills/${skillId}/module`);
        let moduleId: number = parseInt(await response.text());
        loadPage(`/modules/${moduleId}#block-${skillId}`, true);
    }
</script>

<main>

    {#await load()}
        <div></div>
    {:then _}
        Redirecting...
    {/await}

</main>