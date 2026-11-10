import { StyleSheet, Text, View } from 'react-native';

import { genreColor } from '@/theme/colors';

type Props = {
    genre: string;
};

// Small pill with the genre name, always in the same pastel for the same genre
export function GenreChip({ genre }: Props) {
    const color = genreColor(genre);

    return (
        // "26" at the end of the color: about 15% opacity
        <View style={[styles.chip, { backgroundColor: color + '26', borderColor: color }]}>
            <Text style={[styles.label, { color }]}>{genre}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    chip: {
        alignSelf: 'flex-start',
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 999,
        borderWidth: 1,
    },
    label: {
        fontSize: 12,
        fontWeight: '600',
    },
});