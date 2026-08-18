export interface ResearchInfo {
    active: boolean;
    consentText: string | null;
}

export interface ResearchConsent {
    consentGiven: boolean | null;
    participantId: string | null;
}
