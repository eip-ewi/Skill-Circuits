<script lang="ts">

import PageLayout from "./PageLayout.svelte";
import ThemeSelectComponent from "../components/ThemeSelectComponent.svelte";
</script>

<PageLayout>
    <div class="content">
        <h1>Settings</h1>

        <h2>Theme</h2>
        <p>Configure your theme for Skill Circuits.</p>
        <ThemeSelectComponent></ThemeSelectComponent>

        <h2>Skill Blurring</h2>
        <p>Configure whether or not skills you have not yet reached should be blurred.</p>
    </div>
</PageLayout>

<style>
    h1 {
        font-size: var(--font-size-700);
        font-weight: 700;
        margin-bottom: .2em;
    }
    h2 {
        font-size: var(--font-size-500);
        font-weight: 500;
        margin-bottom: .2em;
    }
    .content {
        margin-bottom: 7em;
    }
</style>