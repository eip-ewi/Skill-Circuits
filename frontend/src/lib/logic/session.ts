export async function checkSession(): Promise<boolean> {
    let response = await fetch("/api/auth/status");
    return response.ok;
}