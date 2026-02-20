<script lang="ts">
    import {extractPathVariable, getPage, pageMatches, reloadPageFromUrl} from "./logic/routing.svelte";
    import LandingPage from "./pages/LandingPage.svelte";
    import EditionPage from "./pages/EditionPage.svelte";
    import SubmodulePage from "./pages/SubmodulePage.svelte";
    import ModulePage from "./pages/ModulePage.svelte";
    import {getAuthorisation} from "./logic/authorisation.svelte";
    import {onMount} from "svelte";
    import LoginPage from "./pages/LoginPage.svelte";
    import SkillPage from "./pages/SkillPage.svelte";
    import ErrorPage from "./pages/ErrorPage.svelte";
    import CatalogPage from "./pages/CatalogPage.svelte";
    import PrivacyPage from "./pages/PrivacyPage.svelte";
    import SettingsPage from "./pages/SettingsPage.svelte";
</script>

<svelte:window onpopstate={reloadPageFromUrl}></svelte:window>

{#if getPage() === "/"}
    <LandingPage></LandingPage>

{:else if pageMatches(/^\/editions$/)}
    <CatalogPage></CatalogPage>

{:else if pageMatches(/\/editions\/(\d+)/)}
    <EditionPage editionId={parseInt(extractPathVariable(/\/editions\/(\d+)/))}></EditionPage>

{:else if pageMatches(/\/modules\/(\d+)/)}
    <ModulePage moduleId={parseInt(extractPathVariable(/\/modules\/(\d+)/))}></ModulePage>

{:else if pageMatches(/\/submodules\/(\d+)/)}
    <SubmodulePage submoduleId={parseInt(extractPathVariable(/\/submodules\/(\d+)/))}></SubmodulePage>

{:else if pageMatches(/\/skills\/(\d+)/)}
    <SkillPage skillId={parseInt(extractPathVariable(/\/skills\/(\d+)/))}></SkillPage>

{:else if pageMatches(/^\/settings/)}
    <SettingsPage></SettingsPage>

{:else if pageMatches(/^\/privacy/)}
    <PrivacyPage></PrivacyPage>

{:else if pageMatches(/\/error/)}
    <ErrorPage></ErrorPage>

{:else}
    404

{/if}
