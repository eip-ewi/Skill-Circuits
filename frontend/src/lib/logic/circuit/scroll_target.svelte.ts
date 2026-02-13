export type ScrollTarget =
    | { kind: "block"; id: number }
    | { kind: "group"; id: number };

let scrollTarget: ScrollTarget | undefined = $state(undefined);

export function setScrollTarget(target: ScrollTarget | undefined) {
    scrollTarget = target;
}

export function getScrollTarget(): ScrollTarget | undefined {
    return scrollTarget;
}

export function clearScrollTarget() {
    scrollTarget = undefined;
}

