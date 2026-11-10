import { Stack } from 'expo-router';

// Root layout: wraps every screen of the app (like the NavHost of MainActivity)
export default function RootLayout() {
  return <Stack screenOptions={{headerShown: false}}/>;
}