<script lang="ts">
    import {onMount} from "svelte";
    import {checkAuthentication, getAuthenticatedPerson, isAuthenticated} from "./lib/logic/authentication.svelte";
    import LoginPage from "./lib/pages/LoginPage.svelte";
    import Csrf from "./lib/components/Csrf.svelte";
    import HomePage from "./lib/pages/HomePage.svelte";
    import Router from "./lib/Router.svelte";
    import SessionComponent from "./lib/components/SessionComponent.svelte";
    import HeaderComponent from "./lib/components/HeaderComponent.svelte";
    import {fetchAuthorisation} from "./lib/logic/authorisation.svelte";
    import {getTheme} from "./lib/logic/theme.svelte";
    import FooterComponent from "./lib/components/FooterComponent.svelte";

    $effect(() => {
        const root = document.documentElement;
        const theme = getTheme();
        root.setAttribute("data-theme", theme.name);
        root.setAttribute("data-colour-scheme", theme.colourScheme);
    });

    $effect(() => {
        if (isAuthenticated()) {
            fetchAuthorisation().then(() => {});
        }
    })
</script>

{#await checkAuthentication()}
    <div></div>
{:then _}
    {#if isAuthenticated()}
        <Router></Router>
    {:else}
        {#if window.location.pathname.startsWith("/login")}
            <LoginPage></LoginPage>
        {:else}
            <HomePage></HomePage>
        {/if}
    {/if}
{/await}

<style>
</style>
