import { withCsrf } from "../csrf";
import { getEdition } from "../edition/edition.svelte";
import type { Person } from "../../dto/person";

export async function addEditor(editor: Person) {
    getEdition().editors.push(editor);

    const response = await fetch(
        `/api/editions/${getEdition().id}/editors/${editor.id}`,
        withCsrf({
            method: "POST",
        }),
    );

    if (!response.ok) {
        getEdition().editors.pop();
    }
}

export async function removeEditor(editor: Person) {
    getEdition().editors.splice(
        getEdition().editors.findIndex(e => e.id === editor.id),
        1,
    );

    const response = await fetch(
        `/api/editions/${getEdition().id}/editors/${editor.id}`,
        withCsrf({
            method: "DELETE",
        }),
    );

    if (!response.ok) {
        getEdition().editors.push(editor);
    }
}
