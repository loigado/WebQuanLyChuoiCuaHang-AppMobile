import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'io.loilt.chamcong',
  appName: 'chamcong_mobile',
  webDir: 'dist',
  server: {
    // Ép về http để đồng bộ với server API của bạn, tránh lỗi Mixed Content
    androidScheme: 'http', 
    cleartext: true
  },
  plugins: {
    // Kích hoạt Capacitor HTTP để gửi request qua lớp Native Android
    CapacitorHttp: {
      enabled: true,
    },
  },
};

export default config;