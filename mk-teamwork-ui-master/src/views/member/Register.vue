<template>
    <div class="main user-layout-register">
        <h3><span>register</span></h3>
        <a-form ref="formRegister" :autoFormCreate="(form)=>{this.form = form}" id="formRegister">
            <a-form-item
                    fieldDecoratorId="email"
                    :fieldDecoratorOptions="{rules: [{ required: true, type: 'email', message: 'Please input the email address' }], validateTrigger: ['change', 'blur']}">

                <a-input size="large" type="text" placeholder="Mail"></a-input>
            </a-form-item>
            <a-form-item
                    fieldDecoratorId="name"
                    :fieldDecoratorOptions="{rules: [{ required: true, message: 'Please type in your name' }], validateTrigger: ['change', 'blur']}">

                <a-input size="large" type="text" placeholder="Name"></a-input>
            </a-form-item>

            <a-popover placement="rightTop" trigger="click" :visible="state.passwordLevelChecked">
                <template slot="content">
                    <div :style="{ width: '240px' }">
                        <div :class="['user-register', passwordLevelClass]">强度：<span>{{ passwordLevelName }}</span>
                        </div>
                        <a-progress :percent="state.percent" :showInfo="false" :strokeColor=" passwordLevelColor "/>
                        <div style="margin-top: 10px;">
                            <span>Please enter at least 6 characters. Please do not use a password that is easy to be guessed.</span>
                        </div>
                    </div>
                </template>
                <a-form-item
                        fieldDecoratorId="password"
                        :fieldDecoratorOptions="{rules: [{ required: true, message: 'At least 6 passwords, distinguishment'}, { validator: this.handlePasswordLevel }], validateTrigger: ['change', 'blur']}">
                    <a-input size="large" type="password" @click="handlePasswordInputClick" autocomplete="false"
                             placeholder="The password must be at least 6 digits, and the case is written"></a-input>
                </a-form-item>
            </a-popover>

            <a-form-item
                    fieldDecoratorId="password2"
                    :fieldDecoratorOptions="{rules: [{ required: true, message: 'At least 6 passwords, distinguishment' }, { validator: this.handlePasswordCheck }], validateTrigger: ['change', 'blur']}">

                <a-input size="large" type="password" autocomplete="false" placeholder="Confirm your password again"></a-input>
            </a-form-item>

            <a-form-item
                    fieldDecoratorId="mobile"
                    :fieldDecoratorOptions="{rules: [{ required: true, message: 'please enter a valid phone number', pattern: /^1[3456789]\d{9}$/ }, { validator: this.handlePhoneCheck } ], validateTrigger: ['change', 'blur'] }">
                <a-input size="large" placeholder="11 -bit mobile phone number">
                    <a-select slot="addonBefore" size="large" defaultValue="+86">
                        <a-select-option value="+86">+86</a-select-option>
                        <!--<a-select-option value="+87">+87</a-select-option>-->
                    </a-select>
                </a-input>
            </a-form-item>
            <a-row :gutter="16">
                <a-col class="gutter-row" :span="16">
                    <a-form-item
                            fieldDecoratorId="captcha"
                            :fieldDecoratorOptions="{rules: [{ required: true, message: 'please enter verification code' }], validateTrigger: 'blur'}">
                        <a-input size="large" type="text" placeholder="Verification code">
                            <a-icon slot="prefix" type='safety-certificate' :style="{ color: 'rgba(0,0,0,.25)' }"/>
                        </a-input>
                    </a-form-item>
                </a-col>
                <a-col class="gutter-row" :span="8">
                    <a-button
                            class="getCaptcha"
                            size="large"
                            :disabled="state.smsSendBtn"
                            @click.stop.prevent="getCaptcha"
                            v-text="!state.smsSendBtn && 'get verification code'||(state.time+' s')"></a-button>
                </a-col>
            </a-row>

            <a-form-item>
                <a-button
                        size="large"
                        type="primary"
                        htmlType="submit"
                        class="register-button"
                        :loading="registerBtn"
                        @click.stop.prevent="handleSubmit"
                        :disabled="registerBtn">register
                </a-button>
                <router-link class="login" :to="{ name: 'login' }">Use existing account login</router-link>
            </a-form-item>

        </a-form>
    </div>
</template>

