import { NativeModule, requireNativeModule } from 'expo';
import { AlarmOptions } from './AlarmEngine.types';

declare class AlarmEngineModule extends NativeModule<{}> {
  hello(name: string): string;
  /** Schedules an exact alarm clock. Same id replaces the earlier alarm.
   * Throws ERR_TIME_IN_PAST if the time is not in the future,
   * ERR_INVALID_OPTION for out-of-range options,
   * ERR_EXACT_ALARM_DENIED without permission. */
  scheduleAlarm(
    id: number,
    triggerAtMillis: number,
    options?: AlarmOptions,
  ): void;
}

export default requireNativeModule<AlarmEngineModule>('AlarmEngine');
