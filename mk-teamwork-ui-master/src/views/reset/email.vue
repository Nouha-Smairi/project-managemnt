<template>
    <div class="reset-mail" style="height: 100%;">
        <div class="content">
            <a-card v-if="!loading" :title="isSuccess ? 'Verification successful' : 'verification failed'">
                <div class="header m-b-lg text-center">
                    <p>
                        <a-icon :class="{ 'icon': true, 'isSuccess': isSuccess, 'error': !isSuccess }"
                                :type="isSuccess ? 'check-circle' : 'close-circle'"/>
                    </p>
                    <span v-if="isSuccess">
                        You have successfully bound the mailbox
                    </span>
                    <span v-else>
                        <span v-if="tips">{{tips}}</span>
                        <span v-else>Failed to bind mailbox</span>
                    </span>
                </div>
                <a-button type="primary" block size="large" class="middle-btn" @click="toHome">
                    <span>enter main page</span>
                </a-button>
            </a-card>
        </div>
    </div>
</template>
<script>

    import {_checkBindMail} from "../../api/user";
    import {checkResponse} from "../../assets/js/utils";

    export default {
        data() {
            return {
                loading: true,
                isSuccess: false,
                tips: ''
            }
        },
        created() {
            this.checkBindMail();
        },
        methods: {
            checkBindMail() {
                _checkBindMail({token: this.$route.query.token}).then(res => {
                    this.loading = false;
                    if (!checkResponse(res)) {
                        this.tips = res.msg;
                        return false;
                    }
                    this.isSuccess = true;
                });
            }
        }
    }
</script>
<style lang="less">
    .reset-mail {
        .content {
            width: 600px;
            margin: 50px auto;

            .header {
                font-size: 18px;
            }

            .member-info {
                margin: 36px 0;
                display: flex;
                align-items: center;

                .avatar {
                    margin-right: 12px;
                }

                .info {
                    p {
                        margin: 0;
                    }
                }
            }
        }

        .icon {
            font-size: 72px;
            line-height: 72px;
            margin-bottom: 0px;
        }

        .isSuccess {
            color: #52c41a;
        }

        .error {
            color: red;
        }
    }
</style>
