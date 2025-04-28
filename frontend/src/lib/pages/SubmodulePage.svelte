<script lang="ts">
    import {loadPage} from "../logic/routing.svelte";

    let { submoduleId }: { submoduleId: number } = $props();

    async function load() {
        const editionResponse = await fetch(`/api/submodules/${submoduleId}`);
        let submodule: { moduleId: number } = await editionResponse.json();
        loadPage(`/modules/${submodule.moduleId}`, true);
    }
</script>

<main>

    {#await load()}
        <div></div>
    {:then _}
        Redirecting...
    {/await}

</main>