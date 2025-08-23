<script lang="ts">

    import type {Snippet} from "svelte";
    import HeaderComponent from "../components/HeaderComponent.svelte";
    import FooterComponent from "../components/FooterComponent.svelte";
    import SessionComponent from "../components/SessionComponent.svelte";
    import {isAuthenticated} from "../logic/authentication.svelte";

    let { children, fullWidth }: { children: Snippet, fullWidth?: boolean } = $props();

</script>

<div class="wrapper">
    {#if isAuthenticated()}
        <HeaderComponent></HeaderComponent>
    {/if}

    <div class="main-wrapper">
        <main class:full-width={fullWidth}>
            {@render children() }
        </main>
    </div>

    {#if isAuthenticated()}
        <FooterComponent {fullWidth}></FooterComponent>
        <SessionComponent></SessionComponent>
    {/if}
</div>

<style>
    .wrapper {
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    .main-wrapper {
        flex-grow: 1;
    }

    main {
        height: 100%;
        margin-inline: auto;
        max-width: min(100%, 80rem);
        padding: 4em 2em 2em 2em;
    }

    .full-width {
        max-width: initial;
    }
</style>
