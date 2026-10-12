import type { Checkpoint } from "../../dto/checkpoint";
import { withCsrf } from "../csrf";
import { getCheckpoints, getEdition, getSortedCheckpoints } from "../edition/edition.svelte";
import {
    checkpointDeadlineBefore,
    defaultCheckpointDeadline,
    universityInputValueToZonedDateTime,
} from "../deadlines";

export async function createCheckpoint(): Promise<Checkpoint | undefined> {
    const deadline =
        getCheckpoints().length === 0
            ? defaultCheckpointDeadline()
            : checkpointDeadlineBefore(getSortedCheckpoints()[0]!.deadline);

    const response = await fetch(
        `/api/checkpoints`,
        withCsrf({
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                name: "New checkpoint",
                deadline,
                edition: {
                    id: getEdition().id,
                },
            }),
        }),
    );

    if (response.ok) {
        const checkpoint = await response.json();
        getCheckpoints().push(checkpoint);
        return getCheckpoints().at(-1)!;
    }
    return undefined;
}

export async function editCheckpointName(checkpoint: Checkpoint, newName: string) {
    const oldName = checkpoint.name;
    checkpoint.name = newName;

    const response = await fetch(
        `/api/checkpoints/${checkpoint.id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                name: newName,
            }),
        }),
    );

    if (!response.ok) {
        checkpoint.name = oldName;
    }
}

export async function editCheckpointDeadline(
    checkpoint: Checkpoint,
    newDeadline: string,
): Promise<string> {
    const savedDeadline = universityInputValueToZonedDateTime(newDeadline);

    const response = await fetch(
        `/api/checkpoints/${checkpoint.id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                deadline: savedDeadline,
            }),
        }),
    );

    if (response.ok) {
        return savedDeadline;
    }
    return checkpoint.deadline;
}

export async function deleteCheckpoint(checkpoint: Checkpoint) {
    const response = await fetch(
        `/api/checkpoints/${checkpoint.id}`,
        withCsrf({
            method: "DELETE",
        }),
    );

    if (response.ok) {
        getCheckpoints().splice(getCheckpoints().findIndex(c => c.id === checkpoint.id)!, 1);
    }
}
