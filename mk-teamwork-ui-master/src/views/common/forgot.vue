<template>
    <div class="forgot" style="height: 100%;">
        <div class="content">
            <a-spin :spinning="loading">
                loading...
            </a-spin>
        </div>
    </div>
</template>
<script>
    import {mapState} from 'vuex'
    import {checkResponse} from "@/assets/js/utils";
    import {inviteInfo} from "../../api/common/common";
    import {_joinByInviteLink} from "../../api/projectMember";
    import {_joinByInviteLink as joinOrganation} from "../../api/user";

    export default {
        data() {
            return {
                loading: false,
                inviteLink: undefined,
            }
        },
        computed: {
            ...mapState({
                userInfo: state => state.userInfo,
            })
        },
        created() {
            this.getInviteInfo();
        },
        methods: {
            getInviteInfo() {
                this.loading = true;
                inviteInfo(this.$route.params.code).then(res => {
                    this.inviteLink = res.data;
                    this.loading = false;
                });
            },
            acceptInvite() {
                let app = this;
                if (this.inviteLink.invite_type == 'project') {
                    _joinByInviteLink(this.$route.params.code).then(res => {
                        const result = checkResponse(res);
                        if (!result) {
                            return false;
                        }
                        this.$store.dispatch('setOrganizationList', res.data.organizationList);
                        this.$store.dispatch('setCurrentOrganization', res.data.currentOrganization);
                        setTimeout(function () {
                            app.$router.replace({name: 'task', params: {code: app.inviteLink.source_code}})
                        }, 500);
                    });
                } else if (this.inviteLink.invite_type == 'organization') {
                    joinOrganation(this.$route.params.code).then(res => {
                        this.$store.dispatch('setOrganizationList', res.data.organizationList);
                        this.$store.dispatch('setCurrentOrganization', res.data.currentOrganization);
                        this.$notice({title: 'You have successfully joined the organization', msg: 'You can switch the current organization on the upper right'}, 'notice', 'success');
                        setTimeout(function () {
                            app.$router.replace('/')
                        }, 500);
                    });
                }
            }
        }
    }
</script>
<style lang="less">
    .forgot {
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
    }
</style>
