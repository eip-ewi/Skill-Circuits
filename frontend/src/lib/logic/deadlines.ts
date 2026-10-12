import { TZDate, tzOffset } from "@date-fns/tz";
import {
    differenceInMilliseconds,
    format,
    isAfter,
    isValid,
    parse,
    parseISO,
    subSeconds,
} from "date-fns";
import { enGB } from "date-fns/locale";
import { getDeadlineDisplay } from "./preferences.svelte";

export const UNIVERSITY_TIME_ZONE_LABEL = "Delft";

export type DeadlineFormat = "long" | "readable";

const UNIVERSITY_TIME_ZONE = "Europe/Amsterdam";
const INPUT_FORMAT = "yyyy-MM-dd'T'HH:mm";
const DEADLINE_FORMATS: Record<DeadlineFormat, string> = {
    long: "d MMMM yyyy HH:mm",
    readable: "d MMM yyyy HH:mm",
};

function instant(value: string): Date {
    const date = parseISO(value.replace(/\[[^\]]+]$/, ""));

    if (!isValid(date)) {
        throw new Error(`Invalid instant: ${value}`);
    }

    return date;
}

function formatDeadline(deadline: string, formatType: DeadlineFormat, timeZone?: string): string {
    const date = instant(deadline);
    return format(
        timeZone === undefined ? date : new TZDate(date, timeZone),
        DEADLINE_FORMATS[formatType],
        {
            locale: enGB,
        },
    );
}

export function formatLocalDeadline(deadline: string, format: DeadlineFormat): string {
    return formatDeadline(deadline, format);
}

export function formatUniversityDeadline(deadline: string, format: DeadlineFormat): string {
    return formatDeadline(deadline, format, UNIVERSITY_TIME_ZONE);
}

function formatEarliestDeadline(deadline: string, format: DeadlineFormat): string {
    const date = instant(deadline);
    const localOffset = -date.getTimezoneOffset();
    const universityOffset = tzOffset(UNIVERSITY_TIME_ZONE, date);
    return localOffset <= universityOffset
        ? formatLocalDeadline(deadline, format)
        : formatUniversityDeadline(deadline, format);
}

export function formatPreferredDeadline(deadline: string, format: DeadlineFormat): string {
    switch (getDeadlineDisplay()) {
        case "DELFT":
            return formatUniversityDeadline(deadline, format);
        case "EARLIEST":
            return formatEarliestDeadline(deadline, format);
        case "LOCAL":
            return formatLocalDeadline(deadline, format);
    }
}

export function isPastDeadline(deadline: string): boolean {
    return isAfter(new Date(), instant(deadline));
}

export function compareDeadlines(a: string, b: string): number {
    return differenceInMilliseconds(instant(a), instant(b));
}

export function checkpointDeadlineBefore(deadline: string): string {
    return subSeconds(instant(deadline), 1).toISOString();
}

export function defaultCheckpointDeadline(): string {
    return new Date().toISOString();
}

export function toUniversityInputValue(deadline: string): string {
    return format(new TZDate(instant(deadline), UNIVERSITY_TIME_ZONE), INPUT_FORMAT);
}

export function universityInputValueToZonedDateTime(deadline: string): string {
    const date = parse(deadline, INPUT_FORMAT, TZDate.tz(UNIVERSITY_TIME_ZONE));

    if (!isValid(date)) {
        throw new Error(`Invalid university deadline: ${deadline}`);
    }

    return format(date, "yyyy-MM-dd'T'HH:mm:ssXXX");
}

export function deadlineTimeZoneDiffersFromBrowser(deadline: string): boolean {
    const date = instant(deadline);
    return tzOffset(UNIVERSITY_TIME_ZONE, date) !== -date.getTimezoneOffset();
}
