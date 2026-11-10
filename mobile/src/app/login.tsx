import {useState} from 'react';
import {KeyboardAvoidingView, Platform, Pressable, StyleSheet, Text, TextInput, View} from 'react-native';
import {SafeAreaView} from 'react-native-safe-area-context';

import {MeloDropTitle} from '@/components/MeloDropTitle';
import {PrimaryButton} from '@/components/PrimaryButton';
import {colors} from '@/theme/colors';

export default function LoginScreen() {
    // Like "var identifier by remember { mutableStateOf("") }" in Compose
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    // Computed again at each render, like a val inside a Composable
    const canSubmit = identifier.trim() !== '' && password !== '';

    function handleLogin() {
        console.log('Connexion avec', identifier);
    }

    return (
        <SafeAreaView style={styles.safe}>
            {/* iPhone: pushes the form up when the keyboard opens (Android does it by itself) */}
            <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.screen}>
                <MeloDropTitle size={40}/>
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
                    <PrimaryButton label="Se connecter" onPress={handleLogin} disabled={!canSubmit} />
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
});