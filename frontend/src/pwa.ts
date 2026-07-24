import { readonly, ref } from 'vue';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed'; platform: string }>;
}

const canInstallState = ref(false);
const isOnlineState = ref(typeof navigator === 'undefined' ? true : navigator.onLine);
const isStandaloneState = ref(false);
const updateAvailableState = ref(false);

let initialized = false;
let installPrompt: BeforeInstallPromptEvent | undefined;
let registration: ServiceWorkerRegistration | undefined;
let reloadOnControllerChange = false;

export const pwaCanInstall = readonly(canInstallState);
export const pwaIsOnline = readonly(isOnlineState);
export const pwaUpdateAvailable = readonly(updateAvailableState);

export function initializePwa() {
  if (initialized || typeof window === 'undefined') return;
  initialized = true;
  updateStandaloneState();

  window.addEventListener('beforeinstallprompt', (event) => {
    event.preventDefault();
    installPrompt = event as BeforeInstallPromptEvent;
    canInstallState.value = !isStandaloneState.value;
  });
  window.addEventListener('appinstalled', () => {
    installPrompt = undefined;
    canInstallState.value = false;
    isStandaloneState.value = true;
  });
  window.addEventListener('online', updateNetworkState);
  window.addEventListener('offline', updateNetworkState);
  window.matchMedia('(display-mode: standalone)').addEventListener('change', updateStandaloneState);

  if (import.meta.env.PROD && 'serviceWorker' in navigator) {
    if (document.readyState === 'complete') {
      void registerServiceWorker();
    } else {
      window.addEventListener('load', () => void registerServiceWorker(), { once: true });
    }
  }
}

export async function installPwa() {
  if (!installPrompt) return false;
  const prompt = installPrompt;
  await prompt.prompt();
  const choice = await prompt.userChoice;
  installPrompt = undefined;
  canInstallState.value = false;
  return choice.outcome === 'accepted';
}

export function applyPwaUpdate() {
  if (!registration?.waiting) {
    updateAvailableState.value = false;
    return false;
  }
  reloadOnControllerChange = true;
  registration.waiting.postMessage({ type: 'SKIP_WAITING' });
  return true;
}

function updateNetworkState() {
  isOnlineState.value = navigator.onLine;
}

function updateStandaloneState() {
  const appleNavigator = navigator as Navigator & { standalone?: boolean };
  isStandaloneState.value =
    window.matchMedia('(display-mode: standalone)').matches || appleNavigator.standalone === true;
  if (isStandaloneState.value) {
    canInstallState.value = false;
  }
}

async function registerServiceWorker() {
  try {
    registration = await navigator.serviceWorker.register('/service-worker.js');
    updateAvailableState.value = Boolean(registration.waiting && navigator.serviceWorker.controller);
    registration.addEventListener('updatefound', watchInstallingWorker);
    navigator.serviceWorker.addEventListener('controllerchange', () => {
      if (reloadOnControllerChange) {
        window.location.reload();
      }
    });
  } catch {
    canInstallState.value = false;
  }
}

function watchInstallingWorker() {
  const installingWorker = registration?.installing;
  if (!installingWorker) return;
  installingWorker.addEventListener('statechange', () => {
    if (installingWorker.state === 'installed' && navigator.serviceWorker.controller) {
      updateAvailableState.value = true;
    }
  });
}
