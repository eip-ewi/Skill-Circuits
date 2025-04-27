<script lang="ts">
    import Circuit from "./lib/circuit/Circuit.svelte";
    import Sidebar from "./lib/editor/Sidebar.svelte";
    import {ModuleLevel} from "./lib/data/level";
    import {authState, fetchAuth} from "./lib/data/auth.svelte";
    import {onMount} from "svelte";

    onMount(async () => {
        await fetchAuth(ModuleLevel, 3);
    });
</script>

<header class="header">
    <button>
        <span class="fa-solid fa-arrow-left"></span>
        <span>Go back</span>
    </button>
    <button>
        <span class="fa-solid fa-house"></span>
        <span>Home</span>
    </button>
    <button>
        <span class="fa-solid fa-user"></span>
        <span>Profile</span>
    </button>
</header>

{#if authState.isFetched}
    <Sidebar />

    <Circuit level={ModuleLevel} />
{/if}

<style>
</style>
