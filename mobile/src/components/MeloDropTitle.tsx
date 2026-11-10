import {StyleSheet, Text} from 'react-native';

import {colors} from '@/theme/colors';
import {fonts} from '@/theme/fonts';

type Props = {
    size?: number; // optional, 32 by default
};

export function MeloDropTitle({size = 32}: Props) {
    return (
        <Text style={[styles.title, {fontSize: size}]}>
            melo<Text style={styles.drop}>Drop</Text>
        </Text>
    );
}

const styles = StyleSheet.create({
    title: {
        fontFamily: fonts.display,
        color: colors.textPrimary,
    },
    drop: {
        color: colors.lavender,
    },
});