import {StyleSheet, Text} from 'react-native';

import {colors} from '@/theme/colors';
import {fonts} from '@/theme/fonts';

type Props = {
    size?: number; // optional, 32 by default
};

export function MeloDropTitle({size = 32}: Props) {
    return (
        <Text style={[styles.title, {fontSize: size}]}>
            melo
            <Text style={styles.D}>D</Text>
            <Text style={styles.r}>r</Text>
            <Text style={styles.o}>o</Text>
            <Text style={styles.p}>p</Text>
        </Text>
    );
}

const styles = StyleSheet.create({
    title: {
        fontFamily: fonts.display,
        color: colors.textPrimary,
    },
    D: {
        color: colors.lavender,
    },
    r: {
        color: colors.rose,
    },
    o: {
        color: colors.butter,
    },
    p: {
        color: colors.mint,
    },

});