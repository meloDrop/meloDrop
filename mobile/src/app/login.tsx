import { router } from 'expo-router';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { login } from '@/api/auth';
import { ApiError } from '@/api/client';
import { useSession } from '@/auth/SessionContext';
import { MeloDropTitle } from '@/components/MeloDropTitle';
import { PrimaryButton } from '@/components/PrimaryButton';
import { colors } from '@/theme/colors';

// Turns an API error into a sentence for the user
function loginErrorMessage(error: unknown): string {
    if (error instanceof ApiError) {
        if (error.detail === 'invalid_credentials') return 'Identifiant ou mot de passe incorrect.';
        if (error.detail === 'email_not_verified') return "Confirme d'abord ton e-mail avec le code reçu.";
        if (error.status === 0) return 'Pas de connexion au serveur.';
    }
    return 'Une erreur est survenue, réessaie.';
}

export default function LoginScreen() {
    const { signIn } = useSession();
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const canSubmit = identifier.trim() !== '' && password !== '';

    // async: like a suspend fun launched from a coroutine
    async function handleLogin() {
        setIsLoading(true);
        setError(null);
        try {
            const token = await login(identifier.trim(), password);
            // Saves the JWT on the phone, then goes back to the test screen
            await signIn(token.access_token);
            router.back();
        } catch (e) {
            setError(loginErrorMessage(e));
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <SafeAreaView style={styles.safe}>
            {/* iPhone: pushes the form up when the keyboard opens (Android does it by itself) */}
            <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.screen}>
                <MeloDropTitle size={40} />
                <Text style={styles.subtitle}>
                    Une chanson par jour, choisie par quelqu'un, pas par un algorithme.
                </Text>

                <View style={styles.form}>
                    <TextInput
                        style={styles.input}
                        value={identifier}
                        onChangeText={setIdentifier}
                        placeholder="E-mail ou pseudo"
                        placeholderTextColor={colors.textSecondary}
                        autoCapitalize="none"
                        autoCorrect={false}
                    />

                    <View style={styles.passwordRow}>
                        <TextInput
                            style={[styles.input, styles.passwordInput]}
                            value={password}
                            onChangeText={setPassword}
                            placeholder="Mot de passe"
                            placeholderTextColor={colors.textSecondary}
                            secureTextEntry={!showPassword}
                            autoCapitalize="none"
                        />
                        <Pressable onPress={() => setShowPassword(!showPassword)} style={styles.showButton}>
                            <Text style={styles.showLabel}>{showPassword ? 'Masquer' : 'Afficher'}</Text>
                        </Pressable>
                    </View>

                    {/* A ternary, not "error && ...": never leave a bare string outside <Text> */}
                    {error ? <Text style={styles.error}>{error}</Text> : null}

                    <PrimaryButton
                        label="Se connecter"
                        onPress={handleLogin}
                        disabled={!canSubmit}
                        loading={isLoading}
                    />
                </View>
            </KeyboardAvoidingView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    safe: {
        flex: 1,
        backgroundColor: colors.night,
    },
    screen: {
        flex: 1,
        padding: 24,
        justifyContent: 'center',
        gap: 12,
    },
    subtitle: {
        color: colors.textSecondary,
        fontSize: 15,
    },
    form: {
        marginTop: 32,
        gap: 12,
    },
    input: {
        height: 52,
        borderRadius: 14,
        borderWidth: 1,
        borderColor: colors.outline,
        backgroundColor: colors.card,
        color: colors.textPrimary,
        paddingHorizontal: 16,
        fontSize: 16,
    },
    passwordRow: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    passwordInput: {
        flex: 1,
        paddingRight: 90,
    },
    showButton: {
        position: 'absolute',
        right: 16,
    },
    showLabel: {
        color: colors.lavender,
        fontWeight: '600',
    },
    error: {
        color: colors.danger,
    },
});