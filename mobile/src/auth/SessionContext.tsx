import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';

import { setAuthToken, setOnUnauthorized } from '@/api/client';
import { deleteToken, readToken, saveToken } from './tokenStore';

// What every screen can read about the session
type Session = {
    token: string | null;
    isLoading: boolean; // true while the saved token is being read
    signIn: (token: string) => Promise<void>;
    signOut: () => Promise<void>;
};

const SessionContext = createContext<Session | null>(null);

// Wraps the whole app: the session lives here, like a ViewModel shared by all screens
export function SessionProvider({ children }: { children: ReactNode }) {
    const [token, setToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    async function signIn(newToken: string) {
        await saveToken(newToken);
        setAuthToken(newToken);
        setToken(newToken);
    }

    async function signOut() {
        await deleteToken();
        setAuthToken(null);
        setToken(null);
    }

    // Runs once at start (empty list of dependencies), like LaunchedEffect(Unit)
    useEffect(() => {
        async function restore() {
            const saved = await readToken();
            setAuthToken(saved);
            setToken(saved);
            setIsLoading(false);
        }
        restore();

        // A 401 on a request: back to the login screen
        setOnUnauthorized(signOut);
    }, []);

    return (
        <SessionContext.Provider value={{ token, isLoading, signIn, signOut }}>
            {children}
        </SessionContext.Provider>
    );
}

// Hook to read the session from any screen
export function useSession(): Session {
    const session = useContext(SessionContext);
    if (!session) throw new Error('useSession must be used inside SessionProvider');
    return session;
}