import type {Role} from "../dto/role";

export function roleToName(role: Role) {
    switch (role) {
        case "STUDENT":
            return "Student";
        case "TA":
            return "TA";
        case "HEAD_TA":
            return "Head TA";
        case "TEACHER":
            return "Teacher";
    }
}