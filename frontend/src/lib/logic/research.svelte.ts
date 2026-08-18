import type { ResearchInfo } from "../dto/research";
import { withCsrf } from "./csrf";
import { getEdition } from "./edition/edition.svelte";

export const researchState: {
    researchInfo: ResearchInfo | undefined;
    consentGiven: boolean | null;
} = $state({
    researchInfo: undefined,
    consentGiven: null,
});

export function getResearchInfo(): ResearchInfo {
    return researchState.researchInfo!;
}

export function getResearchConsent(): boolean | null {
    return researchState.consentGiven;
}

export async function fetchResearchInfo() {
    const response = await fetch(`/api/research?edition=${getEdition().id}`);
    const info: ResearchInfo = await response.json();
    researchState.researchInfo = info;
}

export async function updateResearchConsentText(consentText: string) {
    if (!consentText) {
        return;
    }

    const response = await fetch(
        `/api/research?edition=${getEdition().id}`,
        withCsrf({
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: consentText,
        }),
    );

    if (response.ok) {
        researchState.researchInfo!.active = true;
        researchState.researchInfo!.consentText = consentText;
    }
}

export async function disableResearch() {
    const response = await fetch(
        `/api/research?edition=${getEdition().id}`,
        withCsrf({
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
        }),
    );

    if (response.ok) {
        researchState.researchInfo!.active = false;
        researchState.researchInfo!.consentText = null;
    }
}

export async function fetchResearchConsent() {
    const response = await fetch(`/api/research/consent?edition=${getEdition().id}`);
    const consent: { consentGiven: boolean | null } = await response.json();
    researchState.consentGiven = consent.consentGiven;
}

export async function updateResearchConsent(consent: boolean) {
    const response = await fetch(
        `/api/research/consent?edition=${getEdition().id}&consent=${consent}`,
        withCsrf({
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
        }),
    );

    if (response.ok) {
        researchState.consentGiven = consent;
    }
}