<script>
    import md5 from 'md5'
    import {register, getCaptcha} from '@/api/user'
    import {checkResponse} from "../../assets/js/utils";
    import {notice} from "../../assets/js/notice";

    const levelNames = {
        0: '低',
        1: '低',
        2: '中',
        3: '强'
    };
    const levelClass = {
        0: 'error',
        1: 'error',
        2: 'warning',
        3: 'success'
    };
    const levelColor = {
        0: '#ff0000',
        1: '#ff0000',
        2: '#ff7e05',
        3: '#52c41a',
    };
    export default {
        name: 'Register',
        components: {},
        data() {
            return {
                form: null,

                state: {
                    time: 60,
                    smsSendBtn: false,
                    passwordLevel: 0,
                    passwordLevelChecked: false,
                    percent: 10,
                    progressColor: '#FF0000'
                },
                registerBtn: false
            }
        },
        computed: {
            passwordLevelClass() {
                return levelClass[this.state.passwordLevel]
            },
            passwordLevelName() {
                return levelNames[this.state.passwordLevel]
            },
            passwordLevelColor() {
                return levelColor[this.state.passwordLevel]
            }
        },
        methods: {

            handlePasswordLevel(rule, value, callback) {

                let level = 0;

                // Determine whether there are numbers in this string
                if (/[0-9]/.test(value)) {
                    level++
                }
                // Determine whether there is a letter in the string
                if (/[a-zA-Z]/.test(value)) {
                    level++
                }
                // Determine whether there are special symbols in the string
                if (/[^0-9a-zA-Z_]/.test(value)) {
                    level++
                }
                this.state.passwordLevel = level;
                this.state.percent = level * 30;
                if (level >= 2) {
                    if (level >= 3) {
                        this.state.percent = 100
                    }
                    callback()
                } else {
                    if (level === 0) {
                        this.state.percent = 10
                    }
                    callback(new Error('Insufficient password strength'))
                }
            },

            handlePasswordCheck(rule, value, callback) {
                const password = this.form.getFieldValue('password');
                if (value === undefined) {
                    callback(new Error('Please enter the password'))
                }
                if (value && password && value.trim() !== password.trim()) {
                    callback(new Error('Two passwords are inconsistent'))
                }
                callback()
            },

            handlePhoneCheck(rule, value, callback) {
                // console.log('handlePhoneCheck, rule:', rule);
                // console.log('handlePhoneCheck, value', value);
                // console.log('handlePhoneCheck, callback', callback);

                callback()
            },

            handlePasswordInputClick() {
                this.state.passwordLevelChecked = true;
            },

            handleSubmit() {
                this.form.validateFields((err, values) => {
                    if (!err) {
                        this.registerBtn = true;
                        let params = this.form.getFieldsValue();
                        params.password = md5(params.password);
                        params.password2 = md5(params.password2);
                        register(params).then(res => {
                            this.registerBtn = false;
                            if (!checkResponse(res)) {
                                return false;
                            }
                            notice({title: 'hint', msg: 'Register successfully, please log in'}, 'notification', 'success');
                            this.$router.push({name: 'login'})
                        });
                    }
                })
            },

            getCaptcha(e) {
                e.preventDefault();
                const that = this;

                this.form.validateFields(['mobile'], {force: true},
                    (err, values) => {
                        if (!err) {
                            this.state.smsSendBtn = true;

                            const interval = window.setInterval(() => {
                                if (that.state.time-- <= 0) {
                                    that.state.time = 60;
                                    that.state.smsSendBtn = false;
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
                                    })
                                })
                                .catch(err => {
                                    setTimeout(hide, 1);
                                    clearInterval(interval);
                                    this.state.time = 60;
                                    this.state.smsSendBtn = false;
                                    this.requestFailed(err)
                                })
                        }
                    }
                )
            },
            requestFailed(err) {
                this.$notification['error']({
                    message: 'error',
                    description: ((err.response || {}).data || {}).message || 'There is an error in request, please try again later',
                    duration: 4,
                });
                this.registerBtn = false
            },
        },
        watch: {
            'state.passwordLevel'(val) {
                // console.log(val)

            }
        }
    }
</script>
<style lang="less">
    .user-register {

        &.error {
            color: #ff0000;
        }

        &.warning {
            color: #ff7e05;
        }

        &.success {
            color: #52c41a;
        }


    }

    .user-layout-register {
        .ant-input-group-addon:first-child {
            background-color: #fff;
        }
    }
</style>
<style lang="less">
    .user-layout-register {

        & > h3 {
            font-size: 16px;
            margin-bottom: 20px;
        }


        .getCaptcha {
            display: block;
            width: 100%;
            height: 40px;
        }

        .register-button {
            width: 50%;
        }

        .login {
            float: right;
            line-height: 40px;
        }
    }
</style>
