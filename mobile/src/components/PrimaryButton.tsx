import { ActivityIndicator, Pressable, StyleSheet, Text } from 'react-native';

import { colors } from '@/theme/colors';

type Props = {
    label: string;
    onPress: () => void;
    disabled?: boolean;
    loading?: boolean;
};

// Main lavender button. While loading, a spinner replaces the label
export function PrimaryButton({ label, onPress, disabled = false, loading = false }: Props) {
    const inactive = disabled || loading;

    return (
        <Pressable
            onPress={onPress}
            disabled={inactive}
            // The style can be a function: "pressed" is true while the finger is down
            style={({ pressed }) => [styles.button, inactive && styles.inactive, pressed && styles.pressed]}
        >
            {loading ? <ActivityIndicator color={colors.night} /> : <Text style={styles.label}>{label}</Text>}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    button: {
        height: 52,
        borderRadius: 16,
        backgroundColor: colors.lavender,
        alignItems: 'center',
        justifyContent: 'center',
    },
    inactive: {
        opacity: 0.4,
    },
    pressed: {
        opacity: 0.8,
    },
    label: {
        color: colors.night,
        fontSize: 16,
        fontWeight: '700',
    },
});