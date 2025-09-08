import type {Checkpoint} from "../../dto/checkpoint";
import {withCsrf} from "../csrf";
import moment from "moment";
import {getCheckpoints, getEdition, getSortedCheckpoints} from "../edition/edition.svelte";
import type {Person} from "../../dto/person";

export async function addEditor(editor: Person) {
    getEdition().editors.push(editor);

    let response = await fetch(`/api/editions/${getEdition().id}/editors/${editor.id}`, withCsrf({
        method: "POST",
    }));

    if (!response.ok) {
        getEdition().editors.pop()
    }
}


export async function removeEditor(editor: Person) {
    getEdition().editors.splice(getEdition().editors.findIndex(e => e.id === editor.id), 1);

    let response = await fetch(`/api/editions/${getEdition().id}/editors/${editor.id}`, withCsrf({
        method: "DELETE",
    }));

    if (!response.ok) {
        getEdition().editors.push(editor)
    }
}

