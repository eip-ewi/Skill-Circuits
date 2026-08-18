<script lang="ts">
    import { hasEditorRights } from "../../logic/authorisation.svelte";
    import { parseMarkdown } from "../../logic/markdown";
    import {
        getResearchConsent,
        getResearchInfo,
        updateResearchConsent,
    } from "../../logic/research.svelte";
    import Button from "../util/Button.svelte";

    function autoShow(node: HTMLDialogElement) {
        node.showModal();
    }

    function chooseConsentOption(consent: boolean) {
        updateResearchConsent(consent);
    }
</script>

{#if getResearchConsent() == null && getResearchInfo().active && !hasEditorRights()}
    <dialog use:autoShow class="dialog">
        <div class="content">
            <h2 class="title">Research</h2>
            <div class="markdown">
                <!-- eslint-disable-next-line svelte/no-at-html-tags -->
                {@html parseMarkdown(getResearchInfo().consentText ?? "")}
            </div>
            <p>
                You can change your preference at any time by clicking the flask (
                <span class="fa-solid fa-flask"></span>
                ) on the right side of this course's Skill Circuit.
            </p>
            <div class="buttons">
                <Button primary onclick={() => chooseConsentOption(false)}>I do not consent</Button>
                <Button primary onclick={() => chooseConsentOption(true)}>I consent</Button>
            </div>
        </div>
    </dialog>
{/if}

<style>
    .dialog {
        --blur: 0.5rem;

        border: none;
        border-radius: 16px;
        left: 50%;
        outline: none;
        position: fixed;
        top: 50%;
        transform: translate(-50%, -50%);
        transform-origin: top left;
    }

    .dialog::backdrop {
        background: none;
        backdrop-filter: blur(var(--blur));
    }

    .content {
        background: var(--block-colour);
        box-shadow: 2rem 2rem 4rem color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        display: grid;
        gap: 1rem;
        max-height: calc(100vh - 12rem);
        overflow-y: scroll;
        padding: 2rem;
    }

    .title {
        font-size: var(--font-size-700);
        font-weight: 700;
        text-align: center;
    }

    .buttons {
        display: flex;
        gap: 1rem;
        justify-content: space-between;
    }
</style>
