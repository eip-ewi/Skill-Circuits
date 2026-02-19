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
    import FooterComponent from "./lib/components/FooterComponent.svelte";
    import {fetchReleaseDetails} from "./lib/logic/release_details.svelte";
    import {
        fetchPreferences,
        getTheme,
    } from "./lib/logic/preferences.svelte";
    import {systemTheme} from "./lib/data/theme";
    import {addSystemColorSchemeEventListener, setThemeProperties} from "./lib/logic/theme.svelte";

    $effect(() => {
        if (isAuthenticated()) {
            fetchAuthorisation().then(() => {});
            fetchReleaseDetails().then(() => {});
            fetchPreferences().then(() => {});
        } else {
            // System theme is default
            setThemeProperties(systemTheme);
        }
    })

    $effect(() => {
        // If the theme changes, the theme properties need to be updated
        setThemeProperties(getTheme());
    })

    onMount(() => {
        // Since the color scheme is retrieved via a media query, adding an event
        // listener is necessary
        addSystemColorSchemeEventListener();
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
