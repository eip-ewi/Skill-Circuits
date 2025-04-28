import type {Person} from "../dto/person";

let authenticatedPerson: Person | undefined = $state();
let authenticated: boolean = $derived(authenticatedPerson !== undefined);

export function isAuthenticated() {
    return authenticated;
}

export function getAuthenticatedPerson(): Person | undefined {
    return authenticatedPerson;
}

export async function checkAuthentication() {
    const response = await fetch("/api/auth");
    const body = await response.text();
    if (body) {
        authenticatedPerson = JSON.parse(body);
    } else {
        authenticatedPerson = undefined;
    }
}
