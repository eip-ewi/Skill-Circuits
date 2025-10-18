export interface Warning {
    type: string;
    viewModes: ("VIEWER" | "EDITOR" | "ADMIN")[];
    message: string;
}
