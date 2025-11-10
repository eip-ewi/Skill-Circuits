<script lang="ts">
    import logoDark from "/img/tudelft_logo_dark.png";
    import logoLight from "/img/tudelft_logo_light.png";
    import Link from "./util/Link.svelte";
    import {loadPage} from "../logic/routing.svelte";
    import {getTheme} from "../logic/preferences.svelte";

    let { fullWidth }: { fullWidth?: boolean | undefined } = $props();
</script>

<div class="footer-wrapper" class:full-width={fullWidth}>
    <footer>
        <div>
            {#if getTheme().colourScheme === "light"}
                <img width="64" alt="TU Delft" src={logoDark}/>
            {:else}
                <img width="64" alt="TU Delft" src={logoLight}/>
            {/if}
        </div>
        <div class="links">
            <Link href="https://gitlab.ewi.tudelft.nl/skill-circuits/skill-circuits/-/releases" target="_blank">Releases</Link>
            <Link onclick={ () => loadPage("/privacy") }>Privacy statement</Link>
        </div>
    </footer>
</div>

<style>
    .footer-wrapper {
        background-color: var(--footer-colour);
        border-top: var(--footer-border-top);
        color: var(--on-footer-colour);
    }

    footer {
        align-items: center;
        display: flex;
        justify-content: space-between;
        max-width: min(100%, 80rem);
        margin-inline: auto;
        padding: 1em 2em;
    }

    .full-width {
        max-width: initial;
    }

    .links {
        display: flex;
        gap: 2em;
    }
</style>