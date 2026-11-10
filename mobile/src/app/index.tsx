import {Text, View, StyleSheet} from "react-native";
import {useEffect, useState} from 'react';

export default function Index() {
    const [apiStatus, setApiStatus] = useState('Vérification...');
    // Runs once when the screen appears: calls GET /health on the API
    useEffect(() => {
        fetch(`${process.env.EXPO_PUBLIC_API_URL}/health`)
            .then((response) => setApiStatus(response.ok ? 'API joignable' : `Erreur ${response.status}`))
            .catch(() => setApiStatus('API injoignable'));
    }, []);
    return (
        <View style={styles.container}>
            <Text style={styles.title}>meloDrop</Text>
            <Text style={styles.subtitle}>API : {process.env.EXPO_PUBLIC_API_URL}</Text>
            <Text style={styles.subtitle}>{apiStatus}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: "#0C0C10",
    },
    title: {
        color: '#F2F2F5',
        fontSize: 32,
        fontWeight: "700",
    },
    subtitle: {
        color: '#8E8E9A',
        marginTop: 8,
    }
});
