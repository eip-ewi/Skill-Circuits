<script lang="ts">
    import PageLayout from "./PageLayout.svelte";
    import ThemeSelectComponent from "../components/ThemeSelectComponent.svelte";
    import {
        getDeadlineDisplay,
        setDeadlineDisplay,
        getAdditionalIcons,
        getBlurBlocks,
        getTheme,
        setAdditionalIcons,
        setBlurBlocks,
    } from "../logic/preferences.svelte";
    import ExampleBlurredSkillComponent from "../components/settings_examples/ExampleBlurredSkillComponent.svelte";
    import { clearLevel } from "../logic/circuit/level.svelte";
    import ExampleCompletionIcons from "../components/settings_examples/ExampleCompletionIcons.svelte";
    import { deadlineDisplayOptions, type DeadlineDisplay } from "../data/deadline_display";

    clearLevel();

    async function updateBlurBlocks(event: Event) {
        await setBlurBlocks((event.target as HTMLInputElement).checked);
    }

    async function updateDeadlineDisplay(event: Event) {
        await setDeadlineDisplay((event.target as HTMLInputElement).value as DeadlineDisplay);
    }

    async function updateAdditionalIcons(event: Event) {
        await setAdditionalIcons((event.target as HTMLInputElement).checked);
    }
</script>

<PageLayout>
    <div class="content">
        <h1>Settings</h1>

        <h2>Theme</h2>
        <p>Configure your theme for Skill Circuits.</p>
        <p class="theme-selection">
            <b>Selected theme:</b>
            {getTheme().displayName}
        </p>
        <ThemeSelectComponent></ThemeSelectComponent>

        <h2>Deadline timezone</h2>
        <p>Choose which time should be shown by default for checkpoints and task deadlines.</p>
        <fieldset class="deadline-display">
            <legend>Displayed deadline time</legend>
            {#each deadlineDisplayOptions as option (option.value)}
                <label>
                    <input
                        type="radio"
                        name="deadline-display"
                        value={option.value}
                        checked={getDeadlineDisplay() === option.value}
                        onchange={updateDeadlineDisplay} />
                    <span>
                        <b>{option.label}</b>
                        <small>{option.description}</small>
                    </span>
                </label>
            {/each}
        </fieldset>

        <h2>Additional icons</h2>
        <p>Configure whether icons indicating the completion of content should be displayed.</p>
        <label for="completion-icons">
            <input
                id="completion-icons"
                type="checkbox"
                checked={getAdditionalIcons()}
                onchange={e => updateAdditionalIcons(e)} />

            Enable completion icons (see example below)
        </label>

        <ExampleCompletionIcons></ExampleCompletionIcons>

        <h2>Unreached content</h2>
        <p>
            Configure whether blocks (e.g., skills and submodules) you have not yet reached should
            be blurred, unless hovered over.
        </p>
        <label for="block-blurring">
            <input
                id="block-blurring"
                type="checkbox"
                checked={getBlurBlocks()}
                onchange={e => updateBlurBlocks(e)} />

            Enable block blurring (see example below)
        </label>

        <ExampleBlurredSkillComponent></ExampleBlurredSkillComponent>
    </div>
</PageLayout>

<style>
    h1 {
        font-size: var(--font-size-700);
        font-weight: 700;
    }
    h2 {
        font-size: var(--font-size-500);
        font-weight: 500;
        margin-bottom: 0.2em;
        margin-top: 0.8em;
    }
    p {
        margin-bottom: 0.5em;
    }
    p > b {
        font-weight: 700;
    }
    div {
        margin-bottom: 0.5em;
    }
    .content {
        margin-bottom: 7em;
    }
    .deadline-display {
        border: 0;
        display: grid;
        gap: 0.5em;
        margin: 0;
        padding: 0;
    }
    .deadline-display legend {
        height: 0;
        overflow: hidden;
        position: absolute;
        width: 0;
    }
    .deadline-display label {
        align-items: flex-start;
        cursor: pointer;
        display: flex;
        gap: 0.5em;
    }
    .deadline-display span {
        display: grid;
    }
    .deadline-display b {
        font-weight: 500;
    }
    .deadline-display small {
        opacity: 0.75;
    }
</style>
