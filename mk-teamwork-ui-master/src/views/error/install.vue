<template>
    <div class="install" style="height: 100%;">
        <user-layout desc="The task coordination management system is being installed">
            <div class="main">
                <a-form
                        layout="horizontal"
                        class="user-layout-login"
                        @submit.prevent="handleSubmit"
                        :form="form"
                >

                    <a-form-item
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18}"
                            label="database address"
                    >
                        <a-input
                                size="large"
                                type="text"
                                placeholder="MySQL database address"
                                v-decorator="[
                                'mysqlHost',
                                {rules: [{ required: true, message: 'Please enter the MySQL database address' }], validateTrigger: 'change',initialValue: '127.0.0.1'}
                            ]"
                        >
                        </a-input>
                    </a-form-item>
                    <a-form-item
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18}"
                            label="Database name"
                    >
                        <a-input
                                size="large"
                                type="text"
                                placeholder="MySQL Database name"
                                v-decorator="[
                                'mysqlDatabase',
                                {rules: [{ required: true, message: 'Please enter the MySQL database name' }], validateTrigger: 'change',initialValue: 'pearproject'}
                            ]"
                        >
                        </a-input>
                    </a-form-item>
                    <a-form-item
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18}"
                            label="username"
                    >
                        <a-input size="large" type="text" placeholder="MySQL username"
                                 v-decorator="[
                                'mysqlUsername',
                                {rules: [{ required: true, message: 'Please enter the MySQL username' }], validateTrigger: 'change',initialValue: 'root'}
                            ]"
                        >
                        </a-input>
                    </a-form-item>
                    <a-form-item
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18}"
                            label="password"
                    >
                        <a-input size="large" type="password" placeholder="mysql password"
                                 v-decorator="[
                                'mysqlPassword',
                            ]"
                        >
                        </a-input>
                    </a-form-item>
                    <a-form-item
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18}"
                            label="data table prefix"
                    >
                        <a-input size="large" type="text" placeholder="MySQL table prefix"
                                 v-decorator="[
                                'mysqlPrefix',
                                {rules: [{ required: true, message: 'Please enter the MySQL data table prefix' }], validateTrigger: 'change',initialValue: 'pear_'}
                            ]"
                        >
                        </a-input>
                    </a-form-item>
                    <a-form-item
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18}"
                            label="The port number"
                    >
                        <a-input size="large" type="text" placeholder="MySQL port number"
                                 v-decorator="[
                                'mysqlHostport',
                                {rules: [{ required: true, message: 'Please enter the MySQL port number' }], validateTrigger: 'change',initialValue: '3306'}
                            ]">
                        </a-input>
                    </a-form-item>
                    <a-form-item
                            label="clear data"
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18}"
                    >
                        <a-checkbox
                                v-decorator="[
                                'initData',
                                {initialValue: false}
                            ]"
                        >
                        After checking, the preset data will be cleared
                        </a-checkbox>
                    </a-form-item>
                    <a-form-item
                            :labelCol="{ span: 6 }"
                            :wrapperCol="{ span: 18, offset: 6}"
                            style="margin-top:24px">
                        <a-button
                                size="large"
                                type="primary"
                                htmlType="submit"
                                class="middle-btn"
                                :loading="loading"
                                :disabled="loading"
                                block
                        >
                            <span v-if="!loading">install now</span>
                            <span v-else>Installing, please wait...</span>
                        </a-button>
                    </a-form-item>
                </a-form>
            </div>
        </user-layout>
    </div>
</template>
<script>
    import {checkInstall, install} from "../../api/common/common";
    import {checkResponse} from "../../assets/js/utils";
    import UserLayout from "../../components/layout/UserLayout";

    export default {
        components: {
            UserLayout
        },
        data() {
            return {
                loading: false,
                form: this.$form.createForm(this),
                installInfo: {
                    mysqlHost: '',
                    mysqlDatabase: '',
                    mysqlUsername: '',
                    mysqlPassword: '',
                    mysqlPrefix: '',
                    mysqlHostport: '',
                    initData: false,
                }
            }
        },
        created() {
            this.checkInstall();
        },
        mounted() {
            setTimeout(() => {
                this.form = this.$form.createForm(this);
            }, 500)
        },
        methods: {
            handleSubmit() {
                const app = this;
                this.form.validateFields(
                    (err, values) => {
                        if (!err) {
                            app.installInfo = values;
                            app.install();
                        }
                    }
                );
            },
            install() {
                this.loading = true;
                this.installInfo.initData = this.installInfo.initData ? 1 : 0;
                install(this.installInfo).then(res => {
                    this.loading = false;
                    if (!checkResponse(res)) {
                        return false;
                    }
                    this.$router.push({name: 'login'});
                }).catch(() => {
                    this.loading = false;
                });
            },
            checkInstall() {
                checkInstall().then(res => {
                    if (checkResponse(res)) {
                        this.$router.push({name: 'login'});
                    }
                });
            },
        }
    }
</script>
