<script lang="ts">
    import type {Editions} from "../dto/editions";
    import EditionCardComponent from "../components/edition/EditionCardComponent.svelte";
    import HeaderComponent from "../components/HeaderComponent.svelte";
    import {clearLevel} from "../logic/circuit/level.svelte";
    import {getAuthorisation} from "../logic/authorisation.svelte";
    import PageTabs from "../components/util/PageTabs.svelte";
    import Tab from "../components/util/Tab.svelte";
    import PageLayout from "./PageLayout.svelte";
    import {loadPage} from "../logic/routing.svelte";

    let editions: Editions | undefined = $state();

    clearLevel();

    async function load() {
        const response = await fetch("/api/editions");
        editions = await response.json();

        if (editions!.archivedEditions.length + editions!.currentEditions.length + editions!.finishedEditions.length + editions!.upcomingEditions.length === 0) {
            loadPage("/editions");
        }
    }
</script>

<svelte:head>
    <title>My Courses - Skill Circuits</title>
</svelte:head>

<PageLayout>
    <div class="content">

        <PageTabs>
            <Tab page="/">My courses</Tab>
            <Tab page="/editions">Course catalog</Tab>
        </PageTabs>

        <h1>My Courses</h1>

        {#await load() then _}

            {#if editions !== undefined}

                {#if editions.currentEditions.length > 0}
                    <div class="editions">
                        <h2>Current editions</h2>
                        {#each editions.currentEditions as editionView}
                            <EditionCardComponent edition={editionView}></EditionCardComponent>
                        {/each}
                    </div>
                {/if}

                {#if editions.upcomingEditions.length > 0}
                    <div class="editions">
                        <h2>Upcoming editions</h2>
                        {#each editions.currentEditions as editionView}
                            <EditionCardComponent edition={editionView}></EditionCardComponent>
                        {/each}
                    </div>
                {/if}

                {#if editions.finishedEditions.length > 0}
                    <div class="editions">
                        <h2>Finished editions</h2>
                        {#each editions.finishedEditions as editionView}
                            <EditionCardComponent edition={editionView}></EditionCardComponent>
                        {/each}
                    </div>
                {/if}

                {#if editions.archivedEditions.length > 0}
                    <div class="editions">
                        <h2>Archived editions</h2>
                        {#each editions.archivedEditions as editionView}
                            <EditionCardComponent edition={editionView}></EditionCardComponent>
                        {/each}
                    </div>
                {/if}

            {/if}
        {/await}

    </div>

</PageLayout>

<style>
    h1 {
        color: var(--on-background-colour);
        font-size: var(--font-size-700);
        font-weight: 500;
        margin-block: 2rem;
        text-align: center;
    }

    h2 {
        color: var(--on-group-colour);
        grid-column: 1 / -1;
        font-size: var(--font-size-400);
        font-weight: 500;
    }

    .editions {
        background-color: var(--group-colour);
        border: 1px solid var(--group-border-colour);
        border-radius: var(--group-border-radius);
        display: grid;
        gap: 1rem;
        grid-template-columns: repeat(auto-fill, minmax(23rem, 1fr));
        padding: 1rem 2rem 2rem 2rem;
    }

    .editions + .editions {
        margin-top: 2rem;
    }
</style>