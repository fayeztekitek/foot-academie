import { createContext, useContext, useState, useCallback } from 'react';
import { isDemoMode, setDemoMode as saveDemoMode, getApiBaseUrl, setApiBaseUrl as saveApiBaseUrl, getBackendUrl, setBackendUrl as saveBackendUrl } from './demoConfig';

const DemoContext = createContext(null);

export function DemoProvider({ children }) {
  const [demo, setDemo] = useState(isDemoMode());
  const [apiBaseUrl, setApiBase] = useState(getApiBaseUrl());
  const [backendUrl, setBackend] = useState(getBackendUrl());

  const enableDemo = useCallback(() => {
    saveDemoMode(true);
    setDemo(true);
  }, []);

  const disableDemo = useCallback(() => {
    saveDemoMode(false);
    setDemo(false);
  }, []);

  const toggleDemo = useCallback(() => {
    if (demo) disableDemo();
    else enableDemo();
  }, [demo, enableDemo, disableDemo]);

  const updateApiBaseUrl = useCallback((url) => {
    saveApiBaseUrl(url);
    setApiBase(url);
  }, []);

  const updateBackendUrl = useCallback((url) => {
    saveBackendUrl(url);
    setBackend(url);
  }, []);

  return (
    <DemoContext.Provider value={{
      isDemo: demo,
      apiBaseUrl,
      backendUrl,
      enableDemo,
      disableDemo,
      toggleDemo,
      updateApiBaseUrl,
      updateBackendUrl,
    }}>
      {children}
    </DemoContext.Provider>
  );
}

export function useDemo() {
  const ctx = useContext(DemoContext);
  if (!ctx) throw new Error('useDemo must be used within a DemoProvider');
  return ctx;
}
