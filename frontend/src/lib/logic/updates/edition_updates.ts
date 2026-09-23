import { withCsrf } from "../csrf";
import { getEdition } from "../edition/edition.svelte";
import { loadPage } from "../routing.svelte";

export async function setEditionVisibility(newVisibility: boolean) {
    if (getEdition().published === newVisibility) {
        return;
    }

    getEdition().published = newVisibility;

    const response = await fetch(
        `/api/editions/${getEdition().id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                isVisible: newVisibility,
            }),
        }),
    );

    if (!response.ok) {
        getEdition().published = !newVisibility;
    }
}

export async function resetProgress() {
    const response = await fetch(
        `/api/editions/${getEdition().id}/reset-progress`,
        withCsrf({
            method: "POST",
        }),
    );

    if (response.ok) {
        window.location.reload();
    }
}

export async function copyEdition(toEdition: number) {
    const response = await fetch(
        `/api/editions/${getEdition().id}/copy-to/${toEdition}`,
        withCsrf({
            method: "POST",
        }),
    );

    if (response.ok) {
        loadPage(`/editions/${toEdition}`);
    }
}
