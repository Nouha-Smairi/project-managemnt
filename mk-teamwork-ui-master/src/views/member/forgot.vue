<template>
    <div class="main user-layout-forgot">
        <template v-if="!sended">
            <h3><span>Reset</span></h3>
            <a-form
                    :form="formCaptcha"
                    ref="formForgot"
                    id="formForgot">
                <a-form-item>
                    <a-input size="large" type="text" placeholder="Registered mailbox address"
                             v-decorator="['email',{rules: [{ required: true, type: 'email', message: 'Please input the email address' }], validateTrigger: ['change', 'blur']}]">
                        <a-icon slot="prefix" type='mail' :style="{ color: 'rgba(0,0,0,.25)' }"/>

                    </a-input>
                </a-form-item>
                <a-form-item>
                    <a-button
                            size="large"
                            type="primary"
                            htmlType="submit"
                            class="forgot-button"
                            :loading="forgotBtn"
                            @click.stop.prevent="handleSubmitCaptcha"
                            :disabled="forgotBtn">get verification code
                    </a-button>
                    <router-link class="login" :to="{ name: 'login' }">log in with another account</router-link>
                </a-form-item>

            </a-form>
        </template>
        <template v-else>
            <h3><span>reset Password</span></h3>
            <a-form
                    :form="form"
                    ref="formRegister"
                    id="formRegister">
                <a-form-item>
                    <a-input size="large" type="text" placeholder="Mail" :value="email" disabled>
                        <a-icon slot="prefix" type='mail' :style="{ color: 'rgba(0,0,0,.25)' }"/>
                    </a-input>
                </a-form-item>

                <a-popover placement="rightTop" trigger="click" :visible="state.passwordLevelChecked">
                    <template slot="content">
                        <div :style="{ width: '240px' }">
                            <div :class="['user-register', passwordLevelClass]">strength：<span>{{ passwordLevelName }}</span>
                            </div>
                            <a-progress :percent="state.percent" :showInfo="false" :strokeColor=" passwordLevelColor "/>
                            <div style="margin-top: 10px;">
                                <span>Please enter at least 6 characters. Please do not use a password that is easy to be guessed.</span>
                            </div>
                        </div>
                    </template>
                    <a-form-item>
                        <a-input
                                v-decorator="['password',{rules: [{ required: true, message: 'At least 6 passwords, distinguishment' },{ validator: this.handlePasswordLevel }], validateTrigger: ['change', 'blur']}]"
                                size="large" type="password" @click="handlePasswordInputClick" autocomplete="false"
                                 placeholder="The password must be at least 6 digits, and the case is written">
                            <a-icon slot="prefix" type='lock' :style="{ color: 'rgba(0,0,0,.25)' }"/>
                        </a-input>
                    </a-form-item>
                </a-popover>

                <a-form-item>
                    <a-input
                            v-decorator="['password2',{rules: [{ required: true, message: 'At least 6 passwords, distinguishment' },{ validator: this.handlePasswordCheck }], validateTrigger: ['change', 'blur']}]"
                            size="large" type="password" autocomplete="false" placeholder="Confirm your password again">
                        <a-icon slot="prefix" type='check-circle' :style="{ color: 'rgba(0,0,0,.25)' }"/>

                    </a-input>
                </a-form-item>

                <a-form-item>
                    <a-input
                            v-decorator="['captcha',{rules: [{ required: true, message: 'please enter verification code' }], validateTrigger: ['blur']}]"
                            size="large" type="text" placeholder="Verification code">
                        <a-icon slot="prefix" type='safety-certificate' :style="{ color: 'rgba(0,0,0,.25)' }"/>
                    </a-input>
                </a-form-item>

                <a-form-item>
                    <a-button
                            block
                            size="large"
                            type="primary"
                            htmlType="submit"
                            class="register-button"
                            :loading="registerBtn"
                            @click.stop.prevent="handleSubmit"
                            :disabled="registerBtn">submit
                    </a-button>
                </a-form-item>

            </a-form>
        </template>
    </div>
</template>

<script>
    import md5 from 'md5'
    import {forgot} from '@/api/user'
    import {checkResponse} from "../../assets/js/utils";
    import {notice} from "../../assets/js/notice";
    import {_getMailCaptcha, _resetPasswordByMail} from "../../api/user";

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
        name: 'Forgot',
        components: {},
        data() {
            return {
                form: this.$form.createForm(this),
                formCaptcha: this.$form.createForm(this),
                email: '',
                sended: false,
                forgotBtn: false,

                state: {
                    time: 60,
                    smsSendBtn: false,
                    passwordLevel: 0,
                    passwordLevelChecked: false,
                    percent: 10,
                    progressColor: '#FF0000'
                },
                registerBtn: false,
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
            },
        },
        methods: {


            handleSubmitCaptcha() {
                this.formCaptcha.validateFields((err, values) => {
                    if (!err) {
                        this.forgotBtn = true;
                        let params = this.formCaptcha.getFieldsValue();
                        this.email = params.email;
                        _getMailCaptcha(this.email)
                            .then(res => {
                                this.sended = true;
                                this.forgotBtn = false;
                                this.$message.destroy();
                                if (!checkResponse(res)) {
                                    return false;
                                }
                                let tips = 'Professional code acquisition successfully';
                                if (res.data) {
                                    tips += '，Your verification code is:' + res.data;
                                }
                                notice({title: 'hint', msg: tips}, 'notification', 'success', 8);
                            })
                    }
                })
            },
            handleSubmit() {
                this.form.validateFields((err, values) => {
                    if (!err) {
                        this.hidePasswordLevelChecked = false ;
                        this.registerBtn = true;
                        let params = this.form.getFieldsValue();
                        params.email = this.email;
                        params.password = md5(params.password);
                        params.password2 = md5(params.password2);
                        _resetPasswordByMail(params).then(res => {
                            this.registerBtn = false;
                            if (!checkResponse(res)) {
                                return false;
                            }
                            notice({title: 'hint', msg: 'The password is successful, please log in'}, 'notification', 'success');
                            this.$router.push({name: 'login'})
                        });
                    }
                })
            },
            handlePasswordInputClick() {
                this.state.passwordLevelChecked = true;
            },
            hidePasswordLevelChecked() {
                this.state.passwordLevelChecked = false;
            },
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
        },
        watch: {}
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
    .user-layout-forgot {

        & > h3 {
            font-size: 16px;
            margin-bottom: 20px;
        }


        .getCaptcha {
            display: block;
            width: 100%;
            height: 40px;
        }

        .forgot-button {
            width: 50%;
        }

        .login {
            float: right;
            line-height: 40px;
        }
    }
</style>
