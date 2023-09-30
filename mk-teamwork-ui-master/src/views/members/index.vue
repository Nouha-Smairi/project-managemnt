<template>
    <div class="members-index">
        <div class="layout-item left">
            <div class="left-content">
                <div class="search-content">
                    <a-input size="large" v-model="keyword" ref="keywordInput" placeholder="search">
                        <a-icon slot="prefix" class="muted" type="search"/>
                        <a-icon v-if="keyword" slot="suffix" class="muted" type="close-circle" @click="emitEmpty"/>
                    </a-input>
                </div>
                <div class="content-item muted">member</div>
                <div class="menus">
                    <a-menu mode="inline" v-model="selectedKeys" @click="getMembers">
                        <a-menu-item :key="index.toString()" v-for="(item,index) in menus">
                            <a-icon :type="item.icon"/>
                            <span>{{item.title}}</span>
                        </a-menu-item>
                    </a-menu>
                </div>
                <div class="content-item muted">department</div>
                <div class="actions content-item">
                    <a-dropdown
                            :trigger="['click']"
                            v-model="showCreateDepartment"
                            placement="bottomCenter"
                    >
                        <a>
                            <a-icon :style="{fontSize: '14px'}" type="plus-circle"/>
                            Creation department</a>
                        <div slot="overlay">
                            <create-department
                                    v-if="showCreateDepartment"
                                    @update="createDepartmentSuccess"
                            ></create-department>
                        </div>
                    </a-dropdown>
                    <a>
                        <a-icon type="bars"/>
                        Department sorting*</a>
                </div>
                <div class="content-item department">
                    <a-spin :spinning="departmentLoading">
                        <a-tree
                                :loadData="onLoadData"
                                :treeData="treeData"
                                showIcon
                                @select="onSelect"
                        >
                            <template slot="custom" slot-scope="{selected}">
                                <a-icon type="bulb"/>
                            </template>
                        </a-tree>
                    </a-spin>
                </div>
            </div>
        </div>
        <div class="layout-item right">
            <div class="header">
                <div class="title">
                    <span>{{currentMenu.title}} · {{pagination.total}}</span>
                </div>
                <div class="actions">
                    <!-- <a>
                         <a-icon type="reload"/>
                         Batch update member information*
                     </a>-->

                    <a-dropdown :trigger="['click']" placement="bottomCenter">
                        <a class="ant-dropdown-link" href="#">
                            <a-icon type="user-add"/>
                            Add member
                        </a>
                        <a-menu slot="overlay">
                            <a-menu-item>
                                <a href="javascript:;" @click="showInviteMember = true">
                                    <a-icon type="user-add"/>
                                    Chosen
                                </a>
                            </a-menu-item>
                            <a-menu-divider/>
                            <a-menu-item>
                                <a :href="downLoadUrl" target="_blank" class="m-l">
                                    <a-icon type="copy"/>
                                    Download batch import member template
                                </a>
                            </a-menu-item>
                            <a-menu-item>
                                <a-upload name="file"
                                          :showUploadList="false"
                                          :action="uploadAction"
                                          :beforeUpload="beforeUpload"
                                          :headers="headers"
                                          @change="handleChange">
                                    <a class="text-default" :loading="uploadLoading" :disabled="uploadLoading">
                                        <a-icon type="upload" v-show="!uploadLoading"/>
                                        Upload file batch import members
                                    </a>
                                </a-upload>
                            </a-menu-item>
                        </a-menu>
                    </a-dropdown>
                    <template v-if="currentDepartmentCode">
                        <a-dropdown
                                :trigger="['click']"
                                v-model="showCreateChildDepartment"
                                placement="bottomCenter"
                        >
                            <a>
                                <a-icon :style="{fontSize: '14px'}" type="plus-circle"/>
                                Create sub -department</a>
                            <div slot="overlay">
                                <create-department
                                        v-if="showCreateChildDepartment"
                                        :parentDepartmentCode="currentDepartmentCode"
                                        @update="createChildDepartmentSuccess"
                                ></create-department>
                            </div>
                        </a-dropdown>
                        <a-dropdown
                                :trigger="['click']"
                                v-model="showEditDepartment"
                                placement="bottomCenter"
                        >
                            <a>
                                <a-icon :style="{fontSize: '14px'}" type="edit"/>
                                Editorial department</a>
                            <div slot="overlay">
                                <create-department
                                        v-if="showEditDepartment"
                                        :departmentCode="currentDepartmentCode"
                                        @edit="editDepartmentSuccess"
                                ></create-department>
                            </div>
                        </a-dropdown>
                        <a @click="deleteDepartment">
                            <a-icon :style="{fontSize: '14px'}" type="delete"/>
                            Delete department</a>
                    </template>
                </div>
            </div>
            <div class="members-content">
                <vue-scroll ref="contentscroll">
                    <a-list class="member-list" :loading="loading">
                        <div v-if="showLoadingMore" slot="loadMore"
                             :style="{ textAlign: 'center', marginTop: '12px', height: '32px', lineHeight: '32px' }">
                            <!--                            <a-spin v-if="loadingMore"/>-->
                            <a-button @click="onLoadMore">load more</a-button>
                        </div>
                        <a-list-item :key="index" v-for="(item, index) in members">
                            <a-list-item-meta>
                                <a-avatar slot="avatar" :src="item.avatar"/>
                                <div slot="title">
                                    <router-link :to="`/members/profile/${item.code}`" class="text-default">{{ item.name
                                        }}
                                    </router-link>
                                    <a-tag class="m-l-sm" v-if="item.is_owner">owner</a-tag>
                                </div>
                                <div slot="description">
                                    <!--<a-tooltip :mouseEnterDelay="0.3" :title="item.create_time">-->
                                    <span>{{item.email}}
                                    <span v-if="item.departments"> - {{ item.departments }}</span>
                                </span>
                                    <!--</a-tooltip>-->
                                </div>
                            </a-list-item-meta>
                            <template v-if="!item.is_owner">
                                <a class="muted" slot="actions" v-if="item.status == 0"
                                   @click="resumeAccount(item,index)">
                                    <a-tooltip title="Enable account">
                                        <a-icon type="check-circle"/>
                                    </a-tooltip>
                                </a>
                                <a class="muted" slot="actions" v-if="item.status == 1"
                                   @click="forbidAccount(item,index)">
                                    <a-tooltip title="Disable account number">
                                        <a-icon type="stop"/>
                                    </a-tooltip>
                                </a>
                                <a class="muted" slot="actions" @click="deleteAccount(item,index)">
                                    <a-tooltip :title="`from${actionTitle}Remove`">
                                        <a-icon type="user-delete"/>
                                    </a-tooltip>
                                </a>
                            </template>
                        </a-list-item>
                    </a-list>
                </vue-scroll>
            </div>

        </div>
        <invite-department-member v-model="showInviteMember" :department-code="currentDepartmentCode"
                                  v-if="showInviteMember" @update="getMembers"></invite-department-member>
    </div>
