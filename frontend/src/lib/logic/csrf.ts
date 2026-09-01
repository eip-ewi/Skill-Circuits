export function withCsrf(request: RequestInit): RequestInit {
    const csrfHeader = document.querySelector("meta[name='csrf-header']")!.getAttribute("value")!;
    const csrfToken = document.querySelector("meta[name='csrf-token']")!.getAttribute("value")!;

    const headers = new Headers(request.headers);
    headers.set(csrfHeader, csrfToken);
    request.headers = headers;

    return request;
}
