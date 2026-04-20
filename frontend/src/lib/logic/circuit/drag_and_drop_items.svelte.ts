let draggingItem: boolean = $state(false);

export function dragItemEnter(event: DragEvent) {
    if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
        return;
    }
    event.preventDefault();
    draggingItem = true;
}

export function dragItemLeave() {
    draggingItem = false;
}

export function dragItemOver(event: DragEvent) {
    if (!event.dataTransfer!.types.includes("skill-circuits/item")) {
        return;
    }
    event.preventDefault();
    draggingItem = true;
}

export function getDraggingItem() {
    return draggingItem;
}

export function setDraggingItem(value: boolean) {
    draggingItem = value;
}