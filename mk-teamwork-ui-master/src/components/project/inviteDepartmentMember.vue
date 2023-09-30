<template>
    <div>
        <a-modal
                class="invite-department-member"
                :width="360"
                v-model="actionInfo.modalStatus"
                :title="actionInfo.modalTitle"
                :footer="null"
                @cancel="cancel"
        >
            <div class="header"> <span> </span>
                <!-- <span>账号邀请</span>
                <a v-if="!departmentCode" @click="createInviteLink">通过链接邀请</a> -->
            </div>
            <div class="search-content">
                <a-input v-model="keyword" placeholder="Enter a nickname or email address to find">
                    <a-icon slot="prefix" type="search"/>
                </a-input>
            </div>
            <div class="member-list">
                <a-list
                        class="project-list"
                        itemLayout="horizontal"
                        :loading="searching"
                        :dataSource="list"
                        :locale="{emptyText: (keyword && keyword.length > 1) ? 'No related members found' : ''}"
                >
                    <a-list-item slot="renderItem" slot-scope="item">
                    <span slot="actions">
                        <a-button size="small" type="dashed" icon="user-add"
                                  v-if="!item.joined"
                                  @click="invite(item)"
                        >Add to</a-button>
                        <template v-else>
                             <a-icon type="user"></a-icon>
                            <span> joined</span>
                        </template>
                     </span>
                        <a-list-item-meta
                                :description="item.email"
                        >
                            <span slot="title">{{item.name}}</span>
                            <a-avatar slot="avatar" icon="user" :src="item.avatar"/>
                        </a-list-item-meta>
                    </a-list-item>
                </a-list>
            </div>
        </a-modal>
        <a-modal
                class="invite-link"
                :width="600"
                v-model="linkInfo.modalStatus"
                :title="linkInfo.modalTitle"
                :footer="null"
        >
            <div class="header">
                <p>link expiration date：{{linkInfo.overTime}}</p>
                <a-input-search
                        size="large"
                        v-model="linkInfo.link"
                        v-clipboard:copy="linkInfo.link"
                        enterButton="copy Link"
                />
            </div>
        </a-modal>
    </div>
</template>

<script>
    import moment from 'moment';
    import _ from 'lodash'
    import {inviteMember, searchInviteMember} from "../../api/departmentMember";
    import {checkResponse} from "../../assets/js/utils";
    import {createInviteLink} from "../../api/common/common";


    export default {
        name: "inviteDepartmentMember",
        props: {
            value: {
                type: Boolean,
                default() {
                    return false
                }
            },
            departmentCode: {
                type: [String, Number],
                default() {
                    return ''
                }
            },
        },
        data() {
            return {
                form: this.$form.createForm(this),
                actionInfo: {
                    modalStatus: this.value,
                    confirmLoading: false,
                    modalTitle: this.departmentCode ? 'Add members to department' : 'Add members to organization',
                },
                linkInfo: {
                    modalStatus: false,
                    confirmLoading: false,
                    modalTitle: 'invite members',
                    link: '',
                    overTime: '',
                },
                keyword: '',
                searching: false,
                list: [],
                // emptyText: keyword ? 'no search results' : ''
            };
        },
        watch: {
            value(value) {
                this.actionInfo.modalStatus = value;
            },
            keyword() {
                this.search();
            }
        },
        created() {
            if (this.departmentCode) {
                this.searching = true;
                searchInviteMember(this.keyword, this.departmentCode).then(res => {
                    this.searching = false;
                    this.list = res.data;
                });
            }
        },
        methods: {
            invite(item) {
                inviteMember(item.accountCode, this.departmentCode).then((res) => {
                    const success = checkResponse(res);
                    if (success) {
                        item.joined = true;
                        this.$emit('update', item);
                    }
                })
            },
            createInviteLink() {
                if (!this.linkInfo.link) {
                    createInviteLink({
                        inviteType: 'organization',
                        sourceCode: this.$store.state.currentOrganization.code
                    }).then(res => {
                        const success = checkResponse(res);
                        if (success) {
                            this.linkInfo.modalStatus = true;
                            this.linkInfo.link = window.location.href.split('#')[0] + '#/invite_from_link/' + res.data.code;
                            this.linkInfo.overTime = moment(res.data.over_time).format('YYYY年M月D日 HH:mm');
                        }
                    });
                } else {
                    this.linkInfo.modalStatus = true;
                }
            },
            search: _.debounce(
                function () {
                    if (!this.keyword) {
                        this.list = [];
                    }
                    /*if (this.keyword.length <= 1) {
                        return false;
                    }*/
                    this.searching = true;
                    searchInviteMember(this.keyword, this.departmentCode).then(res => {
                        this.searching = false;
                        this.list = res.data;
                    });
                }, 500
            ),
            cancel() {
                this.actionInfo.modalStatus = false;
                this.$emit('input', this.actionInfo.modalStatus);
            }
        }
    }
</script>

<style lang="less">
    .invite-department-member {
        .ant-modal-body {
            padding-top: 0;
            padding-bottom: 24px;
            min-height: 40vh;
        }

        .header {
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            padding: 12px 0;
        }

        .member-list {
            padding-top: 12px;
        }

    }
</style>
