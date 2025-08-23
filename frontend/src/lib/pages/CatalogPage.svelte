<script lang="ts">
    import type {Editions} from "../dto/editions";
    import EditionCardComponent from "../components/edition/EditionCardComponent.svelte";
    import HeaderComponent from "../components/HeaderComponent.svelte";
    import {clearLevel} from "../logic/circuit/level.svelte";
    import {getAuthorisation} from "../logic/authorisation.svelte";
    import PageTabs from "../components/util/PageTabs.svelte";
    import Tab from "../components/util/Tab.svelte";
    import type {EditionCard} from "../dto/edition";
    import Button from "../components/util/Button.svelte";
    import {loadPage} from "../logic/routing.svelte";
    import {withCsrf} from "../logic/csrf";
    import PageLayout from "./PageLayout.svelte";

    let editions: EditionCard[] = $state([]);

    clearLevel();

    async function load() {
        const response = await fetch("/api/editions/open");
        editions = await response.json();
    }

    async function joinEdition(edition: EditionCard) {
        await fetch(`/api/editions/${edition.id}/join`, withCsrf({
            method: "POST",
        }));
        loadPage("/");
    }
</script>

<svelte:head>
    <title>Course Catalog - Skill Circuits</title>
</svelte:head>

<PageLayout>
    <div class="content">

        <PageTabs>
            <Tab page="/">My courses</Tab>
            <Tab page="/editions">Course catalog</Tab>
        </PageTabs>

        <h1>Course Catalog</h1>

        {#await load() then _}

            {#if editions.length === 0}
                <p>There are currently no courses for you.</p>
            {:else}
                <div class="editions">
                    {#each editions as edition}
                        <div class="edition">
                            <h2>
                                <span>{edition.course.name}</span>
                                <span>{edition.name}</span>
                            </h2>
                            <Button primary onclick={ () => joinEdition(edition) }>
                                Join
                            </Button>
                        </div>
                    {/each}
                </div>
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

    .editions {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(23rem, 1fr));
        gap: 1rem;
    }

    .edition {
        background-color: var(--block-colour);
        border: var(--block-border);
        border-radius: var(--block-border-radius);
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        display: grid;
        justify-items: start;
        gap: 1em;
        padding: 1em;
        text-decoration: none;
        transition: transform 150ms ease-in-out;
    }

    h2 {
        display: grid;
        justify-items: start;
    }

    h2 span:first-child {
        font-size: var(--font-size-500);
        font-weight: 700;
    }
</style>
