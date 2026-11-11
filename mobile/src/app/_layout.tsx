import { Unbounded_700Bold, useFonts } from '@expo-google-fonts/unbounded';
import { Stack } from 'expo-router';

import { SessionProvider } from '@/auth/SessionContext';
import { colors } from '@/theme/colors';

export default function RootLayout() {
    const [fontsLoaded] = useFonts({ Unbounded_700Bold });
    if (!fontsLoaded) return null;

    return (
        // Every screen below can call useSession()
        <SessionProvider>
            <Stack
                screenOptions={{
                    headerShown: false,
                    contentStyle: { backgroundColor: colors.night },
                }}
            />
        </SessionProvider>
    );
}