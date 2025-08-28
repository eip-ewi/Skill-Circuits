export function groupBookmarkListItems<ListItem extends { qualifiedName: string }>(items: ListItem[]): { title: string, items: ListItem[] }[] {

    let sortedByName = items.toSorted((a, b) => a.qualifiedName.localeCompare(b.qualifiedName));
    let result = [];

    let previousTitle: string | undefined = undefined;
    let currentGroup: ListItem[] = [];
    for (let item of sortedByName) {

        let groupName = item.qualifiedName.substring(0, item.qualifiedName.lastIndexOf(" > "));
        if (previousTitle !== groupName) {
            if (previousTitle !== undefined && currentGroup.length > 0) {
                result.push({ title: previousTitle, items: currentGroup });
            }
            currentGroup = [];
            previousTitle = groupName;
        }

        currentGroup.push(item);

    }

    if (previousTitle !== undefined && currentGroup.length > 0) {
        result.push({ title: previousTitle, items: currentGroup });
    }

    return result;

}