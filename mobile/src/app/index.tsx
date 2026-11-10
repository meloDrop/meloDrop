import { Link } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { useSession } from '@/auth/SessionContext';
import { MeloDropTitle } from '@/components/MeloDropTitle';
import { PrimaryButton } from '@/components/PrimaryButton';
import { colors } from '@/theme/colors';

// Test screen: shows if a token is saved, until the navigation of module 6
export default function Index() {
    const { token, isLoading, signOut } = useSession();

    return (
        <SafeAreaView style={styles.screen}>
            <View style={{ alignItems: 'center' }}>
                <MeloDropTitle size={50} />
            </View>
            <Text style={styles.status}>
                {isLoading ? 'Lecture du jeton...' : token ? 'Connecté' : 'Pas connecté'}
            </Text>
            {token ? (
                <PrimaryButton label="Se déconnecter" onPress={signOut} />
            ) : (
                <Link href="/login" style={styles.link}>
                    Aller à l'écran de connexion
                </Link>
            )}
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
    status: {
        color: colors.textPrimary,
        fontSize: 18,
        textAlign: 'center',
    },
    link: {
        color: colors.lavender,
        fontSize: 16,
        textAlign: 'center',
    },

});