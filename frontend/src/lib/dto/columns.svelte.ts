let columns: boolean = $state(false);

export function areColumnsEnabled() {
    return columns;
}

export function enableColumns() {
    columns = true;
}

export function disableColumns() {
    columns = false;
}