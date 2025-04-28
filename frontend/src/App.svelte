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

    onMount(async () => {
        await checkAuthentication();
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
        <SessionComponent></SessionComponent>
        <HeaderComponent></HeaderComponent>
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
