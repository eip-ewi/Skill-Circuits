<script lang="ts">
    import Circuit from "./lib/circuit/Circuit.svelte";
    import Sidebar from "./lib/editor/Sidebar.svelte";
    import {ModuleLevel} from "./lib/data/level";
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

<Sidebar />

<Circuit level={ModuleLevel}/>

<style>
</style>
