const { contextBridge, ipcRenderer } = require('electron');

/** main.js の NINE_UNIVERSE_START_FULLSCREEN と同じ（未設定・'0' 以外は true） */
const initialFullscreenPreference = process.env.NINE_UNIVERSE_START_FULLSCREEN !== '0';

contextBridge.exposeInMainWorld('nuElectron', {
	quitApp: () => ipcRenderer.invoke('nu-quit-app'),
	setFullScreen: (on) => ipcRenderer.invoke('nu-set-fullscreen', !!on),
	initialFullscreenPreference: initialFullscreenPreference,
});
