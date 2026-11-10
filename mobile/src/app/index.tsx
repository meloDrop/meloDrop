import { StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useState } from 'react';
import { Link } from 'expo-router';

import { GenreChip } from '@/components/GenreChip';
import { MeloDropTitle } from '@/components/MeloDropTitle';
import { PrimaryButton } from '@/components/PrimaryButton';
import { colors } from '@/theme/colors';



// Test screen: shows the first components
export default function Index() {
    const [count, setCount] = useState(0);
    return (
        <SafeAreaView style={styles.screen}>
            <MeloDropTitle size={40} />
            <View style={styles.row}>
                <GenreChip genre="Rock" />
                <GenreChip genre="Pop" />
                <GenreChip genre="Electro" />
            </View>
            <PrimaryButton label="Bouton de test" onPress={() => console.log('Appui !')} />
            <Link href="/login" style={styles.link}>
                Aller à l'écran de connexion
            </Link>
            <PrimaryButton label={`Appuyé ${count} fois`} onPress={() => setCount((c) => c + 1)} />
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    screen: {
        flex: 1,
        backgroundColor: colors.night,
        padding: 24,
        gap: 24,
        justifyContent: 'center',
    },
    row: {
        flexDirection: 'row',
        gap: 8,
    },
    link: {
        color: colors.lavender,
        fontSize: 16,
        textAlign: 'center',
    },
});