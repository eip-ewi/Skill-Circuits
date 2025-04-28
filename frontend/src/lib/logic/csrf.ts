export function withCsrf(request: RequestInit): RequestInit {
    let csrfHeader = document.querySelector("meta[name='csrf-header']")!.getAttribute("value")!;
    let csrfToken = document.querySelector("meta[name='csrf-token']")!.getAttribute("value")!;

    if (!("headers" in request)) {
        request.headers = {};
    }

    // @ts-ignore
    request.headers[csrfHeader] = csrfToken;

    return request;
}