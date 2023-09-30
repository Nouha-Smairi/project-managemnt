<template>
  <div class="main">
    <a-spin class="text-center" :spinning="oauthLoading">
      <span v-show="oauthLoading">Log in，please wait...</span>
    </a-spin>
    <a-form
        v-show="!oauthLoading"
        class="user-layout-login"
        ref="formLogin"
        id="formLogin"
        :form="form"
    >
      <a-tabs
          :activeKey="customActiveKey"
          :tabBarStyle="{ textAlign: 'center', borderBottom: 'unset' }"
          @change="handleTabClick"
      >
        <a-tab-pane key="tab1" tab="Account password login">
          <a-form-item>
            <a-input size="large" type="text" placeholder="Account name or mailbox address"
                     v-decorator="[
                                'account',
                                {rules: [{ required: true, message: 'Please enter the account name or mailbox address' },{ validator: this.handleUsernameOrEmail }], validateTrigger: 'blur'}
                            ]">
              <a-icon slot="prefix" type="user" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>

          <a-form-item
          >
            <a-input size="large" type="password" autocomplete="false" placeholder="password"
                     v-decorator="[
                                'password',
                                {rules: [{ required: true, message: 'Please enter the password' }], validateTrigger: 'blur'}
                            ]">
              <a-icon slot="prefix" type="lock" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>
        </a-tab-pane>
        <a-tab-pane key="tab2" tab="Mobile phone number login">
          <a-form-item
          >
            <a-input size="large" type="text" placeholder="Phone number"
                     v-decorator="[
                                'mobile',
                                {rules: [{ required: true, pattern: /^1[34578]\d{9}$/, message: 'please enter a valid phone number' },{ validator: this.handleUsernameOrEmail }], validateTrigger: 'change'}
                            ]">
              <a-icon slot="prefix" type="mobile" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>

          <a-row :gutter="16">
            <a-col class="gutter-row" :span="16">
              <a-form-item
              >
                <a-input size="large" type="text" placeholder="Verification code"
                         v-decorator="[
                                'captcha',
                                {rules: [{ required: true, message: 'please enter verification code' }], validateTrigger: 'blur'}
                            ]">
                  <a-icon slot="prefix" type="mail" :style="{ color: 'rgba(0,0,0,.25)' }"/>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col class="gutter-row" :span="8">
              <a-button
                  class="getCaptcha"
                  tabindex="-1"
                  :disabled="state.smsSendBtn"
                  @click.stop.prevent="getCaptcha"
                  v-text="!state.smsSendBtn && 'get verification code' || (state.time+' s')"
              ></a-button>
            </a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>

      <a-form-item>
        <a-checkbox v-model="formLogin.rememberMe">auto login</a-checkbox>
        <a
            class="forge-password"
            style="float: right;"
            @click="routerLink('/member/forgot')"
        >forget the password
        </a>
      </a-form-item>

      <a-form-item style="margin-top:24px">
        <a-button
            size="large"
            type="primary"
            htmlType="submit"
            class="login-button"
            :loading="loginBtn"
            @click.stop.prevent="handleSubmit"
            :disabled="loginBtn"
        >Log in
        </a-button>
      </a-form-item>

      <div class="user-login-other">
        <span>Other login methods</span>
        <a-tooltip :mouseEnterDelay="0.3"
                   title="Login">
          <a @click="dingTalkOauth">
            <a-icon class="item-icon" type="dingding"/>
          </a>
        </a-tooltip>
        <!--<a>
            <a-icon class="item-icon" type="alipay-circle"></a-icon>
        </a>
        <a>
            <a-icon class="item-icon" type="taobao-circle"></a-icon>
        </a>
        <a>
            <a-icon class="item-icon" type="weibo-circle"></a-icon>
        </a>-->
        <router-link class="register" :to="{ name: 'register' }">register account</router-link>
      </div>
    </a-form>
  </div>
</template>

