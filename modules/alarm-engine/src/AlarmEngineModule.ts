import { NativeModule, requireNativeModule } from 'expo';

declare class AlarmEngineModule extends NativeModule<{}> {
  hello(name: string): string;
}

export default requireNativeModule<AlarmEngineModule>('AlarmEngine');
