import { StatusBar } from 'expo-status-bar';
import { useState } from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import AlarmEngine from './modules/alarm-engine/src/AlarmEngineModule';

// Temporary test screen for step 5; replaced by the real test screen in step 11.
export default function App() {
  const [status, setStatus] = useState('No alarm set');

  const ringInOneMinute = () => {
    const triggerAt = Date.now() + 60_000;
    try {
      AlarmEngine.scheduleAlarm(1, triggerAt);
      setStatus(`Alarm 1 set for ${new Date(triggerAt).toLocaleTimeString()}`);
    } catch (e) {
      setStatus(`Error: ${e instanceof Error ? e.message : String(e)}`);
    }
  };

  return (
    <View style={styles.container}>
      <Text>{AlarmEngine.hello('Emine & Emre')}</Text>
      <Button title="Ring in 1 minute" onPress={ringInOneMinute} />
      <Text>{status}</Text>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 16,
  },
});
