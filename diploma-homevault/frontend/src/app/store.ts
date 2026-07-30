import { configureStore } from '@reduxjs/toolkit';
import { baseApi } from '../services/baseApi';
import { authReducer, persistAuthState } from '../features/auth/authSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [baseApi.reducerPath]: baseApi.reducer,
  },
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(baseApi.middleware),
});

let lastAuthState = store.getState().auth;

store.subscribe(() => {
  const nextAuthState = store.getState().auth;
  if (nextAuthState !== lastAuthState) {
    persistAuthState(nextAuthState);
    lastAuthState = nextAuthState;
  }
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
