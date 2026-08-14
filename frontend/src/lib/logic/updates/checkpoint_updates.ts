import type { Checkpoint } from "../../dto/checkpoint";
import { withCsrf } from "../csrf";
import moment from "moment";
import { getCheckpoints, getEdition, getSortedCheckpoints } from "../edition/edition.svelte";

export async function createCheckpoint(): Promise<Checkpoint | undefined> {
    const deadline =
        getCheckpoints().length === 0
            ? moment()
            : moment(getSortedCheckpoints()[0]!.deadline).subtract(1, "second");

    const response = await fetch(
        `/api/checkpoints`,
        withCsrf({
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                name: "New checkpoint",
                deadline: deadline.format("YYYY-MM-DDTHH:mm"),
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
    newDeadline: moment.Moment,
): Promise<string> {
    const response = await fetch(
        `/api/checkpoints/${checkpoint.id}`,
        withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                deadline: newDeadline.format("YYYY-MM-DDTHH:mm"),
            }),
        }),
    );

    if (response.ok) {
        return newDeadline.format("YYYY-MM-DDTHH:mm");
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