<script>
import md5 from 'md5'
import * as dd from 'dingtalk-jsapi';
import {mapActions} from 'vuex'
import {mapState} from 'vuex'
import {Login, getCaptcha} from '@/api/user'
import {info} from '@/api/system';
import config from "@/config/config";
import {checkResponse, createRoute, timeFix} from '@/assets/js/utils'
import {getStore} from '@/assets/js/storage'
import {checkInstall} from "../../api/common/common";
import {_checkLogin} from "../../api/user";
import {dingTalkLoginByCode, dingTalkOauth} from "../../api/oauth";
import {notice} from "../../assets/js/notice";

export default {
  components: {},
  data() {
    return {
      customActiveKey: 'tab1',
      loginBtn: false,
      oauthLoading: false,
      // login type: 0 email, 1 account, 2 telephone
      loginType: 0,
      requiredTwoStepCaptcha: false,
      stepCaptchaVisible: false,
      form: this.$form.createForm(this),
      state: {
        time: 60,
        smsSendBtn: false
      },
      formLogin: {
        account: '',
        password: '',
        captcha: '',
        mobile: '',
        rememberMe: true
      }
    }
  },
  computed: {
    ...mapState({
      system: state => state.system,
    })
  },
  mounted() {
    this.checkInstall();
    if (this.$route.query.logged) {
      this.oauthLoading = true;
      this.checkLogin();
    }
    if (this.$route.query.message) {
      notice({title: this.$route.query.message}, 'notice');
      // notice(this.$route.query.message);
    }
  },
  methods: {
    ...mapActions(['Login', 'Logout']),
    checkInstall() {
      checkInstall().then(res => {
        if (!checkResponse(res)) {
          this.$router.push({name: 'install'});
          return false;
        }
        info().then(res => {
          if (checkResponse(res)) {
            this.$store.dispatch('setSystem', res.data);
            this.dingTalkLogin();
          }
        });
      });
    },
    // handler
    handleUsernameOrEmail(rule, value, callback) {
      const regex = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$/;
      if (regex.test(value)) {
        this.loginType = 0
      } else {
        this.loginType = 1
      }
      callback()
    },
    handleTabClick(key) {
      this.customActiveKey = key
      // this.form.resetFields()
    },
    handleSubmit() {
      const app = this;
      let flag = false;

      let loginParams = {
        remember_me: app.formLogin.rememberMe
      };

      // Log in to use the account password
      if (app.customActiveKey === 'tab1') {
        app.form.validateFields(['account', 'password'], {force: true}, (err, values) => {
          if (!err) {
            flag = true;
            loginParams[!app.loginType ? 'account' : 'account'] = values.account;
            loginParams.password = md5(values.password)
          }
        })
        // Log in to use your mobile phone number
      } else {
        app.form.validateFields(['mobile', 'captcha'], {force: true}, (err, values) => {
          if (!err) {
            flag = true;
            loginParams = Object.assign(loginParams, values)
          }
        })
      }

      if (!flag) return;

      app.loginBtn = true;
      loginParams.clientid = getStore('client_id');
      Login(loginParams).then(res => {
        if (checkResponse(res)) {
          loginParams.token = res.token;
          this.dealDataBeforeLogin(res);
        }
        this.loginBtn = false
      }).catch(() => {
        this.loginBtn = false
      });
    },
    getCaptcha(e) {
      e.preventDefault();
      const app = this;

      this.form.validateFields(['mobile'], {force: true}, (err, values) => {
        if (!err) {
          this.state.smsSendBtn = true;

          const interval = window.setInterval(() => {
            if (app.state.time-- <= 0) {
              app.state.time = 60;
              app.state.smsSendBtn = false;
              window.clearInterval(interval)
            }
          }, 1000);

          const hide = this.$message.loading('The verification code is sent ..', 0);
          getCaptcha(values.mobile)
              .then(res => {
                this.$message.destroy();

                if (!checkResponse(res)) {
                  return false;
                }
                let tips = 'Professional code acquisition successfully';
                if (res.data) {
                  tips += '，Your verification code is:' + res.data;
                }
                this.$notification['success']({
                  message: 'hint',
                  description: tips,
                  duration: 8,
                  placement: 'bottomLeft'
                });
              })
              .catch(err => {
                // setTimeout(hide, 1);
                clearInterval(interval);
                app.state.time = 60;
                app.state.smsSendBtn = false;
                this.requestFailed(err)
              })
        }
      })
    },
    loginSuccess(res, org) {
      const menu = getStore('menu', true);
      if (menu) {
        let routes = this.$router.options.routes;
        menu.forEach(function (v) {
          routes[0].children.push(createRoute(v));
          if (v.children) {
            v.children.forEach(function (v2) {
              routes[0].children.push(createRoute(v2));
              if (v2.children) {
                v2.children.forEach(function (v3) {
                  routes[0].children.push(createRoute(v3));
                });
              }
            });
          }
        });
        this.loginBtn = false;
        this.$router.addRoutes(routes);
        let redirect = this.$route.query.redirect || config.HOME_PAGE + '/' + org.code;
        if (redirect == config.HOME_PAGE) {
          redirect = config.HOME_PAGE + '/' + org.code
        }
        console.log('redirect');
        console.log(redirect);
        this.$router.push({
          path: redirect
        });
        this.$notification.success({
          message: 'welcome',
          description: `${res.data.member.name}，${timeFix()}，Welcome back`,
        });
        this.oauthLoading = false;
      }
    },
    dingTalkOauth() {
      let url = dingTalkOauth() + '?redirect=' + this.$route.query.redirect;
      let redirect = this.$route.query.redirect;
      if (redirect) {
        url += '?redirect=' + redirect;
      }
      location.href = url;
    },
    dingTalkLogin() {
      let app = this;
      dd.ready(function () {
        // The DD.Ready parameter is a callback function, which is triggered when the environmental preparation is ready. JSAPI calls need to be called after the adjustment function is triggered, otherwise it will be invalid.
        dd.runtime.permission.requestAuthCode({
          corpId: "ding42ccb1a1923b200f35c2f4657eb6378f",
          onSuccess: function (result) {
            app.oauthLoading = true;
            dingTalkLoginByCode({code: result.code}).then(res => {
              if (checkResponse(res)) {
                app.dealDataBeforeLogin(res);
              }
            });
          }
        });
      });
    },
    checkLogin() {
      _checkLogin().then(res => {
        this.dealDataBeforeLogin(res);
      });
    },
    async dealDataBeforeLogin(res) {
      let app = this;
      if (res.data) {
        const obj = {
          userInfo: res.data.member,
          tokenList: res.data.tokenList
        };
        let currentOrganization = getStore('currentOrganization', true);
        const organizationList = res.data.organizationList;
        await app.$store.dispatch('SET_LOGGED', obj);
        await app.$store.dispatch('setOrganizationList', organizationList);
        if (!currentOrganization) {
          currentOrganization = organizationList[0];
        } else {
          const has = organizationList.findIndex(item => item.id == currentOrganization.id);
          if (has === -1) {
            currentOrganization = organizationList[0];
          }
        }
        await app.$store.dispatch('setCurrentOrganization', currentOrganization);
        await app.$store.dispatch('GET_MENU').then(() => {
          app.loginSuccess(res, currentOrganization);
        });
      } else {
        app.oauthLoading = false;
        app.$store.dispatch('SET_LOGOUT');
        // app.$router.replace('/login?redirect=' + this.$router.currentRoute.fullPath);
      }
    },
    requestFailed(err) {
      this.$notification['error']({
        message: 'mistake',
        description: ((err.response || {}).data || {}).message || 'There is an error in request, please try again later',
        duration: 4
      });
      this.loginBtn = false
    }
  }
}
</script>

<style lang="less">
.user-layout-login {
  label {
    font-size: 14px;
  }

  .getCaptcha {
    display: block;
    width: 100%;
    height: 40px;
  }

  .forge-password {
    font-size: 14px;
  }

  button.login-button {
    padding: 0 15px;
    font-size: 16px;
    height: 40px;
    width: 100%;
  }

  .user-login-other {
    text-align: left;
    margin-top: 24px;
    line-height: 22px;

    .item-icon {
      font-size: 24px;
      color: rgba(0, 0, 0, 0.2);
      margin-left: 16px;
      vertical-align: middle;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #1890ff;
      }
    }

    .register {
      float: right;
    }
  }
}
</style>
