import type {EditionCard} from "./edition";

export interface Editions {
    currentEditions: EditionCard[];
    upcomingEditions: EditionCard[];
    finishedEditions: EditionCard[];
    archivedEditions: EditionCard[];
}