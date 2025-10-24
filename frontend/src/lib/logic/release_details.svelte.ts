import type {ReleaseDetails} from "../dto/release_details";
import {getLevel} from "./circuit/level.svelte";
import {withCsrf} from "./csrf";

let releaseDetails: ReleaseDetails[] = $state([]);

export function getReleaseDetails(): ReleaseDetails[] {
    return releaseDetails;
}

export async function fetchReleaseDetails() {
    let response = await fetch("/api/user-version/release-info");
    releaseDetails = await response.json();
}

export async function versionUpdate(){
    await fetch(`/api/user-version`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
    }));
    releaseDetails = []
}

export function showChangelog():boolean{
    return releaseDetails.length !== 0
}