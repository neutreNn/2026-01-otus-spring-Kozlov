import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '../../app/store';
import type { AuthResponse, UserResponse } from './types';

const AUTH_STORAGE_KEY = 'homevault.auth';

export interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserResponse | null;
}

const emptyAuthState: AuthState = {
  accessToken: null,
  refreshToken: null,
  user: null,
};

function readStoredAuth(): AuthState {
  if (typeof window === 'undefined') {
    return emptyAuthState;
  }

  try {
    const storedValue = window.localStorage.getItem(AUTH_STORAGE_KEY);
    if (!storedValue) {
      return emptyAuthState;
    }

    const parsedValue = JSON.parse(storedValue) as Partial<AuthState>;
    if (!parsedValue.accessToken || !parsedValue.refreshToken || !parsedValue.user) {
      return emptyAuthState;
    }

    return {
      accessToken: parsedValue.accessToken,
      refreshToken: parsedValue.refreshToken,
      user: parsedValue.user,
    };
  } catch {
    return emptyAuthState;
  }
}

export function persistAuthState(authState: AuthState) {
  if (typeof window === 'undefined') {
    return;
  }

  if (!authState.accessToken || !authState.refreshToken || !authState.user) {
    window.localStorage.removeItem(AUTH_STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authState));
}

const authSlice = createSlice({
  name: 'auth',
  initialState: readStoredAuth(),
  reducers: {
    setCredentials: (state, action: PayloadAction<AuthResponse>) => {
      state.accessToken = action.payload.accessToken;
      state.refreshToken = action.payload.refreshToken;
      state.user = action.payload.user;
    },
    setCurrentUser: (state, action: PayloadAction<UserResponse>) => {
      state.user = action.payload;
    },
    logout: (state) => {
      state.accessToken = null;
      state.refreshToken = null;
      state.user = null;
    },
  },
});

export const { logout, setCredentials, setCurrentUser } = authSlice.actions;
export const authReducer = authSlice.reducer;

export const selectAccessToken = (state: RootState) => state.auth.accessToken;
export const selectRefreshToken = (state: RootState) => state.auth.refreshToken;
export const selectCurrentUser = (state: RootState) => state.auth.user;
export const selectIsAuthenticated = (state: RootState) => Boolean(state.auth.accessToken);
export const selectIsAdmin = (state: RootState) => state.auth.user?.roles.includes('ADMIN') ?? false;