</template>

<script>
    import _ from 'lodash'
    import inviteDepartmentMember from '../../components/project/inviteDepartmentMember'
    import createDepartment from '../../components/project/createDepartment'
    import {list} from "../../api/department";
    import {del, forbid, list as getMembers, resume} from "../../api/user";
    import pagination from "../../mixins/pagination";
    import {checkResponse, getApiUrl, getAuthorization, getUploadUrl} from "../../assets/js/utils";
    import {notice} from "../../assets/js/notice";
    import {removeMember} from "../../api/departmentMember";
    import {del as deleteDepartment} from "../../api/department";
    import {getStore} from "../../assets/js/storage";

    export default {
        name: "members",
        components: {
            inviteDepartmentMember,
            createDepartment,
        },
        mixins: [pagination],
        data() {
            return {
                keyword: '',
                selectedKeys: ['0'],
                menus: [
                    {icon: 'team', title: 'All members'},
                    {icon: 'usergroup-add', title: 'New membership'},
                    {icon: 'user', title: 'Members of the unblocking department'},
                    {icon: 'stop', title: 'deactivated member'},
                ],
                currentMenu: {},
                currentDepartmentCode: '',
                currentTreeNode: '',//current department tree node
                treeData: [],
                departmentLoading: false,
                loading: false,
                members: [],
                showLoadingMore: false,
                loadingMore: false,
                showInviteMember: false,
                showCreateDepartment: false,
                showEditDepartment: false,
                showCreateChildDepartment: false,

                downLoadUrl: getUploadUrl('project/department_member/_downloadTemplate'),
                uploadLoading: false,
                uploadAction: getApiUrl('project/department_member/uploadFile'),

            }
        },
        computed: {
            actionTitle() {
                return this.currentDepartmentCode ? 'department' : 'organization'
            },
            headers() {
                let headers = getAuthorization();
                let organization = getStore('currentOrganization', true);
                if (organization) {
                    headers.organizationCode = organization.code;
                }
                return headers;
            }
        },
        watch: {
            keyword() {
                this.search();
            },
        },
        created() {
            this.currentMenu = this.menus[0];
            this.getMembers({key: 0});
            this.getDepartment();
        },
        methods: {
            getDepartment() {
                this.departmentLoading = true;
                list({page: 1, pageSize: 100}).then(res => {
                    let list = [];
                    if (res.data.list) {
                        res.data.list.forEach((v) => {
                            list.push({
                                key: v.code,
                                title: v.name,
                                isLeaf: !v.hasNext,
                                scopedSlots: {icon: 'custom'}
                            })
                        });
                    }
                    this.treeData = list;
                    this.departmentLoading = false;
                });
            },
            getMembers({key} = {}, reload = true) {
                let app = this;
                if (key != undefined) {
                    this.currentDepartmentCode = '';
                    this.currentMenu = this.menus[key];
                    this.selectedKeys = [key.toString()];
                    this.requestData.searchType = key;
                }
                app.loading = true;
                if (reload) {
                    this.pagination.page = 1;
                }
                getMembers(this.requestData).then(res => {
                    if (reload) {
                        app.members = res.data.list;
                    } else {
                        app.members = app.members.concat(res.data.list);
                    }
                    app.pagination.total = res.data.total;
                    app.showLoadingMore = app.pagination.total > app.members.length;
                    app.loading = false;
                    app.loadingMore = false
                });
            },
            search: _.debounce(
                function () {
                    if (!this.keyword) {
                        this.list = [];
                    }
                    if (this.keyword.length <= 1) {
                        return false;
                    }
                    this.requestData.keyword = this.keyword;
                    this.getMembers();
                }, 500
            ),
            onLoadMore() {
                this.loadingMore = true;
                this.pagination.page++;
                this.getMembers({}, false);
            },
            onSelect(selectedKeys, e) {
                // this.onLoadData(e.node);
                this.selectedKeys = [];
                this.currentMenu = e.node.dataRef;
                this.currentDepartmentCode = e.node.dataRef.key;
                this.currentTreeNode = e.node;
                let app = this;
                this.requestData.searchType = 4;
                this.requestData.departmentCode = e.node.dataRef.key;
                app.loading = true;
                getMembers(this.requestData).then(res => {
                    app.members = res.data.list;
                    app.pagination.total = res.data.total;
                    app.showLoadingMore = app.pagination.total > app.members.length;
                    app.loading = false;
                    app.loadingMore = false
                });
            },
            onLoadData(treeNode) {
                return new Promise((resolve) => {
                    list({page: 1, pageSize: 100, pcode: treeNode.dataRef.key}).then(res => {
                        let list = [];
                        if (res.data.list.length) {
                            res.data.list.forEach((v) => {
                                list.push({
                                    key: v.code,
                                    title: v.name,
                                    isLeaf: !v.hasNext,
                                    scopedSlots: {icon: 'custom'}
                                })
                            });
                        }
                        treeNode.dataRef.isLeaf = !list.length > 0;
                        treeNode.dataRef.children = list;
                        this.treeData = [...this.treeData];
                        resolve();
                    });
                })
            },
            createDepartmentSuccess(data) {
                let list = [...this.treeData];
                list.push({
                    key: data.code,
                    title: data.name,
                    isLeaf: true,
                    scopedSlots: {icon: 'custom'}
                });
                this.treeData = [];
                this.treeData = list;
                this.showCreateDepartment = false
            },
            createChildDepartmentSuccess() {
                this.onLoadData(this.currentTreeNode);
                this.showCreateChildDepartment = false;
            },
            editDepartmentSuccess(data) {
                this.currentTreeNode.dataRef.title = data;
                this.showEditDepartment = false;
            },
            deleteDepartment() {
                let app = this;
                this.$confirm({
                    title: 'Are you sure you want to delete the current department?',
                    content: `Deleting a department will delete its sub-departments at the same time, and members in the department will not be removed from the organization.`,
                    okText: 'delete',
                    okType: 'danger',
                    cancelText: 'Think again',
                    onOk() {
                        deleteDepartment(app.currentDepartmentCode).then((res) => {
                            if (!checkResponse(res)) {
                                return;
                            }
                            if (app.currentTreeNode.$parent.dataRef) {
                                app.onLoadData(app.currentTreeNode.$parent);
                                app.onSelect(null, {node: app.currentTreeNode.$parent});
                            } else {
                                app.getMembers({key: 0});
                                app.getDepartment();
                            }
                            notice({title: 'successfully deleted'}, 'notice', 'success');
                        });
                        return Promise.resolve();
                    }
                });
            },
            forbidAccount(member, index) {
                let app = this;
                this.$confirm({
                    title: 'Are you sure you want to deactivate the current account？',
                    content: `Disabled accounts will not have access to the${this.actionTitle}，However, the account information is still retained, which is convenient for work handover and management. Support account recovery`,
                    okText: 'disabled',
                    okType: 'danger',
                    cancelText: 'Think again',
                    onOk() {
                        forbid(member.code).then((res) => {
                            if (!checkResponse(res)) {
                                return;
                            }
                            app.members.splice(index, 1);
                            notice({title: 'Account successfully deactivated'}, 'notice', 'success');
                        });
                        return Promise.resolve();
                    }
                });
            },
            resumeAccount(member, index) {
                let app = this;
                this.$confirm({
                    title: 'Are you sure you want to activate the account?',
                    content: `Enabled accounts will rejoin the${this.actionTitle}`,
                    okText: 'enable',
                    okType: 'primary',
                    cancelText: 'Think again',
                    onOk() {
                        resume(member.code).then((res) => {
                            if (!checkResponse(res)) {
                                return;
                            }
                            app.members.splice(index, 1);
                            notice({title: 'Account successfully activated'}, 'notice', 'success');
                        });
                        return Promise.resolve();
                    }
                });
            },
            deleteAccount(member, index) {
                let app = this;
                this.$confirm({
                    title: `Are you sure you want to remove the member from ${this.actionTitle}？`,
                    content: `After removal, the account is in${this.actionTitle}The relevant information in the will be cleared`,
                    okText: 'remove',
                    okType: 'danger',
                    cancelText: 'Think again',
                    onOk() {
                        if (app.currentDepartmentCode) {
                            removeMember(member.code, app.currentDepartmentCode).then((res) => {
                                if (!checkResponse(res)) {
                                    return;
                                }
                                app.members.splice(index, 1);
                                notice({title: 'Removed successfully'}, 'notice', 'success');
                            });
                        } else {
                            del(member.code).then((res) => {
                                if (!checkResponse(res)) {
                                    return;
                                }
                                app.members.splice(index, 1);
                                notice({title: 'Removed successfully'}, 'notice', 'success');
                            });
                        }
                        return Promise.resolve();
                    }
                });
            },
            emitEmpty() {
                this.$refs.keywordInput.focus();
                this.keyword = '';
                this.requestData.keyword = '';
                this.getMembers();
            },
            handleChange(info) {
                if (info.file.status === 'uploading') {
                    notice(`Importing, please wait...`, 'message', 'loading', 0);
                    this.uploadLoading = true;
                    return
                }
                if (info.file.status === 'done') {
                    console.log(info);
                    this.uploadLoading = false;
                    if (checkResponse(info.file.response, true)) {
                        const count = info.file.response.data;
                        notice(`Imported successfully`, 'message', 'success');
                        this.getMembers();
                    }
                }
            },
            beforeUpload(file) {
                const isLt2M = file.size / 1024 / 1024 < 2;
                if (!isLt2M) {
                    this.$message.error('Files cannot exceed 2MB!')
                }
                return isLt2M
            },
        }
    }
