<script lang="ts">
import PageLayout from "./PageLayout.svelte";
</script>

<svelte:head>
    <title>Error - Skill Circuits</title>
</svelte:head>

<PageLayout>
    <div class="content">

        <h1>Error</h1>

        <p>Oops something went wrong :(</p>

    </div>
</PageLayout>

<style>
    h1 {
        color: var(--on-background-colour);
        font-size: var(--font-size-700);
        font-weight: 500;
        margin-bottom: 2rem;
        text-align: center;
    }
</style>