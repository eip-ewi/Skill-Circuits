export interface CsrfToken {
    headerName: string;
    token: string;
}

let csrf: CsrfToken | undefined;

export function getCsrfToken(): CsrfToken {
    return (csrf ??= {
        headerName: document.querySelector("meta[name='csrf-header']")!.getAttribute("value")!,
        token: document.querySelector("meta[name='csrf-token']")!.getAttribute("value")!,
    });
}

export function setCsrfToken(newCsrf: CsrfToken) {
    csrf = newCsrf;
}

export function withCsrf(request: RequestInit): RequestInit {
    const { headerName, token } = getCsrfToken();
    const headers = new Headers(request.headers);

    headers.set(headerName, token);
    request.headers = headers;

    return request;
}
