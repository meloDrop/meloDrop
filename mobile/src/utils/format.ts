export function plural(count: number, word: string): string {
    return `${count} ${word}${count > 1 ? 's' : ''}`;
}

export function shownName(user: {username: string, display_name: string | null}) : string {
    return user.display_name || `@${user.username}`;
}