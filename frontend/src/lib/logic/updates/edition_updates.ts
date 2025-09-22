import type {Checkpoint} from "../../dto/checkpoint";
import {withCsrf} from "../csrf";
import moment from "moment";
import {getCheckpoints, getEdition, getModule, getModules, getSortedCheckpoints} from "../edition/edition.svelte";
import type {Module} from "../../dto/module";
import {getCircuit} from "../circuit/circuit.svelte";
import {isLevel} from "../circuit/level.svelte";
import {EditionLevel} from "../../data/level";
import {loadPage} from "../routing.svelte";

export async function setEditionVisibility(newVisibility: boolean) {
    if (getEdition().published === newVisibility) {
        return;
    }

    getEdition().published = newVisibility;

    let response = await fetch(`/api/editions/${getEdition().id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            isVisible: newVisibility,
        }),
    }));

    if (!response.ok) {
        getEdition().published = !newVisibility;
    }
}

export async function downloadTeacherStats() {
    const editionId = getEdition().id

    const urls = [
        `/api/editions/${editionId}/task_stats`,
        `/api/editions/${editionId}/student_stats`
    ];

    urls.forEach(url => {
        const a = document.createElement("a");
        a.href = url;
        a.download = "";
        document.body.appendChild(a);
        a.click();
        a.remove();
    });
}

export async function resetProgress() {
    let response = await fetch(`/api/editions/${getEdition().id}/reset-progress`, withCsrf({
        method: "POST",
    }));

    if (response.ok) {
        window.location.reload();
    }
}

export async function copyEdition(toEdition: number) {
    let response = await fetch(`/api/editions/${getEdition().id}/copy-to/${toEdition}`, withCsrf({
        method: "POST",
    }));

    if (response.ok) {
        loadPage(`/editions/${toEdition}`);
    }
}

