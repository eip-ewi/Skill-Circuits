<script lang="ts">
    import { hasEditorRights } from "../../../logic/authorisation.svelte";
    import {
        disableResearch,
        getResearchConsent,
        getResearchInfo,
        updateResearchConsent,
        updateResearchConsentText,
    } from "../../../logic/research.svelte";
    import Button from "../../util/Button.svelte";
    import { parseMarkdown } from "../../../logic/markdown";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

    let { open = $bindable() }: { open: boolean } = $props();

    let consentText = $state(getResearchInfo().consentText ?? "");
    let consentMarkdown = $derived(parseMarkdown(consentText));

    async function updateConsentText() {
        await updateResearchConsentText(consentText);
    }
</script>

{#if hasEditorRights() || getResearchInfo().active}
    <div class="scrollable glass panel" aria-expanded={open}>
        <div class="heading">
            <h2>Research</h2>
            <div class="controls">
                <button class="button" aria-label="Close panel" onclick={() => (open = false)}>
                    <span class="fa-solid fa-arrow-right"></span>
                </button>
            </div>
        </div>

        <div class="content">
            {#if hasEditorRights()}
                <div class="section">
                    <h3>Consent information</h3>
                    {#if getResearchInfo().active}
                        <p>
                            You can update the consent information students get to see here. Note
                            that updating this requires everyone who already gave consent to give
                            consent to the new text.
                        </p>
                    {:else}
                        <p>
                            You can enable collecting data for your research in this course edition
                            by providing consent information for students. Students will see the
                            consent form upon first entering the course edition with the option to
                            consent or not consent. Students can give or take away their consent at
                            any point.
                        </p>
                    {/if}
                    <textarea
                        rows="10"
                        placeholder="This text will be displayed to students. Markdown is supported."
                        bind:value={consentText}>
                    </textarea>
                    <details>
                        <summary>View preview</summary>
                        <div class="markdown">
                            <!-- eslint-disable-next-line svelte/no-at-html-tags -->
                            {@html consentMarkdown}
                        </div>
                    </details>
                    <div>
                        {#if getResearchInfo().active}
                            <WithConfirmationDialog
                                onconfirm={() => updateConsentText()}
                                icon="fa-solid fa-pencil"
                                action="Update">
                                {#snippet button(showDialog: () => void)}
                                    <Button primary onclick={showDialog}>
                                        <span class="fa-solid fa-pencil"></span>
                                        <span>Update</span>
                                    </Button>
                                {/snippet}
                                <p>
                                    Are you sure you want to change the consent information? Anyone
                                    who already gave consent will need to give their consent again.
                                </p>
                            </WithConfirmationDialog>
                        {:else}
                            <Button primary onclick={() => updateConsentText()}>
                                <span class="fa-solid fa-flask"></span>
                                <span>Enable consent collection</span>
                            </Button>
                        {/if}
                    </div>
                </div>

                {#if getResearchInfo().active}
                    <div class="section">
                        <h3>Disable research</h3>
                        <p>
                            By disabling research you stop collecting new consent data and delete
                            all existing consent data.
                        </p>
                        <div>
                            <WithConfirmationDialog
                                onconfirm={() => disableResearch()}
                                icon="fa-solid fa-xmark"
                                action="Disable">
                                {#snippet button(showDialog: () => void)}
                                    <Button type="caution" onclick={showDialog}>
                                        <span class="fa-solid fa-xmark"></span>
                                        <span>Disable research</span>
                                    </Button>
                                {/snippet}
                                <p>
                                    Are you sure you want to disable consent collection. All
                                    existing consent data will be deleted.
                                </p>
                            </WithConfirmationDialog>
                        </div>
                    </div>
                {/if}
            {:else}
                <div class="section">
                    {#if getResearchConsent() === true}
                        <p>You have given consent to the following consent form.</p>
                        <div class="markdown">
                            <!-- eslint-disable-next-line svelte/no-at-html-tags -->
                            {@html parseMarkdown(getResearchInfo().consentText ?? "")}
                        </div>
                        <p>If you wish to retract your consent, click the button below.</p>
                        <div>
                            <Button type="caution" onclick={() => updateResearchConsent(false)}>
                                <span class="fa-solid fa-xmark"></span>
                                <span>Retract consent</span>
                            </Button>
                        </div>
                    {:else}
                        <p>
                            You have <em class="bold">not</em>
                            given consent to the following consent form.
                        </p>
                        <div class="markdown">
                            <!-- eslint-disable-next-line svelte/no-at-html-tags -->
                            {@html parseMarkdown(getResearchInfo().consentText ?? "")}
                        </div>
                        <p>If you still wish to participate, you can click the button below.</p>
                        <div>
                            <Button onclick={() => updateResearchConsent(true)}>
                                <span class="fa-solid fa-check"></span>
                                <span>Give consent</span>
                            </Button>
                        </div>
                    {/if}
                </div>
            {/if}
        </div>
    </div>
{/if}

<style>
    .panel {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        inset-block: 2rem;
        max-width: 32rem;
        overflow-y: auto;
        overscroll-behavior: contain;
        position: fixed;
        right: 0;
        top: 2rem;
        transform-origin: right;
        transition: transform ease-in-out 150ms;
        z-index: 91;
    }

    .panel[aria-expanded="false"] {
        transform: scaleX(0);
    }
    .panel[aria-expanded="true"] {
        transition-delay: 150ms;
    }

    .heading {
        align-items: center;
        display: flex;
        justify-content: space-between;
        gap: 2rem;
        padding: 2rem 2rem 1rem 2rem;
    }

    .heading h2 {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    .controls {
        display: flex;
        gap: 0.25rem;
    }

    h3 {
        font-size: var(--font-size-400);
        font-weight: 500;
    }

    .section {
        display: grid;
        gap: 0.5em;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: 8px;
        cursor: pointer;
        display: grid;
        justify-items: center;
        padding: 0.5rem;
        text-decoration: none;
    }
    .button:focus-visible,
    .button:hover {
        background: var(--on-glass-surface-active-colour);
    }

    .content {
        display: grid;
        gap: 2rem;
        padding: 0 2rem 2rem 2rem;
    }

    textarea {
        border: none;
        border-radius: 8px;
        padding: 0.5rem 1rem;
        resize: none;
    }

    .bold {
        font-weight: 500;
    }
</style>
