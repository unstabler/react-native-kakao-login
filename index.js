import { NativeModules } from "react-native";

const { ARNKakaoLogin } = NativeModules;


export const login = () => {
    console.log('ARNKakaoLogin', Object.keys(ARNKakaoLogin));
};


const KakaoLogin = {
    login
};


export default KakaoLogin;
