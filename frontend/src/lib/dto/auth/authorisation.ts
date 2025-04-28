export interface Authorisation {
    viewMode: "VIEWER" | "EDITOR" | "ADMIN",
    isAdmin: boolean,
    managedEditions: number[],
    managedCourses: number[],
}