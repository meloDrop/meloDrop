// Same colors as ui/theme/Color.kt in the Android app
export const colors = {
    night: '#0C0C10',
    card: '#18181F',
    outline: '#2A2A33',
    textPrimary: '#F2F2F5',
    textSecondary: '#8E8E9A',
    danger: '#F04848',
    lavender: '#B4A7F5',
    peach: '#FFB38A',
    butter: '#FFE08A',
    mint: '#9EE0B8',
    rose: '#F5A8C8',
};

const genrePalette = [colors.lavender,
    colors.peach,
    colors.butter,
    colors.mint,
    colors.rose];

// The same genre name always gives the same pastel.
// Idea: add up the number of each letter, then keep the remainder of the division by 5.
export function genreColor(genre: string): string {
    let total = 0;
    for (const letter of genre) {
        total = total + letter.charCodeAt(0);
    }
    const index = total % genrePalette.length;
    return genrePalette[index];
}