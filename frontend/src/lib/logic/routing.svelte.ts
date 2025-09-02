let page: string = $state(pageFromUrl())

function pageFromUrl(): string {
    let path = window.location.pathname;
    if (path.startsWith("/page")) {
        return path.replace("/page", "");
    }
    return path;
}

export function getPage() {
    return page;
}

export function reloadPageFromUrl() {
    page = pageFromUrl();
}

export function loadPage(newPage: string, overwrite: boolean = false) {
    if (!newPage.startsWith("/")) {
        newPage = "/" + newPage;
    }
    let url = newPage;
    if (url !== "/") {
        url = "/page" + url;
    }
    if (overwrite) {
        window.history.replaceState({}, "", url);
    } else {
        window.history.pushState({}, "", url);
    }
    page = newPage;
}

export function loadHomePage() {
    window.history.replaceState({}, "", "/");
    page = "/";
}

export function pageMatches(regex: RegExp) {
    return regex.test(page);
}

export function extractPathVariable(regex: RegExp): string {
    const result = regex.exec(page)!;
    return result[1]!;
}