</script>

<style lang="less">
    .members-index {
        margin: 24px auto;
        display: flex;
        flex-direction: row;
        width: 1100px;
        padding: 0 12px;

        .layout-item {
            background: #FFF;
            width: 100%;
        }

        .left {
            padding: 12px 24px 12px 0;
            width: 280px;
            height: 85vh;

            .left-content {
                width: 255px;
                height: 100%;
                border-right: 1px solid #e8e8e8;

                .content-item {
                    padding: 6px 24px 12px 24px;
                }

                .search-content {
                    padding: 12px;

                    .anticon-close-circle {
                        cursor: pointer;
                        color: #ccc;
                        transition: color 0.3s;
                        font-size: 12px;
                    }

                    .anticon-close-circle:hover {
                        color: #999;
                    }

                    .anticon-close-circle:active {
                        color: #666;
                    }
                }

                .menus {
                }

                .actions {
                    display: flex;
                    justify-content: space-between;
                    padding-bottom: 0;
                }

                .department {
                    padding: 6px 12px 12px 12px;

                    .ant-tree li {
                        position: relative;

                        .ant-tree-node-content-wrapper {
                            height: 40px;
                            line-height: 40px;
                            width: 100%;

                            &.ant-tree-node-selected, &:hover {
                                background-color: #f5f5f5;
                            }
                        }

                        .ant-tree-iconEle {
                            height: 40px;
                            line-height: 40px;
                        }

                        .ant-tree-switcher {
                            position: absolute;
                            right: 0;
                            height: 40px;
                            line-height: 40px;

                            .ant-tree-switcher-loading-icon {
                                height: 40px;
                            }
                        }
                    }
                }

            }

            .ant-menu-root {
                border-right: none;
            }
        }

        .right {
            padding: 24px 12px 12px 0;

            .header {
                padding-right: 12px;
                display: flex;
                justify-content: space-between;
                border-bottom: 1px solid #e8e8e8;
                padding-bottom: 24px;

                .title {
                    font-size: 18px;
                }

                .actions {
                    a {
                        margin-left: 12px;
                    }
                }
            }

            .members-content {
                height: 75vh;

                .member-list {
                    margin-right: 12px;
                }
            }
        }
    }

</style>
