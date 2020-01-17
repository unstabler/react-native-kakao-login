import { NativeModules } from 'react-native';

const { ARNKakaoLogin } = NativeModules;

export const getAccessToken = () => {
  return ARNKakaoLogin.getAccessToken();
};
export const login = () => {
  return ARNKakaoLogin.login();
};
export const logout = () => {
  return ARNKakaoLogin.logout();
};

export default {
  login,
  logout,
  getAccessToken,
};
