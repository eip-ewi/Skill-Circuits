import type {EditionCard} from "./edition";
import type {Role} from "./role";

export interface Editions {
    currentEditions: { role: Role, edition: EditionCard }[];
    upcomingEditions: { role: Role, edition: EditionCard }[];
    finishedEditions: { role: Role, edition: EditionCard }[];
    archivedEditions: { role: Role, edition: EditionCard }[];
}