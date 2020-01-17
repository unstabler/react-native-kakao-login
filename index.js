import { NativeModules } from "react-native";

const { ARNKakaoLogin } = NativeModules;

export const login = ARNKakaoLogin.login;
export const logout = ARNKakaoLogin.logout;
export const unlink = ARNKakaoLogin.unlink;
export const getAccessToken = ARNKakaoLogin.getAccessToken;
export const getProfile = ARNKakaoLogin.getProfile;

const KakaoLogin = {
    getAccessToken,
    getProfile,
    login,
    logout,
    unlink,
};

export default KakaoLogin;
