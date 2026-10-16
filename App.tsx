import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';
import AlarmEngine from './modules/alarm-engine/src/AlarmEngineModule';

export default function App() {
  const name = AlarmEngine.hello('Emine & Emre');
  return (
    <View style={styles.container}>
      <Text>{name}</Text>
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
  },
});
