import {getStore} from "../assets/js/storage";

const userInfo = getStore('userInfo', true);
const theme = getStore('theme');
export default {
    theme: theme ? theme : 'dark',
    logged: !!userInfo,//Login status
    userInfo: userInfo,//User Info
    organizationList: getStore('organizationList', true),//List of organizations that can be viewed
    currentOrganization: getStore('currentOrganization', true),//current organization
    system: getStore('system', true),//System Configuration
    windowLoading: false, // window loading
    pageLoading: false, // page loading loading
    socketAction: '',
    boundClient: false,//Whether to bind client

}
