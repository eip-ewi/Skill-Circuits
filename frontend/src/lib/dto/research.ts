export interface ResearchInfo {
    active: boolean;
    consentText: string | null;
    numberOfParticipants: number;
}

export interface ResearchConsent {
    consentGiven: boolean | null;
    participantId: string | null;
}
