import {Stack} from 'expo-router';
import {Unbounded_700Bold, useFonts} from '@expo-google-fonts/unbounded';
import {colors} from '@/theme/colors';

// Root layout: wraps every screen of the app (like the NavHost of MainActivity)
export default function RootLayout() {
    // Loads Unbounded before the first screen (like the files of res/font)
    const [fontsLoaded] = useFonts({Unbounded_700Bold});

// Nothing to show until the font is ready
    if (!fontsLoaded) return null;

    return (
        <Stack
            screenOptions={{
                headerShown: false,
                contentStyle: {backgroundColor: colors.night},
            }}
        />
    );
}