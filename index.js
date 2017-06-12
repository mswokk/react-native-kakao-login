import {NativeModules} from 'react-native';

const {KakaoLoginModule} = NativeModules;

export default {
    /**
     * Launch login screen and returns result
     *  @returns {Promise.<{id:number, access_token: string, nickname : string, image_url: string}>}
     */
    login(){
        return KakaoLoginModule.login();
    },

    /**
     * Logout
     * @returns {*}
     */
    logout(){
        return KakaoLoginModule.logout();
    },
};

